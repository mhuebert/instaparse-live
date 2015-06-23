(ns ^:figwheel-always app.core
  (:require
    [reagent.core :as r]
    [app.layout :as layout]))

(enable-console-print!)

(r/render-component [layout/app] (.getElementById js/document "app"))
