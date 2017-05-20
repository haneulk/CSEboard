package com.hnkim.cseboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hnkim on 2017-01-14.
 */

public class KeywordService extends Service {
    private int count=0, pcount=0, check=1;

    private int Tcount=0, Dcount=1;

    public static java.net.URL URL;
    private static Source source;
    private static int cur;


    ArrayList<String> oldTITLE = new ArrayList<String>();
    ArrayList<String> oldDATE = new ArrayList<String>();
    ArrayList<String> newTITLE = new ArrayList<String>();
    ArrayList<String> newDATE = new ArrayList<String>();

    @Override
    public void onCreate() {
//        Toast.makeText(ServiceNotice.this, "서비스를 시작합니다.", Toast.LENGTH_LONG).show();
//        oldList.add(0);
    }


    //서비스가 종료될때 실행됨
    @Override
    public void onDestroy() {
//        Toast.makeText(ServiceNotice.this, "서비스를 종료합니다.", Toast.LENGTH_LONG).show();
    }

    //서비스가 호출될때마다 실행됨
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSecond =c.get(Calendar.SECOND);

//        Toast.makeText(ServiceNotice.this, "현재 시간"+mHour+":"+mMinute+":"+mSecond, Toast.LENGTH_LONG).show();
        Log.d("refresh start!!","......................");
        Log.d("refresh time", mHour+":"+mMinute+":"+mSecond);
        try {
            parsingFirstItem(MainNotice.url);
        } catch (Exception e) {
            Log.d("refreshPar", e+"");
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
    public void parsingFirstItem(final String url) throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL = new URL(url);
                    InputStream html = URL.openStream();
                    source = new Source(new InputStreamReader(html, "utf-8"));
                    source.fullSequentialParse();   //순차적으로 구문을 분석함
                } catch (Exception e) {
                    Log.d("ERROR_GETLIST", e+" ");
                }

                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); //div타입 태그 불러옴
                int tagNum;
                for (tagNum = 1; tagNum < tabletags.size(); tagNum++) { //DIV 모든 태그중 board_list 태그가 몇번째임을 구함
                    if (tabletags.get(tagNum).toString().trim().equals("<div class=\"board_list\" id=\"board_list\">")) {
                        cur = tagNum; //DIV 클래스가 board_list 면 tagNum 값을 cur로 몇번째인지 저장
                        break;
                    }
                }
                Log.d("ERROR_CUR",cur+"");
                Element DIV, TABLE, TBODY;
                DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(cur);
                TABLE = (Element) DIV.getAllElements(HTMLElementName.TABLE).get(0);
                TBODY = (Element) TABLE.getAllElements(HTMLElementName.TABLE).get(0);



                Log.d("ERROR_SIZE",tabletags.size()+"");
                Log.d("ERROR_TRSIZE",TBODY.getAllElements(HTMLElementName.TR).size()+"");

                int trNum, position=0;
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
                Log.d("refresh_position",""+position);
                int trNum2;
                int size = position + 20;
                String TITLE, DATE;
                try {
                    for (trNum2 = position+1; trNum2 < TBODY.getAllElements(HTMLElementName.TR).size(); trNum2++) {  // 번호 게시글부터 끝까지 파
                        Element TR;
                        TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum2);    //제일 최신 번호게시글

                        Element TR_INFO, TR_INFO_A, TR_DATE;


                        TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목
                        TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                        TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌

                        TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                        DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                        if (count == 0) {
                            oldTITLE.add(TITLE);
                            oldDATE.add(DATE);
                        } else if (count > 0) {
                            newTITLE.add(TITLE);
                            newDATE.add(DATE);

                        }
                    }
//                        Log.d("refresh_old",""+NoldTITLE.get(0).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(1).toString());
//                        Log.d("refresh_old",""+NoldTITLE.get(19).toString());
                    if (count > 0) {
                        Log.d("refresh_old",""+oldTITLE.get(0).toString());
                        Log.d("refresh_new",""+newTITLE.get(0).toString());
                        Log.d("refresh_count",""+count);
                        int i, j, k;
                        outerLoop :
                        for (i = 0; i < 20; i++) {
                            for (j = 0; j < 20; j++) {
                                if (newTITLE.get(i).toString().equals(oldTITLE.get(j).toString()) && newDATE.get(i).toString().equals(oldDATE.get(j).toString())) { //첫번째 아이템에서 old와 new가 같은게 있으면
                                    Log.d("1_refresh", "first item");
                                    oldTITLE.clear();
                                    oldDATE.clear();
                                    oldTITLE.addAll(newTITLE);
                                    oldDATE.addAll(newDATE);
                                    newTITLE.clear();
                                    newDATE.clear();
//                                            Log.d("refresh_er_old",""+NoldTITLE.get(0).toString());
                                    check = 1;
                                    break outerLoop;
                                } else { //첫번째 아이템 비교에서 없으면
                                    Log.d("2_1not_refresh", "first item fail");
                                    for (k = j + 1; k < 20; k++) {  //old의 두번째부터
                                        if (newTITLE.get(i).toString().equals(oldTITLE.get(k).toString()) && newDATE.get(i).toString().equals(oldDATE.get(k).toString())) {
                                            Log.d("2_1_1_refresh", "found");
                                            oldTITLE.clear();
                                            oldDATE.clear();
                                            oldTITLE.addAll(newTITLE);
                                            oldDATE.addAll(newDATE);
                                            newTITLE.clear();
                                            newDATE.clear();
                                            check = 1;
                                            break outerLoop;
                                        } else {
                                            Log.d("2_1_2_refresh", "not found");
                                            check = 0;    //찾는게 도저히 없을때 ㅋㅋㅋ
                                            continue;
                                        }
                                    }
                                    if (check == 0) {
                                        Log.d("2_2_not_refresh", "noti");
                                        //notification 띄움
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Intent intent2 = new Intent(KeywordService.this, MainNotice.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(KeywordService.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                                        Notification.Builder builder = new Notification.Builder(KeywordService.this);
                                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo2));
                                        builder.setSmallIcon(R.drawable.logo2);
                                        builder.setContentTitle("CSEboard");
                                        builder.setContentText("새로운 알림이 도착하였습니다.");
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                                        builder.setContentIntent(pendingIntent);
                                        builder.setAutoCancel(true);
                                        notificationManager.notify(0, builder.build());

                                        oldTITLE.clear();
                                        oldDATE.clear();
                                        oldTITLE.addAll(newTITLE);
                                        oldDATE.addAll(newDATE);
                                        newTITLE.clear();
                                        newDATE.clear();
                                        check = 1;
                                        break outerLoop;
                                    }

                                }
                            }
                        }
                    }
                    count++;
                }
                catch (Exception e) {
                    Log.d("refreshParError", e + "");
                }

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                        }catch (Exception e) {
                            Log.d("ERRORparsing", e+"");
                        }
                    }
                }, 0);
            }
        }.start();
    }

}
