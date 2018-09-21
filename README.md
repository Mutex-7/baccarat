# baccarat

This started out as a baccarat simulator, then I just figured "heck, why not make this playable?".
This was also a good excuse to learn some webdev stuff with Clojurescript and React.
I'm not done with this one just yet, am still in the process of adding the score boards.
After that, I'll work on something more visually appealing then simple text.

## Prerequisites

You will need [Clojure][] 1.9.0 or above installed.
You will need [Boot][] 2.8.1 installed.

[boot]: http://boot-clj.com/
[clojure]: https://clojure.org/

## Running

To start a web server for the application, run:

    boot dev

To start a web server along with clojure and clojurescript tests, run:

    boot tdd

Both of these environments try to include hot reloading of as much as possible. Live reloading of browser content is curently broken.
