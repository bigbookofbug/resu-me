(ns resu-me.core
  (:require [clojure.java.io :as io]
            [resu-me.bugstyle :as bugstyle]
            [resu-me.star-rover :as star-rover]
            [resu-me.common :as common]
            [resu-me.cliparse :as cli]
            [clojure.string :as string]
            [clojure.java.shell :as shell])
  (:gen-class))

(defn write-bugstyle
  [file resume-parsed]
  (println "using template \"bugstyle\"...")
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str
                  (bugstyle/write-preamble)
                  (common/document
                   (bugstyle/parse-to-bugstyle resume-parsed)))))
 ;                  (bugstyle/write-header resume-parsed)
 ;                  (Thread/sleep 500)
 ;                  (bugstyle/write-summary resume-parsed)
 ;                  (bugstyle/write-education resume-parsed)
 ;                  (bugstyle/write-experience resume-parsed)
 ;                  (println "checking for skills...")
 ;                  (Thread/sleep 500)
 ;                  (if (common/skills? resume-parsed)
 ;                    (do
 ;                      (println "skills found, writing.")
 ;                      (bugstyle/write-skills resume-parsed))
 ;                    (println "no skill detected"))))))
  (println "complete! file saved to" file))

(defn write-star-rover
  [file resume-parsed]
  (println "using template \"Star Rover\"...")
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str
                  (loop [cnt 0
                         res nil]
                    (if (>=
                         (- (count (keys resume-parsed)) 1)
                         cnt)
                      (let [fld (nth (keys resume-parsed) cnt)]
                        (let [style (string/lower-case
                                     (str
                                      (get-in resume-parsed [fld :style])))]
                          (if (= style "meta")
                            (do (println "PREAMBLE\n"
                                         (star-rover/write-preamble resume-parsed
                                                                    fld))
                                (recur (inc cnt) (str res (star-rover/write-preamble
                                                          resume-parsed
                                                          fld))))
                            (recur (inc cnt) res))))
                      res))
                  (common/document
                   (star-rover/parse-to-star-rover resume-parsed)))))
  (println "complete! file saved to" file))

;; Maybe take a look at the templates in this repo for inspiration
;; https://github.com/subidit/rover-resume

(defn get-template
  "Get the template listed in the .toml file, and build a resume based on it."
  [file resume-parsed]
  (let [template (get-in resume-parsed [:Template :template])]
    (cond
      (= (string/lower-case template)
         "bugstyle")
      (write-bugstyle file resume-parsed)
      (= (string/lower-case template)
         "star rover")
      (write-star-rover file resume-parsed))))



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
        (cli/validate-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message))
    (let [resume-parsed
          (if (nil? (get-in options [:config]))
            (do
              (println "No valid config file found!")
              (println "Searching for resume.toml in current directory...")
              (cli/parse-config))
            (get-in options [:config]))
          tex-file
          (str (System/getenv "PWD") "/" "resume.tex")]
                                        ;    (print resume-parsed)
      (overwrite-dupe tex-file)
      (get-template tex-file resume-parsed)
      (if (nil? (get-in options [:no-pdf]))
        (do (make-pdf tex-file)
            (println "Generation complete!")
            (println "PDF saved to" (string/replace
                                     (cli/spit-file tex-file)
                                     ".tex"
                                     ".pdf")))
        (println "Skipping PDF Generation"))
      (System/exit 0))))
