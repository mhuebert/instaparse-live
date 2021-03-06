(defproject instaparse-live "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}

            :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                           [org.clojure/clojurescript "0.0-3308"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [com.lucasbradstreet/instaparse-cljs "1.4.1.0-SNAPSHOT"]
                           [secretary "1.2.3"]
                           [cljs-cm-editor "0.1.1-SNAPSHOT"]
                           [reagent "0.5.0"]
                           [cljsjs/markdown "0.6.0-beta1-0"]
                           [re-com "0.5.4"]
                           [fipp "0.6.2"]
                           [cljsjs/firebase "2.2.3-0"]
                           [matchbox "0.0.6"]]


            :plugins [[lein-cljsbuild "1.0.6"]]

            :source-paths ["src"]

            :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

            :profiles {:dev {
                             :dependencies [[ring-mock "0.1.5"]
                                            [ring/ring-devel "1.3.2"]
                                            [leiningen-core "2.5.1"]
                                            [lein-figwheel "0.3.5"]
                                            [org.clojure/tools.nrepl "0.2.10"]
                                            [pjstadig/humane-test-output "0.7.0"]]
                             :plugins [[lein-figwheel "0.3.3"]
                                       [com.cemerick/clojurescript.test "0.3.2"]]
                             :injections [(require 'pjstadig.humane-test-output)
                                          (pjstadig.humane-test-output/activate!)]
                             }}


            :cljsbuild {
                        :builds [{:id "dev"
                                  :source-paths ["src"]
                                  :figwheel {:css-dirs ["resources/public/css"]}
                                  :compiler {:main app.core
                                             :asset-path "js/compiled/out"
                                             :output-to "resources/public/js/compiled/app.js"
                                             :output-dir "resources/public/js/compiled/out"
                                             :source-map-timestamp true }}
                                 {:id "min"
                                  :source-paths ["src"]
                                  :compiler {:output-to "resources/public/js/compiled/app.js"
                                             :main app.core
                                             :optimizations :advanced
                                             :pretty-print false
                                             :externs ["resources/private/js/codemirror-externs.js"]
                                             }}
                                 {:id "test"
                                  :source-paths ["src"  "test"]
                                  :compiler {:output-to "target/test.js"
                                             :optimizations :whitespace
                                             :pretty-print true}}]
                        :test-commands {"unit" ["phantomjs" :runner
                                                "test/vendor/es5-shim.js"
                                                "test/vendor/es5-sham.js"
                                                "test/vendor/console-polyfill.js"
                                                "target/test.js"]}}

            :figwheel {
                       ;; :http-server-root "public" ;; default and assumes "resources"
                       ;; :server-port 3449 ;; default
                       :css-dirs ["resources/public/css"] ;; watch and update CSS

                       ;; Start an nREPL server into the running figwheel process
                       ;; :nrepl-port 7888

                       ;; Server Ring Handler (optional)
                       ;; if you want to embed a ring handler into the figwheel http-kit
                       ;; server, this is for simple ring servers, if this
                       ;; doesn't work for you just run your own server :)
                       ;; :ring-handler hello_world.server/handler

                       ;; To be able to open files in your editor from the heads up display
                       ;; you will need to put a script on your path.
                       ;; that script will have to take a file path and a line number
                       ;; ie. in  ~/bin/myfile-opener
                       ;; #! /bin/sh
                       ;; emacsclient -n +$2 $1
                       ;;
                       ;; :open-file-command "myfile-opener"

                       ;; if you want to disable the REPL
                       ;; :repl false

                       ;; to configure a different figwheel logfile path
                       ;; :server-logfile "tmp/logs/figwheel-logfile.log"
                       })
