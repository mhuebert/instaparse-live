(ns app.keys
  (:require  [goog.ui.KeyboardShortcutHandler]
             [app.routes :as nav]
             [app.db :as db]
             [goog.events :as events]))

(defonce handler (new goog.ui.KeyboardShortcutHandler js/document))
(defonce actions (atom {}))

(defn key-event [e]
  (let [id (.-identifier e)
        func (get @actions id)]
    (if func (func))))

(defn register [shortcut func]
  (let [label (str (rand-int 99999))]
    (.registerShortcut handler label shortcut)
    (swap! actions assoc label func)))

(defn unregister [shortcut]
  (.unregisterShortcut handler shortcut))

(defonce _
         (do
           (register "alt+tab" app.components/editor-jump)
           (register "meta+s" db/save)
           (goog.events/listen handler goog.ui.KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED key-event)))