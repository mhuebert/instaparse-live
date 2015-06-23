(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]]
                   #_[app.macros :refer [cell=]])
  (:require [reagent.core :as r]
            [reagent.cursor :refer [cursor]]
            [app.data :as data]
            [app.keys :as keys]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
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

(def options
  (reaction
    (let [new-options @(cell :options)]
      (try (read-string new-options)
           (catch js/Error e
             data/default-options)))))

(defonce output
         (reaction (compute/parse @cells)))

(keys/register "ctrl+p" (fn [] (reset! output (compute/parse @cells))))

(defn update-cells []
  (doseq [{:keys [editor a]} (map #(-> % last r/state-atom deref) (:editors @ui))]
    (let [new-val (.getValue editor)]
      (.setTimeout js/window #(reset! a new-val) 10))))

(keys/register "ctrl+r" update-cells)