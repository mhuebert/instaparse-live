(ns app.util
  (:require [instaparse.core :as insta]
            [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
            [clojure.walk :refer [postwalk]]
            [reagent.core :as r]
            [cljs.reader :refer [read-string]]
            ))

(enable-console-print!)

(defn string-result
  [result & {:keys [error]}]
  (prn error)
  [:div {:style {:padding     10
                 :color       (if error "#D53182" "inherit")
                 :font-family "monospace"
                 :white-space "pre-wrap"}}
   result])

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
                                (do (prn "New parser")
                                  (reset! last-args args)
                                    (reset! last-val (apply f args))
                                    @last-val)))))

(defonce memoized-parser (memoize-last-val insta/parser))

(defn parse [grammar sample options]
  (try
    (let [options (read-string options)
          _ (prn options)
          parser (apply memoized-parser
                        (apply concat [grammar] (seq (select-keys options [:input-format :output-format :string-ci]))))
          result (apply insta/parses
                        (apply concat [parser sample] (seq (select-keys options [:start :partial :total :unhide :trace]))))
          failure (if (insta/failure? result) (insta/get-failure result) nil)
          output-format (:output-format options)]

      (binding [*print-readably* false]
        (cond failure (string-result (pr-str failure) :error true)
              (= output-format :hiccup) (string-result (postwalk (fn [x] (with-out-str (pr x))) result))
              (= output-format :enlive) (string-result (str "Not sure how to print :enlive yet.\n\n" (apply str result)))
              (= output-format :raw) (string-result result)
              :else (visualized-result (take (or (:max-parses options) 20) result))))
      )
    (catch :default e
      (do
        (prn e (string? e))
        (string-result
          (if (string? e) e
                          (str e "\n\n===\n(likely a problem w/ options map)")))))))

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
                                     editor (.fromTextArea js/CodeMirror node config)
                                     val (or @a "")]
                                (r/set-state % {:editor editor})
                                (.setValue editor val)
                                (add-watch a nil (fn [_ _ _ new-state]
                                                   (if (not= new-state (.getValue editor))
                                                     (.setValue editor (or new-state "")))))
                                (.on editor "change" (fn [_]
                                                       (let [value (.getValue editor)]
                                                         (reset! a value))))
                                )

      :component-will-unmount #(.off (:editor (r/state %)) "change")


      :display-name           "CodeMirror Component"
      :reagent-render         (fn []
                                [:textarea {:style {:width "100%" :height "100%" :display "flex" :background "red" :flex 1}}])
      })))

(def default-grammar "
<S> = (sexp | whitespace)+
sexp = <'('> operation <')'>

<operation> = operator + args
operator = #'[+*-\\\\]'
args = (num | <whitespace> | sexp)+
<num> = #'[0-9]+'
<whitespace> = #'\\s+'")

(def default-sample-code "(+ 1 (- 3 1))")

(def default-options "{
  :max-parses 20
  ; :string-ci true
  ; :partial false
  ; :total false
  ; :input-format  ; [:ebnf, :abnf]
  ; :output-format ; [:hiccup, :enlive]
  ; :unhide ; [:content, :tags, :all]
  ; :start  ; <:rule-name>
}")

(def blank-grammar "Sentence = (word | comma | <space>)+ end

word = #'\\w+'
comma = ','
space = #'\\s'
end = '.' | '?' | '!' ")

(def blank-sample "Hello, world!")