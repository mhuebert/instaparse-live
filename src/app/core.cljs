(ns ^:figwheel-always app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
      [cljsjs.codemirror]
      [app.util :as util]
      [reagent.core :as r]
      [matchbox.core :as m]
      [matchbox.async :as ma]
      [cljs.core.async :as async :refer [<!]]
      [secretary.core :as secretary :refer-macros [defroute]]
    #_[pushy.core :as pushy]
      [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
      [re-com.splits :refer [hv-split-args-desc]]
      [goog.events :as events]
      [goog.history.EventType :as EventType]
      [goog.ui.KeyboardShortcutHandler]
      [cljsjs.firebase :as F]
      )
  (:import goog.History))

; Save -> push the two atoms into the current doc's versions
;         - set current :uid into doc if it's a new doc
;         - only allow edits to user-owned doc
; If anon-user - save anon-user ID in atom
; Fork -> push the two atoms into a new doc's versions
; Load -> grab a doc from Firebase and reset! atoms
; Sign in -> prompt for Github & set all anon-user docs as owned by Github user
; Sign out

; On sign-in - if anon-user is in atom, merge the stuff & then clear the atom
; LINK - sign out
; LINK - sign in with GitHub / Twitter
; Figure out how to batch-update docs that are associated with a given user
; Security rules, basic


; allow printing to the browser console
(enable-console-print!)

; define global atoms to hold app state
(defonce grammar (r/atom util/default-grammar))
(defonce sample (r/atom util/default-sample-code))
(defonce user (r/atom {}))
(defonce ref (m/connect "http://instaparse-live.firebaseio.com/docs"))
(.onAuth ref
         (fn [auth-data] (do (prn (js->clj auth-data))
                             (reset! user (js->clj auth-data :keywordize-keys true)))))
(defonce location (let [location (r/atom {})]
               (add-watch location nil (fn [key reference old-state new-state]
                                         (let [doc (:doc new-state)
                                               version (:version new-state)
                                               doc-ref (m/get-in ref [doc])
                                               version-ref (m/get-in doc-ref [:versions version])]
                                           (prn (.toString version-ref) (.toString doc-ref))
                                           (if version (go
                                                         (let [doc (<! (ma/deref< version-ref))]
                                                           (reset! grammar (:grammar doc))
                                                           (reset! sample (:sample doc))
                                                           ))))))
               location))


(defn sign-in-anon []
  #_(prn (.getAuth ref))
  (if (.getAuth ref) nil (m/auth-anon ref)))
(sign-in-anon)

(defn new-doc []
  (go
    (<! (ma/conj-in!< ref [:docs] {:owner (:uid @user)}))))

(defn signed-in? []
  (if (:uid @user) true false))


(defn current-doc []
  {:grammar @grammar
   :sample @sample})

(defn save-new []
  (go
    (let [doc-ref (.push ref)]
      (<! (ma/reset!< doc-ref {:owner (:uid @user)} ))
      (let [version-ref (.push (m/get-in doc-ref [:versions]))]
        (<! (ma/reset!< version-ref (current-doc)))
        (reset! location {:doc (.key doc-ref)
                          :version (.key version-ref)})))))

(defn save-version []
  (go
    (let [doc-ref (m/get-in ref (:doc @location))
           version-ref (.push (m/get-in doc-ref [:versions]))]
      (<! (ma/reset!< version-ref (current-doc)))
      (swap! location merge {:version (.key version-ref)}))))


(defn save []
  (if (signed-in?)
    (go
      (if (:doc @location) (save-version)
                           (save-new)))))

(defn keyAction [e]
  (let [action (.-identifier e)]
    (case action
      "SAVE" (save))))

(defn registerKeys [handler]
  (doto handler
    (.removeAllListeners)
    (.unregisterAll)
    (.registerShortcut "SAVE" "meta+s")
    (goog.events/listen goog.ui.KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED keyAction))
  handler)

(defonce keyHandler (registerKeys (new goog.ui.KeyboardShortcutHandler js/document)))

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
    [:strong " Doc: "] [:a {:href (str "#/" (:doc @location) "/latest")} (str (:doc @location))]
    [:strong " Version: "] [:a {:href (str "#/" (:doc @location) "/" (:version @location))} (str (:version @location))]
    ]
   [:span {:class "right"}
    [:a "Share"]
    [:a "Save"]
    [:a "Fork"]]])

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto" :marginTop 30 :width "100%"}}
   (util/parse @grammar @sample)])

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
                             :panel-1 [:div {:style {:border "1px solid #C2C2C1" :flex 1 :display "flex"}} [util/cm-editor sample {:theme "solarized light"}]]
                             :panel-2 [parsed-output]
                             ]]]
                :panel-2 [v-box
                          :size "1"
                          :children [[util/cm-editor grammar]]]
                ]]])


; Bind Reagent component to DOM
(defn init []
  (r/render-component [app] (.getElementById js/document "app")))
(init)


; Define routes
(defroute "/:doc/latest" [doc]
          (reset! location {:doc doc})
          (prn (str "Get latest version of " doc)))

(defroute "/:doc/:version" [doc version]
          (reset! location {:doc doc :version version})
          (prn (str "Get version " version " of " doc)))



; Configure routing

(defonce _
         (let [h (History.)]
           (secretary/set-config! :prefix "#")
           (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
           (doto h (.setEnabled true))))


