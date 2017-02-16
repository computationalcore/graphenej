package de.bitsharesmunich.graphenej.interfaces;

import java.util.List;

/**
 * Interface to be implemented by any class that hosts a SubscriptionResponseDeserializer and wants to
 * expose an interface for its management of its listeners.
 *
 * Created by nelson on 1/30/17.
 */
public interface SubscriptionHub {

    /**
     * Adds a given listener to the list of subscription listeners.
     * @param listener: The SubscriptionListener to add.
     */
    void addSubscriptionListener(SubscriptionListener listener);

    /**
     * Removes a given listener from the list.
     * @param listener: The SubscriptionListener to remove.
     */
    void removeSubscriptionListener(SubscriptionListener listener);

    /**
     * Retrieves a list of all subscription listeners.
     * @return
     */
    List<SubscriptionListener> getSubscriptionListeners();
}