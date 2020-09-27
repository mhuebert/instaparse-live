(ns ^:figwheel-always app.core
  (:require
    [app.routes :as routes]
    [reagent.dom :as rdom]
    [app.layout :as layout]))

(enable-console-print!)

(defn ^:dev/after-load render []
  (prn :render)
  (rdom/render [layout/app] (.getElementById js/document "app")))

(defn ^:export init []
  (render)
  (routes/init))

