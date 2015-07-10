(ns app.data)

(def ui-defaults {:save-status "Save"
                  :fork-status "Fork"
                  :editors (sorted-map)
                  :editors2 (sorted-map)
                  :editor-focus nil
                  :power false
                  })

(def option-defaults "{
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

;('editable {})

;{... editable : true}

;get-in cell, cond on key

(def doc-sample {:username "mhuebert"
                 :title "Example"
                 :description "Build a parser in your browser! Read the [**Instaparse docs**](https://github.com/Engelberg/instaparse/blob/master/README.md) to get started.

*—thanks to [instaparse](https://github.com/Engelberg/instaparse) + [instaparse-cljs](https://github.com/lbradstreet/instaparse-cljs)!*"
                 :owner nil})

(def cells-sample {:grammar "(* Sample grammar in ebnf notation *)

Sentence = (word | comma | <space>)+ end
word = #'\\w+'
comma = ','
space = #'\\s'
end = '.' | '?' | '!' "
                    :sample  "Hello, world!"
                    :options option-defaults
                    })

(def doc-loading {})
(def cells-loading {:grammar "Patience := expectation+
expectation = '.'"
                    :sample "..."
                    :options option-defaults
                    })

