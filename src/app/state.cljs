(ns app.state
  (:require-macros [reagent.ratom :refer [reaction run!]])
  (:require [reagent.core :as r :refer [cursor]]
            [app.data :as data]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
  (:import goog.History))

(defonce db (r/atom {:cells data/cells-sample
                     :doc   data/doc-sample
                     :user  {}
                     :ui    data/ui-defaults}))

(def cells (cursor db [:cells] ))
(def doc (cursor db [:doc] ))
(def user (cursor db [:user] ))
(def ui (cursor db [:ui] ))
(defonce history (History.))

(def sample (r/atom (:sample @cells)))
(def grammar (r/atom (:sample @cells)))
(def options (r/atom {}))

(defonce output
         (reaction
           (compute/parse @cells)))

(defonce _
         (run!
           (reset! sample (:sample @cells))
           (reset! grammar (:sample @cells))
           (reset! options (let [new-options (:options @cells)]
                             (try (read-string new-options)
                                  (catch js/Error e
                                    data/option-defaults))))))