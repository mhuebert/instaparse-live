(ns app.keys
  (:require  [goog.ui.KeyboardShortcutHandler]
             [goog.events]))

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

(defonce _
         (goog.events/listen handler goog.ui.KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED key-event))

