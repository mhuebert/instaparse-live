(ns persistence.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [matchbox2.core :as m]
            [matchbox2.async :as ma]
            [cljs.core.async :refer [<!]]))

(defn get-in-ref [path]
  (ma/deref< (m/get-in @m/!root path)))
