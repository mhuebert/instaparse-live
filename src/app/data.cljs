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
  ; :string-ci true
  ; :partial false
  ; :total false
  ; :input-format  ; [:ebnf, :abnf]
  ; :output-format ; [:hiccup, :enlive]
  ; :unhide ; [:content, :tags, :all]
  ; :start  ; <:rule-name>
}")

(def sample-doc {:username "mhuebert"
            :title "Example"
            :description "Build a parser in your browser! This page runs the [ClojureScript port](https://github.com/lbradstreet/instaparse-cljs) of [Instaparse](https://github.com/Engelberg/instaparse)."
            :owner nil})

(def sample-version {
                    :grammar "Sentence = (word | comma | <space>)+ end

word = #'\\w+'
comma = ','
space = #'\\s'
end = '.' | '?' | '!' "
                    :sample  "Hello, world!"
                    :options default-options
                    })

(def loading-doc {})
(def loading-version {:grammar "Patience := expectation+
expectation = '.'"
                      :sample "..."
                      :options default-options
                  })