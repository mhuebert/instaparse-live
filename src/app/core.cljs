(ns ^:figwheel-always app.core
  (:require
    [app.layout :as layout]
    [reagent.core :as r]
    [servant.core :as servant]
    [app.state :as state]
    ))

(enable-console-print!)
(defn on-js-reload [])


(defn init []
  ; Bind Reagent component to DOM
  (r/render-component [layout/app] (.getElementById js/document "app")))

(init)