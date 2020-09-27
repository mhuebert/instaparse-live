(ns cljsjs.firebase
  (:require ["@firebase/app" :as firebase]
            ["@firebase/database"]))

(set! (.-Firebase js/window) firebase)
