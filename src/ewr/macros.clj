(ns ewr.macros
  (:require [clojure.string :as str]
            [markdown.core :as md]))

(defn config-dir []
  "Extracts the closure define “config-dir” at compile time."
  (if cljs.env/*compiler*
    (get-in @cljs.env/*compiler* [:options :closure-defines :config-dir])
    "config"))

(defn in-config-dir
  "prepends a path with the config directors
  preventing missing or double slashes."
  [subpath]
  (str
   (str/replace (config-dir) #"/$" "") "/"
   (str/replace subpath #"^/" "")))


(defn read-config-files []
  "Reads the contents of alle config-files
and sticks them into a map with the filenames as keys."
  (reduce
     (fn [sofar nextpath]
       (let [key (keyword
                  (str/join
                   "." (drop-last
                        (str/split nextpath #"\."))))
             content (read-string
                      (slurp                         
                       (str
                        (str/replace (config-dir) #"/$" "") "/"
                        (str/replace nextpath #"^/" ""))))]
         (assoc sofar key content)))
     {}
     ["publications.edn"
      "settings.edn"
      "snippets.edn"
      ]))

(read-config-files)
 
(defn read-texts []  
  "reads all MD-Files in under <config-dir>/text
and sticks them into a map with the names
of the MD-files as keys."
  (let [grammar-matcher (.getPathMatcher 
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{g4,md}")
        text-dir (in-config-dir "text")        
        texts (->> text-dir
         clojure.java.io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(vector
                 (-> %
                     .getName
                     (str/replace #"\.md$" "")
                     keyword)
                 (md/md-to-html-string
                  (slurp
                   (.getAbsolutePath %)))))
         (into {}))]
    texts))

(defmacro def-config [var]
  "Constructs the configuration as it wille be available at runtime"
  `(def ~var 
     ~(-> 
       (read-config-files)
       (assoc :texts (read-texts)))))


;; ##############
;; ### Legacy ###
;; ##############

(defmacro def-from-file [var path-to-file f]
  `(def ~var
     (~f ~(read-string (slurp path-to-file)))))

(defmacro def-string-from-file [var file f]
  `(def ~var 
     (~f ~(slurp file))))
 
(defmacro build-snippet-map [textmap]  
  ""
  (let [grammar-matcher (.getPathMatcher 
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{g4,md}")
        text-dir "config/text"        
        snippets (->> text-dir
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
 
 
