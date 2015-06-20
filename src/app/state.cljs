(ns app.state
  (:require [reagent.core :as r]
            [app.data :as data])
  (:import goog.History))

(defonce user (r/atom {}))

(defonce ui (r/atom {:show-options false
                     :save-status "Save"
                     :fork-status "Fork"
                     :editors (sorted-map)
                     :editor-focus nil
                     }))

(defonce doc (r/atom data/sample-doc))
(defonce version (r/atom data/sample-version))
(defonce power (r/atom false))
(defonce h (History.))