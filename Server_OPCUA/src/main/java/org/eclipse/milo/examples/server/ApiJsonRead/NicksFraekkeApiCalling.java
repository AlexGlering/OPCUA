package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NicksFraekkeApiCalling extends NicksSecretSauce{
    public static NicksFraekkeDevice[] requestIdsConnected(String URL){
        JsonArray array = toJsonArrayURL(URL);
        NicksFraekkeDevice[] devices = new NicksFraekkeDevice[array.size()];

        JsonObject json;
        for(int i = 0; i < array.size(); i++){
            json = array.get(i).getAsJsonObject();
            devices[i] = new NicksFraekkeDevice(json.get("id").getAsInt(), json.get("defaultName").getAsString(), json.get("online").getAsBoolean());
        }
        return devices;
    }
}
