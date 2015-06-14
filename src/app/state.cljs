(ns app.state
  (:require [reagent.core :as r]
            [app.util :as util])
  )

(defonce grammar (r/atom util/default-grammar))
(defonce sample (r/atom util/default-sample-code))
(defonce user (r/atom {}))
(defonce location (r/atom {}))