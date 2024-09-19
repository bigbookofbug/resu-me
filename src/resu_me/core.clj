(ns resu-me.core
  (:require [clojure.java.io :as io]
            [resu-me.bugstyle :as bugstyle]
            [resu-me.star-rover :as star-rover]
            [resu-me.stylish :as stylish]
            [resu-me.common :as common]
            [resu-me.cliparse :as cli]
            [clojure.string :as string]
            [clojure.java.shell :as shell])
  (:gen-class))

(defn write-bugstyle
  [file resume-parsed]
  (println "Using template \"bugstyle\"...")
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str
                  (bugstyle/write-preamble)
                  (common/document
                   (bugstyle/parse-to-bugstyle resume-parsed)))))
  (println "Complete! file saved to" file))

(defn write-star-rover
  [file resume-parsed]
  (println "Using template \"Star Rover\"...")
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str
                  (loop [cnt 0
                         res nil]
                    (if (>=
                         (- (count (keys resume-parsed)) 1)
                         cnt)
                      (let [fld (nth (keys resume-parsed) cnt)
                            style (string/lower-case
                                     (str
                                      (get-in resume-parsed [fld :style])))]
                          (if (= style "meta")
                            (do (println "PREAMBLE\n"
                                         (star-rover/write-preamble resume-parsed
                                                                    fld))
                                (recur (inc cnt) (str res (star-rover/write-preamble
                                                           resume-parsed
                                                           fld))))
                            (recur (inc cnt) res)))
                      res))
                  (common/document
                   (star-rover/parse-to-star-rover resume-parsed)))))
  (println "Complete! file saved to" file))

(defn write-stylish
  [file resume-parsed]
  (println "Using template \"Stylish\"...")
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr (str
                  (loop [cnt 0
                         res nil]
                    (if (>=
                         (- (count (keys resume-parsed)) 1)
                         cnt)
                      (let [fld (nth (keys resume-parsed) cnt)
                            style (string/lower-case
                                     (str
                                      (get-in resume-parsed [fld :style])))]
                          (if (= style "meta")
                            (do (println "PREAMBLE\n"
                                         (stylish/write-preamble resume-parsed
                                                                    fld))
                                (recur (inc cnt) (str res (stylish/write-preamble
                                                           resume-parsed
                                                           fld))))
                            (recur (inc cnt) res)))
                      res))
                  (common/document
                   (common/latex-begin 'center
                   (stylish/parse-to-stylish resume-parsed))))))
  (println "Complete! file saved to" file))
;; Maybe take a look at the templates in this repo for inspiration
;; https://github.com/subidit/rover-resume
(defn get-template
  "Get the template listed in the .toml file, and build a resume based on it."
  [file resume-parsed]
  (loop [cnt 0]
    (if (>=
         (- (count (keys resume-parsed)) 1)
         cnt)
      (let [fld
            (nth (keys resume-parsed) cnt)
            style
            (string/lower-case (str (get-in resume-parsed [fld :style])))]
        (cond (= style "meta")
              (if (empty? (get-in resume-parsed [fld :template]))
                (do (println (str "
ERROR: No TEMPLATE found in META!
Please provide a 'template', like so:\n
[Template]
style = 'Meta'
template = 'bugstyle'
title = 'Bug Bugson'"))
                    (System/exit 0))

              (let [template (get-in resume-parsed [fld :template])]
                (cond
                  (= (string/lower-case template)
                     "bugstyle")
                  (write-bugstyle file resume-parsed)
                  (= (string/lower-case template)
                     "stylish")
                  (write-stylish file resume-parsed)
                  (= (string/lower-case template)
                     "star rover")
                  (write-star-rover file resume-parsed))))
              :else (recur (inc cnt))))
      (do (println (str "
ERROR: No META field found!
Please provide a 'Meta' section, like so:\n
[Template]
style = 'Meta'
template = 'bugstyle'
title = 'Bug Bugson'
"))
          (System/exit 0)))))



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
  (Thread/sleep 1000)
  (dotimes [n 3]
      (println (apply str (repeat 80 "*"))))
  (println "LATEX OUTPUT:")
  (dotimes [n 3]
      (println (apply str (repeat 80 "*"))))
  (println :out (shell/sh "pdflatex"
            (str "-output-directory="
                 (string/trim-newline (get (shell/sh "dirname" file) :out)))
            (str file)))
  (if (= 1 (get (shell/sh "dirname" file) :exit))
    (do
      (println "Error occured during latex-to-pdf conversion!\n"
               "Please check the output of 'pdflatex' for more info")
      (System/exit 1))
    nil)
  (dotimes [n 3]
      (println (apply str (repeat 80 "*"))))
  (println "END LATEX OUTPUT")
  (dotimes [n 3]
      (println (apply str (repeat 80 "*")))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options exit-message ok?]}
        (cli/validate-args args)]
    ;; TODO - should we keep this ?
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      nil)
    ;; END TODO
    (let [resume-parsed
          (if (nil? (get-in options [:config]))
            (do
              (println "No config file specified!")
              (println "Searching for resume.toml in current directory...")
              (cli/parse-config
               (str (System/getenv "PWD") "/" "resume.toml")))
            (get-in options [:config]))
          tex-file
          (str (System/getenv "PWD") "/" "resume.tex")]
      (overwrite-dupe tex-file)
      (get-template tex-file resume-parsed)
      (if (nil? (get-in options [:no-pdf]))
        (do (make-pdf tex-file)
            (println "Generation complete!\n"
                     "Please check output of 'pdflatex' to ensure there were no errors in pdf generation.")
            (println "PDF saved to" (string/replace
                                     (cli/spit-file tex-file)
                                     ".tex"
                                     ".pdf")))
        (println "Skipping PDF Generation"))
      (System/exit 0))))
