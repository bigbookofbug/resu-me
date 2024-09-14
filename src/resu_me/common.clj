(ns resu-me.common
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn stringify-key
  [k]
  (string/replace
   (str k) ":" ""))

(defn parse-seq
  "internal fucntion, to turn coll into a list of `\\item`. call to it may look something like:
  (list-list (get-in resume-parsed [:Personal :contact]) 0 file/path)"
  [lst cmd]
  (apply str
         (map #(cmd %) lst)))

(defn parse-section
  [resume-parsed section subsection]
  (str
   (if (coll? (get-in resume-parsed [section subsection]))
     (if (= 1 (count (get-in resume-parsed
                             [section subsection])))
       (first (get-in resume-parsed
                      [section subsection]))
       (apply str (get-in resume-parsed
               [section subsection])))
     (get-in resume-parsed
             [section subsection]))))

(defn parse-experience-nested
  [resume-parsed section subsection cnt]
  (get-in resume-parsed [section (keyword (str cnt)) subsection]))

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

(defn key-depth
  ([m]
   (key-depth m 0))
  ([m depth]
   (reduce
     (fn [acc [k v]]
       (if (map? v)
         (assoc acc k (key-depth v (inc depth)))
         (assoc acc k depth)))
     {} m)))

(defn is-nested?
  [resume-parsed k]
  (every? true?
          (map #(map? %)
               (vals (key-depth
                      (get-in resume-parsed [k]))))))
