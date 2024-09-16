(ns resu-me.star-rover
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [resu-me.common :as common]))

(defn write-preamble
  [resume-parsed section]
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
                                                       [section :title])
                                               " -- Page "
                                               (string/trim-newline (ltx 'thepage))
                                               "\\ of "
                                               (string/trim-newline
                                                (ltx 'pageref*
                                                     :args ['LastPage])))))])
     (ltx 'pagestyle
          :args ['empty]))))

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

                        ;;; TODO - sub out appr calls w/ `parse-exp`
                        (str
                         (common/latex-command
                          'subsection
                          :args
                          [(str (parse-exp :company)
                                (if (empty?
                                     (parse-exp :location))
                                  nil
                                  (common/latex-command
                                   'rside
                                   :args [(str
                                           (parse-exp :location))])))])
                         (common/latex-command
                          'subsubsection
                          :args
                          [(str (parse-exp :title)
                                (common/latex-command
                                 'rside
                                 :args [(str
                                         (if (not (empty? (parse-exp :start)))
                                           (str (parse-exp :start)
                                                " -- "))
                                         (parse-exp :end))]))])
                         (if (not (empty? (parse-exp :list)))
                           (common/latex-begin 'itemize
                                               (common/item-list
                                                (parse-exp :list)))
                           nil))]
                    (recur (inc cnt) (str res new-res)))
                  res))))))

(defn write-experience
  [resume-parsed section]
  (str
   (common/latex-command 'section :args [(common/stringify-key section)])
   (common/latex-command
    'subsection
    :args
    [(str (common/parse-section
           resume-parsed
           section :company)
          (if (empty?
             (common/parse-section
              resume-parsed
              section :location))
          nil
          (common/latex-command
           'rside
         :args [(str
                 (common/parse-section
                  resume-parsed
                  section :location))])))])
 (common/latex-command
  'subsubsection
  :args
  [(str (common/parse-section
         resume-parsed
         section :title)
        (common/latex-command
         'rside
         :args [(str
                 (if (not (empty? (common/parse-section
                                   resume-parsed section :start)))
                 (str (common/parse-section
                       resume-parsed section
                       :start)
                 " -- "))
                 (common/parse-section resume-parsed
                                       section :end))]))])
(if (not (empty? (common/parse-section resume-parsed section :list)))
 (common/latex-begin 'itemize
                     (common/item-list
                      (get-in
                       resume-parsed [section
                                      :list])))
 nil)))

(defn write-banner
  [resume-parsed section]
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
                                                                [section :title])))
                           "} \\\\" (common/latex-command 'medskip)
                           (common/quad-list
                            (get-in resume-parsed [section :list])))))

(defn write-list
 [resume-parsed section]
 (str
  (common/latex-command 'section :args [(common/stringify-key section)])
  (common/latex-begin 'itemize
                      (common/item-list
                       (get-in resume-parsed
                               [section :list])))))

(defn write-multicol
  [resume-parsed section]
  (str
  (common/latex-command 'section :args [(common/stringify-key section)])
  (common/latex-begin ['multicols 2]
                    (common/latex-begin 'itemize
                      (common/item-list
                       (get-in resume-parsed
                               [section :list]))))))

(defn write-summary
  [resume-parsed section]
  (str
   (common/parse-section resume-parsed section :list)
   (common/latex-command 'newline)))



(defn parse-to-star-rover
  [resume-parsed]
  (loop [cnt 0
         res nil]
      (if (>=
           (- (count (keys resume-parsed)) 1)
           cnt)
        (let [fld (nth (keys resume-parsed) cnt)
              strfld (common/stringify-key fld)]
          (let [style (if (common/is-nested? resume-parsed fld)
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
            :else (do
                    (recur (inc cnt) res)))))
        res)))
