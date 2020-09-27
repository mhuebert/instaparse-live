(ns persistence.auth
  (:require ["@firebase/app" :default firebase]
            ["@firebase/auth" :as auth]
            [applied-science.js-interop :as j]
            [matchbox2.core :as m]
            [persistence.core]
            [app.state :refer [user]]))

(def !auth
  (delay @m/init!
         (.auth firebase)))

(defn init []
  (.onAuthStateChanged ^js @!auth
                       (j/fn [^:js {:as auth-data
                                    :keys [isAnonymous uid]
                                    [{:keys [displayName]}] :providerData}]
                         (if auth-data
                           (reset! user {:uid uid
                                         :provider (if isAnonymous "anonymous" "github")
                                         :display-name displayName})
                           (j/call @!auth :signInAnonymously)))))

(defn sign-in-anon []
  (j/call @!auth :signInAnonymously))

(defn sign-in-github []
  (j/call @!auth :signInWithPopup (new (.. firebase -auth -GithubAuthProvider))))

(defn signed-in? []
  (if (:uid @user) true false))

(defn sign-out []
  (reset! user {})
  (j/call @!auth :signOut))
