(ns prerender.main
  (:require
   ["puppeteer" :as puppeteer]
   [clojure.string :as str]
   ["path" :as path]
   ;; ["fs" :as fs]
   ;; [plibs.phtmltohiccup :as hth]
   [clojure.core.async :refer [go <!]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def app-page (str "file://"
                   (.resolve path "export/main/index.html")))

(defn prerender
  []
  (go
    (let [browser (<p! (.launch puppeteer))
          page    (<p! (.newPage browser))]
      (try
        (<p! (.goto page app-page))
        (catch js/Error err (js/console.log (ex-cause err))))
      (let [html (<p! (.content page))]
        ;; (spit "content.html" html)
        (print
         (some
          #(if (re-find #"id=\"app\"" %) %)
          (str/split
           html "\n"))))
      (.close browser))))

;TODO: try to only extract the ewr https://stackoverflow.com/questions/46431288/puppeteer-get-innerhtml
