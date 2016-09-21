package com.cl.temptrack.temptrack;

import android.app.ApplicationErrorReport;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by jiezhao on 16/9/20.
 */
public class MonitorService extends Service {
    private static final String TAG = "ChenLong MonitorService";
    private static final boolean DEBUG = true;
    public static final String SERVICE_ACTION = "com.cl.temptrack.action.MonitorService";
    private static final String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    private Process mProcess;
    private String resultDir;

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
                readMemoryInfo();
                saveMemoryInfo();
                readCpuInfo();
                saveCpuInfo();
                savePsInfo();
                unregisterReceiver(mBatteryReceiver);
            }
        }
    }

    private String readMemoryInfo() {
        String memoryInfoPath = "/proc/meminfo";
        return Utils.readFile(memoryInfoPath);
    }

    private void readCpuInfo() {
        String cpuInfoPath = "/sys/devices/system/cpu/cpufreq/all_time_in_state";
        Utils.readFile(cpuInfoPath);
    }

    private void saveMemoryInfo() {
        String memoryInfoTestResultPath = makeDir() + "/memoryinfo.csv";
        String memoryInfoPath = "proc/meminfo";
        if (Utils.isSdCardExist()) {
            Utils.saveFileByCsv(memoryInfoPath, memoryInfoTestResultPath, ":");
            if (DEBUG) Log.e(TAG, "----------save memoryinfo successful----------");
        }
    }

    private void saveCpuInfo() {
        String cpuInfoPath = "/sys/devices/system/cpu/cpufreq/all_time_in_state";
        String cpuInfoTestResultPath = makeDir() + "/cpuinfo.csv";
        if (Utils.isSdCardExist()) {
            Utils.saveFileByCsv(cpuInfoPath, cpuInfoTestResultPath, "\\s{1,}");
            if (DEBUG) Log.e(TAG, "----------save cpuinfo successful----------");
        }
    }

    private void savePsInfo() {
        String psInfoTestResultPath = makeDir() + "/psInfo.csv";
        if (Utils.isSdCardExist()) {
            Utils.saveCommandInfoByCsv("ps", psInfoTestResultPath);
            if (DEBUG) Log.e(TAG, "----------save psinfo successful----------");
        }
    }

    private void createResultCsv() {

    }

    private String makeDir() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String mDateTime = null;
        if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
            mDateTime = formatter.format(cal.getTime().getTime() + 8 * 60 * 60
                    * 1000);
        } else {
            mDateTime = formatter.format(cal.getTime().getTime());
        }

        if (Utils.isSdCardExist()) {
            resultDir = "/sdcard" + File.separator + "TempTrack" + mDateTime;
        } else {
            resultDir = getBaseContext().getFilesDir().getPath()
                    + File.separator + "TempTrack" + mDateTime;
        }

        File resultFile = new File(resultDir);
        resultFile.mkdir();
        Log.e(TAG, "result dir path = " + resultFile.getAbsolutePath());
        return resultFile.getAbsolutePath();
    }


}
