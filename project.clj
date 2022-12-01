(defproject ewr "EWR-Nuklearia"
  :description ""
  :url "https://energiewende-rechner.org/"
  :source-paths ["src" "test"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [stasis "2.5.1"]
                 [selmer "1.12.44"]
                 [hiccup "1.0.5"]
                 [optimus "0.20.2"]
                 [thheller/shadow-cljs "2.16.5"]
                 [hickory "0.7.1"]
                 [org.clojure/clojurescript "1.10.891"]
                 [madvas/cemerick-url "0.1.2"]
                 [thi.ng/color "1.4.0"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.2"]
                 [markdown-clj "1.10.5"]
                 [day8.re-frame/tracing "0.6.0"]
                 [binaryage/devtools "1.0.2"]
                 [day8.re-frame/re-frame-10x "0.7.0"]
                 [pjstadig/humane-test-output "0.10.0"]
                 [re-frame-utils "0.1.0"]
                 [metosin/malli "0.4.0"]
                 [thedavidmeister/cljc-md5 "0.0.2"]]
  :plugins [[lein-auto "0.1.3"]]
  :auto {"build-site" {:paths ["src" "resources"]
                       :file-pattern #"\.(clj|html|svg|php)$"}}
  :ring {:handler site.process/live-view}
  :aliases {"build-site" ["run" "-m" "site.process/export"]}
  :profiles {:dev {:plugins [[lein-ring "0.12.6"]]}})
