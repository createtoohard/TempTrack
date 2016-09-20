package com.cl.temptrack.temptrack;

import android.app.ApplicationErrorReport;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jiezhao on 16/9/20.
 */
public class MonitorService extends Service {
    private static final String TAG = "ChenLong MonitorService";
    private static final boolean DEBUG = true;
    public static final String SERVICE_ACTION = "com.cl.temptrack.action.MonitorService";
    private static final String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";

    private BatteryReceiver mBatteryReceiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBatteryReceiver = new BatteryReceiver();
        registerReceiver(mBatteryReceiver, new IntentFilter(BATTERY_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        return super.onStartCommand(intent, flags, startId);
    }


    // 电池信息监听
    public class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//                totalBatt = String.valueOf(level * 100 / scale);
//                voltage = String.valueOf(intent.getIntExtra(
//                        BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
                String temperature = String.valueOf(intent.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
                if(DEBUG) Log.e(TAG, "Battery temperature = " + temperature);
            }
        }
    }

}
