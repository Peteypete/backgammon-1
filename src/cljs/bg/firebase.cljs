(ns bg.firebase
  (:require [cljsjs.firebase]
            [re-frame.core :as rf]
            [clojure.string :as string]
            [cljs.reader :as reader]))

(defn init []
  (js/firebase.initializeApp
   #js { :apiKey "AIzaSyDMU_888Ctmkz44fveC1j44cQuXXVsCmbU",
         :authDomain "backgammon-b431c.firebaseapp.com",
         :databaseURL "https://backgammon-b431c.firebaseio.com",
         :projectId "backgammon-b431c",
         :storageBucket "backgammon-b431c.appspot.com",
         :messagingSenderId "1010344962139"}))

(defn db-ref [path]
  (.ref (js/firebase.database) (string/join "/" path)))

(defn save! [ref data]
  (.set ref (pr-str data)))

(defn subscribe [path]
  (.on path "value"
       (fn [snapshot]
         (when-let [d (.val snapshot)]
           (rf/dispatch [:sync (reader/read-string d)])))))

(rf/reg-fx
 :firebase/subscribe
 (fn [{:keys [game-id default]}]
   (let [ref (db-ref [game-id])]
     (.once ref "value"
            (fn received [snapshot]
              (subscribe ref)
              (if-let [data (.val snapshot)]
                (rf/dispatch [:sync (reader/read-string data)])
                (do (save! ref default)
                    (rf/dispatch [:sync default]))))))))

(rf/reg-fx
 :firebase/set
 (fn [{:keys [game-id data]}]
   (save! (db-ref [game-id]) data)))
