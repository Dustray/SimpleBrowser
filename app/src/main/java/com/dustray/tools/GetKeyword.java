package com.dustray.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dustray.entity.KeywordEntity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by Dustray on 2016/11/27 0027.
 */

public class GetKeyword {
    private List<KeywordEntity> keywordList = new ArrayList<KeywordEntity>();
    private SQLiteDatabase db;
    private Context mContext;

    public GetKeyword(Context context, SQLiteDatabase db) {
        this.mContext = context;
        this.db = db;
    }

    /**
     * 转存keyword到数据库
     */
    public void recordKeywordToSqlite() {
        for (KeywordEntity ke : keywordList) {
            db.execSQL("insert into allkeyword(keyword)values('" + ke.getKeyword() + "')");
        }
    }

    /**
     * 从bmob获取keyword
     */
    public void getKeywordFromNet() {
        BmobQuery<KeywordEntity> query = new BmobQuery<KeywordEntity>();
        query.setLimit(100);
        //执行查询方法
        query.order("-createdAt");
        query.findObjects(new FindListener<KeywordEntity>() {
            @Override
            public void done(List<KeywordEntity> object, BmobException e) {
                if (e == null) {
                    keywordList = object;
                    db.delete("allkeyword",null,null);
                    recordKeywordToSqlite();
                    MyToast.toast(mContext, "更新过滤器成功：共" + object.size() + "条数据。");
                } else {
                    //Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                    MyToast.toast(mContext, "失败：" + e.getMessage() + "," + e.getErrorCode());

                }
            }
        });


    }
}
