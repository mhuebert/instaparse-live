(ns app.util
  (:require [instaparse.core :as insta]
            [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]

            [clojure.walk :refer [postwalk]]
            [reagent.core :as r]
            ))

(enable-console-print!)


(defn parse [rules sample]
  (try
    (let [parser (insta/parser rules)
          result (insta/parses parser sample)
          failure? (insta/failure? result)]
      (if failure?
        (let [result (into {} (insta/get-failure result))]
          (if (string? result) result
                               (vec (concat
                                      [:div {:class "parse-failure"}]
                                      [[:div
                                        [:strong "Line "] (:line result)
                                        [:strong ", index "] (:index result)
                                        [:strong ", column "] (:column result)
                                        ]
                                       [:div {:style {:marginLeft 10}} (map (fn [reason]
                                                                              (let [tag (name (:tag reason))
                                                                                    expected (with-out-str (prn (:expecting reason)))]
                                                                                [:div [:strong tag] " expected " expected])) (:reason result))]
                                       [:div [:strong "Text: "] (:text result)]]))))
        (let [result (postwalk
                       (fn [x]
                         (if (vector? x) [:div {:class (str "parse-tag " (name (first x)))}
                                          [:span #_"[" [:span {:class "tag-name"} (str (first x) " ")]] (rest x) #_"]"] x))
                       result)]
          (if (empty? result) "No match" [:div {:class "parse-output"} result]))))
    (catch :default e
      (do
        (prn "Caught error:" e)
        [:div {:style {:padding 5}} (str e)]))))

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
