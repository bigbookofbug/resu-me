(ns resu-me.cliparse
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.tools.cli :refer [parse-opts]]))

(defn spit-file
  [file]
  (cond
    (clojure.string/starts-with? file "/") file
    (clojure.string/starts-with? file "~") (clojure.string/replace
                                            (str (System/getenv "HOME") "/" file)
                                            "~/" "")
    :else (str (System/getenv "PWD") "/" file)))

(def cli-opts
  
  )

