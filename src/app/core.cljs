(ns ^:figwheel-always app.core
    (:require
        [cljsjs.codemirror]
        [app.util :as util]
        [reagent.core :as r]
        #_[secretary.core :as secretary :refer-macros [defroute]]
        #_[pushy.core :as pushy]
        [re-com.core   :refer [h-box v-box box gap line scroller border h-split v-split title flex-child-style p]]
        [re-com.splits :refer [hv-split-args-desc]]
        ))

(enable-console-print!)

(defonce editor-content (r/atom "
<S> = (sexp | whitespace)+
sexp = <'('> operation <')'>

<operation> = operator + args
operator = #'[+*-\\\\]'
args = (num | <whitespace> | sexp)+
<num> = #'[0-9]+'
<whitespace> = #'\\s+'"))
(defonce sample-code (r/atom "(+ 1 (- 3 1))"))

(defn parsed-output []
  [:div
   {:id "parsed-output"
    :style {:overflow-y "auto"}}
   (util/parse @editor-content @sample-code)])

 (defn app []
   [h-split
    :height "100%"
    :margin "0"
    :initial-split "65"
    :style {:background "#e0e0e0"}
    :panel-1 [v-split
              :margin "30px 10%"
              :initial-split "25"
              :panel-1 [v-box
                        :size "1"
                        :children [[:div
                                    {:style {:marginBottom 10 :textAlign "center"}}
                                    "Build a parser in your browser! This page runs the " [:a {:href "https://github.com/lbradstreet/instaparse-cljs"} "ClojureScript port"]
                                    " of " [:a {:href "https://github.com/Engelberg/instaparse"} "Instaparse"] "."]
                                   [util/cm-editor sample-code {:theme "solarized light"}]]]
              :panel-2 [parsed-output]
     ]
    :panel-2 [v-box
              :size "1"
              :children [[util/cm-editor editor-content]]]
    ])

(defn init []
  (r/render-component [app] (.getElementById js/document "app")))
(init)
#_(defroute "/" []
          (init))

#_(secretary/set-config! :prefix "/")
#_(def history (pushy/pushy secretary/dispatch!
                          (fn [x]
                            (when (secretary/locate-route x) x))))
#_(pushy/start! history)

 (defn on-js-reload []
    ;; optionally touch your app-state to force rerendering depending on
    ;; your application
    ;; (swap! app-state update-in [:__figwheel_counter] inc)
    )
