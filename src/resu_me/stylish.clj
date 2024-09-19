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
                           'multicol
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
   (if (empty? (get-in resume-parsed
                [section
                :list]))
     (do (println (str "
WARNING: Missing 'list' delcaration in the META field!
If this was not intentional, please provide a 'title', like so:\n
[Template]
template = 'stylish'
style = 'banner'
title = 'Bug Bugson'
list = ['555-555-555', 'ema@ail.com', 'myweb.site']
"))
         (Thread/sleep 3000))
     (ltx 'textls
          :opts [150]
          :body (apply str
                       (interpose " {\\large\\textperiodcentered} "
                                  (get-in resume-parsed
                                          [section :list]))))))]))))

(defn write-banner
  [resume-parsed section]
  (str "{"
   (common/latex-command 'fontsize
                         :args [36
                                36])
   (common/latex-command 'selectfont)
   (common/latex-command 'scshape)
   (if (empty? (common/parse-section
                resume-parsed
                section
                :title))
     (do (println (str "
ERROR: STYLISH reqires a 'title' delcaration in the BANNER field!
Please provide a 'title', like so:\n
[Banner]
style = 'banner'
title = 'Bug Bugson'
list = ['555-555-555', 'ema@ail.com', 'myweb.site']
"))
          (System/exit 0))
   (common/latex-command 'textls
                         :opts [200]
                         :args [(common/parse-section
                                 resume-parsed
                                 section
                                 :title)])) "}"
   (common/latex-command 'vspace
                         :args ["1.5cm"])))

(defn write-summary
  [resume-parsed section]
  (str
   (common/latex-command 'section
                         :args (common/stringify-key section))
   (common/parse-section
          resume-parsed
          section
          :list)))

(defn write-experience-any
  [section-cmd]
  (let [ltx common/latex-command]
    (str
     (common/flush-direction 'left
     (ltx 'textbf
          :args (str
                 (section-cmd :company)
                 (string/trim-newline (ltx 'hfill))
                 " "
                 (if (not (empty? (section-cmd :start)))
                   (str (section-cmd :start)
                        " --- "))
                 (section-cmd :end)))
     (ltx 'newline)
     (ltx 'textbf
          :args
          (str
           (section-cmd :title)
           (if (empty? (section-cmd :location))
             nil
             (ltx 'hfill
                  :body (str (section-cmd :location)))))))
     (if (not (empty? (section-cmd :list)))
       (common/latex-begin ['multicols 2]
       (common/latex-begin 'itemize
                           (common/item-list
                            (section-cmd :list))))
       nil)
     (ltx 'vspace
          :args ["12pt"]))))

(defn write-experience
  [resume-parsed section]
  (str
   (common/latex-command 'section
                         :args [(common/stringify-key section)])
   (let [parse-exp #(if (= % :list)
                      (get-in resume-parsed [section %])
                      (common/parse-section resume-parsed section %))]
     (write-experience-any parse-exp))))

(defn write-experience-nested
  [resume-parsed section]
  (str (common/latex-command 'section :args [(common/stringify-key section)])
       (str (loop [cnt 1
                   res nil]
              (let [parse-exp
                    #(common/parse-experience-nested resume-parsed section % cnt)]
                (if (>= (count (get-in resume-parsed [section]))
                        cnt)
                  (let [new-res
                        (write-experience-any parse-exp)]
                    (recur (inc cnt) (str res new-res)))
                  res))))))

(defn write-list
  [resume-parsed section]
  (str
   (common/latex-command 'section :args [(common/stringify-key section)])
   (common/latex-begin '[itemize]
                         (common/item-list
                          (get-in resume-parsed
                                  [section :list])))))
(defn write-multicol
  [resume-parsed section]
  (str
   (common/latex-command 'section :args [(common/stringify-key section)])
   (common/latex-begin ['multicols 2]
    (common/latex-begin '[itemize]
                         (common/item-list
                          (get-in resume-parsed
                                  [section :list]))))))

(defn parse-to-stylish
  [resume-parsed]
  (loop [cnt 0
         res nil]
      (if (>=
           (- (count (keys resume-parsed)) 1)
           cnt)
        (let [fld (nth (keys resume-parsed) cnt)
              strfld (common/stringify-key fld)
              style (if (common/is-nested? resume-parsed fld)
                      (string/lower-case (str (get-in resume-parsed
                                                      [fld :1 :style])))
                      (string/lower-case (str
                                          (get-in resume-parsed [fld :style]))))]
            (cond
              (= style "banner")
              (do
                (println "BANNER FOUND IN" (string/upper-case strfld))
                (println (write-banner resume-parsed fld))
                (recur (inc cnt) (str res (write-banner resume-parsed fld))))
              (= style "multicol")
              (do
                (println "MULTICOL FOUND IN" (string/upper-case strfld))
                (println (write-multicol resume-parsed fld))
                (recur (inc cnt) (str res (write-multicol resume-parsed fld))))
              (= style "list")
              (do
                (println "LIST FOUND IN" (string/upper-case strfld))
                (println (write-multicol resume-parsed fld))
                (recur (inc cnt) (str res (write-list resume-parsed fld))))
              (= style "summary")
            (do
              (println "SUMMARY FOUND IN" (string/upper-case strfld))
              (println (write-summary resume-parsed fld))
              (recur (inc cnt) (str res (write-summary resume-parsed fld))))
            (= style "experience")
            (do
              (println "EXPERIENCE FOUND IN" (string/upper-case strfld))
              (println (if (common/is-nested? resume-parsed fld)
                         (write-experience-nested resume-parsed fld)
                         (write-experience resume-parsed fld)))
              (recur (inc cnt) (str res (if (common/is-nested? resume-parsed fld)
                                          (write-experience-nested resume-parsed fld)
                                          (write-experience resume-parsed fld)))))
            :else (recur (inc cnt) res)))
        res)))
