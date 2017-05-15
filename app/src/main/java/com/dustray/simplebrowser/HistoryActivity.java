package com.dustray.simplebrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.dustray.adapter.FragmentPagerHistoryAdapter;
import com.dustray.tools.MyToast;

public class HistoryActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private TabLayout tlHistory;
    private ViewPager vpFindHistoryPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("历史纪录");
        db = openOrCreateDatabase("browser.db", MODE_PRIVATE, null);

        initTabLayout();
    }

    private void initTabLayout() {
        //ViewPager
        vpFindHistoryPager = (ViewPager) findViewById(R.id.vp_find_history_pager);
        FragmentPagerHistoryAdapter adapter = new FragmentPagerHistoryAdapter(getSupportFragmentManager(), this);
        vpFindHistoryPager.setAdapter(adapter);
        //TabLayout
        tlHistory = (TabLayout) findViewById(R.id.tl_history);
        tlHistory.setupWithViewPager(vpFindHistoryPager);

//        tbHistory.addTab(tbHistory.newTab().setText("本次历史记录"));
//        tbHistory.addTab(tbHistory.newTab().setText("所有历史记录"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                deleteHistoryDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("警告");
        builder.setMessage("确认清空历史纪录？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.delete("temphistory", null, null);
                db.delete("allhistory", null, null);
                MyToast.toast(HistoryActivity.this, "清空历史纪录成功");
                finish();
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}
