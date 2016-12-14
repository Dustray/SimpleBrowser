package com.dustray.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dustray.entity.KeywordEntity;
import com.dustray.simplebrowser.R;

import java.util.List;

/**
 * Created by Dustray on 2016/11/27 0027.
 */

public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.MyViewHolder> {

    private List<KeywordEntity> keywordList;
    private Context mContext;
    private LayoutInflater inflater;
    private OnItemClickListener mOnItemClickListener;

    public KeywordAdapter(Context context, List<KeywordEntity> datas){
        this. mContext=context;
        this. keywordList=datas;
        inflater=LayoutInflater. from(mContext);
    }

    public interface OnItemClickListener{
        void onClick( int position);
        void onLongClick( int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this. mOnItemClickListener=onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout. item_keyword,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.keywordText.setText( keywordList.get(position).getKeyword());

        if( mOnItemClickListener!= null){
            holder. itemView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });

            holder. itemView.setOnLongClickListener( new View.OnLongClickListener() {
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
        return keywordList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView keywordText;
        public MyViewHolder(View view) {
            super(view);
            keywordText=(TextView) view.findViewById(R.id.c_item_keyword_text);
        }

    }

}
