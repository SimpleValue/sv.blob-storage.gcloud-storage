(ns sv.blob-storage.gcloud-storage
  (:require [sv.gcloud.client :as c]
            [clj-http.util :as util]
            [sv.gcloud.storage.client :as sc]
            [sv.blob-storage :as bs]))

(defn- uuid []
  (java.util.UUID/randomUUID))

(defn create-client []
  (c/create-client
   {:scopes ["https://www.googleapis.com/auth/devstorage.read_write"]}))

(defn- path [config id]
  (str (:prefix config) id))

(deftype GcloudStorage [config]
  bs/BlobStorage
  (store [this in]
    (let [id (uuid)]
      (with-open [in in]
        ((:client config)
         (sc/upload-request
          {:bucket (:bucket config)
           :path (path config id)
           :content in})))
      id))
  (retrieve [this id]
    (sc/stream-object
     (:client config)
     {:bucket (:bucket config)
      :path (path config id)}))
  (exists [this id]
    (boolean
     (sc/get-object-info
      (:client config)
      {:bucket (:bucket config)
       :path (path config id)})))
  (delete [this id]
    (sc/delete-object
     (:client config)
     {:bucket (:bucket config)
      :path (path config id)})
    true))

(defn gcloud-storage [config]
  (GcloudStorage.
   (update-in config [:client] #(or % (create-client)))))
