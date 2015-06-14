(ns app.nav
  (:require [app.state :refer [location]]
            [app.db :as db]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [goog.ui.KeyboardShortcutHandler]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))

; Define routes
(defroute doc-path "/:doc" [doc]
          (reset! location {:doc-id doc}))

(defroute version-path "/:doc/:version" [doc version]
          (reset! location {:doc-id doc :version-id version}))

(defn keyAction [e]
  (let [action (.-identifier e)]
    (prn action)
    (case action
      "SAVE" (db/save))))

(defn registerKeys
  ([]
    (registerKeys (new goog.ui.KeyboardShortcutHandler js/document)))
  ([handler]
   (doto handler
     (.removeAllListeners)
     (.unregisterAll)
     (.registerShortcut "SAVE" "meta+s")
     (goog.events/listen goog.ui.KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED keyAction))
    handler))

(defn dispatch [e]
  (cond
    (string? e) (secretary/dispatch! e)
    (.-isNavigation e) (secretary/dispatch! (.-token e))))

(defn current-path [location]
  (let [{:keys [doc-id version-id]} location]
    (cond (and doc-id version-id) (version-path {:doc doc-id :version version-id})
          doc-id (doc-path {:doc doc-id})
          :else "")))

(defn init []
  (secretary/set-config! :prefix "#")
  (let [h (History.)]
    (goog.events/listen h EventType/NAVIGATE dispatch)
    (dispatch (-> js/window .-location .-hash))
    (.setEnabled h true)
    (add-watch location :set-path (fn [_ _ _ new-state]
                                    (let [{:keys [doc-id version-id] } new-state
                                          path (.substr (current-path new-state) 1)]
                                      (.setToken h path)))))
  (registerKeys))

