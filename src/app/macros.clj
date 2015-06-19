(ns app.macros)

(defmacro <? [expr]
  "Throw errors received from the channel"
  `(app.util/throw-err (cljs.core.async/<! ~expr)))

(defmacro go? [& forms]
  "If an error is thrown in this go block, put it on the channel."
  `(cljs.core.async.macros/go
     (try (do ~@forms)
          (catch js/Error e e))))