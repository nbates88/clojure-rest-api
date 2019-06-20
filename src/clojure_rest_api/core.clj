(ns clojure-rest-api.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(def people-collection (atom []))

(defn getparameter [req pname] (get (:params req) pname))

(defn addperson [firstname surname]
  (swap! people-collection conj {:firstname (str/capitalize firstname)
                                 :surname (str/capitalize surname)}))

(defn addperson-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body (-> (let [p (partial getparameter req)]
               (str (json/write-str (addperson (p :firstname) (p :surname))))))})

(defn people-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str @people-collection))})

(defn hello-name [req] ;(3)
  :status 200
  :headers {"Content-Type" "text/html"}
  :body (->
         (pp/pprint req)
         (str "Hello " (:name (:params req)))))

(defn simple-body-page [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn request-example [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->> (pp/pprint req)
              (str "Request Object: " req))})

(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET  "/request" [] request-example)
  (GET "/hello" [] hello-name)
  (GET "/people/add" [] addperson-handler)
  (GET "/people" [] people-handler)
  (route/not-found "Error, page not found!"))

(defn -main
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))
