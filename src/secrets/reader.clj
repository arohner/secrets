(ns secrets.reader
  (:refer-clojure :exclude (read))
  (:require [me.raynes.conch :as c]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defprotocol SecretsReader
  (read [this] "Returns a secrets object"))

(defrecord EncryptedLocalFileSecretsReader [path passphrase]
  SecretsReader
  (read [this]
    (c/with-programs [gpg]
      (gpg "--batch" "--no-tty" "--decrypt" "--passphrase-fd" "0" path {:in (format "%s\n" passphrase)}))))

(defmacro with-temp-file
  "creates a temp file, binds local-name to the path of the temp
  file. runs body and deletes the file"
  [local-name & body]
  `(let [file# (java.io.File/createTempFile (str (quote ~local-name)) ".txt")
         ~local-name (str file#)]
     (try
       ~@body
       (finally
         (.delete file#)))))

(defrecord EncryptedResourceReader [resource passphrase]
  ;; load secrets from a .jar resource.
  SecretsReader
  (read [this]
    (with-temp-file tmp-path
      (with-open [resource-stream (.openStream resource)]
        (io/copy resource-stream (java.io.File. tmp-path))
        (read (->EncryptedLocalFileSecretsReader tmp-path passphrase))))))
