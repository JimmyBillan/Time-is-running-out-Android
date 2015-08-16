package tiroapp.com.tiro_app;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 16/08/2015.
 */
public class HorlogeService extends Service {

    public static final long TIME_INTERVAL = 1000;

    private Handler mHandler = new Handler();

    private Timer mTimer = null;
    private Integer horloge;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        HorlogeService getService() {
            // Return this instance of MyService so clients can call public methods
            return HorlogeService.this;
        }
    }

    public interface ServiceCallbacks {
        void doSomething();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallbacks callbacks){
        this.serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        if(mTimer != null){
            mTimer.cancel();
            this.horloge = 0;
        }else{
            mTimer = new Timer();
        }
        this.horloge = 0;
        mTimer.scheduleAtFixedRate(new TimeRefreshHorlogeTash(), 0, TIME_INTERVAL);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    public Integer getHorloge() {
        return this.horloge;
    }

    public void setHorloge(Integer horloge){
        this.horloge = horloge;
    }

    private class TimeRefreshHorlogeTash extends TimerTask {
        private String mTimer;

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(serviceCallbacks != null){
                        serviceCallbacks.doSomething();
                    }

                }
            });
        }

    }

}
