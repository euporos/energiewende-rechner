(ns site.process
  (:require [clojure.edn :as edn]
            [clojure.java.shell :refer [sh]]
            [ewr.macros :as m]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [selmer.filters :as sf]
            [selmer.parser]
            [stasis.core :as stasis]))

(m/def-config config)

(defn as-php-var [var-name]
  (str "<?php echo $" var-name "; ?>"))

(sf/add-filter! :php-var
                as-php-var)

(def inject-php? (get-in config [:settings :inject-php?]))

(def settings (get config :settings))
(def features (get settings :features))
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
       (stasis/slurp-directory "resources/snippets" #".*\.(php)$"))))

(defn hash-resource [path]
  (hash (slurp path)))

(defn get-html-pages []
  (let [php-snippets (when inject-php? (get-php))]
    (map-vals
     #(selmer.parser/render % {:php-script (when inject-php? (get php-snippets "/generate_vars.php"))
                               :config          config
                               :app-hash        (hash-resource "export/main/js/compiled/app.js")
                               :request-uri     (as-php-var "full_query_string")
                               :preview-image
                               (if (and inject-php? (features :dynamic-preview))
                                 (as-php-var "og_img_link")
                                 (str (get settings :main-site) "/imgs/rich-preview_3.png"))
                               :snippets        nil
                               :prerendered-app prerendered-app})

     (stasis/slurp-directory "resources/public" #".*\.(html|php)$"))))

(defn get-statics
  []
  (assets/load-assets "public" [#".*\.(svg|png|jpg|jpeg)$"]))

(defn get-pages []
  (merge (get-html-pages)))

(def export-dir "export/main")

(defn export [arg]
  (println "exporting")
  (let [assets (get-statics)
        pages  (get-pages)]
    (println "Saving optimized assets")
    (optimus.export/save-assets assets export-dir)
    (println "exporting pages")
    (stasis/export-pages pages export-dir {:optimus-assets assets})
    (when inject-php?
      (sh "mv" (str export-dir "/index.html") (str export-dir "/index.php"))))
  (when (:exit? arg) (System/exit 0)))


