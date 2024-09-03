(ns resu-me.bugstyle
  (:require [toml-clj.core :as toml]
            [clojure.java.io :as io]
            [resu-me.common :as common]))

;;;too many nested writes i believe - likely need to re-arrange
;;;stylistically - functions beginning with "write-" should be write-commanded"
(defn write-header
  [file resume-parsed]
  (common/write-boilerplate file)
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             "\\begin{document}\n"
             "\\pagenumbering{gobble}\n"
             (common/flush-dir "right"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    (common/parse-list
                                     (get-in resume-parsed [:Personal :contact])
                                     0
                                     nil)))
             "\\vspace{-40pt}\n"))))
