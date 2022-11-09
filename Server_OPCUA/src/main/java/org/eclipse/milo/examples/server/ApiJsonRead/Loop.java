package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.Map;

public class Loop extends Print {
    //eksempel på print
    public static void start(Device[] devices){
        for(Device d: devices){
            print(d.specialID());
            for (Endpoints n: d.getNicksfraekkeEndPoints()){
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
