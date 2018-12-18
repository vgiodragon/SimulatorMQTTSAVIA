package com.giotec.simulatormqttsavia;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MensajePublicar {
    String TAG="GIODEBUG_MensajePublicar";
    String imei;
    String imsi;
    String ses_id;
    String mac;
    String relation;
    String type;
    Double longitude;
    Double latitude;
    String date_simulator;
    private Context ctx;
    MqttAndroidClient mqttAndroidClient;
    private boolean msggSent;

    public MensajePublicar(String imei, String imsi, String ses_id, String mac, String relation, String type,Context ctx) {
        this.imei = imei;
        this.imsi = imsi;
        this.ses_id = ses_id;
        this.mac = mac;
        this.relation = relation;
        this.type = type;
        this.ctx = ctx;
        msggSent=false;
        //CreoClienteMQTT();
    }

    public void setLongi_Lati_DateSimulator(Double longitude,Double latitude,String date_simulator){
        this.longitude = longitude;
        this.latitude = latitude;
        this.date_simulator = date_simulator;
    }

    public void CreoClienteMQTTyPublico(){
        int random= (int) (Math.random()*12345);
        mqttAndroidClient
                = new MqttAndroidClient(ctx, "tcp://"+
                Utils.getIp()+":1883", ses_id+" "+random);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setUserName(Utils.getUserMQTT());
        options.setPassword(Utils.getPassMQTT().toCharArray());
        options.setCleanSession(false);

        options.setAutomaticReconnect(false);
        options.setKeepAliveInterval(30);
        try {
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    MqttMessage message = new MqttMessage(getStringJson().getBytes());
                    try {
                        //Log.d(TAG,"Enviando "+getStringJson());
                        mqttAndroidClient.publish(relation+"/"+type,message);
                        mqttAndroidClient.disconnect();
                        setMsggSent(true);
                    } catch (MqttException e) {
                        Log.d("GIODEBUG_MQTT_SA", ses_id+"__"+relation+" MqttException" + asyncActionToken.toString());
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("GIODEBUG_MQTT_SA", ses_id+"__"+relation+" IMqttActionListener_onFailure_" + asyncActionToken.toString());
                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getStringJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imei",imei);
            jsonObject.put("imsi",imsi);
            jsonObject.put("ses_id",ses_id);
            jsonObject.put("mac",mac);
            jsonObject.put("longitude",longitude);
            jsonObject.put("latitude",latitude);
            jsonObject.put("date_simulator",date_simulator);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public Context getCtx() {
        return ctx;
    }

    public String getTopic(){
        return  relation+"/"+type;
    }


    public boolean isMsggSent() {
        return msggSent;
    }

    public void setMsggSent(boolean msggSent) {
        this.msggSent = msggSent;
    }
}
