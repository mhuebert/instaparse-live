(ns persistence.auth
  (:require [persistence.core :refer [ref]]
            [app.state :refer [user]]))

(defonce _
         (do
           (.onAuth ref
                    (fn [auth-data]
                      (js->clj auth-data)
                      (reset! user (js->clj auth-data :keywordize-keys true))))))

(defn sign-in-anon []
  (if-not (.getAuth ref) (.authAnonymously ref #())))

(defn sign-in-github []
  (.authWithOAuthPopup ref "github"
                       (fn [error auth-data]
                         (if error (prn (js/Error error))
                                   (prn auth-data)))))

(defn signed-in? []
  (if (:uid @user) true false))

(defn sign-out []
  (reset! user {})
  (.unauth ref))