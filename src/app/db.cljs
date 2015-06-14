(ns app.db
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [cljs.core.async :as async :refer [<!]]
            [cljsjs.firebase :as F]
            [goog.events :as events]
            [reagent.core :as r]
            [app.state :as state :refer [grammar sample location user]])
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
   :sample @sample})

(defonce _
         (do
           (.onAuth ref
                    (fn [auth-data]
                      (prn (js->clj auth-data))
                      (reset! user (js->clj auth-data :keywordize-keys true))))))

(add-watch location :update-doc (fn [key reference old-state new-state]
                                  (let [doc-id (:doc-id new-state)
                                        version-id (:version-id new-state)
                                        doc-ref (m/get-in ref [doc-id])]
                                    (if version-id
                                      (go
                                        (let [version-ref (-> doc-ref (.child (str "versions/" version-id)))
                                              doc (<! (ma/deref< version-ref))]
                                          (prn "version" (.toString version-ref))
                                          (reset! grammar (:grammar doc))
                                          (reset! sample (:sample doc))))
                                      (go
                                        (let [version-ref (-> doc-ref (.child "versions") (.limitToLast 1))
                                              doc (<! (ma/deref< version-ref))]
                                          (prn "doc" doc)
                                          (reset! grammar (:grammar doc))
                                          (reset! sample (:sample doc))))))))


(defn sign-in-anon []
  (if-not (.getAuth ref) (m/auth-anon ref)))

(defn signed-in? []
  (if (:uid @user) true false))

(defn save-new []
  (prn "save-new")
  (go
    (let [doc-ref (.push ref)]
      (prn (:uid @user))
      (<! (ma/reset!< doc-ref {:owner (:uid @user)} ))
      (let [version-ref (-> doc-ref (.child "versions") (.push))]
        (<! (ma/reset!< version-ref (current-doc)))
        (reset! location {:doc-id (.key doc-ref)
                          :version-id (.key version-ref)})))))

(defn save-version []
  (prn "save-version")
  (go
    (let [doc-ref (-> ref (.child (:doc-id @location)))
          version-ref (-> doc-ref (.child "versions") (.push))]
      (<! (ma/reset!< version-ref (current-doc)))
      (swap! location merge {:version-id (.key version-ref)}))))

(defn save []
  (if (signed-in?)
    (go
      (if (:doc-id @location) (save-version)
                          (save-new)))
    (prn "not-signed-in")))