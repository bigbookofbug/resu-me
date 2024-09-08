(ns resu-me.core
  (:require [toml-clj.core :as toml]
            [clojure.java.io :as io]
            [resu-me.bugstyle :as bugstyle]
            [resu-me.common :as common]
            [resu-me.cliparse :as cli]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [clj-latex.core :as latex])
  (:gen-class))


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

(defn write-bugstyle
  [file resume-parsed]
  (println "using template \"bugstyle\"...")
  (bugstyle/write-header file resume-parsed)
  (Thread/sleep 500)
  (bugstyle/write-summary file resume-parsed)
  (bugstyle/write-education file resume-parsed)
  (bugstyle/write-experience-header file)
  (bugstyle/write-experience file resume-parsed)
  (println "checking for skills...")
  (Thread/sleep 500)
  (if (common/skills? resume-parsed)
    (do
      (println "skills found, writing.")
      (bugstyle/write-skills file resume-parsed))
    (println "no skill detected"))
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str "\\end{document}")))
  (println "complete! file saved to" file))

;; Maybe take a look at the templates in this repo for inspiration
;; https://github.com/subidit/rover-resume


;; tests
;(def resume-parsed
;                (parse-config "/home/bigbug/Documents/resu-me/resources/first.toml"))
(def test-file "/home/bigbug/Documents/resu-me/resources/test.tex")

(defn get-template
  [file resume-parsed]
  (let [template (get-in resume-parsed [:template])]
    (cond
      (= template "bugstyle") (write-bugstyle file resume-parsed)
      )))

(defn overwrite-dupe
  "Parse the initial config file into clj.
  This will allow transformation into .md and latex later.
  Markdown to be used as an intermediary form to validate everything before converting into a pdf file"
  [file]
  (cond (false? (.exists (io/file file))) 'nil
        :else (io/delete-file file)))

(defn make-pdf
  [file]
  (println "generating pdf . . .")
  (shell/sh "pdflatex"
            (str "-output-directory="
                 (string/trim-newline (get (shell/sh "dirname" file) :out)))
            (str file)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options exit-message ok?]}
        (cli/validate-args args)
        resume-parsed
        (if (nil? (get-in options [:config]))
          (do
        (println "No valid config file found!")
        (println "Searching for resume.toml in current directory...")
        (cli/parse-config))
      (get-in options [:config]))]
;    (print resume-parsed)
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message))
      (overwrite-dupe test-file)
      (get-template test-file resume-parsed)
      (if (nil? (get-in options [:no-pdf]))
        (do (make-pdf test-file)
            (println "Generation complete!")
            (println "PDF saved to" (string/replace
                                     (cli/spit-file test-file)
                                     ".tex"
                                     ".pdf")))
        (println "Skipping PDF Generation"))
      (System/exit 0)))

(defn print-parse
  [args]
  (let [resume-parsed
        (cli/toml-from-arg (str args))]
    resume-parsed))
