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

import static com.hnkim.cseboard.MainNotice.CONNECTION_CONFIRM_CLIENT_URL;


public class MainFreeboard extends AppCompatActivity {

    public static String FHOMEPAGE = "http://cse.kut.ac.kr/";
    public static String FWEBVIEW = "http://cse.kut.ac.kr/freeboard/";
    public static String FNOTICE = "freeboard";
    public static String FNOTICE2 = "index.php?mid=freeboard&page=2";
    public static String FNOTICE3 = "index.php?mid=freeboard&page=3";
    public static String FNOTICE4 = "index.php?mid=freeboard&page=4";
    public static String FNOTICE5 = "index.php?mid=freeboard&page=5";
    public static String FNOTICE6 = "index.php?mid=freeboard&page=6";
    public static String FNOTICE7 = "index.php?mid=freeboard&page=7";
    public static String FNOTICE8 = "index.php?mid=freeboard&page=8";
    public static String FNOTICE9 = "index.php?mid=freeboard&page=9";
    public static String url=FHOMEPAGE + FNOTICE, url2=FHOMEPAGE + FNOTICE2, url3=FHOMEPAGE + FNOTICE3,
            url4=FHOMEPAGE + FNOTICE4, url5=FHOMEPAGE + FNOTICE5, url6=FHOMEPAGE + FNOTICE6,
            url7=FHOMEPAGE + FNOTICE7, url8=FHOMEPAGE + FNOTICE8, url9=FHOMEPAGE + FNOTICE9;
    public static String[] URLS = {url, url2, url3, url4, url5, url6, url7, url8, url9};



    public static FreeNoticeListViewAdapter FreeNoticeAdapter = null;
    public static FreeNumberListViewAdapter FreeNumberAdapter = null;
    public static CustomFastScrollView fastScrollView3, fastScrollView4;

    private int nCount;
    private ConnectivityManager manager;
    public ListView FreeNoticeListView = null;
    public ListView FreeNumberListView = null;


    private ProgressDialog progressDialog;

    ToggleButton tb;
    EditText search;
    InputMethodManager imm;

    private ServiceNotice mService;    // 연결 타입 서비스
    private boolean mBound = false;    // 서비스 연결 여부

    public static String PREFERENCE_LOG_FILE;
    public static ArrayList<String> list;

    public static int freekey=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free);
        getSupportActionBar().setTitle("  자유게시판");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.mylogo2);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        fastScrollView3 = (CustomFastScrollView) findViewById(R.id.fast_scroll_view);
        fastScrollView4 = (CustomFastScrollView) findViewById(R.id.fast_scroll_view2);

        FreeNoticeListView = (ListView)findViewById(R.id.noticeList);
        FreeNumberListView = (ListView)findViewById(R.id.numberList);
        FreeNoticeAdapter = new FreeNoticeListViewAdapter(MainFreeboard.this);
        FreeNumberAdapter = new FreeNumberListViewAdapter(MainFreeboard.this);
        FreeNoticeListView.setAdapter(FreeNoticeAdapter);
        FreeNumberListView.setAdapter(FreeNumberAdapter);
        fastScrollView3.listItemsChanged();


        if(freekey == 1) {
            Toast.makeText(MainFreeboard.this, "새로고침 버튼을 꾹~ 눌러주세요", Toast.LENGTH_LONG).show();
        }
        freekey = 0;


        FreeNoticeListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FreeNoticeListData mData = (FreeNoticeListData)FreeNoticeAdapter.getItem(position);
                if(isPressed(mData)) {
                    String N = mData.mUrl.substring(38);
                    String NU = FWEBVIEW + N;

                    Log.d("ERROR_myurl",NU+"");
                    Intent numin = new Intent(MainFreeboard.this, WebViewContent.class);
                    numin.putExtra("url", NU);
                    startActivity(numin);
                }
            }
        });

        FreeNumberListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FreeNumberListData mData = (FreeNumberListData)FreeNumberAdapter.getItem(position);
                if(isPressed(mData)) {
                    String N = mData.mUrl.substring(38);
                    String NU = FWEBVIEW + N;

                    Log.d("ERROR_myurl",NU+"");
                    Intent numin = new Intent(MainFreeboard.this, WebViewContent.class);
                    numin.putExtra("url", NU);
                    startActivity(numin);
                }

            }
        });

        if (FreeNoticeAdapter.isEmpty() && FreeNumberAdapter.isEmpty()) {
            try {
                if (isInternetConnected()) { //연결 실패시
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
                                    Toast.makeText(MainFreeboard.this, "메인화면으로 돌아갑니다.",Toast.LENGTH_SHORT).show();
                                    dialogInterface.cancel();
                                    finish();
                                }
                            });
                    final AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(184,70,89));
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(172,169,162));
                        }
                    });
                    dialog.show();
                }else {
                    loadingThread();
                    fastScrollView3.listItemsChanged();
                    fastScrollView4.listItemsChanged();
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
                    Toast.makeText(MainFreeboard.this, "공지 숨김", Toast.LENGTH_SHORT).show();
                    FreeNoticeListView.smoothScrollToPosition(0);
                    fastScrollView3.listItemsChanged();
                    fastScrollView4.listItemsChanged();
                    FreeNoticeListView.setVisibility(View.GONE);
                    FreeNumberListView.setVisibility(View.VISIBLE);
                    search.setText("");

                    try {
                        //fab
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FreeNumberListView.smoothScrollToPosition(0);
//                                fastScrollView.listItemsChanged();
                                fastScrollView4.listItemsChanged();
//                                numberAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {

                    }

                } else {
                    Toast.makeText(MainFreeboard.this, "공지 표시", Toast.LENGTH_SHORT).show();
                    fastScrollView3.listItemsChanged();
                    FreeNoticeListView.setVisibility(View.VISIBLE);
                    search.setText("");


//                    numberListView.setVisibility(View.INVISIBLE);
//                    fastScrollView.listItemsChanged();
//                    noticeAdapter.notifyDataSetChanged();


                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FreeNoticeListView.smoothScrollToPosition(0);
                            fastScrollView3.listItemsChanged();
//                            noticeAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FreeNoticeListView.smoothScrollToPosition(0);
//                noticeAdapter.notifyDataSetChanged();
                fastScrollView3.listItemsChanged();
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

                ((FreeNoticeListViewAdapter) FreeNoticeListView.getAdapter()).getFilter().filter(filtertext);
                FreeNumberListView.setVisibility(View.GONE);

                if(FreeNumberListView.getVisibility() == View.GONE && tb.isChecked() && search != null) {
                    FreeNoticeListView.setVisibility(View.GONE);
                    FreeNumberListView.setVisibility(View.VISIBLE);
                    ((FreeNumberListViewAdapter)FreeNumberListView.getAdapter()).getFilter().filter(filtertext);
                }
                else if(FreeNoticeListView.getVisibility() == View.VISIBLE && tb.isChecked() && search != null) {
                    ((FreeNoticeListViewAdapter) FreeNoticeListView.getAdapter()).getFilter().filter(filtertext);
                    FreeNumberListView.setVisibility(View.GONE);
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
                        progressDialog = ProgressDialog.show(MainFreeboard.this, "", "잠시만 기다려 주세요");
                    }
                }, 0);

                startParsing(); //공지사항 파싱 시작

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fastScrollView3.listItemsChanged();
                        fastScrollView4.listItemsChanged();
                        progressDialog.dismiss(); //모든 작업이 끝나면 다이어로그 종료

                    }
                }, 0);
            }

        }.start();


    }

    //공지 아이템 눌림 확인
    boolean isPressed(FreeNoticeListData mData) {
        if(mData != null) return true;
        else return false;
    }

    //번호 아이템 눌림 확인
    boolean isPressed(FreeNumberListData mData) {
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
                if (isInternetConnected()) { //연결 실패시
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
                                    Toast.makeText(MainFreeboard.this, "인터넷을 연결해주세요!!", Toast.LENGTH_SHORT).show();
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
                    if(parsingFreeboard.flag2 == 1) {
                        Toast.makeText(MainFreeboard.this, "인터넷 연결을 다시 확인해주세요", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        FreeNoticeListViewAdapter.noticeData.clear();
                        FreeNumberListViewAdapter.numberData.clear();
                        try {
                            loadingThread();
                            fastScrollView3.listItemsChanged();
                            fastScrollView4.listItemsChanged();
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
            fastScrollView3.listItemsChanged();
            fastScrollView4.listItemsChanged();
        }
    }

    //파싱시작
    public void startParsing() {
        parsingFreeboard pN1 = new parsingFreeboard(url,1);
        parsingFreeboard pN2 = new parsingFreeboard(URLS[1],2);
        parsingFreeboard pN3 = new parsingFreeboard(URLS[2],2);
        parsingFreeboard pN4 = new parsingFreeboard(URLS[3],2);
        parsingFreeboard pN5 = new parsingFreeboard(URLS[4],2);

        try {
            pN1.start();
            pN1.join();
            if(parsingFreeboard.flag2 == 1) {
                Toast.makeText(MainFreeboard.this, "인터넷 연결을 다시 확인해주세요", Toast.LENGTH_SHORT).show();
            } else {
                pN2.start();
                pN2.join();
                pN3.start();
                pN3.join();
                pN4.start();
                pN4.join();
                pN5.start();
                pN5.join();
            }
        } catch (Exception e) {}
    }


}