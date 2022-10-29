package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;

public class NicksfraekkeEndPoints {
    private final String key;
    private final JsonArray endpoints;

    public NicksfraekkeEndPoints(String key, JsonArray endpoints){
        this.key = key;
        this.endpoints = endpoints;
    }

    public String getKey() {
        return key;
    }

    public JsonArray getEndpoints() {
        return endpoints;
    }
}
