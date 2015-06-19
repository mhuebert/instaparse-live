(ns app.state
  (:require [reagent.core :as r]
            [app.data :as data]))

(defonce user (r/atom {}))
(defonce location (r/atom {}))
(defonce ui (r/atom {:show-options false
                     :save-status "Save"
                     :fork-status "Fork"
                     :editors (sorted-map)
                     :editor-focus nil}))
(defonce working-version (r/atom data/blank))
