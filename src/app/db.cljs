(ns app.db
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [app.macros :refer [<? go?]])
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [cljs.core.async :refer [<! put! chan]]
            [cljsjs.firebase :as F]
            [goog.events]
            [reagent.core :as r]
            [app.state :as state :refer [working-version location user ui]])
  (:import goog.History))


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

(defn load-state [version loc]
  (reset! state/working-version version)
  (reset! location {:doc-id (:doc-id loc) :version-id (:version-id loc)}))

(defonce _
         (do
           (.onAuth ref
                    (fn [auth-data]
                      (js->clj auth-data)
                      (reset! user (js->clj auth-data :keywordize-keys true))))))

(defn sign-in-anon []
  (if-not (.getAuth ref) (.authAnonymously ref #())))

(defn sign-in-github []
  (.authWithOAuthPopup ref "github"
                       (fn [error auth-data]
                         (if error (prn (js/Error error))
                                   (prn auth-data)))))

(defn signed-in? []
  (if (:uid @user) true false))

(defn sign-out []
  (.unauth ref))

(defn next-version-id [doc-id]
  (let [counter-ref (-> ref (.root) (.child "counters") (.child doc-id))
        id-chan (chan)]
    (m/swap! counter-ref #(+ 1 (or % 0))
             :callback (fn [err committed ss]
                         (let [err (if err (js/Error err) nil)]
                           (put! id-chan (or err (str "v" (.val ss)))))))
    id-chan))


(defn save-new []
  (go?
    (let [doc-ref (.push ref)]
      (<? (ma/reset!< doc-ref {:owner (:uid @user)
                               :username (get-in @user [:github :username])
                               :parent (:doc-id @location)}))
      (let [version-id (<! (next-version-id (.key doc-ref)))
            version-ref (.child doc-ref (str "/versions/" version-id))]
        (<? (ma/reset-with-priority!< version-ref @working-version (.now js/Date)))
        (reset! location {:doc-id (.key doc-ref)
                          :version-id (.key version-ref)})))))

(defn fork []
  (if-not (= "Forking..." (:fork-status @ui))
    (go
      (swap! ui merge {:fork-status "Forking..."})
      (try
        (do
          (<? (save-new))
          (swap! ui merge {:fork-status "Fork"}))
        (catch js/Error e
          (swap! ui merge {:fork-status (js/Error "Error forking")}))))))

(defn save-version []
  (go?
    (let [doc-id (:doc-id @location)
          doc-ref (-> ref (.child doc-id))
          version-id (<? (next-version-id doc-id))
          version-ref (-> doc-ref (.child "versions") (.child version-id))]
      (<? (ma/reset-with-priority!< version-ref @working-version (.now js/Date)))
      (swap! location merge {:version-id (.key version-ref)}))))

(defn save []
  (if-not (= "Saving..." (:save-status @ui))
    (go
      (swap! ui merge {:save-status "Saving..."})
      (try
        (if (signed-in?)
          (do
            (<? (if (:doc-id @location) (save-version) (save-new)))
            (swap! ui assoc-in [:save-status] "Save"))
          (prn "not-signed-in"))
        (catch js/Error e
          (swap! ui merge {:save-status (js/Error "Error saving")})
          (prn "save error" e))))))

(defn load-doc-version [doc-id version-id]
  (go?
    (let [version-ref (m/get-in ref [doc-id "versions" version-id])
          version (<? (ma/deref< version-ref))]
      (load-state version {:doc-id doc-id :version-id version-id}))))

(defn load-doc-latest [doc-id]
  (go?
    (let [version-ref (-> ref (.child doc-id) (.child "versions") (.limitToLast 1))
          version (<? (ma/deref< version-ref))
          version-id (name (ffirst version))
          version (last (first version))]
      (load-state version {:doc-id doc-id :version-id version-id}))))
