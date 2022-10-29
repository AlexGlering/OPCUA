package org.eclipse.milo.examples.server.ApiJsonRead;

import java.util.ArrayList;

public class NicksFraekkeDevice extends NicksSecretGsonSauce {
    private final int id;
    private final String defaultName;
    private Boolean onlineStatus;
    ArrayList<NicksfraekkeEndPoints> nicksfraekkeEndPoints;

    public NicksFraekkeDevice(int id, String defaultName, Boolean onlineStatus){
        this.id = id;
        this.defaultName = defaultName;
        this.onlineStatus = onlineStatus;
        nicksfraekkeEndPoints = new ArrayList<>();
        if(getOnlineStatus()){
            nicksfraekkeEndPoints = getDataPoints(getId());
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

    public ArrayList<NicksfraekkeEndPoints> getNicksfraekkeEndPoints(){
        return nicksfraekkeEndPoints;
    }

    public ArrayList<String> getKeys(){
        ArrayList<String> keyValue = new ArrayList<>();
        for (NicksfraekkeEndPoints i: getNicksfraekkeEndPoints()){
            keyValue.add(i.getKey());
        }
        return keyValue;
    }

    public NicksfraekkeEndPoints searchWithKey(String k){
        for (NicksfraekkeEndPoints i: getNicksfraekkeEndPoints()){
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
