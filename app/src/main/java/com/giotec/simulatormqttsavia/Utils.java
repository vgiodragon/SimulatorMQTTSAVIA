package com.giotec.simulatormqttsavia;

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

    public static ArrayList<MensajePublicar> readAllJSONS(){
        ArrayList<MensajePublicar> mensajes = new ArrayList<>();;
        Log.d(TAG,"Leyendo AllData");
        JSONArray jsonsArray = new JSONArray();
        //Read From Internal
        String root = Environment.getExternalStorageDirectory().toString();
        //File myDir = new File(root + "/Savia");

        File file = new File(root + "/Savia","Alldata.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {

                try {
                    JSONObject json = new JSONObject(line);
                    mensajes.add(
                            new MensajePublicar(
                                    json.getString("imei"),
                                    json.getString("imsi"),
                                    json.getString("ses_id"),
                                    json.getString("mac"),
                                    json.getString("relation"),
                                    json.getString("type")
                            ));
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
}
