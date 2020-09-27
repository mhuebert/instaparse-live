(ns ^:figwheel-always app.core
  (:require
    [app.routes :as routes]
    [app.state :as state]
    [app.layout :as layout]
    [reagent.dom :as rdom]
    [persistence.auth :as auth]))

(enable-console-print!)

(defn ^:dev/after-load render []
  (rdom/render [layout/app] (.getElementById js/document "app")))

(defn ^:export init []
  (render)
  (auth/init)
  (routes/init))

