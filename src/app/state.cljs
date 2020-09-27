(ns app.state
  (:require-macros [reagent.ratom :refer [reaction run!]])
  (:require [reagent.core :as r :refer [cursor]]

            [app.data :as data]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
  (:import goog.History))

(defonce db (r/atom {:cells data/cells-sample
                     :doc   data/doc-sample
                     :user  nil
                     :ui    data/ui-defaults}))

(defonce cells (cursor db [:cells]))
(defonce doc (cursor db [:doc] ))
(defonce user (cursor db [:user]))
(defonce ui (cursor db [:ui]))
(defonce history (History.))


(defonce sample (cursor cells [:sample]))
(defonce grammar (cursor cells [:grammar]))
(defonce options-txt (cursor cells [:options]))
(defonce options-clj (r/track! #(try
                                  (read-string @options-txt)
                                  (catch js/Error e
                                    (reset! options-txt data/option-defaults)))))

(defonce inputs (r/track #(select-keys @cells [:grammar :sample :options])))
(defonce output (r/track! #(compute/parse @inputs)))

;; derived data - flowing
;; - save work: at each step, stop when value hasn't changed
;; - caches of computed values

;; how are computed values addressable?
;; - derived attributes-of-entities?
;; - a URL (ID) ...
