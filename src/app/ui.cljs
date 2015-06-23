(ns app.ui
  (:require [app.state :as state]
            [reagent.core :as r]))

(defn cm-instance [editor] (-> editor r/state-atom deref :editor))

(defn refresh-editor-state []
  (doseq [{:keys [editor a]} (map #(-> % last r/state-atom deref) (:editors @state/ui))]
    (let [new-val (.getValue editor)
          old-val @a]
      (if (not= new-val old-val)
        (.setTimeout js/window #(reset! a new-val) 10)))))

(defn focus-last-editor []
  (js/setTimeout #(-> @state/ui :editors last last cm-instance .focus) 15))

(defn power-mode []
  (swap! state/ui assoc :power true))

(defn editor-jump []
  (let [{:keys [editors editor-focus]} @state/ui
        editor (cm-instance (or (last (first (subseq editors > editor-focus))) (last (first editors))))]
    (.focus editor)))

(defn focus [id]
  (.setTimeout js/window
               (fn []
                 (.focus (.getElementById js/document id))) 20))