(ns app.compute_test
  (:require [cemerick.cljs.test :refer-macros [is are deftest testing use-fixtures done]]
            [reagent.core :as reagent :refer [atom]]
            [app.compute :as compute]))

(deftest test-vec->element
         (let [element (#'compute/vec->element [:my-tag-name "contents"])]
           (is (= element
                  [:div {:class-name "parse-tag t-my-tag-name"}
                   [:span {:class-name "tag-name"} ":my-tag-name "] "contents"]))))

(def isClient (not (nil? (try (.-document js/window)
                              (catch js/Object e nil)))))

(def rflush reagent/flush)




(defn add-test-div [name]
  (let [doc     js/document
        body    (.-body js/document)
        div     (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when isClient
    (let [div (add-test-div "_testreagent")]
      (let [comp (reagent/render-component comp div #(f comp div))]
        (reagent/unmount-component-at-node div)
        (reagent/flush)
        (.removeChild (.-body js/document) div)))))


(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))

(defn home-page []
  [:div [:h2 "Welcome to ttt"]
   [:div [:a {:href "#/about"} "go to about page"]]])

(deftest test-home
         (with-mounted-component (home-page)
                                 (fn [c div]
                                   (is (found-in #"Welcome to" div)))))
