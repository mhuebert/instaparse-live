(ns app.util
  (:require [instaparse.core :as insta]
            [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
            [clojure.walk :refer [postwalk]]
            [reagent.core :as r]
            ))

(enable-console-print!)

(defn error-message
  [error-msg]
  [:div {:style {:padding 10
                 :color "#D53182"
                 :font-family "monospace"
                 :white-space "pre-wrap"}}
   error-msg])

(defn visualize-result [result]
  (let [result (postwalk
                 (fn [x]
                   (if (vector? x) [:div {:class (str "parse-tag " (name (first x)))}
                                    [:span {:class "tag-name"} (str (first x) " ")] (rest x)]
                                   x))
                 result)]
    [:div {:class "parse-output"} (interpose [:div {:class "parse-sep"}] result)]))

(defn parse [rules sample]
  (try
    (let [parser (insta/parser rules)
          result (insta/parses parser sample)
          failure (if (insta/failure? result) (insta/get-failure result) nil)]
      (if failure
        (error-message (pr-str failure))
        (visualize-result (take 20 result))))
    (catch :default e
      (do
        (error-message e)))))

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

   (r/create-class
     {
      :component-did-mount    #(let [node (.getDOMNode %)
                                     config (clj->js (merge cm-defaults options))
                                     editor (.fromTextArea js/CodeMirror node config)]
                                (r/set-state % {:editor editor})
                                (.setValue editor @a)
                                (.on editor "change" (fn [_]
                                                       (let [value (.getValue js/editor)]
                                                         (reset! a value)))))

      :component-will-unmount #(.off (:editor (r/state %)) "change")


      :display-name           "CodeMirror Component"
      :reagent-render         (fn []
                                [:textarea {:style {:width "100%" :height "100%" :display "flex" :background "red" :flex 1}}])
      })))
