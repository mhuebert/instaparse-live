(ns app.util)

(defn throw-err [e]
  (if (instance? js/Error e) (throw e) e))

