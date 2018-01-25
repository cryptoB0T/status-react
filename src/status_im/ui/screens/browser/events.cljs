(ns status-im.ui.screens.browser.events
  (:require status-im.ui.screens.browser.navigation
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.browser :as browser-store]
            [re-frame.core :as re-frame]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]))

(re-frame/reg-cofx
  :all-stored-browsers
  (fn [cofx _]
    (assoc cofx :all-stored-browsers (browser-store/get-all))))

(handlers/register-handler-fx
  :initialize-browsers
  [(re-frame/inject-cofx :all-stored-browsers)]
  (fn [{:keys [db all-stored-browsers]} _]
    (let [{:accounts/keys [account-creation?]} db]
      (when-not account-creation?
        (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
          {:db (assoc db :browser/browsers browsers)})))))

(re-frame/reg-fx
  :save-browser
  (fn [browser]
    (browser-store/save browser)))

(defn get-new-browser [browser now]
  (cond-> browser
          true
          (assoc :timestamp now)
          (not (:browser-id browser))
          (assoc :browser-id (random/id))
          (not (:name browser))
          (assoc :name (i18n/label :t/browser))))

(defn add-browser-fx [{:keys [db now] :as cofx} browser]
  (let [new-browser (get-new-browser browser now)]
    {:db           (update-in db [:browser/browsers (:browser-id new-browser)] merge new-browser)
     :save-browser new-browser}))

(handlers/register-handler-fx
  :open-dapp-in-browser
  [re-frame/trim-v]
  (fn [cofx [{:keys [name dapp-url] :as contact}]]
    (let [browser {:browser-id (:whisper-identity contact)
                   :name       name
                   :dapp?      true
                   :url        dapp-url
                   :contact    (:whisper-identity contact)}]
      (merge (add-browser-fx cofx browser)
             {:dispatch [:navigate-to :browser (:browser-id browser)]}))))

(handlers/register-handler-fx
  :open-browser
  [re-frame/trim-v]
  (fn [cofx [browser]]
    (merge (add-browser-fx cofx browser)
           {:dispatch [:navigate-to :browser (:browser-id browser)]})))

(handlers/register-handler-fx
  :update-browser
  [re-frame/trim-v]
  (fn [{:keys [db now] :as cofx} [browser]]
    (let [new-browser (get-new-browser browser now)]
      (-> (add-browser-fx cofx new-browser)
          (update-in [:db :browser/options] #(assoc % :browser-id (:browser-id new-browser)))))))

(handlers/register-handler-fx
  :update-browser-options
  [re-frame/trim-v]
  (fn [{:keys [db now] :as cofx} [options]]
    {:db (update db :browser/options merge options)}))