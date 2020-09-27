(ns matchbox2.async
  (:require [matchbox2.core :as m]
            [cljs.core.async :refer [<! >! chan put! close!]]))

(defn with-chan
  "Call a function with a fresh channel, then return the channel"
  [f]
  (let [ch (chan)] (f ch) ch))

(defn chan->cb
  "Create callback that pushes non-nil arguments onto given chan"
  [ch]
  (fn [val] (when val (put! ch val))))

(defn chan->cb-once
  "Create callback that pushes arguments onto chan at-most once"
  [ch]
  (fn [val]
    (when val (put! ch val))
    (close! ch)))

(defn chan->auth-cb
  "Creates a callback to push [err, value] arguments onto a chan, exactly once"
  [ch]
  (fn [err val]
    (put! ch [err val])
    (close! ch)))

;; async

(defn deref< [ref]
  (with-chan #(m/deref ref (chan->cb-once %))))

(defn reset!< [ref val]
  (with-chan #(m/reset! ref val (chan->cb-once %))))

(defn reset-with-priority!< [ref val priority]
  (with-chan #(m/reset-with-priority! ref val priority (chan->cb-once %))))
