(ns stromrechner.macros
  (:require [clojure.string :as str]
            [markdown.core :as md]))

(defmacro def-from-file [var file f]
  `(def ~var
     (~f ~(read-string (slurp file)))))

(defmacro def-string-from-file [var file f]
  `(def ~var
     (~f ~(slurp file))))




(defmacro build-text-map [textmap]
  (let [grammar-matcher (.getPathMatcher 
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{g4,md}")
        snippets (->> "config/text/"
         clojure.java.io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(vector
                 (-> %
                     .getName
                     (str/replace #"\.md$" "")
                     keyword
                     )
                 (md/md-to-html-string
                  (slurp
                   (.getAbsolutePath %)))))
         (into {}))]
    (reduce-kv
     (fn [sofar nkey nval]
       (assoc sofar nkey (assoc nval :text
                                (get snippets nkey))))
     {} textmap)))





