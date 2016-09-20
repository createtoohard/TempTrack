package com.cl.temptrack.temptrack;

import android.graphics.drawable.Drawable;

/**
 * Created by jiezhao on 16/9/19.
 */
public class PackageInfo implements Comparable<PackageInfo> {
    private Drawable icon;
    private String processName;
    private String packageName;
    private int pid;
    private int uid;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {

        this.processName = processName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public int compareTo(PackageInfo arg0) {
        return (this.getProcessName().compareTo(arg0.getProcessName()));
    }
}
