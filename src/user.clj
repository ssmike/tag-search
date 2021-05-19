(use 'clojure.repl)
(use 'clojure.java.javadoc)
(use '[clojure.reflect :only [reflect]])
(use '[clojure.string :only [join]])

(defn inspect [obj]
  "nicer output for reflecting on an object's methods"
  (let [reflection (clojure.reflect/reflect obj)
        members (sort-by :name (:members reflection))]
    (println "Class:" (.getClass obj))
    (println "Bases:" (:bases reflection))
    (println "---------------------\nConstructors:")
    (doseq [constructor (filter #(instance? clojure.reflect.Constructor %) members)]
      (println (:name constructor) "(" (clojure.string/join ", " (:parameter-types constructor)) ")"))
    (println "---------------------\nMethods:")
    (doseq [method (filter #(instance? clojure.reflect.Method %) members)]
      (println (:name method) "(" (clojure.string/join ", " (:parameter-types method)) ") ;=>" (:return-type method)))))
