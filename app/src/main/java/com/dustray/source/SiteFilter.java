package com.dustray.source;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dustray.entity.KeywordEntity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Dustray on 2016/11/8 0008.
 */

public class SiteFilter {
    private List<KeywordEntity> keywordList = new ArrayList<KeywordEntity>();
    private SQLiteDatabase db;
    private Context mContext;
    private String theKey;

    public SiteFilter(Context mContext, SQLiteDatabase db) {
        this.mContext = mContext;
        this.db = db;
    }

    //网页链接过滤
    public boolean filterWebsite(String url) {
        boolean result = true;
        if (url == null) {
            return result;
        }
        //String keyword[] = {"weibo", "tieba", "taobao", "mbd.baidu", "video", "image.baidu", "news", "qq.com", "sohu.com", "huanqiu.com", "xinhuanet.com", "3g.163.com", "s.pae.baidu", "chinanews"};
        //initData();
        getKeyword();
        for (KeywordEntity ke : keywordList) {

            if (url.contains(ke.getKeyword())) {
                theKey = ke.getKeyword();
                result = false;
            }
        }
        return result;
    }
    //获取拦截关键字
    public String getFilterKey(){
        return theKey;
    }
    private void getKeyword() {
                /*查询所有信息*/
        Cursor c = db.rawQuery("select * from allkeyword", null);

        if (c != null) {
            while (c.moveToNext()) {
                KeywordEntity ke = new KeywordEntity();
                ke.setKeyword(c.getString(c.getColumnIndex("keyword")));
                keywordList.add(ke);
            }
            c.close();//释放游标
        }

    }

    //页内文本过滤
    public boolean filterKeyWord(String html) {
        boolean result = true;
        String keyword[] = {"新闻网", "日报", "记者", "日讯", "近日", "媒体", "报道", "依法", "调查", "摄影师", "网友", "爆料", "微博", "全国", "新华", "央视"};
        for (int i = 0; i < keyword.length; i++) {
            if (html.contains(keyword[i])) {
                theKey = keyword[i];
                result = false;
            }
        }
        return result;
    }

    private void initData() {

        BmobQuery<KeywordEntity> query = new BmobQuery<KeywordEntity>();
        query.setLimit(50);
        //执行查询方法
        query.findObjects(new FindListener<KeywordEntity>() {
            @Override
            public void done(List<KeywordEntity> object, BmobException e) {
                if (e == null) {
                    keywordList = object;
                    //MyToast.toast(AddKeywordActivity.this, "查询成功：共" + object.size() + "条数据。");
//                    for (KeywordEntity ke : object) {
//                        //获得playerName的信息
//                        ke.getKeyword();
//                        //获得数据的objectId信息
//                        ke.getObjectId();
//                        //获得createdAt数据创建时间（注意是：createdAt，不是createAt）
//                        ke.getCreatedAt();
//                    }
                } else {
                    //Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                    //MyToast.toast(AddKeywordActivity.this, "失败：" + e.getMessage() + "," + e.getErrorCode());

                }
            }
        });

    }
}
