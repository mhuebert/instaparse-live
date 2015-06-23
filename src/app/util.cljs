(ns app.util)

(enable-console-print!)

(defn throw-err [e]
  (if (instance? js/Error e) (throw e) e))