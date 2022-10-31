package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ApiCall extends Print {
    public static Device[] requestIdsConnected(String URL){
        JsonArray array = toJsonArrayURL(URL);
        Device[] devices = new Device[array.size()];

        JsonObject json;
        for(int i = 0; i < array.size(); i++){
            json = array.get(i).getAsJsonObject();
            devices[i] = new Device(json.get("id").getAsInt(), json.get("defaultName").getAsString(), json.get("online").getAsBoolean());
        }
        return devices;
    }
}
