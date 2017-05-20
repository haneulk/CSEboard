package com.hnkim.cseboard;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
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
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hnkim on 2017-01-03.
 */

class parsingNotice extends Thread {
    public static URL URL;
    private static Source source;
    private static int cur;
    private String purl = "";
    private int pnCount = 0;
    final Calendar cc = Calendar.getInstance();
    private final int mYear = cc.get(Calendar.YEAR);
    private final int pYear = mYear - 1;
    private final int mMonth = cc.get(Calendar.MONTH);

    public static int flag1 = 0;


    public parsingNotice(String url, int nCount) {
        purl = url;
        pnCount = nCount;
    }

    Context mContext;
    public parsingNotice(Context context) {
        mContext = context;
    }

    public void run() {
        try {
            URL = new URL(purl);
            InputStream html = URL.openStream();
            source = new Source(new InputStreamReader(html, "utf-8"));
            source.fullSequentialParse();   //순차적으로 구문을 분석함

        } catch (Exception e) {
            Log.d("ERROR_GETLIST", e + " ");
        }
        flag1 =0 ;
        try {
            List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); //div타입 태그 불러옴

            int tagNum;
            for (tagNum = 1; tagNum < tabletags.size(); tagNum++) { //DIV 모든 태그중 board_list 태그가 몇번째임을 구함
                if (tabletags.get(tagNum).toString().trim().equals("<div class=\"board_list\" id=\"board_list\">")) {
                    cur = tagNum; //DIV 클래스가 board_list 면 tagNum 값을 cur로 몇번째인지 저장
                    break;
                }
            }
//            Log.d("ERROR_CUR", cur + "");
            Element DIV, TABLE, TBODY;
            DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(cur);
            TABLE = (Element) DIV.getAllElements(HTMLElementName.TABLE).get(0);
            TBODY = (Element) TABLE.getAllElements(HTMLElementName.TABLE).get(0);


//            Log.d("ERROR_SIZE", tabletags.size() + "");
//            Log.d("ERROR_TRSIZE", TBODY.getAllElements(HTMLElementName.TR).size() + "");

            int trNum;
            for (trNum = 1; trNum < TBODY.getAllElements(HTMLElementName.TR).size(); trNum++) {
                //for문으로 바꿔야 다음페이지로 넘어갈 수 있음

                try {
                    Element TR, TR_TYPE;
                    String TYPE;
                    TR = (Element) TBODY.getAllElements(HTMLElementName.TR).get(trNum);

                    TR_TYPE = (Element) TR.getAllElements(HTMLElementName.TD).get(0);

                    TYPE = Html.fromHtml(TR_TYPE.getContent().toString().trim()).toString();    //타입에 wt가 많아서 trim으로 없앰

                    if (pnCount == 1) {
                        if (TYPE.equals("공지")) {
                            Element TR_INFO, TR_INFO_A, TR_WRITER, TR_DATE, TR_VIEW;
                            String TITLE, URL, WRITER, DATE, VIEW;
                            TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목

                            TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                            TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌
                            URL = TR_INFO_A.getAttributeValue("href");

                            TR_WRITER = (Element) TR.getAllElements(HTMLElementName.TD).get(2);
                            WRITER = Html.fromHtml(TR_WRITER.getContent().toString().trim()).toString();

                            TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                            DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                            TR_VIEW = (Element) TR.getAllElements(HTMLElementName.TD).get(4);
                            VIEW = Html.fromHtml(TR_VIEW.getContent().toString().trim()).toString();

                            NoticeListViewAdapter.noticeData.add(new noticeListData(TYPE, TITLE, URL, WRITER, DATE, VIEW));
                        }
                        if (!TYPE.equals("공지")) {
                            Element TR_INFO, TR_INFO_A, TR_WRITER, TR_DATE, TR_VIEW;
                            String TITLE, URL, WRITER, DATE, VIEW;
                            TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목

                            TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                            TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌
                            URL = TR_INFO_A.getAttributeValue("href");

                            TR_WRITER = (Element) TR.getAllElements(HTMLElementName.TD).get(2);
                            WRITER = Html.fromHtml(TR_WRITER.getContent().toString().trim()).toString();

                            TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                            DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                            TR_VIEW = (Element) TR.getAllElements(HTMLElementName.TD).get(4);
                            VIEW = Html.fromHtml(TR_VIEW.getContent().toString().trim()).toString();

                            NoticeListViewAdapter.noticeData.add(new noticeListData(TYPE, TITLE, URL, WRITER, DATE, VIEW));
                            NumberListViewAdapter.numberData.add(new numberListData(TYPE, TITLE, URL, WRITER, DATE, VIEW));
                        }
                    } else if (pnCount == 2 && !TYPE.equals("공지")) {
                        Element TR_INFO, TR_INFO_A, TR_WRITER, TR_DATE, TR_VIEW;
                        String TITLE, URL, WRITER, DATE, VIEW;
                        TR_INFO = (Element) TR.getAllElements(HTMLElementName.TD).get(1);    //url과 게시글제목

                        TR_INFO_A = (Element) TR_INFO.getAllElements(HTMLElementName.A).get(0);
                        TITLE = Html.fromHtml(TR_INFO_A.getContent().toString().trim()).toString(); //trim을 써야 span으로 생긴 쓸데없는 공간 빼줌
                        URL = TR_INFO_A.getAttributeValue("href");

                        TR_WRITER = (Element) TR.getAllElements(HTMLElementName.TD).get(2);
                        WRITER = Html.fromHtml(TR_WRITER.getContent().toString().trim()).toString();

                        TR_DATE = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
                        DATE = Html.fromHtml(TR_DATE.getContent().toString().trim()).toString();

                        TR_VIEW = (Element) TR.getAllElements(HTMLElementName.TD).get(4);
                        VIEW = Html.fromHtml(TR_VIEW.getContent().toString().trim()).toString();

                        int _month = Integer.valueOf(DATE.substring(5, 7));
                        int month = mMonth - _month;


                        String cYear = String.valueOf(pYear);
                        if (DATE.substring(0, 4).equals(mYear) || DATE.substring(0, 4).equals(cYear)) {
                            if (month == 0 || (month > -12 && month < -7) || (month >= 1 && month <= 4)) {
                                NoticeListViewAdapter.noticeData.add(new noticeListData(TYPE, TITLE, URL, WRITER, DATE, VIEW));
                                NumberListViewAdapter.numberData.add(new numberListData(TYPE, TITLE, URL, WRITER, DATE, VIEW));
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.d("ERROR_TR", e + " ");
                }
            }


            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        MainNotice.noticeAdapter.notifyDataSetChanged(); //list 갱신
                        MainNotice.numberAdapter.notifyDataSetChanged();
                        MainNotice.fastScrollView.listItemsChanged();
                        MainNotice.fastScrollView2.listItemsChanged();
                    } catch (Exception e) {
                        Log.d("ERRORparsing", e + "");
                    }
                }
            }, 0);
        } catch (Exception e) {
            Log.d("haneul", e + "");
            flag1 = 1;
        }

    }

}

