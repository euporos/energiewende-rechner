(ns site.process
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            ;; [ewr.macros :as m]
            [hiccup.page :refer [html5]]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [selmer.parser]
            [stasis.core :as stasis]))

(def settings (edn/read-string (slurp "config/settings.edn")))
(def shadow-cljs (edn/read-string (slurp "shadow-cljs.edn")))

(def prerendered-app
  (do
    (println "Prerendering App")
    (:out (sh "node" (get-in shadow-cljs [:builds :prerender :output-to])))))

(defn map-vals [f m]
    (->> (map #(update % 1 f) m)
         (into {})))

(defn get-php []
  (-> (map-vals
       #(selmer.parser/render % {:settings settings})
       (stasis/slurp-directory "resources/snippets" #".*\.(html|php)$"))
      (assoc :prerendered-app "boo")))

(defn get-html-pages [opts]
  (map-vals
   #(selmer.parser/render % {:settings settings
                             :preview-image         (if (:include-php opts)
                                                     (get (get-php) "/preview-image.php")
                                                     (str (get settings :main-site)
                                                          "/imgs/rich-preview_3.png"))
                             :snippets    nil
                             :prerendered-app prerendered-app})

   (stasis/slurp-directory "resources/public" #".*\.(html|php)$")))

(defn get-assets
  []
  (assets/load-assets "public" [#".*\.(svg|png|jpg|jpeg|css)$"]))

(defn get-pages [production?]
  (merge (get-html-pages production?)))


(def live-view (optimus/wrap ;; shouldn't be used
                (stasis/serve-pages get-pages)
                get-assets
                optimizations/all
                serve-live-assets))

(def export-dir "export/main")

(defn export [opts]
  (println "exporting")
  (let [assets (optimizations/all (get-assets) {})
        pages  (get-pages opts)]
    (println "emptying export dir" )
    (stasis/empty-directory! export-dir)
    (println "Saving optimized assets")
    (optimus.export/save-assets assets export-dir)
    (println "exporting pages")
    (stasis/export-pages pages export-dir {:optimus-assets assets})
    (when (:include-php opts)
      (sh "mv" (str export-dir "/index.html") (str export-dir "/index.php"))))
  (System/exit 0))

(defn export-without-php
  []
  (export {}))

(defn export-with-php
  []
  (export {:include-php true}))
