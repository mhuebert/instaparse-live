(ns ^:figwheel-always app.core
  (:require
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [app.util :as util]
    [app.nav :as nav]
    [app.state :as state :refer [user location]]
    [reagent.core :as r]
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
    [:a {:class "button" :href "#/new"} "New"]
    [:a {:class "button" :on-click db/save-new} "Fork"]

    [:span {:class (if-not (:doc-id @location) "hidden")}
     [:strong [:a {:href (str "#/" (:doc-id @location) "/" (:version-id @location))} (:version-id @location) ]]]]

   [:span {:style {:color "red"}} (str (last @state/errors))]
   [:span {:class "right"}
    (cond (= "anonymous" (:provider @user)) "Anonymous" (= {} @user) "Signing in" :else (:uid @user))
    [:a {:class "button" :on-click db/save :title "CMD-s"} (:save-status @state/ui)]
    ]])

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   (util/parse @state/grammar @state/sample @state/options)])

(defn options []
  [:div {:class "options"}
   [:div
    {:class    "button"
     :style    {:text-align "center"}
     :on-click nav/toggle-options}
    "Options"]
   (if (= true (:show-options @state/ui)) [:div [util/cm-editor state/options {:mode "clojure" :style "background:white"}]
                                           ])])

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
                           #_[:div {:class "description"} "Description of this parser..."]
                           [v-split
                            :margin "0 10% 20px"
                            :initial-split "25"
                            :panel-1 [:div {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}} [util/cm-editor state/sample {:theme "solarized light"}]]
                            :panel-2 [parsed-output]
                            ]]]
               :panel-2 [v-box
                         :size "1"
                         :style {:position "relative"}
                         ; :panel-1 [util/cm-editor state/grammar]
                         ; :panel-2 [util/cm-editor state/options]
                         :children [[util/cm-editor state/grammar]
                                    [options]]]
               ]]])

; Bind Reagent component to DOM
(r/render-component [app] (.getElementById js/document "app"))

; Configure routing
(defonce _
         (do
           (nav/init)
           (db/sign-in-anon)))


