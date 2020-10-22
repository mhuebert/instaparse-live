(ns persistence.docs
  (:require [persistence.auth :as auth]
            [app.state :as state :refer [user ui cells]]
            [app.util]
            [app.data :as data]
            [persistence.firebase :as fire]
            [kitchen-async.promise :as p]))

; todo:
; - list docs for user
; - review security rules

(defn next-version-id [doc-id]
  (p/->> (fire/swap! (fire/get-in [:counters doc-id])
                     #(+ 1 (or % 0)))
         (str "v")))


(defn save-new! []
  (let [doc-ref (.push (fire/get-in [:docs]))
        doc-id (.-key doc-ref)
        new-doc (merge @state/doc {:owner (:uid @user)
                                   :username (get-in @user [:github :username])
                                   :parent {:doc-id (:id @state/doc) :version-id (:id @cells)}
                                   :id doc-id})]
    (p/do
      (fire/reset! doc-ref new-doc)
      (swap! state/doc merge new-doc)
      (p/let [version-id (next-version-id doc-id)
              version-ref (fire/get-in [:versions doc-id version-id])
              version (merge @cells {:id version-id})]
        (fire/reset-with-priority! version-ref version (.now js/Date))
        (reset! cells version)))))

(defn new! []
  (reset! state/doc {:title nil :description nil :owner (:uid @state/user) :username (get-in @state/user [:github :username])})
  (reset! state/cells data/cells-sample)
  (.setToken ^js state/history "/new"))

(defn fork! []
  (state/refresh-editor-state!)
  (if-not (= "Forking..." (:fork-status @ui))
    (swap! ui merge {:fork-status "Forking..."})
    (try
      (p/do
        (save-new!)
        (swap! ui merge {:fork-status "Fork"}))
      (catch js/Error e
        (prn e)
        (swap! ui merge {:fork-status (js/Error "Error forking")})))))

(defn save-version! []
  (p/do
    (fire/reset! (fire/get-in [:docs (:id @state/doc)]) @state/doc)
    (p/let [doc-id (:id @state/doc)
            version-id (next-version-id doc-id)
            version-ref (fire/get-in [:versions doc-id version-id])
            version (assoc @cells :id version-id)]
      (fire/reset-with-priority! version-ref version (.now js/Date))
      (swap! cells assoc :id version-id))))

(defn save! []
  (state/refresh-editor-state!)
  (when (and (auth/signed-in?) (not= "Saving..." (:save-status @ui)))
    (try
      (p/do
        (swap! ui merge {:save-status "Saving..."})
        (if (:id @state/doc)
          (save-version!)
          (save-new!))
        (swap! ui assoc-in [:save-status] "Save")
        (.setToken state/history (str "/" (:id @state/doc))))
      (catch js/Error e
        (swap! ui merge {:save-status (js/Error "Error saving")})
        (js/console.error e)
        (prn "save error" e)))))

(defn get-doc [id]
  @(fire/get-in [:docs id]))

(defn start-load []
  (swap! state/db assoc :cells data/cells-loading))

(defn latest-version-id [doc-id]
  (p/let [the-count @(fire/get-in [:counters doc-id])]
    (str "v" (or the-count 1))))

(defn view-doc
  ([doc-id] (view-doc doc-id nil))
  ([doc-id version-id]
   (start-load)
   (p/let [doc (get-doc doc-id)
           version-id (or version-id (latest-version-id doc-id))
           version @(fire/get-in [:versions doc-id version-id])]
     (swap! state/db assoc
            :doc doc
            :cells version))))

(defn view-sample! []
  (swap! state/db assoc
         :doc data/doc-sample
         :cells data/cells-sample))
