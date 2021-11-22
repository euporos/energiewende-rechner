(ns ewr.macros
  (:require [clojure.edn :as edn]
            [clojure.java.io]
            [clojure.pprint :refer [pprint]]
            [clojure.set :as set]
            [clojure.string :as str]
            [markdown.core :as md]
            ))

(defn deep-merge [a b]
  (cond
    (nil? a)
    b

    (nil? b)
    a

    (and (map? a) (map? b))
    (merge-with deep-merge a b)

    (and (vector? a) (vector? b))
    (->> (concat a b)
         (distinct)
         (into []))

    (and (set? a) (set? b))
    (set/union a b)

    (string? b)
    b

    (number? b)
    b

    (boolean? b)
    b

    (keyword? b)
    b

    (symbol? b)
    b

    :else
    (throw (ex-info "failed to merge config value" {:a a :b b}))
    ))

(defmacro def-string-from-file [var file f]
  `(def ~var
     (~f ~(slurp file))))

(def default-config-dirs ["config/default" "config/default_stage" "config/default_dev"])

(defn get-config-dirs
  []
  (if-let [config-var (System/getenv "EWR_CONFIG_DIRS")]

    (let [config-dirs (str/split config-var #" +")]
      (do
        (println "got config dirs from Environment: " config-dirs)
        config-dirs))
    (do
      (println "using standard config dirs: " default-config-dirs)
      default-config-dirs)))

(defmacro slurp-file
  [path]
  (slurp path))

;; (println "the following features will be disabled: " (disabled-features))

(defn in-dir
  "prepends a path with the config directors
  preventing missing or double slashes."
  [dir subpath]
  (str
   (str/replace dir #"/$" "") "/"
   (str/replace subpath #"^/" "")))

(defn read-files-into-map
  [dir extension parser keywordize?]
  (let [rework-keys #(if keywordize?
                       (keyword (str/replace % #"\.[^\.]+$" "")) %)
        grammar-matcher (.getPathMatcher
                         (java.nio.file.FileSystems/getDefault)
                         (str "glob:*.{" extension "}"))
        texts           (->> dir
                             clojure.java.io/file
                             file-seq
                             (filter #(.isFile %))
                             (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
                             (mapv #(vector
                                     (-> % .getName rework-keys)
                                     (parser
                                      (slurp
                                       (.getAbsolutePath %)))))
                             (into {})
                             )]
    texts))

(defn read-configuration-dir
    [config-dir]
    (->
     (read-files-into-map config-dir "edn" edn/read-string true)
     (assoc :texts (read-files-into-map (in-dir config-dir "/text") "md" md/md-to-html-string true))))

(defn read-configuration
  [configuration-dirs]
  (reduce
    (fn [sofar next-config-dir]
      (deep-merge sofar (read-configuration-dir next-config-dir)))
    {}
    configuration-dirs))

(defmacro def-config [var]
  "Constructs the configuration as it wille be available at runtime"
  (let [merged-config (read-configuration (get-config-dirs))]
    (println "Merged Settings are: ")
    (pprint  (:settings merged-config))
    `(def ~var
       ~merged-config)))
