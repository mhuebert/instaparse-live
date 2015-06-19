(ns ^:figwheel-always app.core
  (:require
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [app.util :as util]
    [app.nav :as nav]
    [app.state :as state :refer [user location working-version]]
    [reagent.core :as r]
    [reagent.cursor :refer [cursor]]
    [re-com.core :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
    [re-com.splits :refer [hv-split-args-desc]]
    [app.db :as db]))

(enable-console-print!)
(defn on-js-reload [])

(defn fancy-errors [body]
  (if (instance? js/Error body) [:span {:style {:color "white" :background "red" :padding "2px 4px"}} (.-message body)] body))

(defn header []
  [:div
   {:id "header"}
   [:span {:class "left"}
    [:a {:class "button" :href "#/new"} "New"]
    [:a {:class "button" :on-click db/fork} (fancy-errors (:fork-status @state/ui))]
    [:a {:class "button" :on-click db/save :title "CMD-s"} (fancy-errors (:save-status @state/ui))]

    [:span {:class (if-not (:doc-id @location) "hidden")}
     [:strong [:a {:href (str "#/" (:doc-id @location) "/" (:version-id @location))} (:version-id @location) ]]]]

   [:span {:class "right"}
    (condp = (:provider @user)
      "github" [:span [:a {:style {:cursor "pointer" :color "#777"} :on-click db/sign-out} "Log out"]
                " "
                [:a {:href (str "https://www.github.com/" (get-in @user [:github :username]))} (get-in @user [:github :displayName])]]
      "anonymous" [:a {:on-click db/sign-in-github :style {:cursor "pointer"}} "Sign In with Github"]
      nil "authenticating...")]])

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   (util/parse @working-version)])

(defn options []
  [:div {:class "options"}
   [:div
    {:class    "button"
     :style    {:text-align "center"}
     :on-click nav/toggle-options}
    "Options"]
   (if (= true (:show-options @state/ui))
     [:div [util/cm-editor (cursor [:options] working-version) {:mode "clojure" :style "background:white"}]])])

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
                            :panel-1 [:div {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}} [util/cm-editor (cursor [:sample] working-version) {:theme "solarized light"}]]
                            :panel-2 [parsed-output]
                            ]]]
               :panel-2 [v-box
                         :size "1"
                         :style {:position "relative"}
                         ; :panel-1 [util/cm-editor state/grammar]
                         ; :panel-2 [util/cm-editor state/options]
                         :children [[util/cm-editor (cursor [:grammar] working-version)]
                                    [options]]]
               ]]])

; Bind Reagent component to DOM
(r/render-component [app] (.getElementById js/document "app"))

; Configure routing
(defonce _
         (do
           (nav/init)
           (db/sign-in-anon)))


