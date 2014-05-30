(ns juno.resource
  (:require 
    [clojure.core :as core]            
    [schema.core :as s]
    [plumbing.core :as p :include-macros true]
    [plumbing.fnk.pfnk :as pfnk :include-macros true]
    [plumbing.graph :as graph :include-macros true]
    [plumbing.map :as map]))

(defn resource-transform [g]
  (assoc (map/map-leaves
          (fn [node-fn]
            (pfnk/fn->fnk
             (fn [m]
               (let [r (node-fn m)]
                 (when-let [shutdown (:shutdown r)]
                   (swap! (:shutdown-hooks m) conj shutdown))
                 (assert (contains? r :resource))
                 (:resource r)))
             [(assoc (pfnk/input-schema node-fn) :shutdown-hooks s/Any)
              (pfnk/output-schema node-fn)]))
          g)
    :shutdown-hooks (p/fnk [] (atom nil))))