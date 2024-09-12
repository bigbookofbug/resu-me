(ns resu-me.common
  (:require [toml-clj.core :as toml]
            [clojure.string :as string]
            [clojure.java.io :as io]))


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
  "Takes a string, symbol, or collection as its first argument, and a body as its second argument.\n
  Output:\n
  `\\begin{ARG}\n`
  `\tBODY\n`
  `\\end{ARG}`\n
  Example:\n
  `(latex-begin 'itemize \"hello\")`"
  [command & body]
  (str (latex-command 'begin :args command)
       (apply str body)
       (latex-command 'end :args (if (coll? command)
                                    (first (seq command))
                                    command))))

(defn flush-direction
  "single function that allows for flushing left or write in LaTex - with the 'body' arg being the contents within the flush"
  [direction & body]
  (latex-begin (str "flush" direction)
               (apply str body)))

(defn document
  [& body]
  (latex-begin 'document (apply str body)))
