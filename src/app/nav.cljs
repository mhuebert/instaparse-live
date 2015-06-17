(ns app.nav
  (:require [app.state :refer [location]]
            [app.db :as db :refer [load-doc-version load-doc-latest load-state]]
            [app.data :as data]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [goog.ui.KeyboardShortcutHandler]
            [secretary.core :as secretary :refer-macros [defroute]]
            [app.state :as state])
  (:import goog.History))

; Define routes

(defroute home "/new" []
          (load-state {:grammar data/blank-grammar :sample data/blank-sample :options data/default-options} {}))

(defroute doc-path "/:doc-id" [doc-id]
          (load-doc-latest doc-id))

(defroute version-path "/:doc-id/:version-id" [doc-id version-id]
          (load-doc-version doc-id version-id))


(defn toggle-options []
  (swap! state/ui merge {:show-options (not (:show-options @state/ui))})
  (js/setTimeout #(.focus (last (last @state/editors))) 15)
  )

(defn editor-jump []
  (let [editors @state/editors
        focused @state/editor-focus
        editor (or (last (first (subseq editors > focused))) (last (first editors)))]
    (.focus editor))
  )


(defn keyAction [e]
  (let [action (.-identifier e)]
    (case action
      "SAVE" (db/save)
      "OPTIONS" (toggle-options)
      "EDITOR-JUMP" (editor-jump))))


(defn registerKeys
  ([]
    (registerKeys (new goog.ui.KeyboardShortcutHandler js/document)))
  ([handler]
   (doto handler
     (.removeAllListeners)
     (.unregisterAll)
     (.registerShortcut "SAVE" "meta+s")
     (.registerShortcut "OPTIONS" "ctrl+o")
     (.registerShortcut "EDITOR-JUMP" "alt+tab")
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
