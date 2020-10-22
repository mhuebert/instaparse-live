(ns app.layout
  (:require
    [app.editors :refer [cm-editor editable-text]]
    [app.keys :as keys]
    [app.state :as state :refer [user doc cursor]]
    [reagent.core :as r]
    [re-com.core :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
    [re-com.splits :refer [hv-split-args-desc]]
    [fipp.edn :refer [pprint]]
    [persistence.docs :as docs]
    [persistence.auth :as auth]
    [app.ui :as ui]))


(defn fancy-errors [body]
  (if (instance? js/Error body) [:span {:style {:color "white" :background "red" :padding "2px 4px"}} (.-message body)] body))
(r/track! #(prn ::user @state/user))
(defn header []
  [:div
   {:id "header"}
   [:span {:class-name "left"}
    [:a {:class-name "button" :on-click #(docs/new!)} "New"]
    [:a {:class-name "button" :on-click #(docs/fork!)} (fancy-errors (:fork-status @state/ui))]
    (if (and (:uid @state/user) (= (:owner @state/doc) (:uid @state/user)))
      [:a {:class-name "button" :on-click #(docs/save!) :title "CMD-s"}
       (fancy-errors (:save-status @state/ui))])

    [:span {:class-name (str "button" (if-not (:id @state/doc) "hidden"))}
     [:strong [:a {:href (str "#/" (:id @state/doc) "/" (:id @state/cells))} (:id @state/cells)]]]
    (if-not (:auto-update @state/options-clj)
      [:span {:class-name "button"
              :on-click #(state/refresh-editor-state!)
              :style {:background "#0B749F" :font-size "13px" :padding "2px 4px" :color "white"}}
       "refresh (ctrl+r)"])]

   [:span {:class-name "right"}
    (condp = (:provider @state/user)
      "github" [:span (:display-name @user) " — " [:a {:style {:cursor "pointer" :color "#777"} :on-click #(auth/sign-out)} "Log out"]]
      "anonymous" [:a {:on-click #(auth/sign-in-github) :style {:cursor "pointer"}} "Sign In with Github"]
      nil "authenticating...")]])

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   @state/output])

(defn option-view []
  (let [show-options (r/atom false)
        toggle-show-options (fn [] (reset! show-options (not @show-options)))]
    (r/create-class {
                     :component-did-mount (fn [] (keys/register "ctrl+o" (fn []
                                                                           (toggle-show-options)
                                                                           (ui/focus-last-editor!))))
                     :component-will-unmount (fn [] (keys/unregister "ctrl+o"))
                     :render (fn []
                               [:div {:class-name "options"}
                                [:div
                                 {:class-name "button"
                                  :style {:text-align "center"}
                                  :on-click toggle-show-options}
                                 "Options"]
                                (if @show-options
                                  [:div [cm-editor state/options-txt {:mode "clojure" :style "background:white"}]])])})))

(defn description [doc]
  (let [!desc (cursor state/doc [:description])
        !title (cursor state/doc [:title])]
    (fn [doc]
      (let [{:keys [username title]} @doc
            is-owner (= (:owner @state/doc) (:uid @state/user))]
        [:div {:style {:margin "15px 10%" :overflow-y "auto"}}
         username
         (if (and username (or is-owner title)) " / ")
         [:strong [editable-text !title {:empty-text "title"
                                         :input {:style {:width "350px"}}}]]
         [editable-text !desc {:empty-text "description"
                               :edit-text "edit description"
                               :markdown true
                               :only-power-edit true}]]))))

(defn editor []
  [h-split
   :height "100%"
   :margin "0"
   :initial-split "65"
   :style {:background "#e0e0e0"}
   :panel-1
   [v-box
    :width "100%"
    :children [[description state/doc]
               [v-split
                :margin "0 10% 20px"
                :initial-split "25"
                :panel-1 [:div
                          {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}}
                          [cm-editor state/sample {:theme "solarized light"}]]
                :panel-2 [parsed-output]]]]
   :panel-2 [v-box
             :size "1"
             :style {:position "relative"}
             :children [[cm-editor state/grammar {:mode "ebnf"}]
                        [option-view]]]])


(defn app []
  [v-box
   :height "100%"
   :style {:flex 1}
   :children [[header]
              [editor]]])
