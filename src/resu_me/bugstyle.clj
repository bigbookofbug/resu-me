(ns resu-me.bugstyle
  (:require [clojure.java.io :as io]
            [resu-me.common :as common]))

(def line-sep "\\noindent\\rule{\\textwidth}{0.4pt}\n")

(defn write-preabmle
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

(defn write-header
  [resume-parsed]
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
                 (common/parse-list
                  (get-in resume-parsed [:Personal :contact])))]))
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
                  :args (get-in resume-parsed [:Personal :name])))])
    line-sep)))

(defn write-summary
  [resume-parsed]
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
                 (common/parse-summary resume-parsed))])
    (common/latex-command 'newline))))

(defn write-education
  [resume-parsed]
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
                                       :args 'Education))])
    line-sep)
   (common/flush-direction
    'left
    (common/latex-command
     'textbf
     :args (str
            (common/parse-education resume-parsed :institute)
            (common/latex-command 'hfill)
            (common/parse-education resume-parsed :end)))
    (common/latex-command 'newline)
    (apply str (interpose ", "
                          (list (common/parse-education resume-parsed :degree)
                                (common/parse-education resume-parsed :area))))
    (if (common/education-highlights? resume-parsed)
      (str
       (common/latex-begin
        ['multicols 2]
        (common/latex-begin '[itemize]
                            (common/parse-list
                             (get-in resume-parsed
                                     [:Education_Section :highlights])))))
      (println "Skipping education highlights ...")))))

(defn write-experience
  [resume-parsed]
  (str (common/flush-direction 'left
       (common/latex-command
        'setstretch
        :args [0.5
               (str (common/latex-command
                     'fontsize
                     :args ["12pt" "12pt"])
                    (common/latex-command 'selectfont)
                    (common/latex-command 'textbf
                                          :args "Work Experience"))]))
       line-sep
       (str (loop [cnt 1
                   res nil]
              (let [parse-exp
                    #(common/parse-experience resume-parsed % cnt)]
                (if (>= (count (get-in resume-parsed [:Experience]))
                        cnt)
                    (let [new-res
                          (str
                           (common/flush-direction 'left
                         (common/latex-command
                          'setstretch
                          :args [0.5
                                 (str (common/latex-command
                                       'textbf
                                       :args (str (parse-exp :company) ","))
                                      (parse-exp :location)
                                      (common/latex-command 'hfill)
                                      (common/latex-command
                                       'textbf
                                       :args (str (parse-exp :start)
                                                  " --- "
                                                  (parse-exp :end))))])
                         (common/latex-command 'newline)
                         (common/latex-command
                          'textit
                          :args (parse-exp :title))
                         (common/latex-begin 'itemize
                                             (common/parse-list
                                              (parse-exp :duties)))))]
                    (recur (inc cnt) (str res new-res)))
                    res))))))

(defn write-skills
  [resume-parsed]
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
                                       :args 'Skills))])
    line-sep)
   (common/flush-direction
    'left
    (common/latex-begin
     ['multicols 2]
     (common/latex-begin '[itemize]
                         (common/parse-list
                          (get-in resume-parsed
                                  [:Skills_Section :skills])))))))


(defn write-skill
  [file resume-parsed]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str (common/flush-direction "left"
                                   (str
                                    "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\\textbf{Skills}}\n"
                                    line-sep))
                 (common/flush-direction "left"
                                   (str
                                    "\\setstretch{0.5}\n"
                                    "\\begin{multicols}{2}\n"
                                    "\\begin{itemize}\n"
                                    (common/parse-list
                                     (get-in resume-parsed [:Skills_Section :skills]))
                                    "\\end{itemize}\n"
                                    "\\end{multicols}\n"))))))
