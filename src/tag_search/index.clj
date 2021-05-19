(ns tag-search.index
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.stacktrace])
  (:import [org.jaudiotagger.audio AudioFileIO]
           [org.jaudiotagger.tag FieldKey]))

(defn list-files
  ([path] (list-files (if (string? path) (io/file path) path) ()))
  ([file acc]
   (if (.isDirectory file)
     (reduce (fn [acc file]
               (-> file (list-files acc) lazy-seq))
             acc
             (->> file .listFiles reverse))
     (cons file acc))))


(defn extract-file-meta
  [file]
  (let [tag (-> file
                AudioFileIO/read
                .getTag)
        key-map {:artist FieldKey/ARTIST
                 :album FieldKey/ALBUM
                 :title FieldKey/TITLE}]
    (into {:path (.getPath file)}
          (map #(do {(key %) (.getFirst tag (val %))}) key-map))))


(defn try-extract-file-meta
  [file]
  (try
    (extract-file-meta file)
   (catch org.jaudiotagger.audio.exceptions.CannotReadException _
     nil)
   (catch java.lang.Exception e
     (clojure.stacktrace/print-stack-trace e)
     nil)))


(defn extract-meta
  [base]
  (filter (comp not nil?) (map try-extract-file-meta (list-files base))))


(defn extract-terms
  [field]
  (map str/lower-case (str/split field #"[^a-zA-Z1-9]+")))


(defn extract-keys
  [meta]
  (->> (dissoc meta :path)
       (map (fn [[k v]]
              (map #(list % k)
                   (filter (comp not empty?)
                           (extract-terms v)))))
       (reduce (partial reduce conj) '())))


(defn group-by-keys
  [kws]
  (persistent!
    (reduce (fn [acc [k w]]
              (let [old-entry (acc k)
                    new-entry (cons w old-entry)]
                (conj! acc [k new-entry])))
            (transient {})
            kws)))

(defn build-index-map
  [metas]
   (let [meta-vector (vec metas)
         len (count meta-vector)
         keys (for [i (range len)
                      token (extract-keys (meta-vector i))]
                  (let [[word keyw] token]
                    [word [keyw i]]))]
     {:index meta-vector
      :inverted-index (group-by-keys keys)}))

(defn build-index
  [base]
  (build-index-map (extract-meta base)))


(comment
  (def base "/home/ssmike/Downloads")
  (spit "/home/ssmike/lst2" (str/join "\n" (map #(.getPath %) (list-files base))))

  (def flacs (filter #(str/ends-with? (.getPath %) ".flac") (list-files base)))

  (:inverted-index (build-index (extract-meta base)))
  )
