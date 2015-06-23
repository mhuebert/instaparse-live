(ns app.dispatch
  (:require [persistence.docs :as docs]
            [persistence.auth :as auth]
            [app.ui :as ui]))


(defn dispatch [[action & args]]

  (condp = action

    :new! (docs/new)
    :save! (do (ui/refresh-editor-state)
               (docs/save))
    :fork! (do (ui/refresh-editor-state)
               (docs/fork))


    :view-doc (apply docs/view-doc args)
    :view-doc-version (apply docs/view-doc-version args)
    :view-sample (docs/show-sample)

    :sign-out (auth/sign-out)
    :sign-in-github (auth/sign-in-github)
    :sign-in-anon (auth/sign-in-anon)


    :enter-power-mode (ui/power-mode)
    :refresh-editor-state (ui/refresh-editor-state)
    :editor-jump (ui/editor-jump)
    :focus-last-editor (ui/focus-last-editor)
    :focus (apply ui/focus args)

    ))