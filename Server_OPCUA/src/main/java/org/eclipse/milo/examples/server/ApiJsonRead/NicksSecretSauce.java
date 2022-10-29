package org.eclipse.milo.examples.server.ApiJsonRead;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NicksSecretSauce {
    public static void print(Object i){
        System.out.println(i.toString());
    }

    private static String urlRequest(String URL){
        try {
            java.net.URL url = new URL(URL);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine = br.readLine();
            br.close();
            return inputLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "URL ERROR";
    }

    public static JsonArray toJsonArrayURL(String URL){
        return new Gson().fromJson(urlRequest(URL),JsonArray.class);
    }
}

