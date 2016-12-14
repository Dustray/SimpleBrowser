package com.dustray.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dustray.entity.UrlEntity;
import com.dustray.simplebrowser.R;

import java.util.List;

/**
 * Created by Dustray on 2016/11/28 0028.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<UrlEntity> historyList;
    private Context mContext;
    private LayoutInflater inflater;
    private HistoryAdapter.OnItemClickListener mOnItemClickListener;

    public HistoryAdapter(Context context, List<UrlEntity> datas) {
        this.mContext = context;
        this.historyList = datas;
        inflater = LayoutInflater.from(mContext);
    }

    public interface OnItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnItemClickListener(HistoryAdapter.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_history, parent, false);
        HistoryAdapter.MyViewHolder holder = new HistoryAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.MyViewHolder holder, final int position) {
        String title = historyList.get(position).getTheTitle();
        String url = historyList.get(position).getTheURL();
        if (title.length() > 40) {
            title = title.substring(0, 39) + "...";
        }
        if (url.length() > 40) {
            url = url.substring(0, 39) + "...";
        }
        holder.historyTitleText.setText(title);
        holder.historyUrlText.setText(url);

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    //如果返回值为true的话这个点击事件会被长点击独占，否则相反。
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView historyTitleText;
        TextView historyUrlText;

        public MyViewHolder(View view) {
            super(view);
            historyTitleText = (TextView) view.findViewById(R.id.c_item_history_title_text);
            historyUrlText = (TextView) view.findViewById(R.id.c_item_history_url_text);
        }

    }
}
