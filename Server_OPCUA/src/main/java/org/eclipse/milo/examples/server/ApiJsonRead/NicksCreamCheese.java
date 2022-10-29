package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonElement;

import java.util.Map;

public class NicksCreamCheese extends NicksSecretSauce{
    //eksempel på print
    public static void start(NicksFraekkeDevice[] devices){
        for(NicksFraekkeDevice d: devices){
            print(d.specialID());
            for (NicksfraekkeEndPoints n: d.getNicksfraekkeEndPoints()){
                print("     "+n.getKey());
                for (JsonElement j: n.getEndpoints()){
                    for(Map.Entry<String, JsonElement> k :j.getAsJsonObject().entrySet()){
                        print("         "+k.getKey()+" = "+k.getValue());
                    }
                    print("");
                }
            }
            print("");
        }
    }
}
