(ns tag-search.search
  (:require [tag-search.index :as index]))

(defn match-relevance
  [kw]
  (case kw
    :title 6
    :artist 1
    :album 3))

; (match-relevance :title)

(defn merge-hits
  [lists]
  (apply merge-with + {}
              (for [lst lists
                    [kw number] lst]
                {number (match-relevance kw)})))

; (merge-hits '(((:title 1)), ((:album 1) (:title 2))))

(defn search
  [{index :index inv :inverted-index :as all} request limit]
  (let [terms (index/extract-terms request)
        lists (map inv terms)
        merged (merge-hits lists)
        top (take limit (sort (fn [[_ x] [_ y]] (- 0 (compare x y))) merged))]
    (map (fn [[i score]] (assoc (index i) :score score)) top)))



(comment
  (def base (index/build-index index/base))
  (search base "Is the answer" 300)
  )
