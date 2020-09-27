(ns matchbox2.core
  (:refer-clojure :exclude [get-in swap! reset! deref])
  (:require ["@firebase/app" :default firebase]
            ["@firebase/database"]
            [clojure.walk :as walk]))



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

(defn extract-cb [args]
  (if (and (>= (count args) 2)
           (= (first (take-last 2 args)) :callback))
    [(last args) (drop-last 2 args)]
    [nil args]))

(defn throw-fb-error [err & [msg]]
  (throw (ex-info (or msg "FirebaseError") {:err err})))

(defn value
  "Data stored within snapshot"
  [^js snapshot]
  (hydrate (.val snapshot)))

(defn deref [^js  ref cb]
  (.once ref "value" (comp cb value)))

(defn swap! [^js ref f & args]
  (let [[cb args] (extract-cb args)]
    (.transaction ref
                  #(-> % hydrate ((fn [x] (apply f x args))) serialize)
                  (or cb
                    js/undefined))))

(defn reset! [^js ref val & [cb]]
  (.set ref (serialize val) (if cb
                              (fn [err]
                                (if err
                                  (throw-fb-error err)
                                  (cb ref)))
                              js/undefined)))

(defn reset-with-priority! [^js ref val priority & [cb]]
  (.setWithPriority ref (serialize val) priority
                    (if cb
                      (fn [err]
                        (if err
                          (throw-fb-error err)
                          (cb ref)))
                      js/undefined)))
