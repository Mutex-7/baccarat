(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"public"}
 :dependencies '[[org.clojure/clojure "1.9.0"]             ;; CLJ
                 [org.clojure/clojurescript "1.10.339"]    ;; CLJS
                 [adzerk/boot-cljs "2.1.4"]                ;; CLJS compiler
                 [adzerk/boot-test "1.2.0"]                ;; CLJ testing
                 [adzerk/boot-reload "0.6.0"]              ;; Reloading
                 [adzerk/boot-cljs-repl "0.3.3"]           ;; CLJS repl
                 [com.cemerick/piggieback "0.2.1"]         ;; Needed for boot-cljs-repl
                 [weasel "0.7.0"]                          ;; Needed for boot-cljs-repl
                 [org.clojure/tools.nrepl "0.2.12"]        ;; Needed for boot-cljs-repl
                 [pandeiro/boot-http "0.8.3"]              ;; Web server
                 [compojure "1.6.1"]                       ;; Compojure
                 [crisptrutski/boot-cljs-test "0.3.4"]     ;; CLJS testing - fried
                 [reagent "0.8.1"]                         ;; Reagent
                 [javax.servlet/javax.servlet-api "3.1.0"] ;; ring/ring-core needs this in dev/testing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.3"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"] ;; Aw screw it already, all other ajax libs look broken.
                 [doo "0.1.8"] ;; cljs tests
                 [cljsjs/marked "0.3.5-1"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-test :refer [test]]
         '[pandeiro.boot-http :refer [serve]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :testbed :phantom
               :output-to "main.js"
               :namespaces '#{baccarat.engine-test}})

(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (merge-env! :source-paths dirs)
  identity)

(deftask tdd
  "Launch a TDD Environment"
  [e testbed ENGINE kw "The JS testbed engine (default phantom)"
   o output-to NAME str "The JS output file name for test (default main.js)"
   O optimizations LEVEL kw "The optimization level (default none)"
   t dirs PATH #{str} "test paths"
   k httpkit bool "Use http-kit web server instead of jetty"
   v verbose bool "Print which files have changed"
   p port PORT int "The web server port to listen on (default:3000)"
   n namespaces NS #{sym} "the set of namespace symbols to run tests in"]
  (let [dirs (or dirs (:test-dirs defaults))
        output-to (or output-to (:output-to defaults))
        testbed (or testbed (:testbed defaults))
        namespaces (or namespaces (:namespaces defaults))]
    (comp
     (serve :handler 'baccarat.handlers/app
            :resource-root "target"
            :reload true
            :httpkit httpkit
            :port port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (cljs)
     (test-cljs :js-env testbed
                :update-fs? true
                :optimizations optimizations
                :namespaces namespaces)
     (test :namespaces namespaces)
     (target :dir #{"target"}))))

(deftask dev
  "Launch reloadable environment."
  []
  (comp
   (serve :handler 'baccarat.handlers/app
          :resource-root "target"
          :reload true)
   (watch :verbose true)
   (reload :ws-host "localhost")
   (cljs-repl)
   (cljs)
   (target :dir #{"target"})))
