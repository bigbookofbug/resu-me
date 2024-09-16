(ns resu-me.stylish
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [resu-me.common :as common]))

(defn write-preamble
  [resume-parsed section]
  (let [ltx common/latex-command]
    (str
     (common/document-class 'scrartcl
                            ["a4paper"
                             "oneside"
                             "final"])
     (apply str (map #(common/use-package %)
                     (list 'scrlayer-scrpage
                           'titlesec
                           'marvosym
                           'ebgaramond
                           'microtype
                           "tabularx,colortbl")))
     (ltx 'titleformat
          :args [(ltx 'section)
                 (apply str
                        (map #(ltx %) (list
                                       'large
                                       'scshape
                                       'raggedright)))
                 ""
                 "0em"
                 ""])
     "[titlerule]\n"
     (ltx 'pagestyle
          :args ['scrheadings])
     (ltx 'addtolength
          :args [(ltx 'voffset)
                 "-0.5in"])
     (ltx 'addtolength
          :args [(ltx 'textheight)
                 "3cm"])
     (common/new-command 'gray
                         (ltx 'rowcolor
                              :opts ['gray]
                              :args [".90"]))
     (ltx 'renewcommand
          :args [(ltx 'headfont)
                 (apply str
                        (map #(ltx %) (list
                                       'normalfont
                                       'rmfamily
                                       'scshape)))])
     (ltx 'cofoot
          :args [(str (ltx 'fontsize
                      :args [12.5
                             17])
                 (ltx 'selectfont)
                 (ltx 'textls
                      :opts [150]
                      :body (apply str
                                    (interpose " "
                                               (get-in resume-parsed
                                                       [section :list])))))]))))
