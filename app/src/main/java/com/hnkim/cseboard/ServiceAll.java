package com.hnkim.cseboard;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.hnkim.cseboard.MainNotice.CONNECTION_CONFIRM_CLIENT_URL;

/**
 * Created by hnkim on 2017-01-17.
 */

public class ServiceAll extends Service {
    private int Ncount = 0, Fcount = 0, Ncheck = 1, Fcheck=1;
    private int Tcount = 0, Dcount = 1;
    public int key = 1;

    private String Nurl ="";
    private String Furl ="";

    public static java.net.URL URL;
    private static Source source;
    private static int cur;
    private static String NOTICE = "http://cse.kut.ac.kr/notice";
    private static String FREEBOARD = "http://cse.kut.ac.kr/freeboard";

    ArrayList<String> NoldTITLE = new ArrayList<String>();
    ArrayList<String> NoldDATE = new ArrayList<String>();
    ArrayList<String> NnewTITLE = new ArrayList<String>();
    ArrayList<String> NnewDATE = new ArrayList<String>();

    ArrayList<String> FoldTITLE = new ArrayList<String>();
    ArrayList<String> FoldDATE = new ArrayList<String>();
    ArrayList<String> FnewTITLE = new ArrayList<String>();
    ArrayList<String> FnewDATE = new ArrayList<String>();

    private ConnectivityManager manager;

    @Override
    public void onCreate() {
        if(SettingsActivity.select_allRefresh == null) {
            Toast.makeText(ServiceAll.this, "앱이 강제로 종료되었습니다. 원활한 서비스를 위해 앱을 켜주세요", Toast.LENGTH_LONG).show();
        }
        else if(!SettingsActivity.select_allRefresh.isChecked() && isServiceRunning(".ServiceAll")) {
            Log.d("exit","exitFree");
            SettingsActivity.select_allRefresh.setChecked(false);
            SettingsActivity.select_noticeRefresh.setEnabled(true);
            SettingsActivity.select_freeboardRefresh.setEnabled(true);
            onDestroy();
        } else if(SettingsActivity.select_allRefresh.isChecked() && !isServiceRunning(".ServiceAll")){
            Toast.makeText(ServiceAll.this, "♥", Toast.LENGTH_LONG).show();
        }
//        Toast.makeText(ServiceNotice.this, "서비스를 시작합니다.", Toast.LENGTH_LONG).show();
//        oldList.add(0);

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
    //서비스가 종료될때 실행됨
    @Override
    public void onDestroy() {
//        Toast.makeText(ServiceNotice.this, "서비스를 종료합니다.", Toast.LENGTH_LONG).show();
        NoldTITLE.clear();
        NoldDATE.clear();
        NnewTITLE.clear();
        NnewDATE.clear();
        FoldTITLE.clear();
        FoldDATE.clear();
        FnewTITLE.clear();
        FnewDATE.clear();
        Ncount = 0;
        Fcount = 0;
        Log.d("end_refresh", Ncount + "");
        Log.d("end_refresh", Fcount + "");
    }

    //서비스가 호출될때마다 실행됨
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(SettingsActivity.select_allRefresh == null) {
            Toast.makeText(ServiceAll.this, "앱이 강제로 종료되었습니다. 원활한 서비스를 위해 앱을 켜주세요", Toast.LENGTH_LONG).show();
        }
        else if(!SettingsActivity.select_allRefresh.isChecked() && isServiceRunning(".ServiceAll")) {
            Log.d("exit","exitFree");
            SettingsActivity.select_allRefresh.setChecked(false);
            SettingsActivity.select_noticeRefresh.setEnabled(true);
            SettingsActivity.select_freeboardRefresh.setEnabled(true);
            onDestroy();
        } else if(SettingsActivity.select_allRefresh.isChecked() && !isServiceRunning(".ServiceAll")) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);
            int mSecond = c.get(Calendar.SECOND);

            parsingNoticeFirstItem pN1 = new parsingNoticeFirstItem(NOTICE);
            parsingFreeboardFirstItem pN2 = new parsingFreeboardFirstItem(FREEBOARD);

//        Toast.makeText(ServiceNotice.this, "현재 시간"+mHour+":"+mMinute+":"+mSecond, Toast.LENGTH_LONG).show();
            Log.d("refresh start!!", "......................");
            Log.d("refresh time", mHour + ":" + mMinute + ":" + mSecond);
            try {

                if (isInternetConnected()) {
                    stopSelf();
                    SettingsActivity.am.cancel(SettingsActivity.sender);
                    SettingsActivity.select_allRefresh.setChecked(false);
                    SettingsActivity.select_freeboardRefresh.setEnabled(true);
                    SettingsActivity.select_noticeRefresh.setEnabled(true);
                } else {
                    pN1.start();
                    pN1.join();
                    pN2.start();
                    pN2.join();
                }


            } catch (Exception e) {
                Log.d("refreshPar", e + "");
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }


    //Service 객체와 (화면단 Activity 사이에서) 통신(데이터를 주고받을) 때 사용하는 메서드
    //데이터를 전달할 필요가 없으면 return null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //번호 게시글 첫번째 아이템 파싱해오기
    class parsingNoticeFirstItem extends Thread {
        private parsingNoticeFirstItem(final String url) {
            Nurl = url;
        }

        public void run() {
            try {
                URL = new URL(Nurl);
                InputStream html = URL.openStream();
                source = new Source(new InputStreamReader(html, "utf-8"));
                source.fullSequentialParse();   //순차적으로 구문을 분석함
            } catch (Exception e) {
                Log.d("ERROR_GETLIST", e + " ");
            }

            try {
                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); //div타입 태그 불러옴
                int tagNum;
                for (tagNum = 1; tagNum < tabletags.size(); tagNum++) { //DIV 모든 태그중 board_list 태그가 몇번째임을 구함
                    if (tabletags.get(tagNum).toString().trim().equals("<div class=\"board_list\" id=\"board_list\">")) {
                        cur = tagNum; //DIV 클래스가 board_list 면 tagNum 값을 cur로 몇번째인지 저장
                        break;
                    }
                }
                Log.d("ERROR_CUR", cur + "");
                Element DIV, TABLE, TBODY;
                DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(cur);
                TABLE = (Element) DIV.getAllElements(HTMLElementName.TABLE).get(0);
                TBODY = (Element) TABLE.getAllElements(HTMLElementName.TABLE).get(0);

                Log.d("ERROR_SIZE", tabletags.size() + "");
                Log.d("ERROR_TRSIZE", TBODY.getAllElements(HTMLElementName.TR).size() + "");

                int trNum, position = 0;
                for (trNum = 1; trNum < TBODY.getAllElements(HTMLElementName.TR).size(); trNum++) {
                    //for문으로 바꿔야 다음페이지로 넘어갈 수 있음
                    try {
                        Element TR, TR_TYPE;
                        String TYPE;
                        TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum);

                        TR_TYPE = (Element) TR.getAllElements(HTMLElementName.TD).get(0);

                        TYPE = Html.fromHtml(TR_TYPE.getContent().toString().trim()).toString();    //타입에 wt가 많아서 trim으로 없앰

                        if (TYPE.equals("공지")) {
                            position++;
                        }

                    } catch (Exception e) {
                        Log.d("reParERROR_TR", e + " ");
                    }
                }
                Log.d("refresh_position", "" + position);
                int trNum2;
                int size = position + 20;
                String TITLE, DATE;
                try {
                    for (trNum2 = position + 1; trNum2 < TBODY.getAllElements(HTMLElementName.TR).size(); trNum2++) {  // 번호 게시글부터 끝까지 파
                        Element TR;
                        TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum2);    //제일 최신 번호게시글

                        Element TR_INFO, TR_INFO_A, TR_DATE;


                        TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목
                        TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                        TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌

                        TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                        DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                        if (Ncount == 0) {
                            NoldTITLE.add(TITLE);
                            NoldDATE.add(DATE);
                        } else if (Ncount > 0) {
                            NnewTITLE.add(TITLE);
                            NnewDATE.add(DATE);

                        }
                    }
//                        Log.d("refresh_old",""+NoldTITLE.get(0).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(1).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(19).toString());
                    if (Ncount > 0) {
                        Log.d("refresh_old", "" + NoldTITLE.get(0).toString());
                        Log.d("refresh_new", "" + NnewTITLE.get(0).toString());
                        Log.d("refresh_count", "" + Ncount);
                        int i, j, k;
                        outerLoop:
                        for (i = 0; i < 20; i++) {
                            for (j = 0; j < 20; j++) {
                                if (NnewTITLE.get(i).toString().equals(NoldTITLE.get(j).toString()) && NnewDATE.get(i).toString().equals(NoldDATE.get(j).toString())) { //첫번째 아이템에서 old와 new가 같은게 있으면
                                    Log.d("1_refresh", "first item");
                                    NoldTITLE.clear();
                                    NoldDATE.clear();
                                    NoldTITLE.addAll(NnewTITLE);
                                    NoldDATE.addAll(NnewDATE);
                                    NnewTITLE.clear();
                                    NnewDATE.clear();
//                                            Log.d("refresh_er_old",""+NoldTITLE.get(0).toString());
                                    Ncheck = 1;
                                    break outerLoop;
                                } else { //첫번째 아이템 비교에서 없으면
                                    Log.d("2_1not_refresh", "first item fail");
                                    for (k = j + 1; k < 20; k++) {  //old의 두번째부터
                                        if (NnewTITLE.get(i).toString().equals(NoldTITLE.get(k).toString()) && NnewDATE.get(i).toString().equals(NoldDATE.get(k).toString())) {
                                            Log.d("2_1_1_refresh", "found");
                                            NoldTITLE.clear();
                                            NoldDATE.clear();
                                            NoldTITLE.addAll(NnewTITLE);
                                            NoldDATE.addAll(NnewDATE);
                                            NnewTITLE.clear();
                                            NnewDATE.clear();
                                            Ncheck = 1;
                                            break outerLoop;
                                        } else {
                                            Log.d("2_1_2_refresh", "not found");
                                            Ncheck = 0;    //찾는게 도저히 없을때 ㅋㅋㅋ
                                            continue;
                                        }
                                    }
                                    if (Ncheck == 0) {
                                        Log.d("2_2_not_refresh", "noti");
                                        //notification 띄움
                                        MainNotice.noticekey=1;
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Intent intent2 = new Intent(ServiceAll.this, MainNotice.class);
//                                                intent2.putExtra("plzUpdate", key);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(ServiceAll.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                                        Notification.Builder builder = new Notification.Builder(ServiceAll.this);
                                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo2));
                                        builder.setSmallIcon(R.drawable.logo2);
//                                               builder.setSmallIcon(getNotificationIcon());
                                        builder.setContentTitle(NnewTITLE.get(0).toString() + "");
                                        builder.setContentText("새로운 게시물을 확인하세요.");
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                                        builder.setContentIntent(pendingIntent);
                                        builder.setAutoCancel(true);
                                        builder.setPriority(Notification.PRIORITY_HIGH);
                                        notificationManager.notify(0, builder.build());

                                        NoldTITLE.clear();
                                        NoldDATE.clear();
                                        NoldTITLE.addAll(NnewTITLE);
                                        NoldDATE.addAll(NnewDATE);
                                        NnewTITLE.clear();
                                        NnewDATE.clear();
                                        Ncheck = 1;
                                        break outerLoop;
                                    }
                                }
                            }
                        }
                    }
                    Ncount++;
                } catch (Exception e) {
                    Log.d("refreshParError", e + "");
                }
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                        } catch (Exception e) {
                            Log.d("ERRORparsing", e + "");
                        }
                    }
                }, 0);
            } catch (Exception e) {

            }
        }
    }


    class parsingFreeboardFirstItem extends Thread {
        private parsingFreeboardFirstItem(final String url) {
            Furl = url;
        }
        public void run() {
            try {
                URL = new URL(Furl);
                InputStream html = URL.openStream();
                source = new Source(new InputStreamReader(html, "utf-8"));
                source.fullSequentialParse();   //순차적으로 구문을 분석함
            } catch (Exception e) {
                Log.d("ERROR_GETLIST", e + " ");
            }

            try {
                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); //div타입 태그 불러옴
                int tagNum;
                for (tagNum = 1; tagNum < tabletags.size(); tagNum++) { //DIV 모든 태그중 board_list 태그가 몇번째임을 구함
                    if (tabletags.get(tagNum).toString().trim().equals("<div class=\"board_list\" id=\"board_list\">")) {
                        cur = tagNum; //DIV 클래스가 board_list 면 tagNum 값을 cur로 몇번째인지 저장
                        break;
                    }
                }
                Log.d("ERROR_CUR", cur + "");
                Element DIV, TABLE, TBODY;
                DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(cur);
                TABLE = (Element) DIV.getAllElements(HTMLElementName.TABLE).get(0);
                TBODY = (Element) TABLE.getAllElements(HTMLElementName.TABLE).get(0);

                Log.d("ERROR_SIZE", tabletags.size() + "");
                Log.d("ERROR_TRSIZE", TBODY.getAllElements(HTMLElementName.TR).size() + "");

                int trNum, position = 0;
                for (trNum = 1; trNum < TBODY.getAllElements(HTMLElementName.TR).size(); trNum++) {
                    //for문으로 바꿔야 다음페이지로 넘어갈 수 있음
                    try {
                        Element TR, TR_TYPE;
                        String TYPE;
                        TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum);

                        TR_TYPE = (Element) TR.getAllElements(HTMLElementName.TD).get(0);

                        TYPE = Html.fromHtml(TR_TYPE.getContent().toString().trim()).toString();    //타입에 wt가 많아서 trim으로 없앰

                        if (TYPE.equals("공지")) {
                            position++;
                        }

                    } catch (Exception e) {
                        Log.d("reParERROR_TR", e + " ");
                    }
                }
                Log.d("refresh_position", "" + position);
                int trNum2;
                int size = position + 20;
                String TITLE, DATE;
                try {
                    for (trNum2 = position + 1; trNum2 < TBODY.getAllElements(HTMLElementName.TR).size(); trNum2++) {  // 번호 게시글부터 끝까지 파
                        Element TR;
                        TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum2);    //제일 최신 번호게시글

                        Element TR_INFO, TR_INFO_A, TR_DATE;


                        TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목
                        TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                        TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌

                        TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                        DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                        if (Fcount == 0) {
                            FoldTITLE.add(TITLE);
                            FoldDATE.add(DATE);
                        } else if (Fcount > 0) {
                            FnewTITLE.add(TITLE);
                            FnewDATE.add(DATE);

                        }
                    }
//                        Log.d("refresh_old",""+NoldTITLE.get(0).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(1).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(19).toString());
                    if (Fcount > 0) {
                        Log.d("refresh_old", "" + FoldTITLE.get(0).toString());
                        Log.d("refresh_new", "" + FnewTITLE.get(0).toString());
                        Log.d("refresh_count", "" + Fcount);
                        int i, j, k;
                        outerLoop:
                        for (i = 0; i < 20; i++) {
                            for (j = 0; j < 20; j++) {
                                if (FnewTITLE.get(i).toString().equals(FoldTITLE.get(j).toString()) && FnewDATE.get(i).toString().equals(FoldDATE.get(j).toString())) { //첫번째 아이템에서 old와 new가 같은게 있으면
                                    Log.d("1_refresh", "first item");
                                    FoldTITLE.clear();
                                    FoldDATE.clear();
                                    FoldTITLE.addAll(FnewTITLE);
                                    FoldDATE.addAll(FnewDATE);
                                    FnewTITLE.clear();
                                    FnewDATE.clear();
//                                            Log.d("refresh_er_old",""+NoldTITLE.get(0).toString());
                                    Fcheck = 1;
                                    break outerLoop;
                                } else { //첫번째 아이템 비교에서 없으면
                                    Log.d("2_1not_refresh", "first item fail");
                                    for (k = j + 1; k < 20; k++) {  //old의 두번째부터
                                        if (FnewTITLE.get(i).toString().equals(FoldTITLE.get(k).toString()) && FnewDATE.get(i).toString().equals(FoldDATE.get(k).toString())) {
                                            Log.d("2_1_1_refresh", "found");
                                            FoldTITLE.clear();
                                            FoldDATE.clear();
                                            FoldTITLE.addAll(FnewTITLE);
                                            FoldDATE.addAll(FnewDATE);
                                            FnewTITLE.clear();
                                            FnewDATE.clear();
                                            Fcheck = 1;
                                            break outerLoop;
                                        } else {
                                            Log.d("2_1_2_refresh", "not found");
                                            Fcheck = 0;    //찾는게 도저히 없을때 ㅋㅋㅋ
                                            continue;
                                        }
                                    }
                                    if (Fcheck == 0) {
                                        Log.d("2_2_not_refresh", "noti");
                                        //notification 띄움
                                        MainFreeboard.freekey=1;
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Intent intent2 = new Intent(ServiceAll.this, MainFreeboard.class);
//                                                intent2.putExtra("plzUpdate", key);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(ServiceAll.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                                        Notification.Builder builder = new Notification.Builder(ServiceAll.this);
//                                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo2));
                                        builder.setSmallIcon(R.drawable.logo2);
//                                               builder.setSmallIcon(getNotificationIcon());
                                        builder.setContentTitle(FnewTITLE.get(0).toString() + "");
                                        builder.setContentText("새로운 게시물을 확인하세요.");
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                                        builder.setContentIntent(pendingIntent);
                                        builder.setAutoCancel(true);
                                        builder.setPriority(Notification.PRIORITY_HIGH);
                                        notificationManager.notify(0, builder.build());

                                        FoldTITLE.clear();
                                        FoldDATE.clear();
                                        FoldTITLE.addAll(FnewTITLE);
                                        FoldDATE.addAll(FnewDATE);
                                        FnewTITLE.clear();
                                        FnewDATE.clear();
                                        Fcheck = 1;
                                        break outerLoop;
                                    }
                                }
                            }
                        }
                    }
                    Fcount++;
                } catch (Exception e) {
                    Log.d("refreshParError", e + "");
                }
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                        } catch (Exception e) {
                            Log.d("ERRORparsing", e + "");
                        }
                    }
                }, 0);
            }catch(Exception e) {

            }
        }
    }

//    인터넷 연결 확인
    boolean isInternetConnected() {
        manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork == null;
    }

//    private static boolean isOnline() {
//        CheckConnect cc = new CheckConnect(CONNECTION_CONFIRM_CLIENT_URL);
//        cc.start();
//        try{
////            cc.join();
//            return cc.isSuccess();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return false;
//    }
}

