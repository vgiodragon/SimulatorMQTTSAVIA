package com.giotec.simulatormqttsavia;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "GIODEBUG";
    ArrayList<MensajePublicar> mensajes;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    int PERMISSION_ALL = 5;
    private final int code_request=1234;
    Spinner spNUsers;
    Spinner sptest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasPermissions(this, PERMISSIONS))
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

        mensajes = Utils.readAllJSONS(getApplicationContext());
        spNUsers = findViewById(R.id.spinner3);
        sptest = findViewById(R.id.spinner4);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case code_request:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "COARSE LOCATION permitido", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "COARSE LOCATION no permitido", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void Start (View view) {
        int total = Integer.parseInt(spNUsers.getSelectedItem().toString());
        int test = Integer.parseInt(spNUsers.getSelectedItem().toString());
        Log.d(TAG,"Start ! "+total+"_"+test);

        new PrincipalTask(total,test,0).execute();

    }

    private class PrincipalTask extends AsyncTask<Void, Void, Void> {
        int total;
        int test;
        int tanda;

        public PrincipalTask(int total, int test, int tanda) {
            this.total = total;
            this.test = test;
            this.tanda=tanda;
            Log.d(TAG,"Ready to send tandas !");
            new Start_New_Thread(tanda,total).execute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
             try {
                sleep(60000);
             } catch (InterruptedException e) {
                e.printStackTrace();
             }

             return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(tanda<15)
                new PrincipalTask(total,test,tanda+1).execute();
            else
                Toast.makeText(getApplicationContext(),"Listo enviado los 15 ",
                        Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }


    private class Start_New_Thread extends AsyncTask<Void, Void, Void> {
        int tanda;
        int total;

        public Start_New_Thread(int tanda, int total) {
            this.tanda = tanda;
            this.total = total;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.d(TAG,"Enviando Tanda "+tanda);
            for (int grupo = 0;grupo<total;grupo++){
            //for (int grupo = 0;grupo<3;grupo++){
                new sendTriples(tanda,grupo).execute();
            }

           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class sendTriples extends AsyncTask<Void, Void, Void> {
        int tanda;
        int grupo;

        public sendTriples(int tanda, int grupo) {
            this.tanda = tanda;
            this.grupo = grupo;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG,"Send Triple "+grupo);
            double latitude = -12.111111f +0.005f*tanda;
            double[] longitudes = new double[]{-76.940022f, -76.951022f, -76.944022f};
            MensajePublicar jsonsArray3_2 = mensajes.get(grupo*3+2);
            MensajePublicar jsonsArray3_1 = mensajes.get(grupo*3+1);
            MensajePublicar jsonsArray3 = mensajes.get(grupo*3);

            jsonsArray3_2.setLongi_Lati_DateSimulator(longitudes[2]-0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));

            jsonsArray3_1.setLongi_Lati_DateSimulator(longitudes[1]+0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));

            jsonsArray3.setLongi_Lati_DateSimulator(longitudes[0]-0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));

            jsonsArray3_2.CreoClienteMQTTyPublico();
            jsonsArray3_1.CreoClienteMQTTyPublico();
            jsonsArray3.CreoClienteMQTTyPublico();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }


    }


}
