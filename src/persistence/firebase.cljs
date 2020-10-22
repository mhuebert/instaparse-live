(ns persistence.firebase
  (:refer-clojure :exclude [get-in swap! reset! deref])
  (:require ["@firebase/app" :default firebase]
            ["@firebase/database" :as db]
            [clojure.walk :as walk]
            [applied-science.js-interop :as j]
            [kitchen-async.promise :as p]))

(defonce init! (delay
                 (.initializeApp firebase
                                 #js{:apiKey "AIzaSyAEP6hl5IEOwRHn-ImgiHKmVSs3CjyAY68"
                                     :authDomain "instaparse-live.firebaseapp.com"
                                     :databaseURL "https://instaparse-live.firebaseio.com"
                                     :projectId "instaparse-live"
                                     :storageBucket "instaparse-live.appspot.com"
                                     :messagingSenderId "382105941430"
                                     :appId "1:382105941430:web:37caefbddb33669121fd17"})))

(defonce !root
         ;; Create a reference for firebase
         (delay
           (.ref (.database firebase) "/")))

(defn get-in
  ([path] (get-in @!root path))
  ([ref path]
   (reduce (fn [^js ref segment]
             (.child ref (cond-> segment
                                 (keyword? segment)
                                 name))) ref path)))

(defn- keywords->strings [x]
  (if (keyword? x) (str x) x))

(defn- hydrate-keywords [x]
  (if (and (string? x) (= \: (first x))) (keyword (subs x 1)) x))

(defn hydrate [v]
  (walk/postwalk
    hydrate-keywords
    (js->clj v :keywordize-keys true)))

(defn serialize [v]
  (->> (walk/stringify-keys v)
       (walk/postwalk keywords->strings)
       clj->js))

(defn value
  "Data stored within snapshot"
  [^js snapshot]
  (hydrate (.val snapshot)))

(defn once [^js ref]
  (p/-> (.once ref "value")
        value))

(extend-protocol IDeref
  db/Reference
  (-deref [this] (once this)))

(defn swap! [^js ref f & args]
  (p/-> (.transaction ref #(-> % hydrate ((fn [x] (apply f x args))) serialize))
        (j/get :snapshot)
        value))

(defn reset! [^js ref val]
  (.set ref (serialize val)))

(defn reset-with-priority! [^js ref val priority]
  (.setWithPriority ref (serialize val) priority))
