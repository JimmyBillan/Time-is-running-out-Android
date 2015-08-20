package tiroapp.com.tiro_app.controller;

import android.content.SharedPreferences;

/**
 * Created by user on 26/07/2015.
 */
public class HorlogeVIEW {
    static SharedPreferences pref;

    public static String converteMinutesToReadable(Integer timer){

        String theReturn = "0h00";

        if(timer > 1439){
            if(timer / 1440 == 1 ){
                theReturn = Integer.toString(timer / 1440)+"Day";
            }else{
                theReturn = Integer.toString(timer / 1440)+"Days";
            }

            if(((timer % 1440)/60) > 1){
               theReturn = theReturn+Integer.toString((timer % 1440)/60)+"h";
            }

        }else if(timer< 1440 && timer > 0){
            String t ;
            Integer modulo = timer % 60;
            if(modulo % 60 < 10){t="0"+Integer.toString(modulo);}
            else
            {t=Integer.toString(timer % 60);}
                theReturn = Integer.toString(timer / 60)+"h"+t;
        }
        return  theReturn;
    }

    public static String convertSecondeToReadable(Integer timer){
        String theReturn = "00h00";
        if(timer > 86400){
            Integer modulo = (timer % 86400);
            if( modulo < 3600 ){
                theReturn = Integer.toString(timer / 86400)+"d"+Integer.toString(modulo/60) +"m";
            }else{
                theReturn = Integer.toString(timer / 86400)+"d"+Integer.toString(modulo/3600)+"h";
            }

        } else if(timer < 86400 && timer > 3600){
            theReturn = Integer.toString(timer/3600)+"h"+Integer.toString((timer % 3600)/60);
        }else if(timer > 0){
            theReturn = Integer.toString(timer/60)+"m"+Integer.toString((timer % 60))+"s";
        }
        return theReturn;
    }


}
