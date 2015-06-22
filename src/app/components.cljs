(ns app.components
  (:require             [reagent.core :as r]
                        [reagent.cursor :refer [cursor]]
                        [app.state :as state]
                        [app.db :as db]
                        [app.util :as util]))

(defn editable-text
  ([a] (editable-text a {}))
  ([a options]
   (let [options (merge {:edit-text "edit"
                         :finish-text "done"
                         :owner true} options)
         input-id (rand-int 9999)
         handle-change (fn [e]
                         (reset! a (.-value (.-target e))))
         editing (r/atom false)
         toggle-edit (fn []
                       (reset! editing (not @editing))
                       (util/focus input-id)
                       (if-not @editing (db/save))
                       )]
     (fn []
       (let [input-options {:id input-id :on-change handle-change :on-blur toggle-edit :value @a}
             md (:markdown options)
             owner (= (:uid @state/user) (:owner @state/doc))
             display-opts {:class (str (if owner "as-owner"))
                           :on-click (if owner toggle-edit #())
                           }]
         [:span
          {:class "editable-text"
           :style {:display (if (:markdown options) "block" "inline")}}
          [:span {:class (if @editing "" "hidden")}
           (if (:markdown options)
             [:textarea input-options]
             [:input (merge input-options (:input options))])]
          [:span { :class (if @editing "hidden" "")}
           (if md
             [:div (merge display-opts {:dangerouslySetInnerHTML {:__html (.toHTML js/markdown (or @a ""))}})]
             [:span display-opts @a])
           (if-not (and (:only-power-edit options) (not (:power @state/ui)))
             (if (and owner (empty? @a))
               [:span " " [:a {:class "text-link" :on-click toggle-edit}
                           (:empty-text options)]]))]])))))

(def cm-defaults {
                  :lineNumbers false
                  :lineWrapping true
                  :styleActiveLine true
                  #_:scrollbarStyle #_"null"
                  :theme "solarized dark"
                  :mode "javascript"})

(defn editor-jump []
  (let [{:keys [editors editor-focus]} @state/ui
        editor (or (last (first (subseq editors > editor-focus))) (last (first editors)))]
    (.focus editor)))

(defn focus-last-editor []
  (js/setTimeout #(.focus (last (last (:editors @state/ui)))) 15))

(defn cm-editor
  ([a] (cm-editor a {}))
  ([a options]
   (let [ui-editors (cursor [:editors] state/ui)
         ui-editor-focus (cursor [:editor-focus] state/ui)]

     (r/create-class
       {
        :component-did-mount    #(let [node (.getDOMNode %)
                                       config (clj->js (merge cm-defaults options))
                                       editor (.fromTextArea js/CodeMirror node config)
                                       val (or @a "")
                                       id (or (:name options) (.now js/Date))]
                                  (swap! ui-editors merge {id editor})
                                  (r/set-state % {:editor editor :id id})
                                  (.setValue editor val)
                                  (add-watch a nil (fn [_ _ _ new-state]
                                                     (if (not= new-state (.getValue editor))
                                                       (.setValue editor (or new-state "")))))
                                  (.on editor "change" (fn [_]
                                                         (let [value (.getValue editor)]
                                                           (reset! a value))))
                                  (.on editor "focus" (fn [_] (reset! ui-editor-focus id))))

        :component-will-unmount (fn [x]
                                  (let [{:keys [id editor]} (r/state x)]
                                    (swap! ui-editors dissoc id)
                                    (.off editor)))


        :display-name           "CodeMirror Component"
        :render         (fn []
                          [:textarea {:style {:width "100%" :height "100%" :display "flex" :background "red" :flex 1}}])
        }))))