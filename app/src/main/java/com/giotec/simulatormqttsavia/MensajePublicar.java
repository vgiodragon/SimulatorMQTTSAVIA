package com.giotec.simulatormqttsavia;

public class MensajePublicar {
    String imei;
    String imsi;
    String ses_id;
    String mac;
    String relation;
    String type;
    Double longitude;
    Double latitude;
    String date_simulator;

    public MensajePublicar(String imei, String imsi, String ses_id, String mac, String relation, String type) {
        this.imei = imei;
        this.imsi = imsi;
        this.ses_id = ses_id;
        this.mac = mac;
        this.relation = relation;
        this.type = type;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setDate_simulator(String date_simulator) {
        this.date_simulator = date_simulator;
    }
}
