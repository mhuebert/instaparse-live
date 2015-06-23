(ns persistence.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [matchbox.core :as m]
            [matchbox.async :as ma]
            [cljs.core.async :refer [<!]]))

(defonce ref (m/connect "http://instaparse-live.firebaseio.com/"))

(defn get-in-ref [path]
  (go
    (let [item-ref (m/get-in ref path)]
      (<! (ma/deref< item-ref)))))