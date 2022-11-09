package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;

import static org.eclipse.milo.examples.server.ApiJsonRead.Print.toJsonArrayURL;

public class Endpoints {
    private final String key;
    private final JsonArray endpoints;

    public Endpoints(int id, String key){
        this.key = key;
        this.endpoints = toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev/"+key+"/data");
    }

    public String getKey() {
        return key;
    }

    public JsonArray getEndpoints() {
        return endpoints;
    }
}
