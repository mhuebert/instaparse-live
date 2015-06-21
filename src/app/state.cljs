(ns app.state
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [reagent.cursor :refer [cursor]]
            [app.data :as data]
            [goog.ui.KeyboardShortcutHandler]
            [app.compute :as compute])
  (:import goog.History))

#_(defonce cells (r/atom {}))
#_(defn cell
  ([label] (label @cells))
  ([label value] (swap! cells assoc label value)))

(defonce user (r/atom {}))

(defonce ui (r/atom {:save-status "Save"
                     :fork-status "Fork"
                     :editors (sorted-map)
                     :editor-focus nil
                     }))

(defonce doc (r/atom data/sample-doc))
(defonce version (r/atom data/sample-version))

(defonce output
         (reaction
           (compute/parse @version)))

(defonce power (r/atom false))

(defonce history (History.))

