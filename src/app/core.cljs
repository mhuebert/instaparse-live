(ns ^:figwheel-always app.core
  (:require
    [app.routes]
    [reagent.core :as r]
    [app.layout :as layout]))

(enable-console-print!)

(defn ^:export init []
  (r/render-component [layout/app] (.getElementById js/document "app")))

