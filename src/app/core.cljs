(ns ^:figwheel-always app.core
  (:require
    [cljsjs.markdown]
    [app.components :as c]
    [app.routes :as routes]
    [app.keys :as keys]
    [app.compute :as compute]
    [app.state :as state :refer [user doc cell]]
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
    [:a {:class "button" :on-click routes/new-doc} "New"]
    [:a {:class "button" :on-click db/fork} (fancy-errors (:fork-status @state/ui))]
    (if (and (:uid @state/user) (= (:owner @state/doc) (:uid @state/user)))
      [:a {:class "button" :on-click db/save :title "CMD-s"} (fancy-errors (:save-status @state/ui))])

    [:span {:class (if-not (:id @doc) "hidden")}
     [:strong [:a {:href (str "#/" (:id @doc) "/" (:id @state/cells))} (:id @state/cells)]]]]

   [:span {:class "right"}
    (condp = (:provider @user)
      "github" [:span [:a {:style {:cursor "pointer" :color "#777"} :on-click db/sign-out} "Log out"]
                " "
                [:a {:href (str "https://www.github.com/" (get-in @user [:github :username]))} (get-in @user [:github :displayName])]]
      "anonymous" [:a {:on-click db/sign-in-github :style {:cursor "pointer"}} "Sign In with Github"]
      nil "authenticating...")]])

(defn parsed-output []
  [:div
   {:id    "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   @state/output])

(defn options []
  (let [show-options (r/atom false)
        toggle (fn [] (reset! show-options (not @show-options)))]
    (r/create-class {
                     :component-did-mount    (fn [] (keys/register "ctrl+o" (fn [] (toggle) (c/focus-last-editor))))
                     :component-will-unmount (fn [] (keys/unregister "ctrl+o"))
                     :render                 (fn []
                                               [:div {:class "options"}
                                                [:div
                                                 {:class    "button"
                                                  :style    {:text-align "center"}
                                                  :on-click toggle}
                                                 "Options"]
                                                (if @show-options
                                                  [:div [c/cm-editor (cell :options) {:mode "clojure" :style "background:white"}]])])})))

(defn description []

  (fn []
    (let [owner (= (:owner @state/doc) (:uid @state/user))]
      [:div {:style {:margin "15px 10%" :overflow-y "auto"}}
       [:a {:href (str "https://www.github.com/" (:username @state/doc))} (:username @state/doc)]
       (if (and (:username @state/doc) (or owner (:title @state/doc))) " / ")
       [:strong [c/editable-text (cursor [:title] state/doc) {:empty-text "title"
                                                                 :input      {:style {:width "350px"}}}]]
       [c/editable-text (cursor [:description] state/doc) {:empty-text "description"
                                                              :edit-text  "edit description"
                                                              :markdown   true
                                                              :only-power-edit true}]
       ])))

(defn editor []
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
                :panel-1 [:div
                          {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}}
                          [c/cm-editor (cell :sample) {:theme "solarized light"}]]
                :panel-2 [parsed-output]
                ]]]
   :panel-2 [v-box
             :size "1"
             :style {:position "relative"}
             :children [[c/cm-editor (cell :grammar) {:mode "ebnf"}]
                        [options]]]])


(defn app []
  [v-box
   :height "100%"
   :children [[header]
              [editor]]])

; Bind Reagent component to DOM
(r/render-component [app] (.getElementById js/document "app"))
