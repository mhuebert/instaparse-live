(ns ^:figwheel-always app.core
  (:require
    [cljsjs.markdown]
    [app.util :as util]
    [app.nav :as nav]
    [app.state :as state :refer [user doc]]
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
    [:a {:class "button" :on-click nav/new-doc} "New"]
    [:a {:class "button" :on-click db/fork} (fancy-errors (:fork-status @state/ui))]
    (if (and (:uid @state/user) (= (:owner @state/doc) (:uid @state/user)))
      [:a {:class "button" :on-click db/save :title "CMD-s"} (fancy-errors (:save-status @state/ui))])

    [:span {:class (if-not (:id @doc) "hidden")}
     [:strong [:a {:href (str "#/" (:id @doc) "/" (:id @state/version))} (:id @state/version)]]]]

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
   (util/parse @state/version)])

(defn options []
  [:div {:class "options"}
   [:div
    {:class    "button"
     :style    {:text-align "center"}
     :on-click nav/toggle-options}
    "Options"]
   (if (= true (:show-options @state/ui))
     [:div [util/cm-editor (cursor [:options] state/version) {:mode "clojure" :style "background:white"}]])])

(defn description []

  (fn []
    (let [owner (= (:owner @state/doc) (:uid @state/user))]
      [:div {:style {:margin "15px 10%" :overflow-y "auto"}}
       [:a {:href (str "https://www.github.com/" (:username @state/doc))} (:username @state/doc)]
       (if (and (:username @state/doc) (or owner (:title @state/doc))) " / ")
       [:strong [util/editable-text (cursor [:title] state/doc) {:empty-text "title"
                                                                 :input      {:style {:width "350px"}}}]]
       [util/editable-text (cursor [:description] state/doc) {:empty-text "description"
                                                              :edit-text  "edit description"
                                                              :markdown   true
                                                              :only-power-edit true}]
       ])))

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
                :children [[description]
                           [v-split
                            :margin "0 10% 20px"
                            :initial-split "25"
                            :panel-1 [:div {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}} [util/cm-editor (cursor [:sample] state/version) {:theme "solarized light"}]]
                            :panel-2 [parsed-output]
                            ]]]
               :panel-2 [v-box
                         :size "1"
                         :style {:position "relative"}
                         :children [[util/cm-editor (cursor [:grammar] state/version) {:mode "ebnf"}]
                                    [options]]]
               ]]])

; Bind Reagent component to DOM
(r/render-component [app] (.getElementById js/document "app"))

; Configure routing
(defonce _
         (do
           (nav/init)
           (db/sign-in-anon)))


