(ns app.editors
  (:require [reagent.core :as r]
            [app.state :as state :refer [cursor]]
            ["markdown" :refer [markdown]]
            ["/codemirror.js" :as CM]
            ["/codemirror-ebnf.js"]
            [reagent.dom :as rdom]
            [app.ui :as ui]
            [persistence.docs :as docs]
            [applied-science.js-interop :as j]))

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
         finish! #(do (reset! editing false) (docs/save!))
         edit! (fn []
                 (reset! editing true)
                 (ui/focus! input-id))]
     (fn []
       (let [input-options {:id input-id
                            :on-change handle-change
                            :on-key-down #(when (= "Enter" (j/get % :key))
                                            (finish!))
                            :on-blur finish!
                            :value @a}
             md (:markdown options)
             owner (= (:uid @state/user) (:owner @state/doc))
             display-opts {:class-name (str (if owner "as-owner"))
                           :on-click (when owner edit!)}]
         [:span
          {:class-name "editable-text"
           :style {:display (if (:markdown options) "block" "inline")}}
          [:span {:class-name (when-not @editing "hidden")}
           (if (:markdown options)
             [:textarea input-options]
             [:input (merge input-options (:input options))])]
          [:span {:class-name (if @editing "hidden" "")}
           (if md
             [:div (merge display-opts {:dangerouslySetInnerHTML {:__html (.toHTML markdown (or @a ""))}})]
             [:span display-opts @a])
           (if-not (and (:only-power-edit options) (not (:power @state/ui)))
             (if (and owner (empty? @a))
               [:span " " [:a {:class-name "text-link" :on-click edit!}
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
         ui-editor-focus (cursor state/ui [:editor-focus])]

     (r/create-class
       {:component-did-mount
        (fn [^js component]
          (let [node (rdom/dom-node component)
                config (clj->js (merge cm-defaults options))
                editor (.fromTextArea CM node config)
                id (or (:name options) (.now js/Date))]
            (swap! ui-editors merge {id component})
            (r/set-state component {:editor editor :id id :a a})
            (.setValue editor (str @a))
            (add-watch a :editor (fn [_ _ _ new-state]
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
