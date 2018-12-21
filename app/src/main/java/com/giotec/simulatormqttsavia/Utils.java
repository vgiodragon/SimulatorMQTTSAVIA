package com.giotec.simulatormqttsavia;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {
    private final static String TAG = "GIODEBUG_UTILS";
    //private final static String ip = "192.168.52.232";
    private final static String ip = "190.119.192.232";
    private final static String userMQTT = "Raspberry";
    private final static String passMQTT = "yrrebpsaR";

    public static ArrayList<MensajePublicar> readAllJSONS(Context ctx){
        ArrayList<MensajePublicar> mensajes = new ArrayList<>();;
        Log.d(TAG,"Leyendo AllData");
        String root = Environment.getExternalStorageDirectory().toString();

        File file = new File(root + "/Savia","Alldata.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int leidos =0;
            while ((line = br.readLine()) != null && leidos <35) {

                try {
                    JSONObject json = new JSONObject(line);
                    mensajes.add(
                            new MensajePublicar(
                                    json.getString("imei"),
                                    json.getString("imsi"),
                                    json.getString("ses_id"),
                                    json.getString("mac"),
                                    json.getString("relation"),
                                    json.getString("type"),
                                    ctx
                            ));
                    leidos++;
                } catch (JSONException e) {
                    Log.d(TAG,"Cant: "+mensajes.size()+" JSONException "+e.toString());
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"Cantidad de leidos "+mensajes.size());
            br.close();
        }
        catch (IOException e) {
            Log.d(TAG,"IOException "+e.toString());
            //You'll need to add proper error handling here
        }
        return mensajes;
    }

    public static String getIp() {
        return ip;
    }

    public static String getUserMQTT() {
        return userMQTT;
    }

    public static String getPassMQTT() {
        return passMQTT;
    }


}
