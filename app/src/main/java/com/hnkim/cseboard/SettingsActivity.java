package com.hnkim.cseboard;

/**
 * Created by hnkim on 2017-01-10.
 */

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import static com.hnkim.cseboard.MainNotice.CONNECTION_CONFIRM_CLIENT_URL;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_AUTO_UPDATE = "pref_auto_update";
    SwitchPreference autoUpdate;
    ListPreference select_services;
    public static CheckBoxPreference select_noticeRefresh, select_freeboardRefresh, select_allRefresh;

    private ConnectivityManager manager;
    public static AlarmManager am;
    public static PendingIntent sender;

    Intent ServiceNotice, ServiceFreeboard, ServiceAll;

    static int index;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_AUTO_UPDATE)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));


        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

//                if(index == 0) {
//                    Log.d("haneul","i'm 선택안함");
//                } else if(index == 1 ) {
//                    Log.d("haneul", "i'm 공지사항");
//                } else if(index == 2) {
//                    Log.d("haneul", "i'm 자유게시판");
//                } else {
//                    Log.d("haneul", "i'm 공지사항+자유게시판");
//                }

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("무음");

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void setOnPreferenceChange(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.settings);
        ServiceNotice = new Intent(SettingsActivity.this, ServiceNotice.class);
        ServiceFreeboard = new Intent(SettingsActivity.this, ServiceFreeboard.class);
        ServiceAll = new Intent(SettingsActivity.this, ServiceAll.class);


        autoUpdate = (SwitchPreference)findPreference("autoUpdate");
//        select_services = (ListPreference)findPreference("select_services");
        select_noticeRefresh = (CheckBoxPreference)findPreference("select_noticeRefresh");
        select_freeboardRefresh = (CheckBoxPreference)findPreference("select_freeboardRefresh");
        select_allRefresh = (CheckBoxPreference)findPreference("select_allRefresh");

        autoUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(autoUpdate.isChecked()) {
                    if (isInternetConnected()) { //연결 실패시
                        autoUpdate.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

                        builder.setTitle("인터넷이 연결되지 않았습니다.")
                                .setIcon(R.drawable.sally)
                                .setMessage("와이파이 설정화면으로 이동하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        autoUpdate.setChecked(false);
                                        Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                        wifiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(wifiIntent);
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        autoUpdate.setChecked(false);
                                        Toast.makeText(SettingsActivity.this, "서비스를 이용하시려면 인터넷을 연결해주세요.", Toast.LENGTH_LONG).show();
                                        dialogInterface.cancel();
                                    }
                                });
                        final AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(184, 70, 89));
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(172, 169, 162));
                            }
                        });
                        dialog.show();
                    }
                    else {  //인터넷 연결 성공시
                        //연속 클릭 방지
//                        autoUpdate.setEnabled(false);
//
//                        Handler h = new Handler();
//                        h.postDelayed(new splashhandler(),500);
                            select_noticeRefresh.setEnabled(true);
                            select_freeboardRefresh.setEnabled(true);
                            select_allRefresh.setEnabled(true);
                        //서비스
//                        ServiceNotice = new Intent(SettingsActivity.this, ServiceNotice.class);

//                    startService(new Intent(SettingsActivity.this, ServiceNotice.class));
                    }
                }
                else {  // 스위치 OFF시
                    if(select_noticeRefresh.isChecked()) {
                        if(sender != null) {
                        am.cancel(sender);
                        stopService(ServiceNotice);
                        Log.d("stopRefresh", "auto refresh is stopped1");
                        select_noticeRefresh.setChecked(false);
                        } else {
                            stopService(ServiceNotice);
                            select_noticeRefresh.setChecked(false);
                        }
                    } else if(select_freeboardRefresh.isChecked()) {
                        if(sender != null) {
                            am.cancel(sender);
                            stopService(ServiceFreeboard);
                            Log.d("stopRefresh", "auto refresh is stopped2");
                            select_freeboardRefresh.setChecked(false);
                        } else {
                            stopService(ServiceFreeboard);
                            select_freeboardRefresh.setChecked(false);
                        }
                    } else if(select_allRefresh.isChecked()){
                        if(sender != null) {
                        am.cancel(sender);
                        stopService(ServiceAll);
                        Log.d("stopRefresh", "auto refresh is stopped3");
                        select_allRefresh.setChecked(false);
                        } else {
                            stopService(ServiceAll);
                            select_allRefresh.setChecked(false);
                        }
                    }

//                    autoUpdate.setEnabled(false);
//                    Handler h = new Handler();
//                    h.postDelayed(new splashhandler(),500);

                }
                return false;
            }
        });

        //ServiceNotice
        select_noticeRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(select_noticeRefresh.isChecked()) {  //공지사항 새글 알림 체크시
                    select_freeboardRefresh.setEnabled(false);
                    select_allRefresh.setEnabled(false);

                    select_noticeRefresh.setEnabled(false);
                    Handler h = new Handler();
                    h.postDelayed(new splashhandlerN(),800);

                    sender = PendingIntent.getService(SettingsActivity.this, 0, ServiceNotice, 0);
                    long triggerTime = SystemClock.elapsedRealtime();
                    triggerTime += 1 * 1000;
                    am = (AlarmManager) getSystemService(ALARM_SERVICE);

                    //인터넷연결계속확인
                    if(isInternetConnected()) {
                        Log.d("inSettingsActivity", "internet is not connected");
                        am.cancel(sender);
                        stopService(ServiceFreeboard);
                        select_noticeRefresh.setChecked(false);
                        select_freeboardRefresh.setEnabled(true);
                        select_allRefresh.setEnabled(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

                        builder.setTitle("인터넷이 연결되지 않았습니다.")
                                .setIcon(R.drawable.sally)
                                .setMessage("서비스를 종료합니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                        final AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(184, 70, 89));
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                            }
                        });
                        dialog.show();
                    } else {    //인터넷 연결이 되어있으면
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, 60 * 1000, sender);
                        Log.d("startRefresh", "auto Notice refresh is started");
                        Toast.makeText(SettingsActivity.this, "서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    select_freeboardRefresh.setEnabled(true);
//                    select_allRefresh.setEnabled(true);

                    select_noticeRefresh.setEnabled(false);
                    Handler n = new Handler();
                    n.postDelayed(new splashhandlerN(),1000);

                    select_freeboardRefresh.setEnabled(false);
                    Handler f = new Handler();
                    f.postDelayed(new splashhandlerF(),1000);

                    select_allRefresh.setEnabled(false);
                    Handler nf = new Handler();
                    nf.postDelayed(new splashhandlerNF(),1000);

                    if(sender != null) {
                        am.cancel(sender);
                        Log.d("stopRefresh", "auto Notice refresh is stopped");
                        Toast.makeText(SettingsActivity.this, "서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                        stopService(ServiceNotice);
                    } else {
                        stopService(ServiceNotice);
                        select_noticeRefresh.setChecked(false);
                    }
                }
                return false;
            }
        });

        ///////////////////////////////////////////////////////////////   체크박스   ////////////////////////////////////////////////////////////////////////////

        select_freeboardRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(select_freeboardRefresh.isChecked()) {   //자유게시판 새글 알림 체크시
                    select_noticeRefresh.setEnabled(false);
                    select_allRefresh.setEnabled(false);

                    select_freeboardRefresh.setEnabled(false);
                    Handler h = new Handler();
                    h.postDelayed(new splashhandlerF(),800);

                    sender = PendingIntent.getService(SettingsActivity.this, 0, ServiceFreeboard, 0);
                    long triggerTime = SystemClock.elapsedRealtime();
                    triggerTime += 60 * 1000;
                    am = (AlarmManager) getSystemService(ALARM_SERVICE);


                    //인터넷연결계속확인
                    if(isInternetConnected()) {
                        Log.d("inSettingsActivity", "internet is not connected");
                        am.cancel(sender);
                        stopService(ServiceFreeboard);
                        select_freeboardRefresh.setChecked(false);
                        select_noticeRefresh.setEnabled(true);
                        select_allRefresh.setEnabled(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

                        builder.setTitle("인터넷이 연결되지 않았습니다.")
                                .setIcon(R.drawable.sally)
                                .setMessage("서비스를 종료합니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                        final AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(184, 70, 89));
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                            }
                        });
                        dialog.show();
                    } else {    //인터넷 연결이 되어있으면
                            Log.d("startRefresh", "auto Freeboard refresh is started");
                            Toast.makeText(SettingsActivity.this, "서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
                            am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, 60 * 1000, sender);
                    }
                } else {        //체크 해제시
//                    select_noticeRefresh.setEnabled(true);
//                    select_allRefresh.setEnabled(true);

                    select_noticeRefresh.setEnabled(false);
                    Handler n = new Handler();
                    n.postDelayed(new splashhandlerN(),1000);

                    select_freeboardRefresh.setEnabled(false);
                    Handler f = new Handler();
                    f.postDelayed(new splashhandlerF(),1000);

                    select_allRefresh.setEnabled(false);
                    Handler nf = new Handler();
                    nf.postDelayed(new splashhandlerNF(),1000);

                    if(sender != null) {
                        am.cancel(sender);
                        Log.d("stopRefresh", "auto Freeboard refresh is stopped");
                        Toast.makeText(SettingsActivity.this, "서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                        stopService(ServiceFreeboard);
                    } else {
                        stopService(ServiceFreeboard);
                        select_freeboardRefresh.setChecked(false);
                    }
                }
                return false;
            }
        });

        select_allRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(select_allRefresh.isChecked()) {   //공지사항 + 자유게시판 새글 알림 체크시
                    select_noticeRefresh.setEnabled(false);
                    select_freeboardRefresh.setEnabled(false);

                    select_allRefresh.setEnabled(false);
                    Handler h = new Handler();
                    h.postDelayed(new splashhandlerNF(),800);

                    sender = PendingIntent.getService(SettingsActivity.this, 0, ServiceAll, 0);
                    long triggerTime = SystemClock.elapsedRealtime();
                    triggerTime += 1 * 1000;
                    am = (AlarmManager) getSystemService(ALARM_SERVICE);

                    //인터넷연결계속확인
                    if(isInternetConnected()) {
                        Log.d("inSettingsActivity", "internet is not connected");
                        am.cancel(sender);
                        stopService(ServiceFreeboard);
                        select_allRefresh.setChecked(false);
                        select_noticeRefresh.setEnabled(true);
                        select_freeboardRefresh.setEnabled(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

                        builder.setTitle("인터넷이 연결되지 않았습니다.")
                                .setIcon(R.drawable.sally)
                                .setMessage("서비스를 종료합니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                        final AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(184, 70, 89));
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                            }
                        });
                        dialog.show();
                    } else {    //인터넷 연결이 되어있으면
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, 60 * 1000, sender);
                        Log.d("startRefresh", "auto Notice&Freeboard refresh is started");
                        Toast.makeText(SettingsActivity.this, "서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    select_noticeRefresh.setEnabled(true);
//                    select_freeboardRefresh.setEnabled(true);

                    select_noticeRefresh.setEnabled(false);
                    Handler n = new Handler();
                    n.postDelayed(new splashhandlerN(),1000);

                    select_freeboardRefresh.setEnabled(false);
                    Handler f = new Handler();
                    f.postDelayed(new splashhandlerF(),1000);

                    select_allRefresh.setEnabled(false);
                    Handler nf = new Handler();
                    nf.postDelayed(new splashhandlerNF(),1000);

                    if(sender != null) {
                        am.cancel(sender);
                        Log.d("stopRefresh", "auto Notice&Freeboard refresh is stopped");
                        Toast.makeText(SettingsActivity.this, "서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                        stopService(ServiceAll);
                    } else {
                        stopService(ServiceAll);
                        select_allRefresh.setChecked(false);
                    }
                }
                return false;
            }
        });
    }

    class splashhandler implements Runnable {
        public void run() {
            autoUpdate.setEnabled(true);
        }
    }

    class splashhandlerN implements Runnable {
        public void run() { select_noticeRefresh.setEnabled(true);}
    }

    class splashhandlerF implements Runnable {
        public void run() { select_freeboardRefresh.setEnabled(true);}
    }

    class splashhandlerNF implements Runnable {
        public void run() { select_allRefresh.setEnabled(true);}
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName);
    }

    //인터넷 연결 확인
    boolean isInternetConnected() {
        manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork == null;
    }

    private static boolean isOnline() {
        CheckConnect cc = new CheckConnect(MainNotice.CONNECTION_CONFIRM_CLIENT_URL);
        cc.start();
        try{
            cc.join();
            return cc.isSuccess();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Boolean isServiceRunning(String serviceName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
