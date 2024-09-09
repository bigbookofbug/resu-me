(ns resu-me.bugstyle
  (:require [clojure.java.io :as io]
            [resu-me.common :as common]))

(defn write-header
  [file resume-parsed]
  (common/write-boilerplate file)
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             "\\pagenumbering{gobble}\n"
             "\\begin{document}\n"
             (common/flush-direction "right"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    (common/parse-list
                                     (get-in resume-parsed [:Personal :contact]))"}"))
             "\\vspace{-40pt}\n"
             (common/flush-direction "left"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{16pt}{16pt}\\selectfont\n"
                                    "{\\textbf{"
                                    (get-in resume-parsed [:Personal :name])"}}}\n"
                                    common/line-sep))))))
(defn write-preabmle
  []
  (str
   (common/document-class 'article ["a4paper"
                                    "12pt"])
   (apply str (map #(common/use-package %)
                     (list 'setspace
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

(defn write-document
  [resume-parsed]
  (common/document
;;to be continued
   ))


(defn write-summary
  [file resume-parsed]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             (common/flush-direction "left"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    (common/parse-summary resume-parsed)
                                    "}\n"
                                    "\\newline\n"))))))

(defn write-education
  [file resume-parsed]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             (common/flush-direction "left"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    "\\textbf{Education}}\n"
                                    common/line-sep))
             (common/flush-direction "left"
                               (str "{\\textbf{"
                                    (common/parse-education resume-parsed :institute)
                                    " \\hfill "
                                    (common/parse-education resume-parsed :end)
                                    "}}\n"
                                    "\\newline\n"
                                    (common/parse-education resume-parsed :degree) ", "
                                    (common/parse-education resume-parsed :area)
                                    (if (common/education-highlights? resume-parsed)
                                      (str
                                           "\\begin{multicols}{2}\n"
                                           "\\begin{itemize}\n"
                                           ;;FIXME
                                           (common/parse-list
                                            (get-in resume-parsed [:Education_Section :highlights]))
;;FIXME = no iseq from boolean
                                           "\\end{itemize}\n"
                                           "\\end{multicols}\n")
                                      (println "Skipping Education Highlights ..."))))))))

(defn write-experience
  ([file resume-parsed]
   (loop [cnt 1]
     (let [parse-exp
           #(common/parse-experience resume-parsed % cnt)]
       (if (>= (count (get-in resume-parsed [:Experience]))
               cnt)
         (do (with-open [wrtr (io/writer file :append true)]
               (.write wrtr
                       (str (common/flush-direction "left" (str
                                                      "\\setstretch{0.5}\n"
                                                      "{\\textbf{"(parse-exp :company)",} "
                                                      (parse-exp :location)" \\hfill \n"
                                                      "\\textbf{"(parse-exp :start)" --- "(parse-exp :end)"}}\n"
                                                      "\\newline\n"
                                                      "\\textit{"(parse-exp :title)"}\n"
                                                      "\\begin{itemize}\n"
                                                      (common/parse-list
                                                       (parse-exp :duties))
                                                      "\\end{itemize}\n")))))
             (recur (inc cnt))))))))

(defn write-experience-header
  [file]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
                (str (common/flush-direction "left" (str
                                               "\\setstretch{0.5}\n"
                                               "{\\fontsize{12pt}{12pt}\\selectfont \\textbf{Work Experience}}\n"
                                               common/line-sep))))))
(defn write-skills
  [file resume-parsed]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str (common/flush-direction "left"
                                   (str
                                    "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\\textbf{Skills}}\n"
                                    common/line-sep))
                 (common/flush-direction "left"
                                   (str
                                    "\\setstretch{0.5}\n"
                                    "\\begin{multicols}{2}\n"
                                    "\\begin{itemize}\n"
                                    (common/parse-list
                                     (get-in resume-parsed [:Skills_Section :skills]))
                                    "\\end{itemize}\n"
                                    "\\end{multicols}\n"))))))
