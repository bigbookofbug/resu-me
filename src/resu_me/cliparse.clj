(ns resu-me.cliparse
  (:require [clojure.java.io :as io]
            [toml-clj.core :as toml]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]))

(defn parse-config
  "If it exists, parse the initial config file into clj map.
  This will allow transformation into .toml and latex later.
  If no file is provided, searches for a \"resume.toml\" in the pwd"
  ([file]
  (cond (false? (.exists (io/file file))) (do
                                            (println "File doesn't exist!")
                                            (System/exit 0))
        (nil? (re-find #"\.toml$" file)) (do
                                           (println "Please provide a .toml file.")
                                           (System/exit 0))
        :else (with-open [reader (io/reader file)]
                (toml/read reader {:key-fn keyword}))))
  ([]
   (let [file
         (str (System/getenv "PWD") "/" "resume.toml")]
     (cond (false? (.exists (io/file file))) (do
                                               (println "File doesn't exist!")
                                               (System/exit 0))
           (nil? (re-find #"\.toml$" file)) (do
                                              (println "Please provide a .toml file.")
                                              (System/exit 0))
           :else (with-open [reader (io/reader file)]
                   (toml/read reader {:key-fn keyword}))))))

(defn spit-file
  "Hacky method of handling relative paths"
  [file]
  (cond
    (string/starts-with? file "/") file
    (string/starts-with? file "~") (clojure.string/replace
                                            (str (System/getenv "HOME") "/" file)
                                            "~/" "")
    :else (str (System/getenv "PWD") "/" file)))

(def cli-opts
  [
   ["-c" "--config CONFIG"
    "TOML config file for resume. If no file is provided, program will search for a \"resume.toml\" file in the pwd."
   ; :default-desc "resume.toml"
    :parse-fn #(parse-config (spit-file %))]
   [nil "--no-pdf" "Generate a .tex file only."]
   ["-h" "--help"]])

(defn opts-use
  [summary]
  (->> ["Lorem Ipsum"
        ""
        "Basic usage: resu-me -c resume.toml"
        ""
        "Options:"
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
