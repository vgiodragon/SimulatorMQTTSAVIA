package com.giotec.simulatormqttsavia;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Alarma {
    String Vic_date;
    String Hora;
    String Agre_date;
    String Hllegada;

    public Alarma(String agre_date, String vic_date,String hora) {
        Vic_date = vic_date;
        Hora = hora;
        Agre_date = agre_date;
        Hllegada = new SimpleDateFormat("HH:mm:ss.SS").format(new Date());
    }

    public String toStringCSV(){
        return Agre_date+","+Vic_date+","+Hora+","+Hllegada+"\n";
    }

}
