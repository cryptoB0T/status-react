(ns status-im.ui.components.list-selection
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.dialog :as dialog]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- open-share [content]
  (when (or (:message content)
            (:url content))
    (.share react/sharing (clj->js content))))

(defn share-options [text]
  [{:label  (i18n/label :t/sharing-copy-to-clipboard)
    :action #(react/copy-to-clipboard text)}
   {:label  (i18n/label :t/sharing-share)
    :action #(open-share {:message text})}])

(defn show [options]
  (if platform/ios?
    (action-sheet/show options)
    (dialog/show options)))

(defn share [text dialog-title]
  (show {:title       dialog-title
         :options     (share-options text)
         :cancel-text (i18n/label :t/sharing-cancel)}))

(defn browse [browse-command link]
  (show {:title       (i18n/label :t/browsing-title)
         :options     [{:label "@browse"}
                       :action (do
                                 (re-frame/dispatch [:select-chat-input-command
                                                     (assoc browse-command :prefill [link])
                                                     nil
                                                     true])
                                 (js/setTimeout #(re-frame/dispatch [:send-current-message]) 100))
                       {:label  (i18n/label :t/browsing-open-in-web-browser)
                        :action (.openURL react/linking link)}]
         :cancel-text (i18n/label :t/browsing-cancel)}))


(defn share-or-open-map [address lat lng]
  (show {:title       (i18n/label :t/location)
         :options     [{:label  (i18n/label :t/sharing-copy-to-clipboard-address)
                        :action (react/copy-to-clipboard address)}
                       {:label  (i18n/label :t/sharing-copy-to-clipboard-coordinates)
                        :action (react/copy-to-clipboard (str lng "," lat))}
                       {:label  (i18n/label :t/open-map)
                        :action (.openURL react/linking (if platform/ios?
                                                          (str "http://maps.apple.com/?ll=" lng "," lat)
                                                          (str "geo:" lng "," lat)))}]
         :cancel-text (i18n/label :t/cancel)}))
