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
\\renewcommand{\\baselinestretch}{1}
"))))

(defn flush-dir
  "single function that allows for flushing left or write in LaTex - with the 'body' arg being the contents within the flush"
  [direction body]
    (str "\\begin{flush" direction "}\n"
         body
         "\\end{flush" direction "}\n"))

(defn parse-list
  "internal fucntion, to turn an item into a list. call to it may look something like:
  (parse-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  [lst len strn]
  (let [res strn]
    (if (>= len (count lst))
      res
      (parse-list lst (inc len) (str res "\\item " (nth lst len) "\n ")))))
