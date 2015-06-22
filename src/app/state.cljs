(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]]
                   #_[app.macros :refer [cell=]])
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
(defonce history (History.))

(defn cell [label]
  (cursor [label] cells))


(defonce output
         (reaction
           (compute/parse @cells)))

