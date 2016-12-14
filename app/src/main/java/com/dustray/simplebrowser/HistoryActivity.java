package com.dustray.simplebrowser;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.dustray.adapter.HistoryAdapter;
import com.dustray.entity.UrlEntity;
import com.dustray.tools.MyToast;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private List<UrlEntity> historyList = new ArrayList<UrlEntity>();
    private HistoryAdapter historyAdapter;
    private RecyclerView rvHistory;
    private SQLiteDatabase db;
    private int historyType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        rvHistory = (RecyclerView) findViewById(R.id.rv_history);
        db = openOrCreateDatabase("browser.db", MODE_PRIVATE, null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("历史纪录");

        Bundle bundle = this.getIntent().getExtras();
        //传递name参数为tinyphp
        historyType = bundle.getInt("type");

        initData();
        initRecyclerView();
    }

    /**
     * 初始化/刷新Adapter，初始化/刷新RecyclerView
     */
    private void initRecyclerView() {
        historyAdapter = new HistoryAdapter(HistoryActivity.this, historyList);
        //item长按/短按事件
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onLongClick(int position) {
                MyToast.toast(HistoryActivity.this, "长按还没啥用。。。");
            }

            @Override
            public void onClick(int position) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                String passString = historyList.get(position).getTheURL();
                intent.putExtra("resultUrl", passString);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //MyToast.toast(AddKeywordActivity.this, "查询成功：共" + keywordList.size() + "条数据。");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        rvHistory.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        rvHistory.setAdapter(historyAdapter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        getHistory();
    }

    /**
     * 获取历史纪录
     */
    private void getHistory() {
                /*查询所有信息*/
        Cursor c = null;
        if (historyType == 1) {
            c = db.rawQuery("select * from temphistory order by _id desc", null);
        } else if (historyType == 0) {
            c = db.rawQuery("select * from allhistory order by _id desc", null);
        }
        if (c != null) {
            while (c.moveToNext()) {
                UrlEntity ue = new UrlEntity();
                ue.setTheTitle(c.getString(c.getColumnIndex("title")));
                ue.setTheURL(c.getString(c.getColumnIndex("url")));
                historyList.add(ue);
            }
            c.close();//释放游标
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.c_btn_history_delete:
                db.delete("temphistory", null, null);
                db.delete("allhistory", null, null);
                MyToast.toast(HistoryActivity.this, "清空历史纪录成功");
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
