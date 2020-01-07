(ns grube-api.json
  (:require [cheshire.core :as cheshire]
            [cheshire.factory :as cheshire.format]
            [cheshire.generate :refer [add-encoder]]
            [clj-time.coerce :as tc]
            [taoensso.sente.interfaces :as sente.interfaces])
  (:import com.fasterxml.jackson.core.JsonGenerator
           java.text.SimpleDateFormat
           java.util.SimpleTimeZone
           org.joda.time.DateTime))

(defn cheshire-add-jodatime-encoder! []
  (add-encoder
   org.joda.time.DateTime
   (fn [^org.joda.time.DateTime d ^JsonGenerator jg]
     (let [sdf (SimpleDateFormat. cheshire.factory/default-date-format)]
       (.setTimeZone sdf (SimpleTimeZone. 0 "UTC"))
       (.writeString jg (.format sdf (tc/to-date d)))))))

(cheshire-add-jodatime-encoder!)

(defn ^:private keywordize
  [value]
  (if (string? value)
    (keyword value)
    value))

(defn ^:private serialize
  [object]
  (cheshire/encode object))

(defn ^:private deserialize
  [string]
  (let [object (->> string
                    cheshire/decode
                    (map keywordize)
                    vec)]
    object))

(deftype JsonTransitPacker []
  sente.interfaces/IPacker
  (pack   [_ object] (serialize object))
  (unpack [_ string] (deserialize string)))
