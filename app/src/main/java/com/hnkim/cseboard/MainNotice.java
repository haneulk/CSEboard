package com.hnkim.cseboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class MainNotice extends AppCompatActivity {
    public static String HOMEPAGE = "http://cse.kut.ac.kr/";
    public static String WEBVIEW = "http://cse.kut.ac.kr/notice/";
    public static String NOTICE = "notice";
    public static String NOTICE2 = "index.php?mid=notice&page=2";
    public static String NOTICE3 = "index.php?mid=notice&page=3";
    public static String NOTICE4 = "index.php?mid=notice&page=4";
    public static String NOTICE5 = "index.php?mid=notice&page=5";
    public static String NOTICE6 = "index.php?mid=notice&page=6";
    public static String NOTICE7 = "index.php?mid=notice&page=7";
    public static String NOTICE8 = "index.php?mid=notice&page=8";
    public static String NOTICE9 = "index.php?mid=notice&page=9";
    public static String url=HOMEPAGE + NOTICE, url2=HOMEPAGE + NOTICE2, url3=HOMEPAGE + NOTICE3,
                           url4=HOMEPAGE + NOTICE4, url5=HOMEPAGE + NOTICE5, url6=HOMEPAGE + NOTICE6,
                            url7=HOMEPAGE + NOTICE7, url8=HOMEPAGE + NOTICE8, url9=HOMEPAGE + NOTICE9;
    public static String[] URLS = {url, url2, url3, url4, url5, url6, url7, url8, url9};



    public static NoticeListViewAdapter noticeAdapter = null;
    public static NumberListViewAdapter numberAdapter = null;
    public static CustomFastScrollView fastScrollView, fastScrollView2;

    private int nCount;
    private ConnectivityManager manager;
    public static final String CONNECTION_CONFIRM_CLIENT_URL = "http://clients3.google.com/generate_204";
    public static final String WIFE_STATE = "WIFE";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";

    public ListView noticeListView = null;
    public ListView numberListView = null;


    private ProgressDialog progressDialog;

    ToggleButton tb;
    EditText search;
    InputMethodManager imm;

    private ServiceNotice mService;    // 연결 타입 서비스
    private boolean mBound = false;    // 서비스 연결 여부

    public static String PREFERENCE_LOG_FILE;


    public static ArrayList<String> list;
    public static int noticekey=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        actionBar.setIcon(R.drawable.logo3);

        getSupportActionBar().setTitle("  공지사항");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.mylogo2);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        fastScrollView = (CustomFastScrollView) findViewById(R.id.fast_scroll_view);
        fastScrollView2 = (CustomFastScrollView) findViewById(R.id.fast_scroll_view2);

        noticeListView = (ListView)findViewById(R.id.noticeList);
        numberListView = (ListView)findViewById(R.id.numberList);
        noticeAdapter = new NoticeListViewAdapter(MainNotice.this);
        numberAdapter = new NumberListViewAdapter(MainNotice.this);
        noticeListView.setAdapter(noticeAdapter);
        numberListView.setAdapter(numberAdapter);
        fastScrollView.listItemsChanged();

        if(noticekey == 1) {
            Toast.makeText(MainNotice.this, "새로고침 버튼을 꾹~ 눌러주세요", Toast.LENGTH_LONG).show();
        }
        noticekey = 0;

        noticeListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                noticeListData mData = (noticeListData)noticeAdapter.getItem(position);
                if(isPressed(mData)) {
//                    Toast.makeText(MainNotice.this, "pressed",Toast.LENGTH_SHORT).show();
//                    String BB = mData.mUrl;
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BB)));

                    String N = mData.mUrl.substring(35);
                    String NU = WEBVIEW + N;

                    Log.d("ERROR_myurl",NU+"");
                    Intent numin = new Intent(MainNotice.this, WebViewContent.class);
                    numin.putExtra("url", NU);
                    startActivity(numin);
                }

                //
                //
            }
        });

        numberListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                numberListData mData = (numberListData)numberAdapter.getItem(position);
                if(isPressed(mData)) {
//                    Toast.makeText(MainNotice.this, "pressed", Toast.LENGTH_SHORT).show();
                    String N = mData.mUrl.substring(35);
                    String NU = WEBVIEW + N;

                    Log.d("ERROR_myurl",NU+"");
                    Intent numin = new Intent(MainNotice.this, WebViewContent.class);
                    numin.putExtra("url", NU);
                    startActivity(numin);
                }

            }
        });

            if (noticeAdapter.isEmpty() && numberAdapter.isEmpty()) {
                try {
                    if(isInternetConnected()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);

                            builder.setTitle("인터넷이 연결되지 않았습니다.")
                                    .setIcon(R.drawable.sally)
                                    .setMessage("와이파이 설정화면으로 이동하시겠습니까?")
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                            Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                            wifiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(wifiIntent);

                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(MainNotice.this, "메인화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                                            dialogInterface.cancel();
                                            finish();
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
                    }else {
                                loadingThread();
                                fastScrollView.listItemsChanged();
                                fastScrollView2.listItemsChanged();

                    }

                } catch (Exception e) {


            }
        }



        setView();

    }   //onCreate 끝

    public void setView() {


        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        tb = (ToggleButton)findViewById(R.id.tb);

        tb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tb.isChecked()) {
                    Toast.makeText(MainNotice.this, "공지 숨김", Toast.LENGTH_SHORT).show();
                    noticeListView.smoothScrollToPosition(0);
                    fastScrollView.listItemsChanged();
                    fastScrollView2.listItemsChanged();
                    noticeListView.setVisibility(View.GONE);
                    numberListView.setVisibility(View.VISIBLE);
                    search.setText("");

//                    numberlistfirstitem();
//                    Toast.makeText(MainNotice.this, ""+ list.get(0).toString(),Toast.LENGTH_LONG).show();

//                    noticeAdapter.notifyDataSetChanged();

                    try {
                        //fab
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                numberListView.smoothScrollToPosition(0);
//                                fastScrollView.listItemsChanged();
                                fastScrollView2.listItemsChanged();
//                                numberAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {

                    }

                } else {
                    Toast.makeText(MainNotice.this, "공지 표시", Toast.LENGTH_SHORT).show();
                    fastScrollView.listItemsChanged();
                    noticeListView.setVisibility(View.VISIBLE);
                    search.setText("");


//                    numberListView.setVisibility(View.INVISIBLE);
//                    fastScrollView.listItemsChanged();
//                    noticeAdapter.notifyDataSetChanged();


                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            noticeListView.smoothScrollToPosition(0);
                            fastScrollView.listItemsChanged();
//                            noticeAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticeListView.smoothScrollToPosition(0);
//                noticeAdapter.notifyDataSetChanged();
                fastScrollView.listItemsChanged();
            }
        });

        //search
        search = (EditText)findViewById(R.id.search);
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                String filtertext = edit.toString();

                ((NoticeListViewAdapter) noticeListView.getAdapter()).getFilter().filter(filtertext);
                numberListView.setVisibility(View.GONE);

                if(numberListView.getVisibility() == View.GONE && tb.isChecked() && search != null) {
                    noticeListView.setVisibility(View.GONE);
                    numberListView.setVisibility(View.VISIBLE);
                    ((NumberListViewAdapter)numberListView.getAdapter()).getFilter().filter(filtertext);
                }
                else if(noticeListView.getVisibility() == View.VISIBLE && tb.isChecked() && search != null) {
                    ((NoticeListViewAdapter) noticeListView.getAdapter()).getFilter().filter(filtertext);
                    numberListView.setVisibility(View.GONE);
                }
            }
        });
    }

    //배경 눌렀을때 입력창 사라짐
    public void linearOnClick(View v) {
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    public void loadingThread() throws IOException {

        new Thread() {

            @Override
            public void run() {

                Handler Progress = new Handler(Looper.getMainLooper()); //네트워크 쓰레드와 별개로 따로 핸들러를 이용하여 쓰레드를 생성한다.
                Progress.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = ProgressDialog.show(MainNotice.this, "", "잠시만 기다려 주세요");
                    }
                }, 0);

                startParsing(); //공지사항 파싱 시작

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fastScrollView.listItemsChanged();
                        fastScrollView2.listItemsChanged();
                        progressDialog.dismiss(); //모든 작업이 끝나면 다이어로그 종료

                    }
                }, 0);
            }

        }.start();


    }

    //공지 아이템 눌림 확인
    boolean isPressed(noticeListData mData) {
        if(mData != null) return true;
        else return false;
    }

    //번호 아이템 눌림 확인
    boolean isPressed(numberListData mData) {
        if(mData != null) return true;
        else return false;
    }

    //토글 버튼 눌림 확인
    boolean isTogglePressed() {
        if(tb.isChecked()) return true; //공지숨겨졌음
        else return false;
    }

    //인터넷 연결 확인
     boolean isInternetConnected() {
        manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork == null;
    }

    private static boolean isOnline() {
        CheckConnect cc = new CheckConnect(CONNECTION_CONFIRM_CLIENT_URL);
        cc.start();
        try{
//            cc.join();
            return cc.isSuccess();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getWhatKindOfNetwork(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return WIFE_STATE;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_STATE;
            }
        }
        return NONE_STATE;
    }


    //menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notice_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh :
                    if(isInternetConnected()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        builder.setTitle("인터넷이 연결되지 않았습니다.")
                                .setIcon(R.drawable.sally)
                                .setMessage("와이파이 설정화면으로 이동하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                        Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                        wifiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(wifiIntent);
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(MainNotice.this, "인터넷을 연결해주세요!!", Toast.LENGTH_SHORT).show();
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
                } else {
                        if(parsingNotice.flag1 == 1) {
                            Toast.makeText(MainNotice.this, "인터넷 연결을 다시 확인해주세요", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else {
                            NoticeListViewAdapter.noticeData.clear();
                            NumberListViewAdapter.numberData.clear();
                            try {
                                loadingThread();
                                fastScrollView.listItemsChanged();
                                fastScrollView2.listItemsChanged();
                            } catch (Exception e) {
                            }
                        }
                }
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if ( progressDialog != null) {
            progressDialog.dismiss();
            fastScrollView.listItemsChanged();
            fastScrollView2.listItemsChanged();
        }
    }

    //파싱시작
    public void startParsing() {
        parsingNotice pN1 = new parsingNotice(url,1);
        parsingNotice pN2 = new parsingNotice(URLS[1],2);
        parsingNotice pN3 = new parsingNotice(URLS[2],2);
        parsingNotice pN4 = new parsingNotice(URLS[3],2);
        parsingNotice pN5 = new parsingNotice(URLS[4],2);


            try {
                pN1.start();
                pN1.join();
                if(parsingNotice.flag1 == 1) {
                    Toast.makeText(MainNotice.this, "인터넷 연결을 다시 확인해주세요", Toast.LENGTH_SHORT).show();
                }
                else {

                    pN2.start();
                    pN2.join();
                    pN3.start();
                    pN3.join();
                    pN4.start();
                    pN4.join();
                    pN5.start();
                }
                } catch (Exception e) {
            }
        }

//    }




}
