(ns text-only-gatling-report.core
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:gen-class))

(def stats (json/read-str
            (slurp "./resources/stats.json")
            :key-fn keyword))

(defn ^:private println-indented
  [level text]
  (println
   (format "%s%s"
           (str/join "" (repeat level " "))
           text)))

(def ^:private unwanted-keys
  #{:pathFormatted
    :path
    :htmlName})

(defn ^:private remove-unwanted-entries
  [coll]
  (filter (fn [[k _]]
            (not (some #{k} unwanted-keys))) coll))

(defn ^:priavte transform-entry
  [k v]
  (cond
    (str/starts-with? (name k) "req_")
    [(name (:name v)) v]

    (= k :stats)
    ["sumary" v]

    (= k :contents)
    ["details" v]

    (= k :percentiles1)
    ["percentiles1 (usually p50)" v]

    (= k :percentiles2)
    ["percentiles2 (usually p75)" v]

    (= k :percentiles3)
    ["percentiles3 (usually p90)" v]

    (= k :percentiles4)
    ["percentiles4 (usually p99)" v]

    :else
    [(name k) v]))

(defn print-text-report! [[k v] level]
  (if (map? v)
    (do (println-indented level (first (transform-entry k v)))
        (mapv #(print-text-report! % (inc level)) (remove-unwanted-entries v)))
    (println-indented level (apply format "%s: %s" (transform-entry k v)))))

(mapv #(print-text-report! % 0) (remove-unwanted-entries stats))


(defn -main
  [& args]
  (doseq [arg args]
    (println arg)))
