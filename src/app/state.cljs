(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r :refer [cursor]]
            [app.data :as data]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
  (:import goog.History))

(defonce db (r/atom {:cells data/cells-sample
                     :doc   data/doc-sample
                     :user  {}
                     :ui    data/ui-defaults
                     }))

(def cells (cursor db [:cells] ))
(def doc (cursor db [:doc] ))
(def user (cursor db [:user] ))
(def ui (cursor db [:ui] ))
(defonce history (History.))

(defn cell [label]
  (cursor cells [label] ))

(def options
  (reaction
    (let [new-options @(cell :options)]
      (try (read-string new-options)
           (catch js/Error e
             data/option-defaults)))))

(defonce output
         (reaction
           (compute/parse @cells)))

