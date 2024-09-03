(ns resu-me.core
  (:require [toml-clj.core :as toml]
            [clojure.java.io :as io]
            [resu-me.bugstyle :as bugstyle]
            [resu-me.common :as common]
            [clj-latex.core :as latex])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn parse-config 
  "Parse the initial config file into clj.
  This will allow transformation into .md and latex later.
  Markdown to be used as an intermediary form to validate everything before converting into a pdf file"
  [file]
  (cond (false? (.exists (io/file file))) (println "File doesn't exist!")
        (nil? (re-find #"\.toml$" file)) (println "Please provide a .toml file.")
        :else (with-open [reader (io/reader file)]
                (toml/read reader {:key-fn keyword}))))

;; Maybe take a look at the templates in this repo for inspiration
;; https://github.com/subidit/rover-resume


;; tests
(def resume-parsed
                (parse-config "/home/bigbug/Documents/resu-me/resources/first.toml"))
(def test-file "/home/bigbug/Documents/resu-me/resources/test.tex")

;;figure out how to iterate over contact items
;(def cont-header (str
;                  (get-in resume-parsed [:Personal :name]) "\n"
;                  (first (get-in resume-parsed [:Personal :contact]))))
