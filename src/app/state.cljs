(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go go-loop]]
                   #_[app.macros :refer [cell=]])
  (:require [reagent.core :as r]
            [worker.compute.transit :refer [read write]]
            [reagent.cursor :refer [cursor]]
            [app.data :as data]
            [worker.compute.transit :refer [read write]]
            [worker.compute.core :as compute]
            [cljs.core.async :as a :refer [<! >!]])
  (:import goog.History))


(defonce db (r/atom {:cells data/sample-cells
                     :doc data/sample-doc
                     :user {}
                     :ui data/ui-defaults
                     }))

(def cells (cursor [:cells] db))
(def doc (cursor [:doc] db))
(def user (cursor [:user] db))
(def ui (cursor [:ui] db))

(defonce history (History.))

(defn cell [label]
  (cursor [label] cells))

(defonce output (r/atom ""))
#_(defonce output
         (reaction
           (compute/parse @cells)))

(defonce new-output (a/chan))
(go
  (while true
    (reset! output (<! new-output))))

(defn worker-compute []
  (let [worker (new js/Worker "js/compute.js")]
    (aset worker "onmessage" (fn [e]
                               (let [data (read (.-data e))]
                                 (a/put! new-output data))))
    (.postMessage worker (write @cells))))
(add-watch cells ::listener
           (fn [k a old new]
             (worker-compute)))

