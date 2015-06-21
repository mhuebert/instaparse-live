(ns app.util)

(enable-console-print!)

(defn throw-err [e]
  (if (instance? js/Error e) (throw e) e))

(defn memoize-last-val [f]
  (let [last-args (atom {})
        last-val (atom {})]
    (fn [& args]
      (if (= @last-args args) @last-val
                              (do
                                (reset! last-args args)
                                (reset! last-val (apply f args))
                                @last-val)))))

(defn focus [id]
  (.setTimeout js/window
               (fn []
                 (.focus (.getElementById js/document id))) 20))