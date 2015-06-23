(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]]
                   #_[app.macros :refer [cell=]])
  (:require [reagent.core :as r]
            [reagent.cursor :refer [cursor]]
            [app.data :as data]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
  (:import goog.History))

(defonce db (r/atom {:cells data/cells-sample
                     :doc   data/doc-sample
                     :user  {}
                     :ui    data/ui-defaults
                     }))

(def cells (cursor [:cells] db))
(def doc (cursor [:doc] db))
(def user (cursor [:user] db))
(def ui (cursor [:ui] db))
(defonce history (History.))

(defn cell [label]
  (cursor [label] cells))

(def options
  (reaction
    (let [new-options @(cell :options)]
      (try (read-string new-options)
           (catch js/Error e
             data/option-defaults)))))

(defonce output
         (reaction (compute/parse @cells)))

