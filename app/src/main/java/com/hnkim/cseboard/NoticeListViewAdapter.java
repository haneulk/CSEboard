package com.hnkim.cseboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hnkim on 2017-01-03.
 */

public class NoticeListViewAdapter extends BaseAdapter implements Filterable{
    private Context mContext = null;
    public static ArrayList<noticeListData> noticeData = new ArrayList<noticeListData>();
    public ArrayList<noticeListData> filteredItemList = noticeData;

    Filter listFilter;

        public NoticeListViewAdapter(Context context) {
            super();
            mContext = context;
        }

    //수정
//        @Override
//        public int getCount() {
//            return noticeData.size();
//        }

        @Override
        public int getCount() {
            return filteredItemList.size();
        }

    //수정
//        @Override
//        public Object getItem(int position) {
//            return noticeData.get(position);
//        }

        @Override
        public Object getItem(int position) {
            return filteredItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder holder;

            if (v == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.list_style, null);

                holder.mType = (TextView) v.findViewById(R.id.item_number);
                holder.mTitle = (TextView) v.findViewById(R.id.item_title);
                holder.mWriter = (TextView) v.findViewById(R.id.item_writer);
                holder.mDate = (TextView) v.findViewById(R.id.item_date);
                holder.mView = (TextView) v.findViewById(R.id.item_view);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            //수정
//            noticeListData mData = noticeData.get(position);
            noticeListData mData = filteredItemList.get(position);

            ImageView iconImageView = (ImageView)v.findViewById(R.id.imageview);

/*
            //공지사항부분 배경은 회색으로 덮기
            if(mData.mType.equals("공지")) {
                holder.mType.setTextColor(Color.rgb(184,70,89));
                holder.mType.setText(Html.fromHtml(mData.mType));
                holder.mTitle.setTypeface(null, Typeface.BOLD);
                holder.mTitle.setTextColor(Color.rgb(184,70,89));
                holder.mTitle.setText(Html.fromHtml(mData.mTitle));
//                holder.mTitle.setText(Html.fromHtml("<font color=\"red\">"+mData.mTitle)); //이런식으로 바꾸면 원하는 rgb 컬러로 바꾸기 어려움

                holder.mWriter.setText(mData.mWriter);
                holder.mDate.setText(mData.mDate);
                holder.mView.setText(mData.mView + "명 읽음");
                v.setBackgroundColor(Color.rgb(237,237,237));

            } else {
                holder.mType.setTextColor(Color.rgb(0,0,0));
                holder.mType.setText(Html.fromHtml(mData.mType));
                holder.mTitle.setTypeface(null, Typeface.NORMAL);
                holder.mTitle.setTextColor(Color.rgb(0,0,0));
                holder.mTitle.setText(Html.fromHtml(mData.mTitle));
                holder.mWriter.setText(Html.fromHtml(mData.mWriter));
                holder.mDate.setText(Html.fromHtml(mData.mDate));
                holder.mView.setText(Html.fromHtml(mData.mView) + "명 읽음");

//                holder.mTitle.setBackgroundColor(Color.WHITE);
            }
*/

            if(mData.mType.equals("공지")) {
                holder.mType.setTextColor(Color.rgb(184,70,89));
                holder.mType.setText(mData.getType());
                holder.mTitle.setTypeface(null, Typeface.BOLD);
                holder.mTitle.setTextColor(Color.rgb(184,70,89));
                holder.mTitle.setText(mData.getTitle());
//                holder.mTitle.setText(Html.fromHtml("<font color=\"red\">"+mData.mTitle)); //이런식으로 바꾸면 원하는 rgb 컬러로 바꾸기 어려움

                holder.mWriter.setText(mData.getWriter());
                holder.mDate.setText(mData.getDate());
                holder.mView.setText(mData.getView() + "명 읽음");
                v.setBackgroundColor(Color.rgb(237,237,237));
                iconImageView.setImageResource(R.drawable.tack4);

//

            } else {
                holder.mType.setTextColor(Color.rgb(0,0,0));
                holder.mType.setText(Html.fromHtml(mData.mType));
                holder.mTitle.setTypeface(null, Typeface.NORMAL);
                holder.mTitle.setTextColor(Color.rgb(0,0,0));
                holder.mTitle.setText(Html.fromHtml(mData.mTitle));
                holder.mWriter.setText(Html.fromHtml(mData.mWriter));
                holder.mDate.setText(Html.fromHtml(mData.mDate));
                holder.mView.setText(Html.fromHtml(mData.mView) + "명 읽음");
                iconImageView.setImageDrawable(mData.getIcon());

//                holder.mTitle.setBackgroundColor(Color.WHITE);
            }

            return v;
        }

        public class ViewHolder {
            public TextView mType;
            public TextView mTitle;
            public TextView mUrl;
            public TextView mWriter;
            public TextView mDate;
            public TextView mView;
        }

    @Override
    public Filter getFilter() {
        if(listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0) {
                results.values = noticeData;
                results.count = noticeData.size();
            } else {
                ArrayList<noticeListData> itemList = new ArrayList<noticeListData>();
                for(noticeListData item : noticeData) {
                    if(item.getTitle().toLowerCase().contains(constraint.toString().toLowerCase()) || item.getWriter().contains(constraint.toString())) {
//                        int startPos = item.toString().indexOf(constraint.toString().toLowerCase());
//                        int endPos = startPos + constraint.toString().toLowerCase().length();
//                        Spannable spanText = Spannable.Factory.getInstance().newSpannable(filteredItemList.get(position).getTitle())
                        itemList.add(item);
                    }
                }
                results.values = itemList;
                results.count = itemList.size();
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            filteredItemList = (ArrayList<noticeListData>) results.values;

            if(results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}



