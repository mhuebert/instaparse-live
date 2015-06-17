(ns app.state
  (:require [reagent.core :as r]
            [app.data :as data]))

(defonce editors (atom (sorted-map)))
(defonce editor-focus (atom nil))
(defonce options (r/atom data/default-options))
(defonce grammar (r/atom data/default-grammar))
(defonce sample (r/atom data/default-sample-code))
(defonce user (r/atom {}))
(defonce location (r/atom {}))
(defonce ui (r/atom {:show-options false}))
