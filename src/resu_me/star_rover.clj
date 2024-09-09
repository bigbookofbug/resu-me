(ns resu-me.star-rover
  (:require [clojure.java.io :as io]
            [clj-latex.core :as latex]
            [resu-me.common :as common]))
;; LATEX commands begin w/ \
;; args are in {}
;; options are in []

(defn write-preabmle
  []
  (str
   (common/document-class 'article ["a4paper"
                                    "12pt"])
   (apply str (map #(common/use-package %)
                     (list "setspace"
                           "times"
                           "soul")))
   (common/use-package "geometry" ["left=2.5cm"
                                   "top=2.5cm"
                                   "right=2.5cm"
                                   "bottom=2.5cm"
                                   "bindingoffset=0.5cm"])
   (common/use-package "inputenc" ["utf8"])))
