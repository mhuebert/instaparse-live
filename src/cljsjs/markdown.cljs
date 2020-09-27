(ns cljsjs.markdown
  (:require ["markdown" :as Markdown]))
(js/console.log Markdown)
(set! (.-markdown js/window) Markdown)
