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

(def blank-grammar "Sentence = (word | comma | <space>)+ end

word = #'\\w+'
comma = ','
space = #'\\s'
end = '.' | '?' | '!' ")

(def blank-sample "Hello, world!")