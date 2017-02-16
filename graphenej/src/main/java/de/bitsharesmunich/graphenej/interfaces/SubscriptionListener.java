package de.bitsharesmunich.graphenej.interfaces;

import de.bitsharesmunich.graphenej.ObjectType;
import de.bitsharesmunich.graphenej.models.SubscriptionResponse;

/**
 * Generic interface that must be implemented by any class that wants to be informed about a specific
 * event notification.
 *
 * Created by nelson on 1/26/17.
 */
public interface SubscriptionListener {

    /**
     * Every subscription listener must implement a method that returns the type of object it is
     * interested in.
     * @return: Instance of the ObjectType enum class.
     */
    ObjectType getInterestObjectType();


    /**
     * Method called whenever there is an update that might be of interest for this listener.
     * Note however that the objects returned inside the SubscriptionResponse are not guaranteed to be
     * only of the object type requested by this class in the getInterestObjectType.
     *
     * @param response: SubscriptionResponse instance, which may or may not contain an object of interest.
     */
    void onSubscriptionUpdate(SubscriptionResponse response);
}
