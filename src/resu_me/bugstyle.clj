(ns resu-me.bugstyle
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [resu-me.common :as common]))

;; TODO
;; separate out list into switch for sections
;; that allows for multicol support on all sections (high-level maybe)

(def line-sep "\\noindent\\rule{\\textwidth}{0.4pt}\n")

(defn write-preamble
  []
  (str
   (common/document-class 'article ["a4paper"
                                    "12pt"])
   (apply str (map #(common/use-package %)
                     (list 'setspace
                           'multicol
                           'times
                           'soul)))
   (common/use-package 'geometry ["left=2.5cm"
                                   "top=2.5cm"
                                   "right=2.5cm"
                                   "bottom=2.5cm"
                                   "bindingoffset=0.5cm"])
   (common/use-package 'inputenc ["utf8"])
   (common/latex-command 'renewcommand
                         :args [(common/latex-command 'baselinestretch) 1])
   (common/latex-command 'pagenumbering
                         :args 'gobble)))

(defn write-multicol
  [resume-parsed section]
  (str
   (common/flush-direction
    'left
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command
                  'fontsize
                  :args ["12pt" "12pt"])
                 (common/latex-command 'selectfont)
                 (common/latex-command 'textbf
                                       :args (common/stringify-key section)))])
    line-sep)
   (common/flush-direction
    'left
    (common/latex-begin
     ['multicols 2]
     (common/latex-begin '[itemize]
                         (common/item-list
                          (get-in resume-parsed
                                  [section :list])))))))
(defn write-list
  [resume-parsed section]
  (str
   (common/flush-direction
    'left
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command
                  'fontsize
                  :args ["12pt" "12pt"])
                 (common/latex-command 'selectfont)
                 (common/latex-command 'textbf
                                       :args (common/stringify-key section)))])
    line-sep)
   (common/flush-direction
    'left
     (common/latex-begin '[itemize]
                         (common/item-list
                          (get-in resume-parsed
                                  [section :list]))))))

(defn write-banner
  [resume-parsed section]
  (str
   (common/flush-direction
    'right
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command
                  'fontsize
                  :args ["12pt" "12pt"])
                 (common/latex-command 'selectfont)
                 (common/item-list
                  (get-in resume-parsed [section :list])))]))
   (common/latex-command
    'vspace
    :args "-40pt")
   (common/flush-direction
    'left
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command 'fontsize
                                       :args ["16pt" "16pt"])
                 (common/latex-command 'selectfont)
                 (common/latex-command
                  'textbf
                  :args (get-in resume-parsed [section :title])))])
    line-sep)))

(defn write-summary
  [resume-parsed section]
  (str
   (common/flush-direction
    'left
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command
                  'fontsize
                  :args ["12pt" "12pt"])
                 (common/latex-command 'selectfont)
                 (common/parse-section resume-parsed section :list))])
    (common/latex-command 'newline))))

(defn write-experience-header
  [section]
  (str
   (common/flush-direction
    'left
    (common/latex-command
     'setstretch
     :args [0.5
            (str (common/latex-command
                  'fontsize
                  :args ["12pt" "12pt"])
                 (common/latex-command 'selectfont)
                 (common/latex-command 'textbf
                                       :args (common/stringify-key section)))])
    line-sep)))

(defn write-experience-any
  [section-cmd]
  (common/flush-direction
   'left
   (common/latex-command
    'setstretch
    :args [0.5
           (str (common/latex-command
                 'textbf
                 :args (str (section-cmd :company)))
                (if (empty?
                     (section-cmd :location))
                  nil
                  (str (section-cmd :location)))
                (common/latex-command 'hfill)
                (common/latex-command
                 'textbf
                 :args (str
                        (if (not (empty? (section-cmd :start)))
                          (str (section-cmd :start)
                               " --- "))
                        (section-cmd :end))))])
   (common/latex-command 'newline)
   (common/latex-command
    'textit
    :args (section-cmd :title))
   (if (not (empty? (section-cmd :list)))
     (common/latex-begin 'itemize
                         (common/item-list
                          (section-cmd :list)))
     nil)))

(defn write-experience
  [resume-parsed section]
  (str
   (write-experience-header section)
   (let [parse-exp #(if (= % :list)
                      (get-in resume-parsed [section %])
                      (common/parse-section resume-parsed section %))]
     (write-experience-any parse-exp))))



(defn write-experience-nested
  [resume-parsed section]
(str
   (write-experience-header section)
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

(defn parse-to-bugstyle
  [resume-parsed]
  (println "PREAMBLE\n" (write-preamble))
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
            :else
            (recur (inc cnt) res)))
        res)))
