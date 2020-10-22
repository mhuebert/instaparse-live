(ns app.keys
  (:require [goog.ui.KeyboardShortcutHandler]
            [goog.events]
            [app.ui :as ui]
            [app.state :as state]
            [persistence.docs :as docs]))

(defonce handler (new goog.ui.KeyboardShortcutHandler js/document))
(defonce actions (atom {}))

(defn key-event [e]
  (let [id (.-identifier e)
        func (get @actions id)]
    (if func (func))))

(defn unregister [shortcut]
  (.unregisterShortcut handler shortcut))

(defn register [shortcut func]
  (unregister shortcut)
  (let [label (str (rand-int 99999))]
    (.registerShortcut handler label shortcut)
    (swap! actions assoc label func)))

(register "meta+s" #(docs/save!))
(register "ctrl+r" #(state/refresh-editor-state!))
(register "alt+tab" #(ui/editor-jump!))

(defn init []
  (goog.events/listen handler goog.ui.KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED key-event))

