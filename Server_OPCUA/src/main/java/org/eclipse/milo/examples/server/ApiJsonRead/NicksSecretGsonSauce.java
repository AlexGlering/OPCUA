package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NicksSecretGsonSauce extends NicksSecretSauce{

    ArrayList<NicksfraekkeEndPoints> getDataPoints(int id){
        JsonArray array = toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev");
        String key;
        ArrayList<NicksfraekkeEndPoints> arrayL = new ArrayList<>();
        for (JsonElement j: array){
            key = j.getAsJsonObject().get("key").toString();
            key = key.substring(1, key.length()-1);
            NicksfraekkeEndPoints t = new NicksfraekkeEndPoints(key, toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev/"+key+"/data"));
            arrayL.add(t);

        }
        return arrayL;
    }
}
