(ns app.editors
  (:require [reagent.core :as r :refer [cursor]]
            [app.state :as state]
            [app.dispatch :refer [dispatch]]
            ["markdown" :refer [markdown]]
            ["/codemirror.js" :as CM]
            ["/codemirror-ebnf.js"]
            [reagent.dom :as rdom]))

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
                       (dispatch [:focus input-id])
                       (if-not @editing (dispatch [:save!]))
                       )]
     (fn []
       (let [input-options {:id input-id :on-change handle-change :on-blur toggle-edit :value @a}
             md (:markdown options)
             owner (= (:uid @state/user) (:owner @state/doc))
             display-opts {:class-name (str (if owner "as-owner"))
                           :on-click (if owner toggle-edit #())
                           }]
         [:span
          {:class-name "editable-text"
           :style {:display (if (:markdown options) "block" "inline")}}
          [:span {:class-name (if @editing "" "hidden")}
           (if (:markdown options)
             [:textarea input-options]
             [:input (merge input-options (:input options))])]
          [:span { :class-name (if @editing "hidden" "")}
           (if md
             [:div (merge display-opts {:dangerouslySetInnerHTML {:__html (.toHTML markdown (or @a ""))}})]
             [:span display-opts @a])
           (if-not (and (:only-power-edit options) (not (:power @state/ui)))
             (if (and owner (empty? @a))
               [:span " " [:a {:class-name "text-link" :on-click toggle-edit}
                           (:empty-text options)]]))]])))))

(def cm-defaults {
                  :lineNumbers false
                  :lineWrapping true
                  :styleActiveLine true
                  #_:scrollbarStyle #_"null"
                  :theme "solarized dark"
                  :mode "javascript"})

(defn cm-editor
  ([a] (cm-editor a {}))
  ([a options]
   (let [ui-editors (cursor state/ui [:editors])
         ui-editor-focus (cursor state/ui [:editor-focus] )]

     (r/create-class
       {:component-did-mount
        (fn [^js component]
          (let [node (rdom/dom-node component)
                config (clj->js (merge cm-defaults options))
                editor (.fromTextArea CM node config)
                val (str @a)
                id (or (:name options) (.now js/Date))]
            (swap! ui-editors merge {id component})
            (r/set-state component {:editor editor :id id :a a})
            (.setValue editor val)
            (add-watch a nil (fn [_ _ _ new-state]
                               (if (not= new-state (.getValue editor))
                                 (.setValue editor (or new-state "")))))
            (.on editor "change" (fn [_]
                                   (when (:auto-update @state/options-clj)
                                     (reset! a (.getValue editor)))))
            (.on editor "focus" (fn [_] (reset! ui-editor-focus id)))))

        :component-will-unmount
        (fn [x]
          (let [{:keys [id editor]} (r/state x)]
            (swap! ui-editors dissoc id)
            (.off editor)))
        :render
        (fn [] [:textarea {:style {:width "100%" :height "100%" :display "flex" :background "red" :flex 1}}])}))))

