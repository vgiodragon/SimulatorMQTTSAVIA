package com.giotec.simulatormqttsavia;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MiCallBackMQTT implements MqttCallback {
    private final String GioDBug = "GioDBug_MCBMQTT";

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(GioDBug,"Connection was lost!");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(GioDBug,"Message llego Arrived!: " + topic + ": " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(GioDBug,"Delivery Complete!");
    }

    public Alarma JsonToAlarma(String contentAsString){
        Alarma alarma = null;
        try {
            JSONObject jsonObject = new JSONObject(contentAsString);
            alarma = new Alarma(
                    jsonObject.getString("Agre_date"),
                    jsonObject.getString("Vic_date"),
                    jsonObject.getString("Hora")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alarma;
    }

}