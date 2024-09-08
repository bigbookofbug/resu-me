(ns resu-me.cliparse
  (:require [clojure.java.io :as io]
            [toml-clj.core :as toml]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]))

(defn parse-config 
  "Parse the initial config file into clj.
  This will allow transformation into .md and latex later.
  Markdown to be used as an intermediary form to validate everything before converting into a pdf file"
  [file]
  (cond (false? (.exists (io/file file))) (do
                                            (println "File doesn't exist!")
                                            (System/exit 0))
        (nil? (re-find #"\.toml$" file)) (do
                                           (println "Please provide a .toml file.")
                                           (System/exit 0))
        :else (with-open [reader (io/reader file)]
                (toml/read reader {:key-fn keyword}))))

(defn toml-exists?
  [file]
  (cond (false? (.exists (io/file file))) (do
                                            (println "File doesn't exist!")
                                            nil)
        (nil? (re-find #"\.toml$" file)) (do
                                           (println "Please provide a .toml file.")
                                           nil)
        :else true))

(defn spit-file
  [file]
  (cond
    (string/starts-with? file "/") file
    (string/starts-with? file "~") (clojure.string/replace
                                            (str (System/getenv "HOME") "/" file)
                                            "~/" "")
    :else (str (System/getenv "PWD") "/" file)))

(def cli-opts
  [
   ["-c" "--config CONFIG" "TOML config file for resume"
    :default (parse-config (spit-file "resume.toml"))
    :default-desc "resume.toml"
    :parse-fn #(parse-config (spit-file %))]
   [nil "--no-pdf" "Generate a .tex file only."]
   ["-h" "--help"]])

(defn opts-use
  [summary]
  (->> ["Lorem Ipsum"
        ""
        "Basic usage: resu-me -c resume.toml"
        ""
        "Optionss:"
        summary
        ""
        "lorem ipsum"]
        (string/join \newline)))

(defn validate-args
  [args]
  (let [{:keys [options arguments summary]} (parse-opts args cli-opts)]
    (cond
      (:help options)
      {:exit-message (opts-use summary) :ok? true}
      :else {:options options})))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn toml-from-arg
  [& args]
  (get-in
   (parse-opts args cli-opts)
   [:config]))

(defn no-pdf?
  [& args]
  (if (nil?
       (get-in (parse-opts args cli-opts)
               [:options :no-pdf]))
    false
    true))

(defn test-args [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
   ;   options)))
      (get-in options [:config]))))


