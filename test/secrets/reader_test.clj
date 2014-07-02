(ns secrets.reader-test
  (:require [clojure.test :refer :all]
            [secrets.reader :as secrets]))

(deftest local-file-secrets-reader
  (let [reader (secrets.reader.EncryptedLocalFileSecretsReader. "./test-data/hello-world.gpg" "foo bar")]
    (is (= "Hello world!" (secrets.reader/read reader)))))
