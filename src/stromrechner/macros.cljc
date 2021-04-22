(ns stromrechner.macros
  (:require [clojure.string :as str]
            [markdown.core :as md]
            ;; [stromrechner.google-defines :as gf]
            ))

;; (goog-define testdf "config")

(def testdf "config")


;; (defn in-cfg-dir
;;   "makes sure there is exactly one slash /
;;   between cfg-dir and path."
;;   [path]
;;   (str
;;    (str/replace gf/config-dir #"/$" "") "/"
;;    (str/replace path #"^/" "")))

(defmacro def-from-file [var path-to-file f]
  `(def ~var
     (~f ~(read-string (slurp path-to-file)))))


;; (defmacro def-from-config-file [var path f]
;;   `(def ~var  ~(str gf/config-dir "/" path )
;;      ;; (~f ~(read-string (slurp (gf/in-cfg-dir path))))
;;      ))

(defmacro def-config [var config-dir]
  `(def ~var
     ~(reduce
       (fn [sofar nextpath]
         (let [key (keyword
                    (str/join
                     "." (drop-last
                          (str/split nextpath #"\."))))
               content (read-string
                        (slurp
                         (str config-dir "/" nextpath)
                         ;; (str
                         ;;  (str/replace gf/config-dir #"/$" "") "/"
                         ;;  (str/replace nextpath #"^/" ""))
                         ))]
           (assoc sofar key content)))
       {}
       ["publications.edn"
        "settings.edn"])))

 
(defmacro def-string-from-file [var file f]
  `(def ~var 
     (~f ~(slurp file)))) 
 
(defmacro build-snippet-map [textmap]  
  ""
  (let [grammar-matcher (.getPathMatcher 
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{g4,md}")
        text-dir "config/text"
        ;; (str/replace gf/in-config-dir #"/$" "/text")
        
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
 
