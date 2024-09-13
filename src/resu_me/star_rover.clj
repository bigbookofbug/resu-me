(ns resu-me.star-rover
  (:require [clojure.java.io :as io]
            [clj-latex.core :as latex]
            [clojure.string :as string]
            [resu-me.common :as common]))

(defn write-preamble
  [resume-parsed]
  (let [ltx common/latex-command]
    (str
     (common/document-class 'article ["11pt"])
     (apply str (map #(common/use-package %)
                     (list 'xcolor
                           'enumitem
                           'xhfill
                           'titlesec
                           'lastpage
                           'fancyhdr
                           'fontawesome
                           'multicol
                           'soul)))
     (common/use-package 'FiraSans ["sfdefault"
                                    "semibold"
                                    "lf"])
     (common/use-package 'geometry ["a4paper"
                                    "margin=1in"])
     (common/use-package 'hyperref ["bookmarks=false"
                                  "hidelinks"])
     (ltx 'definecolor
          :args ['accent 'HTML "141E61"])
     (ltx 'newcommand
          :args [(ltx
                  'linkicon)
                 (ltx
                  'color
                  :args
                  ['gray
                   (ltx
                    'footnotesize)
                   (ltx
                    'raisebox
                    :args ["1pt"
                           (ltx
                            'faIcon
                            :args 'external-link-alt)])])])
     (ltx 'setcounter
          :args ['secnumdepth
                 0])
     (ltx "pdfgentounicode=1")
     (ltx 'setlist
          :opts ['itemize]
          :args ["nosep, left=0pt..1.5em"])
     (ltx 'setlist
          :opts ['enumerate]
          :args ["left=0pt..1.5em"])
     (ltx 'setlist
          :opts ['description]
          :args ["itemsep=0pt"])
     (common/new-command
      'midrule
      (str
       (ltx 'leavevmode)
       (ltx
        'xrfill
        :opts [".5ex"]
        :args ["1pt"]
        :body
        (str '[accent]
             (string/replace
              (str "~"(ltx 'so :args ["#1"])"~")
              "\n"
              "")
             (ltx
              'xrfill
              :opts [".5ex"]
              :args ["1pt"]
              :body '[accent]))))
      :number-args 1)
     (ltx 'titlespacing :args [(ltx 'section)
                               "0pt"
                               "*4"
                               "*1"])
     (ltx 'titlespacing :args [(ltx 'subsection)
                               "0pt"
                               "*3"
                               "*0"])
     (ltx 'titlespacing :args [(ltx 'subsubsection)
                               "0pt"
                               "*0"
                               "*0"])
     (ltx 'titleformat :args [(ltx 'section)
                              (apply str (map #(string/trim-newline %)
                                              (list (ltx 'large)
                                                    (ltx 'bfseries)
                                                    (ltx 'uppercase))))
                              ""
                              ""
                              (ltx 'midrule)])
     (ltx 'titleformat* :args [(ltx 'subsection)
                              (apply str (map #(string/trim-newline %)
                                              (list (ltx 'large)
                                                    (ltx 'bfseries)
                                                    (ltx 'scshape))))])
     (common/new-command 'aux
                         (str "$|$"
                              (ltx 'space
                                   :args [(apply str (map #(string/trim-newline %)
                                                          (list (ltx 'normalfont)
                                                                (ltx 'itshape)
                                                                " #1")))]))
                         :number-args 1)
     (common/new-command 'rside
                         (str
                          (ltx
                           'hfill
                           :args [(apply str (map #(string/trim-newline %)
                                                  (list (ltx 'normalfont)
                                                        (ltx 'color
                                                             :args ['accent])
                                                        " #1")))]))
                         :number-args 1)
     (ltx 'pagestyle :args ['fancy])
     (ltx 'fancyhf :args [""])
     (ltx 'renewcommand :args [(ltx 'headrulewidth)
                               "0pt"])
     (ltx 'fancyfoot
          :opts ['C]
          :args [(str (ltx 'small) (ltx 'color
                                        :args ['gray]
                                        :body (str
                                               (get-in resume-parsed
                                                       [:Personal :name])
                                               " -- Page "
                                               (string/trim-newline (ltx 'thepage))
                                               "\\ of "
                                               (string/trim-newline
                                                (ltx 'pageref*
                                                     :args ['LastPage])))))]))))

(defn write-banner
  [resume-parsed]
  (common/latex-begin 'center
                      (str "{\n"
                           (common/latex-command 'fontsize :args [36 12])
                           (common/latex-command 'fontseries
                                                 :args ['heavy]
                                                 :body (common/latex-command 'selectfont))
                           (common/latex-command 'color
                                                 :args ['accent]
                                                 :body (str
                                                        (get-in resume-parsed
                                                                [:Personal :name])))
                           "} \\\\" (common/latex-command 'medskip)
                           (common/quad-list
                            (get-in resume-parsed [:Personal :contact])))))

(defn write-education
  [resume-parsed]
  (str
   (common/latex-command 'section :args ['Education])
   (common/latex-command 'subsection
                         :args
                         [(apply str (map #(string/trim-newline %)
                                              (list
                                               (common/parse-education
                                                resume-parsed
                                                :institute)
                                               " "
                                               (common/latex-command
                                                'aux
                                                :args [(str (common/parse-education
                                                             resume-parsed
                                                             :degree)
                                                            " in "
                                                            (common/parse-education
                                                             resume-parsed
                                                             :area))]) " "
                                               (common/latex-command
                                                'rside
                                                :args
                                                [(str (common/parse-education
                                                       resume-parsed
                                                       :start)
                                                      " -- "
                                                      (common/parse-education
                                                       resume-parsed
                                                       :end))]))))])
   (if (common/education-highlights? resume-parsed)
     (str
       (common/latex-begin '[itemize]
                            (common/item-list
                             (get-in resume-parsed
                                     [:Education_Section :highlights]))))
      (println "Skipping education highlights ..."))))

(defn write-experience
  [resume-parsed]
  (str (common/latex-command 'section :args ['Experience])
       (str (loop [cnt 1
                   res nil]
              (let [parse-exp
                    #(common/parse-experience resume-parsed % cnt)]
                (if (>= (count (get-in resume-parsed [:Experience]))
                        cnt)
                  (let [new-res
                        (str
                         (common/latex-command
                          'subsection
                          :args
                          [(str (parse-exp
                                 :company)
                                (common/latex-command
                                 'rside
                                 :args [(str
                                         (parse-exp
                                          :location))]))])
                         (common/latex-command
                          'subsubsection
                          :args
                          [(str (parse-exp
                                 :title)
                                (common/latex-command
                                 'rside
                                 :args [(str
                                         (parse-exp
                                          :start)
                                         " -- "
                                         (parse-exp
                                          :end))]))])
                         (common/latex-begin 'itemize
                                             (common/item-list
                                              (parse-exp :duties))))]
                    (recur (inc cnt) (str res new-res)))
                  res))))))

(defn write-skills
 [resume-parsed]
 (str
  (common/latex-command 'section :args ['Skills])
  (common/latex-begin 'itemize
                      (common/item-list
                       (get-in resume-parsed
                               [:Skills_Section :skills])))))

(defn write-summary
  [resume-parsed]
  (str
   (common/parse-summary resume-parsed)
   (common/latex-command 'newline)))




;(defn write-test
;  [resume-parsed]
;  (str
;   (write-preamble resume-parsed)
;   (common/document
;    (write-banner resume-parsed)
;    (write-summary resume-parsed)
;    (write-education resume-parsed)
;    (write-experience resume-parsed)
;    (write-skills resume-parsed))))
