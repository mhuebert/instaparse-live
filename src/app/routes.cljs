(ns app.routes
  (:require [app.state :as state]
            [goog.events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [persistence.docs :as docs]
            [app.ui :as ui])
  (:import goog.History))

(defroute "/" []
          (docs/view-sample!))

(defroute "/new" []
          (docs/new!))

(defroute "/power" []
          (ui/power-mode!))

(defroute doc-path "/:doc-id" [doc-id]
         (docs/view-doc doc-id))

(defroute version-path "/:doc-id/:version-id" [doc-id version-id]
          (docs/view-doc doc-id version-id))

(defn dispatch-route
  ([] (dispatch-route (-> js/window .-location .-hash)))
  ([^js e]
   (cond
     (string? e) (secretary/dispatch! e)
     (.-isNavigation e) (secretary/dispatch! (.-token e)))))

(defn init []
  (secretary/set-config! :prefix "#")
  (goog.events/listen state/history EventType/NAVIGATE dispatch-route)
  (dispatch-route)
  (.setEnabled state/history true))

