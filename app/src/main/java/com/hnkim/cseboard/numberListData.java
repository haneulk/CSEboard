package com.hnkim.cseboard;

import android.graphics.drawable.Drawable;

/**
 * Created by hnkim on 2017-01-03.
 */

public class numberListData {

    public String mType;
    public String mTitle;
    public String mUrl;
    public String mWriter;
    public String mDate;
    public String mView;
    public Drawable iconDrawable;

    public numberListData() {
    }

    public numberListData(String mType, String mTitle, String mUrl, String mWriter, String mDate, String mView) {
        this.mType = mType;
        this.mTitle = mTitle;
        this.mUrl = mUrl;
        this.mWriter = mWriter;
        this.mDate = mDate;
        this.mView = mView;
    }

    public void setType(String type) {
        mType = type;
    }
    public void setTitle(String title) {
        mTitle = title;
    }
    public void setUrl(String url) {
        mUrl = url;
    }
    public void setWriter(String writer) {
        mWriter = writer;
    }
    public void setDate(String date) {
        mDate = date;
    }
    public void setView(String view) {
        mView = view;
    }

    public String getType() {
        return this.mType;
    }
    public String getTitle() {
        return this.mTitle;
    }
    public String getUrl() {
        return this.mUrl;
    }
    public String getWriter() {
        return this.mWriter;
    }
    public String getDate() {
        return this.mDate;
    }
    public String getView() {
        return this.mView;
    }
    public Drawable getIcon() {return this.iconDrawable;}
}