package com.cl.temptrack.temptrack;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jiezhao on 16/9/19.
 */
public class ProcessInfo {

    private static final String TAG = "ChenLong";
    private static final boolean DEBUG = true;

    private static final String PACKAGE_NAME = "com.cl.temptrack.temptrack";
    private static final int ANDROID_M = 22;

    /**
     * 获取当前正在运行的所有进程
     */
    public List<PackageInfo> getRunningProcess(Context context) {
        if(DEBUG) Log.e(TAG, "ProcessInfo getRunningProcess");
        List<PackageInfo> mPackageInfoList = new ArrayList<PackageInfo>();
        PackageManager pm = context.getPackageManager();

        // 获取系统当前正在运行的所用进程
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRuningAppProcessInfo = am.getRunningAppProcesses();

        for (ApplicationInfo appinfo : getPackagesInfo(context)) {
            PackageInfo mPackageInfo = new PackageInfo();
            // 过滤掉系统应用和本应用
            if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                    || ((appinfo.processName != null) && (appinfo.processName
                    .equals(PACKAGE_NAME)))) {
                continue;
            }
            for (ActivityManager.RunningAppProcessInfo runningProcess : mRuningAppProcessInfo) {
                if ((runningProcess.processName != null)
                        && runningProcess.processName
                        .equals(appinfo.processName)) {
                    mPackageInfo.setPid(runningProcess.pid);
                    mPackageInfo.setUid(runningProcess.uid);
                    break;
                }
            }
            mPackageInfo.setPackageName(appinfo.processName);
            mPackageInfo.setProcessName(appinfo.loadLabel(pm).toString());
            mPackageInfo.setIcon(appinfo.loadIcon(pm));
            mPackageInfoList.add(mPackageInfo);
        }
        Collections.sort(mPackageInfoList);
        return mPackageInfoList;
    }

    /**
     * 通过 pid 获取包名
     */
    public int getPidByPackageName(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // Note: getRunningAppProcesses return itself in API 22
        if (Build.VERSION.SDK_INT < ANDROID_M) {
            List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningProcess : run) {
                if ((runningProcess.processName != null)
                        && runningProcess.processName.equals(packageName)) {
                    return runningProcess.pid;
                }
            }
        } else {
            try {
                Process p = Runtime.getRuntime().exec("top -m 100 -n 1");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(packageName)) {
                        line = line.trim();
                        String[] splitLine = line.split("\\s+");
                        if (packageName.equals(splitLine[splitLine.length - 1])) {
                            return Integer.parseInt(splitLine[0]);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取所有安装的应用信息
     */
    public List<PackageInfo> getAllPackages(Context context) {
        List<PackageInfo> progressList = new ArrayList<PackageInfo>();
        PackageManager pm = context.getPackageManager();

        for (ApplicationInfo appinfo : getPackagesInfo(context)) {
            PackageInfo mPackageInfo = new PackageInfo();
            if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                    || ((appinfo.processName != null) && (appinfo.processName
                    .equals(PACKAGE_NAME)))) {
                continue;
            }
            mPackageInfo.setPackageName(appinfo.processName);
            mPackageInfo.setProcessName(appinfo.loadLabel(pm).toString());
            mPackageInfo.setIcon(appinfo.loadIcon(pm));
            progressList.add(mPackageInfo);
        }
        Collections.sort(progressList);
        return progressList;
    }

    /**
     * get information of all applications.
     *
     * @param context
     *            context of activity
     * @return packages information of all applications
     */
    private List<ApplicationInfo> getPackagesInfo(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        List<ApplicationInfo> appList = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        return appList;
    }

    /**
     * get pid by package name
     *
     * @param context
     *            context of activity
     * @param packageName
     *            package name of monitoring app
     * @return pid
     */
    public PackageInfo getPackageInfoByPackageName(Context context, String packageName) {
        if (Build.VERSION.SDK_INT < ANDROID_M) {
            List<PackageInfo> processList = getRunningProcess(context);
            for (PackageInfo programe : processList) {
                if ((programe.getPackageName() != null)
                        && (programe.getPackageName().equals(packageName))) {
                    return programe;
                }
            }
        } else {
            PackageInfo mPackageInfo = new PackageInfo();
            int pid = getPidByPackageName(context, packageName);
            mPackageInfo.setPid(pid);
            mPackageInfo.setUid(0);
            return mPackageInfo;
        }
        return null;
    }

    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // Note: getRunningTasks is deprecated in API 21(Official)
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null)
            return (runningTaskInfos.get(0).topActivity).toString();
        else
            return null;
    }

    public boolean isForeground(Context context, String packageName) {
        if (packageName.equals("") | packageName == null)
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runinfo : runningAppProcesses) {
            String pn = runinfo.processName;
            if (pn.equals(packageName)
                    && runinfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                if (DEBUG) Log.e(TAG, "is foreground packagename = " + pn);
                return true;
        }
        return false;
    }
}
