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
            [app.keys :as keys]
            [app.state :as state :refer [user ui cells]]))


; todo:
; - list docs for user
; - review security rules

(defonce ref (m/connect "http://instaparse-live.firebaseio.com/"))

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
  (reset! user {})
  (.unauth ref))

(defn next-version-id [doc-id]
  (let [counter-ref (m/get-in ref [:counters doc-id])
        id-chan (chan)]
    (m/swap! counter-ref #(+ 1 (or % 0))
             :callback (fn [err committed ss]
                         (let [err (if err (js/Error err) nil)]
                           (put! id-chan (or err (str "v" (.val ss)))))))
    id-chan))


(defn save-new []
  (let [doc-ref (.push (m/get-in ref [:docs]))
        doc-id (.key doc-ref)
        new-doc (merge @state/doc {:owner    (:uid @user)
                                   :username (get-in @user [:github :username])
                                   :parent   {:doc-id (:id @state/doc) :version-id (:id @cells)}
                                   :id       doc-id})]
    (go?
      (<? (ma/reset!< doc-ref new-doc))
      (swap! state/doc merge new-doc))

    (go?
      (let [version-id (<! (next-version-id doc-id))
            version-ref (m/get-in ref [:versions doc-id version-id])
            version (merge @cells {:id version-id})]

        (<? (ma/reset-with-priority!< version-ref version (.now js/Date)))
        (reset! cells version)))))

(defn fork []
  (state/update-cells)
  (if-not (= "Forking..." (:fork-status @ui))
    (go
      (swap! ui merge {:fork-status "Forking..."})
      (try
        (do
          (<? (save-new))
          (swap! ui merge {:fork-status "Fork"}))
        (catch js/Error e
          (prn e)
          (swap! ui merge {:fork-status (js/Error "Error forking")}))))))

(defn save-version []
  (go
    (<? (ma/reset!< (m/get-in ref [:docs (:id @state/doc)]) @state/doc)))
  (go?
    (let [doc-id (:id @state/doc)
          version-id (<? (next-version-id doc-id))
          version-ref (m/get-in ref [:versions doc-id version-id])
          version (assoc @cells :id version-id)]

      (<? (ma/reset-with-priority!< version-ref version (.now js/Date)))
      (swap! cells assoc :id version-id))))

(defn save []
  (state/update-cells)
  (if (and (signed-in?) (not= "Saving..." (:save-status @ui)))
    (go
      (try
        (do
          (swap! ui merge {:save-status "Saving..."})
          (<? (if (:id @state/doc) (save-version) (save-new)))
          (swap! ui assoc-in [:save-status] "Save")
          (.setToken state/history (str "/" (:id @state/doc))))
        (catch js/Error e
          (swap! ui merge {:save-status (js/Error "Error saving")})
          (prn "save error" e))))))

(keys/register "meta+s" save)

(defn get-in-ref [path]
  (go
    (let [item-ref (m/get-in ref path)]
      (<! (ma/deref< item-ref)))))

(defn get-doc [id]
  (go? (<? (get-in-ref [:docs id]))))

(defn get-cells
  ([doc-id]
   (go?
      (let [version-id (str "v"
                             (or (<? (get-in-ref ["counters" doc-id])) 1))]
        (<? (get-cells doc-id version-id)))))
  ([doc-id version-id]
   (go? (<? (get-in-ref [:versions doc-id version-id])))))

