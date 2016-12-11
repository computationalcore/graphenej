package de.bitsharesmunich.graphenej.interfaces;

import com.google.gson.JsonElement;

import java.io.Serializable;

/**
 * Interface to be implemented by any entity for which makes sense to
 * have a JSON-formatted string and object representation.
 */
public interface JsonSerializable extends Serializable {

    String toJsonString();

    JsonElement toJsonObject();
}
