package com.hnkim.cseboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by hnkim on 2017-01-03.
 */

//번호 ListViewAdapter
public class NumberListViewAdapter extends BaseAdapter implements Filterable {
    private Context mContext = null;
    public static  ArrayList<numberListData> numberData = new ArrayList<numberListData>();
    public ArrayList<numberListData> filteredItemList2 = numberData;



    Filter listFilter2;

    public NumberListViewAdapter(Context context) {
        super();
        mContext = context;
//        this.arrayList.addAll(numberData);
    }

    @Override
    public int getCount() {
        return filteredItemList2.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItemList2.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    public String getTitle() { return numberData.}

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
        numberListData mData = filteredItemList2.get(position);
        ImageView iconImageView = (ImageView)v.findViewById(R.id.imageview);
        /*
        //공지사항부분 배경은 회색으로 덮기
        if(!mData.mType.equals("공지")) {
            holder.mType.setTextColor(Color.rgb(0,0,0));
            holder.mType.setText(Html.fromHtml(mData.mType));
            holder.mTitle.setTypeface(null, Typeface.NORMAL);
            holder.mTitle.setTextColor(Color.rgb(0,0,0));
            holder.mTitle.setText(Html.fromHtml(mData.mTitle));
            holder.mWriter.setText(Html.fromHtml(mData.mWriter));
            holder.mDate.setText(Html.fromHtml(mData.mDate));
            holder.mView.setText(Html.fromHtml(mData.mView) + "명 읽음");
        }
*/
//        if(!mData.mType.equals("공지")) {
            holder.mType.setTextColor(Color.rgb(0,0,0));
            holder.mType.setText(mData.getType());
            holder.mTitle.setTypeface(null, Typeface.NORMAL);
            holder.mTitle.setTextColor(Color.rgb(0,0,0));
            holder.mTitle.setText(mData.getTitle());
            holder.mWriter.setText(mData.getWriter());
            holder.mDate.setText(mData.getDate());
            holder.mView.setText(mData.getView() + "명 읽음");
        iconImageView.setImageDrawable(mData.getIcon());
//        }


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
        if(listFilter2 == null) {
            listFilter2 = new NumberListViewAdapter.ListFilter();
        }
        return listFilter2;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0) {
                results.values = numberData;
                results.count = numberData.size();
            } else {
                ArrayList<numberListData> itemList2 = new ArrayList<numberListData>();
                for(numberListData item : numberData) {
                    if(item.getTitle().toLowerCase().contains(constraint.toString().toLowerCase()) || item.getWriter().contains(constraint.toString())) {
                        itemList2.add(item);
                    }
                }
                results.values = itemList2;
                results.count = itemList2.size();
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            filteredItemList2 = (ArrayList<numberListData>) results.values;

            if(results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
