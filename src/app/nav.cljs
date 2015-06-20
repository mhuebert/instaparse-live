(ns app.nav
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [app.state :as state]
            [app.db :as db]
            [reagent.cursor :refer [cursor]]
            [cljs.core.async :refer [<!]]
            [app.data :as data]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [goog.ui.KeyboardShortcutHandler]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History))

(defroute "/" []
          (reset! state/doc data/sample-doc )
          (reset! state/version data/sample-version))

(defn new-doc []
  (reset! state/doc {:title nil :description nil :owner (:uid @state/user) :username (get-in @state/user [:github :username])})
  (reset! state/version data/sample-version)
  (.setToken state/h "/new"))

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


(defn toggle-options []
  (swap! state/ui merge {:show-options (not (:show-options @state/ui))})
  (js/setTimeout #(.focus (last (last (:editors @state/ui)))) 15))

(defn editor-jump []
  (let [{:keys [editors editor-focus]} @state/ui
        editor (or (last (first (subseq editors > editor-focus))) (last (first editors)))]
    (.focus editor)))

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

#_(defn get-path [loc]
  (if (= nil (:doc-id loc)) "#/" (version-path loc)))

(defn init []
  (secretary/set-config! :prefix "#")
  (goog.events/listen state/h EventType/NAVIGATE dispatch)
  (dispatch (-> js/window .-location .-hash))
  (.setEnabled state/h true)
  #_(add-watch location :set-path (fn [_ _ old-location new-location]
                                  (let [path (.substr (get-path new-location) 1)]
                                    (.setToken h path))))

  (registerKeys))
