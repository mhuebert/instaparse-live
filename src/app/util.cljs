(ns app.util
  (:require [instaparse.core :as insta]
            [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
            [clojure.walk :refer [postwalk]]
            [reagent.core :as r]
            [reagent.cursor :refer [cursor]]
            [fipp.edn :refer [pprint]]
            [cljs.reader :refer [read-string]]
            [app.state :as state]
            [app.db :as db]))

(enable-console-print!)

(defn string-result
  [result & {:keys [error]}]
  [:div {:style {:padding     10
                 :color       (if error "#D53182" "inherit")
                 :font-family "monospace"
                 :white-space "pre-wrap"}}
   result])

(defn focus [id]
  (.setTimeout js/window
               (fn []
                 (.focus (.getElementById js/document id))) 20))


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
                       (focus input-id)
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
           (if-not (and (:only-power-edit options) (not @state/power))
                   (if (and owner (empty? @a))
                     [:span " " [:a {:class "text-link" :on-click toggle-edit}
                                 (:empty-text options)]]))]])))))

(defn visualized-result [result]
  (let [result (postwalk
                 (fn [x]
                   (if (vector? x) [:div {:class (str "parse-tag " (name (first x)))}
                                    [:span {:class "tag-name"} (str (first x) " ")] (rest x)]
                                   x))
                 result)]
    [:div {:class "parse-output"} (interpose [:div {:class "parse-sep"}] result)]))


(defn memoize-last-val [f]
  (let [last-args (atom {})
        last-val (atom {})]
    (fn [& args]
        (if (= @last-args args) @last-val
                                (do
                                  (reset! last-args args)
                                  (reset! last-val (apply f args))
                                  @last-val)))))

(defonce memoized-parser (memoize-last-val insta/parser))

(defn parse [{:keys [grammar sample options]}]

  (try
    (let [options (read-string options)
          parser (apply memoized-parser
                        (apply concat [grammar] (seq (select-keys options [:input-format :output-format :string-ci]))))
          result (apply insta/parses
                        (apply concat [parser sample] (seq (select-keys options [:start :partial :total :unhide :trace]))))
          failure (if (insta/failure? result) (insta/get-failure result) nil)
          output-format (:output-format options)
          max-parses (or (:max-parses options) 20)]
      (cond failure (string-result (pr-str failure) :error true)
            (contains? [:hiccup :enlive] output-format) (string-result (with-out-str (pprint result)))
            :else (visualized-result (take max-parses result))))
    (catch :default e
      (let [message (if (string? e) e (str "Options Map " e))]
        (string-result message :error true)))))

(def cm-defaults {
                  :lineNumbers false
                  :lineWrapping true
                  :styleActiveLine true
                  #_:scrollbarStyle #_"null"
                  :theme "solarized dark"
                  :mode "javascript"
                  })

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
        :reagent-render         (fn []
                                  [:textarea {:style {:width "100%" :height "100%" :display "flex" :background "red" :flex 1}}])
        }))))

(defn throw-err [e]
  (if (instance? js/Error e) (throw e) e))
