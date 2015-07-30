(ns persistence.docs
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [app.macros :refer [<? go?]])
  (:require [persistence.core :refer [ref get-in-ref]]
            [persistence.auth :as auth]
            [matchbox.core :as m]
            [matchbox.async :as ma]
            [cljs.core.async :refer [<! put! chan]]
            [cljsjs.firebase]
            [app.state :as state :refer [user ui cells]]
            [app.util]
            [app.data :as data]))

; todo:
; - list docs for user
; - review security rules

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

(defn new []
  (reset! state/doc {:title nil :description nil :owner (:uid @state/user) :username (get-in @state/user [:github :username])})
  (reset! state/cells data/cells-sample)
  (.setToken state/history "/new"))

(defn fork []
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
  (if (and (auth/signed-in?) (not= "Saving..." (:save-status @ui)))
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

(defn get-doc [id]
  (go? (<? (get-in-ref [:docs id]))))

(defn start-load []
  (swap! state/db assoc :cells data/cells-loading))

(defn get-version
  ([doc-id]
   (go?
      (let [version-id (str "v"
                             (or (<? (get-in-ref ["counters" doc-id])) 1))]
        (<? (get-version doc-id version-id)))))
  ([doc-id version-id]
   (go? (<? (get-in-ref [:versions doc-id version-id])))))

(defn view-doc [doc-id]
  (start-load)
  (go (swap! state/db assoc :doc (<! (get-doc doc-id))))
  (go (swap! state/db assoc :cells (<! (get-version doc-id)))))

(defn view-doc-version [doc-id version-id]
  (start-load)
  (go (swap! state/db assoc :doc (<! (get-doc doc-id))))
  (go (swap! state/db assoc :cells (<! (get-version doc-id version-id)))))

(defn view-sample []
  (swap! state/db merge {:doc   data/doc-sample
                         :cells data/cells-sample}))