(ns resu-me.common
  (:require [toml-clj.core :as toml]
            [clojure.java.io :as io]))

(defn write-boilerplate
  [file]
  (with-open [wrtr (io/writer file)]
    (.write wrtr (str "
\\documentclass[a4paper, 12pt]{article}
\\usepackage{setspace}
\\usepackage{times}
\\usepackage[utf8]{inputenc}
\\usepackage[
  left=2.5cm,
  top=2.5cm,
  right=2.5cm,
  bottom=2.5cm, bindingoffset=0.5cm]{geometry}
\\usepackage{soul}
\\usepackage{multicol}
\\renewcommand{\\baselinestretch}{1}
"))))

(defn flush-dir
  "single function that allows for flushing left or write in LaTex - with the 'body' arg being the contents within the flush"
  [direction body]
    (str "\\begin{flush" direction "}\n"
         body
         "\\end{flush" direction "}\n"))

;;logic here can be used to parse experiences - though the list will start at 1
(defn parse-list
  "internal fucntion, to turn an item into a list. call to it may look something like:
  (parse-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  ([lst len strn]
   (let [res strn]
     (if (>= len (count lst))
       res
       (parse-list lst (inc len) (str res "\\item " (nth lst len) "\n ")))))
  ([lst]
   (let [res nil
         len 0]
     (parse-list lst (inc len) (str res "\\item " (nth lst len) "\n ")))))

(def line-sep "\\noindent\\rule{\\textwidth}{0.4pt}\n")

(defn parse-summary
  [resume-parsed]
  (str
   (get-in resume-parsed
           [:Summary_Section :summary])))

(defn parse-education
  [resume-parsed section]
  (str
   (get-in resume-parsed [:Education_Section section])))

