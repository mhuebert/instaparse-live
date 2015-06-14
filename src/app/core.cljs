(ns ^:figwheel-always app.core
  (:require
    [cljsjs.codemirror]
    [app.util :as util]
    [app.nav :as nav]
    [app.db :as persistence]
    [app.state :as state :refer [user location]]
    [reagent.core :as r]
    #_[pushy.core :as pushy]
    [re-com.core :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
    [re-com.splits :refer [hv-split-args-desc]]
    [app.db :as db]))


(enable-console-print!)


(defn on-js-reload []
  #_(registerKeys keyHandler)

  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

; Sub-components

(defn header []
  [:div
   {:id "header"}
   [:span {:class "left"}
    [:strong [:a {:on-click (fn [x] (prn x))} "Permalink "]]
    [:strong "User: "] (if (:uid @user) (str (:uid @user) " ") "Signing in...")
    [:strong " Doc: "] [:a {:href (str "#/" (:doc-id @location))} (str (:doc-id @location))]
    [:strong " Version: "] [:a {:href (str "#/" (:doc-id @location) "/" (:version-id @location))} (str (:version-id @location))]
    ]
   [:span {:class "right"}
    [:a "Share"]
    [:a "Save"]
    [:a "Fork"]]])

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   (util/parse @state/grammar @state/sample)])

; The main app component
(defn app []
  [v-box
   :height "100%"
   :children [[header]
              [h-split
               :height "100%"
               :margin "0"
               :initial-split "65"
               :style {:background "#e0e0e0"}
               :panel-1
               [v-box
                :width "100%"
                :children [[:div
                            {:style {:margin "20px 10%" :textAlign "center"}}
                            "Build a parser in your browser! This page runs the " [:a {:href "https://github.com/lbradstreet/instaparse-cljs"} "ClojureScript port"]
                            " of " [:a {:href "https://github.com/Engelberg/instaparse"} "Instaparse"] "."]
                           [v-split
                            :margin "0 10% 20px"
                            :initial-split "25"
                            :panel-1 [:div {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}} [util/cm-editor state/sample {:theme "solarized light"}]]
                            :panel-2 [parsed-output]
                            ]]]
               :panel-2 [v-box
                         :size "1"
                         :children [[util/cm-editor state/grammar]]]
               ]]])

; Bind Reagent component to DOM
(r/render-component [app] (.getElementById js/document "app"))

; Configure routing
(defonce _
         (do
           (nav/init)
           (db/sign-in-anon)))


