(ns app.ui
  (:require [app.state :as state]
            [reagent.core :as r]))

(defn cm-instance [editor] (-> editor r/state-atom deref :editor))

(defn focus-last-editor! []
  (js/setTimeout #(-> @state/ui :editors last last cm-instance .focus) 15))

(defn power-mode! []
  (swap! state/ui assoc :power true))

(defn editor-jump! []
  (let [{:keys [editors editor-focus]} @state/ui
        editor (cm-instance (or (last (first (subseq editors > editor-focus))) (last (first editors))))]
    (.focus editor)))

(defn focus! [id]
  (.setTimeout js/window
               (fn []
                 (.focus (.getElementById js/document id))) 20))
