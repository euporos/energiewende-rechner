(ns site.process
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [ewr.macros :as m]
            [hiccup.page :refer [html5]]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [selmer.parser]
            [stasis.core :as stasis]))



(m/def-config config)

(def inject-php? (get-in config [:settings :inject-php?]))

(def settings (edn/read-string (slurp "config/default/settings.edn")))
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
      (assoc :prerendered-app prerendered-app)))

(defn get-html-pages []
  (map-vals
   #(selmer.parser/render % {:config config
                             :preview-image
                             (str (get-in config [:settings :preview-api])
                                  (if inject-php?
                                    (get (get-php) "/preview-image.php")
                                    "/imgs/rich-preview_3.png"))
                             :snippets    nil
                             :prerendered-app prerendered-app})

   (stasis/slurp-directory "resources/public" #".*\.(html|php)$")))

(defn get-assets
  []
  (assets/load-assets "public" [#".*\.(svg|png|jpg|jpeg|css)$"]))

(defn get-pages []
  (merge (get-html-pages)))

(def live-view (optimus/wrap ;; shouldn't be used
                (stasis/serve-pages get-pages)
                get-assets
                optimizations/all
                serve-live-assets))

(def export-dir "export/main")

(defn export []
  (println "exporting")
  (let [assets (optimizations/all (get-assets) {})
        pages  (get-pages)]
    (println "emptying export dir" )
    (stasis/empty-directory! export-dir)
    (println "Saving optimized assets")
    (optimus.export/save-assets assets export-dir)
    (println "exporting pages")
    (stasis/export-pages pages export-dir {:optimus-assets assets})
    (when inject-php?
      (sh "mv" (str export-dir "/index.html") (str export-dir "/index.php"))))
  (System/exit 0))
