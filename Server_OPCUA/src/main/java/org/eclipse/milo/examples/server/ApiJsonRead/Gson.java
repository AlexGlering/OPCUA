package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;

public class Gson extends Print {

    ArrayList<Endpoints> getDataPoints(int id){
        JsonArray array = toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev");
        String key;
        ArrayList<Endpoints> arrayL = new ArrayList<>();
        for (JsonElement j: array){
            key = j.getAsJsonObject().get("key").toString();
            key = key.substring(1, key.length()-1);
            Endpoints t = new Endpoints(key, toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev/"+key+"/data"));
            arrayL.add(t);

        }
        return arrayL;
    }
}
