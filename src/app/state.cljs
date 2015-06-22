(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [reagent.cursor :refer [cursor]]
            [app.data :as data]
            [goog.ui.KeyboardShortcutHandler]
            [app.compute :as compute])
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

(defn cell [label] (cursor [label] cells))
(defn cell! [label value]
  (swap! cells assoc label value))

(defonce output
         (reaction
           (compute/parse @cells)))

(defonce history (History.))