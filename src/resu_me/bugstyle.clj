(ns resu-me.bugstyle
  (:require [toml-clj.core :as toml]
            [clojure.java.io :as io]
            [resu-me.common :as common]))

(defn write-header
  [file resume-parsed]
  (common/write-boilerplate file)
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             "\\pagenumbering{gobble}\n"
             "\\begin{document}\n"
             (common/flush-dir "right"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    (common/parse-list
                                     (get-in resume-parsed [:Personal :contact]))"}"))
             "\\vspace{-40pt}\n"
             (common/flush-dir "left"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{16pt}{16pt}\\selectfont\n"
                                    "{\\textbf{"
                                    (get-in resume-parsed [:Personal :name])"}}}\n"
                                    common/line-sep))))))


(defn write-summary
  [file resume-parsed]
  (with-open [wrtr (io/writer file :append true)]
    (.write wrtr
            (str
             (common/flush-dir "left"
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
             (common/flush-dir "left"
                               (str "\\setstretch{0.5}\n"
                                    "{\\fontsize{12pt}{12pt}\\selectfont\n"
                                    "\\textbf{Education}}\n"
                                    common/line-sep))
             (common/flush-dir "left"
                               (str "{\\textbf{"
                                    (common/parse-education resume-parsed :institute)
                                    " \\hfill "
                                    (common/parse-education resume-parsed :end)
                                    "}}\n"
                                    "\\newline\n"
                                    (common/parse-education resume-parsed :degree) ", "
                                    (common/parse-education resume-parsed :area)
                                    (if (not (nil?
                                              (common/parse-education resume-parsed :highlights)))
                                      (do (str
                                           "\\begin{multicols}{2}\n"
                                           "\\begin{itemize}\n"
                                           (common/parse-list
                                            (get-in resume-parsed [:Education_Section :highlights]))
                                           "\\end{itemize}\n"
                                           "\\end{multicols}\n")))))))))
