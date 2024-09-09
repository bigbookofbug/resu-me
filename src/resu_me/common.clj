(ns resu-me.common
  (:require [toml-clj.core :as toml]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(defn write-boilerplate
  [file]
  (with-open [wrtr (io/writer file)]
    (.write wrtr (str "
\\documentclass[a4paper, 12pt]{article}
\\usepackage{setspace}
\\usepackage{times}
\\usepackage[utf8]{inputenc}
\\usepackage[
  left=2.5cm,
  top=2.5cm,
  right=2.5cm,
  bottom=2.5cm, bindingoffset=0.5cm]{geometry}
\\usepackage{soul}
\\usepackage{multicol}
\\renewcommand{\\baselinestretch}{1}
"))))

;(defn flush-dir
;  "single function that allows for flushing left or write in LaTex - with the 'body' arg being the contents within the flush"
;  [direction body]
;    (str "\\begin{flush" direction "}\n"
;         body
;         "\\end{flush" direction "}\n"))


(def line-sep "\\noindent\\rule{\\textwidth}{0.4pt}\n")

(defn parse-summary
  [resume-parsed]
  (str
   (get-in resume-parsed
           [:Summary_Section :summary])))

(defn parse-education
  [resume-parsed section]
  (str
   (get-in resume-parsed [:Education_Section section])))

(defn parse-experience
                [resume-parsed exp cnt]
                (get-in resume-parsed [:Experience (keyword (str cnt)) exp]))

(defn skills?
  [resume-parsed]
  (get-in resume-parsed [:has_skill]))

(defn education-highlights?
  [resume-parsed]
  (get-in resume-parsed [:Education_Section :highlights]))



(defn latex-command
                [command &{:keys [args opts body]
                           :or {args nil opts nil body nil}}]
                (str "\\" command
                      (if (nil? opts)
                        ""
                        (str "[" (string/join ", " opts) "]"))
                      (if (nil? args)
                        ""
                        (if (sequential? args)
                          (apply str (map #(str "{"%"}") args))
                          (str "{"args"}")))
                      (if (nil? body)
                        ""
                        (str " " body))
                      "\n"))
(defn use-package
  [package & opts]
  (latex-command "usepackage"
                 :args package
                 :opts (first opts)))
(defn document-class
  [class & opts]
  (latex-command "documentclass"
                 :args class
                 :opts (first opts)))

(defn render-item
                ([body]
                 (latex-command "item"
                                       :body body))
                ([body opts]
                 (latex-command "item"
                                :body body
                                :opts opts)))

;;bandage until i can figure out how to properly recur on nested maps
;;so that i can pass args such as (parse list '("test 1" (list "test 2" ["arg"]) "3"))
(defn parse-list
  "internal fucntion, to turn an item into a list. call to it may look something like:
  (parse-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  [lst]
  (apply str
         (map #(render-item %) lst)))

(defn latex-begin
  [command & body]
  (str (latex-command "begin" :args command)
       (apply str body)
       (latex-command "end" :args command)))

(defn flush-direction
  "single function that allows for flushing left or write in LaTex - with the 'body' arg being the contents within the flush"
  [direction & body]
  (latex-begin (str "flush" direction)
               (apply str body)))

(defn document
  [& body]
  (latex-begin "document" (apply str body)))
;                (str (latex-command "begin" :args "document")
;                (apply str body)
;                (latex-command "end" :args "document")))


;(defn deep-list [f lst]
;                (cond (empty? lst) nil
;                      (sequential? (first lst))
;                      (cons (deep-list f (first lst))
;                            (deep-list f (rest lst)))
;                      :else (cons (f (first lst))
;                                  (deep-list f (rest lst)))))

;(defn do-list
;  ([kw sym]
;   (do-list kw sym 0))
;  ([kw sym cnt]
;     (if (< cnt (count sym))
;       (do
;         (let [symb (nth sym cnt)]
;           (if (list? symb)
;             (println (cons kw (map #(if (symbol? %) 
;                                       (list 'quote %) %) symb)))
;             (println (list kw (list 'quote symb))))
;           (do-list kw sym (inc cnt)))
;         'nil))))
