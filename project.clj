(defproject cjohansen-no "EWR-Nuklearia"
  :description ""
  :url "https://energiewende-rechner.org/"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [stasis "2.5.1"]
                 [selmer "1.12.44"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.10.5"]
                 [optimus "0.20.2"]
                 [org.clojure/clojurescript "1.10.891"]]
  :ring {:handler site.process/live-view}
  :aliases {"build-site-without-php" ["run" "-m" "site.process/export-without-php"]
            "build-site-with-php" ["run" "-m" "site.process/export-with-php"]}
  :profiles {:dev {:plugins [[lein-ring "0.12.6"]]}})

