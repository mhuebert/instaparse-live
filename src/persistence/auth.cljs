(ns persistence.auth
  (:require ["@firebase/app" :default firebase]
            ["@firebase/auth" :as auth]
            [applied-science.js-interop :as j]
            [persistence.firebase :as fire]
            [app.state :refer [user]]
            [kitchen-async.promise :as p]))

(def !auth
  (delay @fire/init!
         (.auth firebase)))

(defn init []
  (.onAuthStateChanged ^js @!auth
                       (j/fn [^:js {:as auth-data
                                    :keys [isAnonymous uid]
                                    [{:keys [displayName]}] :providerData}]
                         (if auth-data
                           (p/let []
                                  (reset! user {:uid uid
                                                :provider (if isAnonymous "anonymous" "github")
                                                :display-name displayName}))
                           (j/call @!auth :signInAnonymously)))))

(defn sign-in-anon []
  (j/call @!auth :signInAnonymously))

(defn sign-in-github []
  (-> @!auth
      (j/call :signInWithPopup (new (.. firebase -auth -GithubAuthProvider)))
      (j/call :then #(do (js/console.log (js-arguments)) (swap! user assoc :access-token (j/get-in % [:credential :accessToken]))))
      (j/call :then prn)))

(defn signed-in? []
  (boolean (:uid @user)))

(defn sign-out []
  (reset! user {})
  (j/call @!auth :signOut))
