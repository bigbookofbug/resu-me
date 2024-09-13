(ns resu-me.common
  (:require [clojure.string :as string]
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

;; TODO
(defn new-command
  [command definition
   &{:keys [number-args default-arg starred?]
     :or {number-args 0 default-arg nil starred? nil}}]
  (str "\\newcommand"
       (if (nil? starred?)
         nil
         "*")
       "{"(latex-command command)"}"
       "["number-args"]"
       (if (nil? default-arg)
         nil
         (str "["(string/join ", " default-arg)"]"))
       "{"definition"}"))

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
(defn item-list
  "internal fucntion, to turn coll into a list of `\\item`. call to it may look something like:
  (list-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  [lst]
  (apply str
         (map #(render-item %) lst)))

(defn quad-list
  "internal fucntion, to turn a coll into a list of `\\quad`. call to it may look something like:
  (parse-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  [lst]
  (apply str
         (map #(str % " " (latex-command 'quad)) lst)))

(defn latex-begin
  "Return: `string`. Start and end a LaTeX environment, with `command` being the environment, and `body` being the content of the environment. \n
  Example-input:\n
  `(latex-begin 'center \"hello\")\n`
  Output:\n
  `\\begin{center}\n`
  `\thello\n`
  `\\end{center}`\n"
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
