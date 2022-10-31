package org.eclipse.milo.examples.server.ApiJsonRead;

public class App extends Loop {

    public static void main(String[] args) {
        Device[] devices = ApiCall.requestIdsConnected("http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev");
        start(devices);
    }
}