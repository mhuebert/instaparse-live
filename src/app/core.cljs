(ns app.core
  (:require
    [app.routes :as routes]
    [app.layout :as layout]
    [reagent.dom :as rdom]
    [persistence.auth :as auth]
    [app.keys :as keys]))

(defn ^:dev/after-load render []
  (rdom/render [layout/app] (.getElementById js/document "app")))

(defn ^:export init []
  (render)
  (auth/init)
  (routes/init)
  (keys/init))

