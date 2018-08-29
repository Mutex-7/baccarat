(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"public"}
 :dependencies '[[org.clojure/clojure "1.8.0"]                       ;; CLJ
                 [org.clojure/clojurescript "1.10.339"]              ;; CLJS
                 [adzerk/boot-cljs "2.1.4"]                          ;; CLJS compiler
                 [adzerk/boot-test "1.2.0"]                          ;; CLJ testing
                 [adzerk/boot-reload "0.6.0"]                        ;; Reloading
                 [adzerk/boot-cljs-repl "0.3.3"]                     ;; CLJS repl
                 [com.cemerick/piggieback "0.2.1" :scope "test"]     ;; Needed for boot-cljs-repl
                 [weasel "0.7.0" :scope "test"]                      ;; Needed for boot-cljs-repl
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]    ;; Needed for boot-cljs-repl
                 [pandeiro/boot-http "0.8.3"]                        ;; Web server
                 [compojure "1.6.1"]                                 ;; Compojure
                 [crisptrutski/boot-cljs-test "0.3.4" :scope "test"] ;; CLJS testing
                 [reagent "0.8.1"]                                   ;; Reagent
                 [javax.servlet/javax.servlet-api "3.1.0"]           ;; ring/ring-core needs this in dev/testing
                 [cljsjs/marked "0.3.5-1"]])                         ;; Markdown helper

;; In the future, adjust some nrepl stuff for cider?
;; going to need a js engine for tests. find one.

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-test :refer [test]]
         '[pandeiro.boot-http :refer [serve]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :output-to "main.js"
               :testbed :phantom
               :namespaces '#{baccarat.validators}})

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
     (serve :handler 'baccarat.core/app
            :resource-root "target"
            :reload true
            :httpkit httpkit
            :port port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file output-to
                :js-env testbed
                :update-fs? true
                :optimizations optimizations
                :namespaces namespaces)
     (test :namespaces namespaces)
     (target :dir #{"target"}))))

(deftask dev
  "Launch reloadable environment."
  []
  (comp
   (serve :handler 'baccarat.core/app
          :resource-root "target"
          :reload true)
   (watch)
   (reload)
   (cljs-repl)
   (cljs)
   (target :dir #{"target"})))
