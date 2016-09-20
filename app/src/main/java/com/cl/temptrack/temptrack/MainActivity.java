package com.cl.temptrack.temptrack;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "ChenLong MainActivity";
    private static final boolean DEBUG = true;
    private static final int TIMEOUT = 30000;

    private List<PackageInfo> processList;
    private ProcessInfo mProcessInfo;
    private ListView mPackageList;
    private Button mStartTestBtn;
    private int pid, uid;
    private boolean isServiceStop = false;
    private Intent mMonitorIntent;
    private UpdateReceiver mReceiver;
//    private Long mExitTime = (long) 0;
//    private UpdateReceiver receiver;

    private TextView mNavTitle;
    private ImageView mGoBackBtn;
    private ImageView mSetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG) Log.e(TAG, "MainActivity onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mProcessInfo = new ProcessInfo();

        initLayout();

        mReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonitorService.SERVICE_ACTION);
        registerReceiver(mReceiver, filter);

    }


    private void initLayout() {
        mPackageList = (ListView) findViewById(R.id.processlist);
        mGoBackBtn = (ImageView) findViewById(R.id.go_back);
        mSetBtn = (ImageView) findViewById(R.id.btn_set);
        mStartTestBtn = (Button) findViewById(R.id.test);
        mNavTitle = (TextView) findViewById(R.id.nav_title);

        initPackageList();

        mGoBackBtn.setVisibility(View.INVISIBLE);

        mSetBtn.setImageResource(R.drawable.settings_button_selector);
        mSetBtn.setOnClickListener(this);

        mStartTestBtn.setOnClickListener(this);
        mStartTestBtn.setText(R.string.start_test);

        mNavTitle.setText(R.string.app_name);

    }

    private void initPackageList() {
        mPackageList.setAdapter(new ListAdapter());
        mPackageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RadioButton rdBtn = (RadioButton) ((LinearLayout) view).getChildAt(0);
                rdBtn.setChecked(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test:
                startTest();
                break;
            case R.id.go_back:
                break;
            case R.id.btn_set:
                goToSettingsActivity();
                break;
        }


    }

    private void startTest() {
        mMonitorIntent = new Intent();
        mMonitorIntent.setClass(MainActivity.this, MonitorService.class);
        ListAdapter adapter = (ListAdapter) mPackageList.getAdapter();
        if(getString(R.string.start_test).equals(mStartTestBtn.getText().toString())) {
            if (adapter.checkedProg != null) {
                String packageName = adapter.checkedProg.getPackageName();
                String processName = adapter.checkedProg.getProcessName();
                Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                String startActivity = "";
                if (DEBUG) Log.e(TAG, "start test" + packageName);
                // clear logcat
                try {
                    Runtime.getRuntime().exec("logcat -c");
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                try {
                    startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, getString(R.string.toast_can_not_start_app), Toast.LENGTH_LONG).show();
                    return;
                }
                waitForAppStart(packageName);

                mMonitorIntent.putExtra("processName", processName);
                mMonitorIntent.putExtra("pid", pid);
                mMonitorIntent.putExtra("uid", uid);
                mMonitorIntent.putExtra("packageName", packageName);
                mMonitorIntent.putExtra("startActivity", startActivity);
                startService(mMonitorIntent);
                isServiceStop = false;
                mStartTestBtn.setText(getString(R.string.stop_test));
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.toast_choose_app), Toast.LENGTH_LONG).show();
            }
        } else {
            mStartTestBtn.setText(getString(R.string.start_test));
            stopService(mMonitorIntent);
        }

    }

    private void waitForAppStart(String packageName) {
        if(DEBUG) Log.e(TAG, "wait for app start");
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = mProcessInfo.getPidByPackageName(getBaseContext(), packageName);
            if (pid != 0) {
                isProcessStarted = true;
                break;
            }
            if (isProcessStarted) {
                break;
            }
        }
    }

    // Adapter
    private class ListAdapter extends BaseAdapter {
        List<PackageInfo> mPackageInfos;
        PackageInfo checkedProg;
        int lastCheckedPosition = -1;

        public ListAdapter() {
            mPackageInfos = mProcessInfo.getAllPackages(getBaseContext());
        }

        @Override
        public int getCount() {
            return mPackageInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PackageInfo pr = (PackageInfo) mPackageInfos.get(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            }
            Viewholder holder = (Viewholder) convertView.getTag();
            if (holder == null) {
                holder = new Viewholder();
                convertView.setTag(holder);
                holder.imgViAppIcon = (ImageView) convertView.findViewById(R.id.image);
                holder.txtAppName = (TextView) convertView.findViewById(R.id.text);
                holder.mRadioButton = (RadioButton) convertView.findViewById(R.id.radio_btn);
                holder.mRadioButton.setFocusable(false);
                holder.mRadioButton.setOnCheckedChangeListener(checkedChangeListener);
            }
            holder.imgViAppIcon.setImageDrawable(pr.getIcon());
            holder.txtAppName.setText(pr.getProcessName());
            holder.mRadioButton.setId(position);
            holder.mRadioButton.setChecked(checkedProg != null && getItem(position) == checkedProg);
            return convertView;
        }

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    final int checkedPosition = buttonView.getId();
                    if (lastCheckedPosition != -1) {
                        RadioButton tempButton = (RadioButton) findViewById(lastCheckedPosition);
                        if ((tempButton != null) && (lastCheckedPosition != checkedPosition)) {
                            tempButton.setChecked(false);
                        }
                    }
                    checkedProg = mPackageInfos.get(checkedPosition);
                    lastCheckedPosition = checkedPosition;
                }
            }
        };
    }

    // Viewholder
    static class Viewholder {
        TextView txtAppName;
        ImageView imgViAppIcon;
        RadioButton mRadioButton;
    }


    // BroadcastReceiver
    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceStop = intent.getExtras().getBoolean("isServiceStop");
            if (isServiceStop) {
                mStartTestBtn.setText(getString(R.string.start_test));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((System.currentTimeMillis() - mExitTime) > 2000) {
//                Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
//                mExitTime = System.currentTimeMillis();
//            } else {
//                if (monitorService != null) {
//                    Log.d(LOG_TAG, "stop service");
//                    stopService(monitorService);
//                }
//                Log.d(LOG_TAG, "exit Emmagee");
//                finish();
//            }
            Toast.makeText(this,R.string.toast_test_over,Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 跳转到设置页面
    private void goToSettingsActivity() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

}
