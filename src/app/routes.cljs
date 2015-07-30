(ns app.routes
  (:require [app.state :as state]
            [app.dispatch :refer [dispatch]]
            [goog.events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))

(defroute "/" []
          (dispatch [:view-sample]))

(defroute "/new" []
          (dispatch [:new!]))

(defroute "/power" []
          (dispatch [:enter-power-mode]))

(defroute doc-path "/:doc-id" [doc-id]
          (dispatch [:view-doc doc-id]))

(defroute version-path "/:doc-id/:version-id" [doc-id version-id]
          (dispatch [:view-doc-version doc-id version-id]))

(defn dispatch-route
  ([] (dispatch-route (-> js/window .-location .-hash)))
  ([e]
   (cond
     (string? e) (secretary/dispatch! e)
     (.-isNavigation e) (secretary/dispatch! (.-token e)))))

(defn init []
  (secretary/set-config! :prefix "#")
  (goog.events/listen state/history EventType/NAVIGATE dispatch-route)
  (dispatch-route)
  (.setEnabled state/history true)
  (dispatch [:sign-in-anon]))

(defonce _ (init))

