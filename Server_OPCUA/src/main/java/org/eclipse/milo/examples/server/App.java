package org.eclipse.milo.examples.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class App{
    public static void main(String[] args) {
        try {
            // get URL content
            String a = "http://gw-2ab0.sandbox.tek.sdu.dk/ssapi/zb/dev";
            URL url = new URL(a);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                for(String i: inputLine.split("eui")){
                    System.out.println(i);
                }
            }
            br.close();
            System.out.println("Done");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}