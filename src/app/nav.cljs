(ns app.nav
  (:require [app.state :refer [location]]
            [app.db :as db :refer [load-doc-version load-doc-latest load-state]]
            [app.util :as util]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [goog.ui.KeyboardShortcutHandler]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))

; Define routes

(defroute home "/new" []
          (load-state {:grammar util/blank-grammar :sample util/blank-sample :options util/default-options} {}))

(defroute doc-path "/:doc-id" [doc-id]
          (load-doc-latest doc-id))

(defroute version-path "/:doc-id/:version-id" [doc-id version-id]
          (load-doc-version doc-id version-id))

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

(defn get-path [loc]
  (if (= nil (:doc-id loc)) "#/" (version-path loc))
  )

(defn init []
  (secretary/set-config! :prefix "#")
  (let [h (History.)]
    (goog.events/listen h EventType/NAVIGATE dispatch)
    (dispatch (-> js/window .-location .-hash))
    (.setEnabled h true)
    (add-watch location :set-path (fn [_ _ _ new-location]
                                    (let [path (.substr (get-path new-location) 1)]
                                      (.setToken h path)))))
  (registerKeys))

