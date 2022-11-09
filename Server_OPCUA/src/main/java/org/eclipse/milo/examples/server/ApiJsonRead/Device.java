package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;

public class Device extends Print{
    private final int id;
    private final String defaultName;
    private Boolean onlineStatus;
    ArrayList<Endpoints> nicksfraekkeEndPoints;

    public Device(int id, String defaultName, Boolean onlineStatus){
        this.id = id;
        this.defaultName = defaultName;
        this.onlineStatus = onlineStatus;
        nicksfraekkeEndPoints = new ArrayList<>();
        if(getOnlineStatus()){
            JsonArray array = toJsonArrayURL("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev/"+id+"/ldev");
            String key;
            ArrayList<Endpoints> arrayL = new ArrayList<>();
            for (JsonElement j: array){
                key = j.getAsJsonObject().get("key").toString();
                key = key.substring(1, key.length()-1);
                Endpoints t = new Endpoints(id, key);
                arrayL.add(t);

            }

            nicksfraekkeEndPoints = arrayL;
        }
    }

    public int getId() {
        return id;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String specialID(){
        return getId()+"-"+getDefaultName().replace(" ", "_");
    }

    public Boolean getOnlineStatus() {
        return onlineStatus;
    }

    public ArrayList<Endpoints> getNicksfraekkeEndPoints(){
        return nicksfraekkeEndPoints;
    }

    public ArrayList<String> getKeys(){
        ArrayList<String> keyValue = new ArrayList<>();
        for (Endpoints i: getNicksfraekkeEndPoints()){
            keyValue.add(i.getKey());
        }
        return keyValue;
    }

    public Endpoints searchWithKey(String k){
        for (Endpoints i: getNicksfraekkeEndPoints()){
            if(i.getKey().equals(k)){
                return i;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        print("----");
        return getDefaultName() + " - ID: " + getId() + " | Online Status: "+getOnlineStatus();
    }
}
