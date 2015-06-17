(ns app.db
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [cljs.core.async :as async :refer [<! put! chan]]
            [cljsjs.firebase :as F]
            [goog.events :as events]
            [reagent.core :as r]
            [app.state :as state :refer [grammar sample location user options]])
  (:import goog.History))


; Save -> push the two atoms into the current doc's versions
;         - set current :uid into doc if it's a new doc
;         - only allow edits to user-owned doc
; If anon-user - save anon-user ID in atom
; Fork -> push the two atoms into a new doc's versions
; Load -> grab a doc from Firebase and reset! atoms
; Sign in -> prompt for Github & set all anon-user docs as owned by Github user
; Sign out

; On sign-in - if anon-user is in atom, merge the stuff & then clear the atom
; LINK - sign out
; LINK - sign in with GitHub / Twitter
; Figure out how to batch-update docs that are associated with a given user
; Security rules, basic


(defonce ref (m/connect "http://instaparse-live.firebaseio.com/docs"))

(defn current-doc []
  {:grammar @grammar
   :sample @sample
   :options @options})

(defn load-state [version loc]
  (prn "load-state" loc)
  (reset! location {:doc-id (:doc-id loc) :version-id (:version-id loc)})
  (reset! grammar (:grammar version))
  (reset! sample (:sample version))
  (reset! options (:options version)))

(defonce _
         (do
           (.onAuth ref
                    (fn [auth-data]
                      (prn (js->clj auth-data))
                      (reset! user (js->clj auth-data :keywordize-keys true))))))

(defn sign-in-anon []
  (if-not (.getAuth ref) (.authAnonymously ref #())))

(defn signed-in? []
  (if (:uid @user) true false))

(defn next-version-id [doc-id]
  (let [counter-ref (-> ref (.root) (.child "counters") (.child doc-id))
        id-chan (chan)]
    (m/swap! counter-ref #(+ 1 (or % 0))
             :callback (fn [err committed ss]
                         (if err (prn err) (put! id-chan (str "x" (.val ss))))))
    id-chan))

(defn save-new []
  (prn "save-new")
  (go
    (let [doc-ref (.push ref)]
      (<! (ma/reset!< doc-ref {:owner (:uid @user) :parent (:doc-id @location)}))
      (let [version-id (<! (next-version-id (.key doc-ref)))
            version-ref (.child doc-ref (str "/versions/" version-id))
            ]
        (<! (ma/reset-with-priority!< version-ref (current-doc) (.now js/Date)))
        (reset! location {:doc-id (.key doc-ref)
                          :version-id (.key version-ref)})))))

(defn save-version []
  (prn "save-version")
  (go
    (let [doc-id (:doc-id @location)
          doc-ref (-> ref (.child doc-id))
          version-id (<! (next-version-id doc-id))
          version-ref (-> doc-ref (.child "versions") (.child version-id))]
      (<! (ma/reset-with-priority!< version-ref (current-doc) (.now js/Date)))
      (swap! location merge {:version-id (.key version-ref)}))))

(defn save []
  (if (signed-in?)
    (go
      (if (:doc-id @location) (save-version) (save-new)))
    (prn "not-signed-in")))

(defn load-doc-version [doc-id version-id]
  (prn "load-doc-version")
  (go
    (let [version-ref (m/get-in ref [doc-id "versions" version-id])
          version (<! (ma/deref< version-ref))]
      (load-state version {:doc-id doc-id :version-id version-id}))))

(defn load-doc-latest [doc-id]
  (prn "load-doc-latest")
  (go
    (let [version-ref (-> ref (.child doc-id) (.child "versions") (.limitToLast 1))
          _ (prn (.toString version-ref))
          version (<! (ma/deref< version-ref))
          _ (prn "version" version)
          version-id (name (ffirst version))
          _ (prn "version-id" version-id)
          version (last (first version))
          _ (prn "version" version)]
      (load-state version {:doc-id doc-id :version-id version-id}))))
