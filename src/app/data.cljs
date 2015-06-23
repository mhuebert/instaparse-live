(ns app.data)

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
  :auto-update true ; when false, ctrl+r to refresh
  :input-format :ebnf ;abnf
  :string-ci false
  ; :partial true/false
  ; :total true/false
  ; :output-format [:hiccup :enlive]
  ; :unhide ; [:content :tags :all]
  ; :start  ; :rule-name

}")



(def sample-doc {:username "mhuebert"
            :title "Example"
            :description "Build a parser in your browser! Read the [**Instaparse docs**](https://github.com/Engelberg/instaparse/blob/master/README.md) to get started.

*—thanks to [instaparse](https://github.com/Engelberg/instaparse) + [instaparse-cljs](https://github.com/lbradstreet/instaparse-cljs)!*"
            :owner nil})

(def sample-cells {
                    :grammar "(* Sample grammar in ebnf notation *)

Sentence = (word | comma | <space>)+ end
word = #'\\w+'
comma = ','
space = #'\\s'
end = '.' | '?' | '!' "
                    :sample  "Hello, world!"
                    :options default-options
                    })

(def loading-doc {})
(def loading-cells {:grammar "Patience := expectation+
expectation = '.'"
                      :sample "..."
                      :options default-options
                  })

(def ui-defaults {:save-status "Save"
                  :fork-status "Fork"
                  :editors (sorted-map)
                  :editors2 (sorted-map)
                  :editor-focus nil
                  :power false})