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
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Thread.sleep;
import static java.security.AccessController.getContext;

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
    MqttAndroidClient mqttAndroidClient;
    volatile ArrayList<Alarma> malarmas;
    TextView Tstart;
    TextView Tsave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasPermissions(this, PERMISSIONS))
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        Tstart = findViewById(R.id.textView3);
        Tsave = findViewById(R.id.textView4);
        mensajes = Utils.readAllJSONS(getApplicationContext());
        spNUsers = findViewById(R.id.spinner3);
        sptest = findViewById(R.id.spinner4);
        malarmas=new ArrayList<>();
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

    private void creoClienteMQTTSuscribe(){
        int random= (int) (Math.random()*12345);
        mqttAndroidClient
                = new MqttAndroidClient(getApplicationContext(), "tcp://"+
                Utils.getIp()+":1883", "rand "+random);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setUserName("CTIC-SMARTCITY");
        options.setPassword("YTICTRAMS-CITC".toCharArray());

        mqttAndroidClient.setCallback(new MiCallBackMQTT() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Turno nuevoTurno = convertJsonTurno(new String(message.getPayload()));
                Log.d("GIODEBUG_MQTT","Llego del topic " + topic + ": " + new String(message.getPayload()));
                malarmas.add(JsonToAlarma(new String(message.getPayload())));

            }
        });
        try {
            if(mqttAndroidClient!=null) {
                IMqttToken token = mqttAndroidClient.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Log.d("GIODEBUG_MQTT","conectado Sucess de suscripcion");
                        //suscrito=true;
                        try {
                            mqttAndroidClient.subscribe("Alarma/CERO", 0);

                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        //suscrito=false;
                        Log.d("GIODEBUG_MQTT", "IMqttActionListener_onFailure");
                    }

                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void DisconnectMQTT(){
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void Start (View view) {
        //int total = Integer.parseInt(spNUsers.getSelectedItem().toString());
        creoClienteMQTTSuscribe();
        String red =spNUsers.getSelectedItem().toString();
        int test = Integer.parseInt(sptest.getSelectedItem().toString());
        Log.d(TAG,"Start ! "+red+"_"+test);
        Tstart.setText(Tstart.getText()+"\n"+red+" "+test+" "+
                new SimpleDateFormat("HH:mm:ss").format(new Date()));
        new PrincipalTask(red,test,0).execute();

    }

    private class PrincipalTask extends AsyncTask<Void, Void, Void> {
        //int total;
        String red;
        int test;
        int tanda;

        public PrincipalTask(String red, int test, int tanda) {
            this.red = red;
            this.test = test;
            this.tanda=tanda;
            Log.d(TAG,"Ready to send tandas !");
            new Start_New_Thread(tanda).execute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG,"Espero 50s");
             try {
                Thread.sleep(50000);
             } catch (InterruptedException e) {
                e.printStackTrace();
             }
            Log.d(TAG,"Fin 50s");

             return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(tanda<15)
                new PrincipalTask(red,test,tanda+1).execute();
            else{
                Toast.makeText(getApplicationContext(),"Listo enviado los 15 ",
                        Toast.LENGTH_LONG).show();
                Tstart.setText(Tstart.getText()+" // "+
                        new SimpleDateFormat("HH:mm:ss").format(new Date()));
                Log.d(TAG,"Acá reinicio la suscripcion");
                SaveToCSV(red,test);
                DisconnectMQTT();
            }
        }
    }

    private void SaveToCSV(String red, int test) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Savia");
        String filename = red+"_"+test+".csv";

        File file = new File (myDir, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            //out.write(string.getBytes());
            //String titlesCSV = "fecha_llegada, hora_llegada, fecha_llegada_ntp, hora_llegada_ntp, fecha_envio, hora_envio, value\n";
            String titlesCSV = "Agre_date,Vic_date,Hora,Hllegada\n";
            out.write(titlesCSV.getBytes());
            for (Alarma alarma : malarmas)
                out.write(alarma.toStringCSV().getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT)
                .show();
        Tsave.setText(Tsave.getText()+"\n"+red+"_"+test+".csv");
    }

    private class Start_New_Thread extends AsyncTask<Void, Void, Void> {
        int tanda;
        //int total;

        public Start_New_Thread(int tanda) {
            this.tanda = tanda;
            //this.total = total;
            Log.d(TAG,"Enviando Tanda "+tanda);
            //for (int grupo = 0;grupo<total;grupo++){
            for (int grupo = 0;grupo<10;grupo++){
                new sendTriples(tanda,grupo).execute();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG,"Espero 9.9s que acabe de enviar toda la tanda "+tanda);

            try {
                Thread.sleep(9900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Fin tanda "+tanda);

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

            jsonsArray3_1.setLongi_Lati_DateSimulator(longitudes[1]+0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));
            jsonsArray3_1.CreoClienteMQTTyPublico();

            jsonsArray3.setLongi_Lati_DateSimulator(longitudes[0]-0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));
            jsonsArray3.CreoClienteMQTTyPublico();

            jsonsArray3_2.setLongi_Lati_DateSimulator(longitudes[2]-0.002f*(tanda%2),
                    latitude, new SimpleDateFormat("HH:mm:ss.SS").format(new Date()));
            jsonsArray3_2.CreoClienteMQTTyPublico();
            //try {
          //      Thread.sleep(15000);
          //  } catch (InterruptedException e) {
           //     e.printStackTrace();
         //   }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
        }

    }



}
