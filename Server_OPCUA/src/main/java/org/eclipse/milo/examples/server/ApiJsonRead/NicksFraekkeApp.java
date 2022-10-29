package org.eclipse.milo.examples.server.ApiJsonRead;

public class NicksFraekkeApp extends NicksCreamCheese{

    public static void main(String[] args) {
        NicksFraekkeDevice[] devices = NicksFraekkeApiCalling.requestIdsConnected("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev");
        start(devices);
    }
}