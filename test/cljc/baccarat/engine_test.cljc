(ns baccarat.engine-test
  (:require [baccarat.engine :as engine :refer [num->value
                                                num->card
                                                dealer-draw?
                                                shoe-check
                                                score
                                                update-money
                                                new-bet
                                                new-record]]
            #?(:clj [clojure.test :refer [deftest are testing is]]
               :cljs [cljs.test :refer [deftest are testing is]])))

(deftest num->value-test
  (testing "Card number to value conversion"
    (testing "/ Happy path" ;; (num % 13) + 1
      (are [expected actual] (= expected actual)
        1 (num->value 0)
        2 (num->value 1)
        3 (num->value 2)
        9 (num->value 8)
        0 (num->value 9) ;; 10
        0 (num->value 10) ;; J
        0 (num->value 11) ;; Q
        0 (num->value 12) ;; K
        1 (num->value 13) ;; A/1, wrap around test
        2 (num->value 14) ;; 2, wrap around test
        6 (num->value 200))))) ;; test multiple wrap around

;; TODO more tests here
(deftest num->card-test
  (testing "Card number to card face conversion"
    (testing "/ Happy path"
      (are [expected actual] (= expected actual)
        "A♠" (num->card 0)))))

(deftest dealer-draw-test
  (testing "dealer-draw? test"
    (testing "/ No draw boundary"
      (are [expected actual] (= expected actual)
        false (dealer-draw? 4 0)
        false (dealer-draw? 4 1)
        false (dealer-draw? 5 2)
        false (dealer-draw? 5 3)
        false (dealer-draw? 6 4)
        false (dealer-draw? 6 5)
        false (dealer-draw? 7 6)
        false (dealer-draw? 7 7)
        false (dealer-draw? 3 8)
        false (dealer-draw? 4 9)))
    (testing "/ Draw boundary"
      (are [expected actual] (= expected actual)
        true (dealer-draw? 3 0)
        true (dealer-draw? 3 1)
        true (dealer-draw? 4 2)
        true (dealer-draw? 4 3)
        true (dealer-draw? 5 4)
        true (dealer-draw? 5 5)
        true (dealer-draw? 6 6)
        true (dealer-draw? 6 7)
        true (dealer-draw? 2 8)
        true (dealer-draw? 3 9)))))

(deftest shoe-check-test
  (testing "Shoe refil boundary test"
    (testing "/ Needs refill"
      (is (> (count (shoe-check '(1 2 3 4 5 6))) 7)))
    (testing "/ Doesn't need refill"
      (is (= 7 (count (shoe-check '(1 2 3 4 5 6 7))))))))

(deftest score-test
  (testing "Score test"
    (are [expected actual] (= expected actual)
      0 (score '[4 4])
      0 (score '[0 0 7])
      1 (score '[8 1])
      2 (score '[3 7])
      3 (score '[5 6])
      4 (score '[0 2 9])
      5 (score '[4 8 0])
      6 (score '[8 3 2])
      7 (score '[3 2])
      8 (score '[2 1 2])
      9 (score '[1 6]))))

(deftest update-money-test
  (testing "Update money test"
    (let [money 500]
      (testing "/ Successful bets"
        (testing "/ Player"
          (is (= 510 (update-money money (new-record '[8] '[1] (new-bet 10 0 0 0 0))))))
        (testing "/ Dealer"
          (is (= 510 (update-money money (new-record '[1] '[8] (new-bet 0 10 0 0 0))))))
        (testing "/ Tie"
          (is (= 580 (update-money money (new-record '[5] '[5] (new-bet 0 0 10 0 0))))))
        (testing "/ Panda insurance"
          (is (= 750 (update-money money (new-record '[7] '[5] (new-bet 0 0 0 10 0))))))
        (testing "/ Dragon insurance"
          (is (= 900 (update-money money (new-record '[5] '[6] (new-bet 0 0 0 0 10)))))))
      (testing "/ Unsuccessful bets"
        (testing "/ Player"
          (is (= 490 (update-money money (new-record '[1] '[8] (new-bet 10 0 0 0 0))))))
        (testing "/ Dealer"
          (is (= 490 (update-money money (new-record '[8] '[1] (new-bet 0 10 0 0 0))))))
        (testing "/ Tie"
          (is (= 490 (update-money money (new-record '[5] '[6] (new-bet 0 0 10 0 0))))))
        (testing "/ Panda insurance"
          (is (= 490 (update-money money (new-record '[2] '[2] (new-bet 0 0 0 10 0))))))
        (testing "/ Dragon insurance"
          (is (= 490 (update-money money (new-record '[2] '[2] (new-bet 0 0 0 0 10))))))))))
