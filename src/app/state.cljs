(ns app.state
  (:require-macros [reagent.ratom :refer [reaction run!]])
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]

            [app.data :as data]
            [app.compute :as compute]
            [cljs.reader :refer [read-string]])
  (:import goog.History))

(defonce db (r/atom {:cells data/cells-sample
                     :doc   data/doc-sample
                     :user  nil
                     :ui    data/ui-defaults}))

(defn cursor [ref path]
  (let [state (ratom/run!
                (get-in @ref path))]
    (reify
      IDeref
      (-deref [_] @state)
      IReset
      (-reset! [_ new-value] (swap! ref assoc-in path new-value))
      ISwap
      (-swap! [o f] (swap! ref update-in path f))
      (-swap! [o f a] (swap! ref update-in path f a))
      (-swap! [o f a b] (swap! ref update-in path f a b))
      (-swap! [o f a b xs] (swap! ref update-in path #(apply f % a b xs)))
      IWatchable
      (-add-watch [this key f] (add-watch state key f))
      (-remove-watch [this key] (remove-watch state key))
      (-notify-watches [this old new] (-notify-watches state old new)))))

(defonce cells (cursor db [:cells]))
(defonce doc (cursor db [:doc] ))
(defonce user (cursor db [:user]))
(defonce ui (cursor db [:ui]))
(defonce history (History.))

(defonce sample (cursor db [:cells :sample]))
(add-watch sample :sample (partial prn :SAMPLE))
(defonce grammar (cursor db [:cells :grammar]))
(defonce options-txt (cursor db [:cells :options]))
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

(defn refresh-editor-state! []
  (doseq [{:keys [editor a]} (map #(-> % last r/state-atom deref) (:editors @ui))]
    (let [new-val (.getValue editor)
          old-val @a]
      (when (not= new-val old-val)
        (.setTimeout js/window #(reset! a new-val) 10)))))
