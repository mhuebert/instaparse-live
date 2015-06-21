(ns app.routes
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [app.state :as state]
            [app.db :as db]
            [reagent.cursor :refer [cursor]]
            [cljs.core.async :refer [<!]]
            [app.data :as data]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))


(defroute "/" []
          (reset! state/doc data/sample-doc )
          (reset! state/version data/sample-version))

(defn new-doc []
  (reset! state/doc {:title nil :description nil :owner (:uid @state/user) :username (get-in @state/user [:github :username])})
  (reset! state/version data/sample-version)
  (.setToken state/history "/new"))

(defroute "/new" [] (new-doc))

(defroute "/power" []
          (reset! state/power true))

(defroute doc-path "/:doc-id" [doc-id]
          (if (not= doc-id (:id @state/doc))
            (do (reset! state/version data/loading-version)
                (reset! state/doc data/loading-doc)))
          (go (reset! state/doc (<! (db/get-doc doc-id))))
          (go (reset! state/version (<! (db/get-version doc-id)))))

(defroute version-path "/:doc-id/:version-id" [doc-id version-id]
          (go (reset! state/doc (<! (db/get-doc doc-id))))
          (go (reset! state/version (<! (db/get-version doc-id version-id)))))

(defn dispatch
  ([] (dispatch (-> js/window .-location .-hash)))
  ([e]
   (cond
     (string? e) (secretary/dispatch! e)
     (.-isNavigation e) (secretary/dispatch! (.-token e)))))

(defn init []
  (secretary/set-config! :prefix "#")
  (goog.events/listen state/history EventType/NAVIGATE dispatch)
  (dispatch)
  (.setEnabled state/history true))

(defonce _
         (do
           (init)
           (db/sign-in-anon)))

