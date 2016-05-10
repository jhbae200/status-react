(ns syng-im.models.commands
  (:require [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id]]
            [syng-im.components.styles :refer [color-blue
                                               color-dark-mint]]
            [syng-im.utils.utils :refer [log toast]]))

;; todo delete
(def commands [{:command      :money
                :text         "!money"
                :description  "Send money"
                :color        color-dark-mint
                :request-icon {:uri "icon_lock_white"}
                :icon         {:uri "icon_lock_gray"}
                :suggestion   true}
               {:command     :location
                :text        "!location"
                :description "Send location"
                :color       "#9a5dcf"
                :suggestion  true}
               {:command      :phone
                :text         "!phone"
                :description  "Send phone number"
                :color        color-dark-mint
                :request-text "Phone number request"
                :suggestion   true
                :handler      #(dispatch [:sign-up %])}
               {:command      :confirmation-code
                :text         "!confirmationCode"
                :description  "Send confirmation code"
                :request-text "Confirmation code request"
                :color        color-blue
                :request-icon {:uri "icon_lock_white"}
                :icon         {:uri "icon_lock_gray"}
                :suggestion   true
                :handler      #(dispatch [:sign-up-confirm %])}
               {:command     :send
                :text        "!send"
                :description "Send location"
                :color       "#9a5dcf"
                :suggestion  true}
               {:command     :request
                :text        "!request"
                :description "Send request"
                :color       "#48ba30"
                :suggestion  true}
               {:command      :keypair-password
                :text         "!keypairPassword"
                :description  ""
                :color        color-blue
                :request-icon {:uri "icon_lock_white"}
                :icon         {:uri "icon_lock_gray"}
                :suggestion   false
                :handler      #(dispatch [:save-password %])}
               {:command     :help
                :text        "!help"
                :description "Help"
                :color       "#9a5dcf"
                :suggestion  true}])

(defn get-commands [db]
  ;; todo: temp. must be '(get db :commands)'
  ;; (get db :commands)
  commands)

(defn set-commands [db commands]
  (assoc db :commands commands))

;; todo delete
(def suggestions (filterv :suggestion commands))

(defn get-command [db command-key]
  (first (filter #(= command-key (:command %)) (get-commands db))))

(defn find-command [commands command-key]
  (first (filter #(= command-key (:command %)) commands)))

(defn get-chat-command-content [db]
  (get-in db (db/chat-command-content-path (current-chat-id db))))

(defn set-chat-command-content [db content]
  (assoc-in db
            [:chats (get-in db db/current-chat-id-path) :command-input :content]
            content))

(defn get-chat-command [db]
  (get-in db (db/chat-command-path (current-chat-id db))))

(defn set-response-chat-command [db msg-id command-key]
  (let [chat-id (current-chat-id db)]
    (-> db
        (assoc-in [:chats chat-id :command-input :content] nil)
        (assoc-in [:chats chat-id :command-input :command]
                  (get-command db command-key))
        (assoc-in [:chats chat-id :command-input :to-msg-id] msg-id))))

(defn set-chat-command [db command-key]
  (set-response-chat-command db nil command-key))

(defn get-chat-command-to-msg-id [db]
  (get-in db (db/chat-command-to-msg-id-path (current-chat-id db))))

(defn stage-command [db command-info]
  (update-in db (db/chat-staged-commands-path (current-chat-id db))
             (fn [staged-commands]
               (if staged-commands
                 (conj staged-commands command-info)
                 [command-info]))))

(defn unstage-command [db staged-command]
  (update-in db (db/chat-staged-commands-path (current-chat-id db))
             (fn [staged-commands]
               (filterv #(not= % staged-command) staged-commands))))

(defn clear-staged-commands [db]
  (assoc-in db (db/chat-staged-commands-path (current-chat-id db)) []))

(defn get-chat-command-request [db]
  (get-in db (db/chat-command-request-path (current-chat-id db)
                                           (get-chat-command-to-msg-id db))))

(defn set-chat-command-request [db msg-id handler]
  (update-in db (db/chat-command-requests-path (current-chat-id db))
             #(assoc % msg-id handler)))

(defn parse-command-msg-content [commands content]
  (update content :command #(find-command commands (keyword %))))

(defn parse-command-request [commands content]
  (update content :command #(find-command commands (keyword %))))