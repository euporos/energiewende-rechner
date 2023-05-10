(ns site.watch
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [hawk.core :as hawk]
            [medley.core :as m])
  (:import [clojure.lang ExceptionInfo]
           [java.io File]))

(def ansi-codes
  {:reset   "\u001b[0m"
   :black   "\u001b[30m" :gray           "\u001b[1m\u001b[30m"
   :red     "\u001b[31m" :bright-red     "\u001b[1m\u001b[31m"
   :green   "\u001b[32m" :bright-green   "\u001b[1m\u001b[32m"
   :yellow  "\u001b[33m" :bright-yellow  "\u001b[1m\u001b[33m"
   :blue    "\u001b[34m" :bright-blue    "\u001b[1m\u001b[34m"
   :magenta "\u001b[35m" :bright-magenta "\u001b[1m\u001b[35m"
   :cyan    "\u001b[36m" :bright-cyan    "\u001b[1m\u001b[36m"
   :white   "\u001b[37m" :bright-white   "\u001b[1m\u001b[37m"
   :default "\u001b[39m"})

(defn log [{:keys [log-color]} & strs]
  (let [text (str/join " " strs)]
    (if log-color
      (println (str (ansi-codes log-color) "auto> " text (ansi-codes :reset)))
      (println (str "auto> " text)))))

(defn file-filter [{:keys [file-pattern]}]
  (if (some? file-pattern)
    (fn [_ {^File f :file}] (and (.isFile f) (re-find file-pattern (str f))))
    hawk/file?))

(defn file-changed [queue]
  (fn [_ {:keys [file]}] (swap! queue conj file)))

(def default-config
  {:file-pattern #"\.(clj|cljs|cljx|cljc)$"
   :wait-time    50
   :log-color    :magenta
   :args []})

(defn merge-config [config]
  (merge default-config
         (cond-> config
           (:file-pattern config) (update :file-pattern re-pattern))))

(defn show-modified [files]
  (let [paths files]
    (str/join ", " paths)))

(defn auto [config]
  (let [config    (merge-config  config)
        {:keys [wait-time func args paths init-func init-args]} config
        queue     (atom (m/queue))]

    (println "Watching files matching " (:file-pattern config))
    (require (symbol (namespace func)))

    (when (and init-func init-args)
      (log "running init function " init-func)
      (require (symbol (namespace init-func)))
      (apply (eval init-func) init-args))

    (log "starting watch of " paths)

    (hawk/watch! [{:paths   paths
                   :filter  (file-filter config)
                   :handler (file-changed queue)}])
    (while true
      (Thread/sleep wait-time)
      (when-let [files (seq (m/deref-reset! queue (m/queue)))]
        (log config "Files changed:" (show-modified files))
        (try
          (apply (eval func) args)
          (log config "Completed.")
          (catch Exception e
            (log config "Failed." e)))))))

