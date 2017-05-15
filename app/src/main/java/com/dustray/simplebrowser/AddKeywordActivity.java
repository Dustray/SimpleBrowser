package com.dustray.simplebrowser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.dustray.adapter.KeywordAdapter;
import com.dustray.adapter.KeywordAdapter.OnItemClickListener;
import com.dustray.entity.KeywordEntity;
import com.dustray.tools.GetKeyword;
import com.dustray.tools.MyToast;
import com.dustray.tools.SharedPreferencesHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class AddKeywordActivity extends AppCompatActivity implements View.OnClickListener {

    private List<KeywordEntity> keywordList = new ArrayList<KeywordEntity>();
    private KeywordAdapter keywordAdapter;

    private RecyclerView rvKeyword;
    private TextInputEditText keywordText;
    private Button addKeywordBtn;

    private SQLiteDatabase db;
    private String myUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_keyword);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("添加关键字");
        getSupportActionBar().setElevation(0);
        db = openOrCreateDatabase("browser.db", MODE_PRIVATE, null);//初始化 db
        keywordText = (TextInputEditText) findViewById(R.id.keyword_text);
        keywordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    submitKeyWordToNet();
                    return false;
                } else {
                    return false;
                }
            }
        });
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            //传递name参数为tinyphp
            myUrl = bundle.getString("url");
            keywordText.setText(myUrl);
        }
        addKeywordBtn = (Button) findViewById(R.id.add_keyword_btn);
        addKeywordBtn.setOnClickListener(this);
        rvKeyword = (RecyclerView) findViewById(R.id.rv_keyword);

        initData();

    }

    /**
     * 初始化/刷新Adapter，初始化/刷新RecyclerView
     */
    private void initRecyclerView() {
        keywordAdapter = new KeywordAdapter(AddKeywordActivity.this, keywordList);
        //item长按/短按事件
        keywordAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLongClick(int position) {

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d1 = null, d2 = null;
                try {
                    d1 = sDateFormat.parse(sDateFormat.format(new Date()));
                    d2 = sDateFormat.parse(keywordList.get(position).getCreatedAt());
                    long diff = d1.getTime() - d2.getTime();//这样得到的差值是微秒级别
                    SharedPreferencesHelper spHelper = new SharedPreferencesHelper(AddKeywordActivity.this);
                    if (spHelper.getUserType() == 1 || diff < 300000) {//用户类型为监护人或时长小于300秒
                        deleteKeywordFromNet(keywordList.get(position).getObjectId());
                    } else {
                        MyToast.toast(AddKeywordActivity.this, "时间超过5分钟，不能删除");

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    MyToast.toast(AddKeywordActivity.this, "删除异常" + e.toString());
                }
            }

            @Override
            public void onClick(int position) {
                MyToast.toast(AddKeywordActivity.this, "提示，长按删除");
            }
        });

        //MyToast.toast(AddKeywordActivity.this, "查询成功：共" + keywordList.size() + "条数据。");
        rvKeyword = (RecyclerView) findViewById(R.id.rv_keyword);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        rvKeyword.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        rvKeyword.setAdapter(keywordAdapter);
        //设置分隔线
        //rvKeyword.addItemDecoration(new DividerGridItemDecoration(this));
    }

    /**
     * 初始化数据（从Bmob查询keyword）
     */
    private void initData() {
        BmobQuery<KeywordEntity> query = new BmobQuery<KeywordEntity>();
        query.setLimit(100);
        //执行查询方法
        query.order("-createdAt");
        query.findObjects(new FindListener<KeywordEntity>() {
            @Override
            public void done(List<KeywordEntity> object, BmobException e) {
                if (e == null) {
                    keywordList = object;
                    initRecyclerView();
                } else {
                    MyToast.toast(AddKeywordActivity.this, "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 从Bmob删除一条数据
     */
    public void deleteKeywordFromNet(String id) {
        KeywordEntity ke = new KeywordEntity();
        ke.setObjectId(id);
        ke.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {

                    initData();
                    GetKeyword gk = new GetKeyword(AddKeywordActivity.this, db);
                    gk.getKeywordFromNet();
                    MyToast.toast(AddKeywordActivity.this, "删除成功");
                } else {
                    MyToast.toast(AddKeywordActivity.this, "删除失败" + e.toString());
                }
            }
        });
    }

    /**
     * 普通点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_keyword_btn) {
            if (keywordText.getText().toString().equals("")) {
                MyToast.toast(AddKeywordActivity.this, "关键字不能为空");
            } else {
                submitKeyWordToNet();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        //收起键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    0);
        }
    }

    /**
     * 提交keyword到Bmob
     */
    private void submitKeyWordToNet() {
        KeywordEntity ke = new KeywordEntity();
        ke.setKeyword(keywordText.getText().toString());
        ke.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    MyToast.toast(AddKeywordActivity.this, "添加数据成功");
                    keywordText.setText("");

                    //从bmob查询并刷新adapter
                    initData();
                    //软件更新本地关键字
                    GetKeyword gk = new GetKeyword(AddKeywordActivity.this, db);
                    gk.getKeywordFromNet();

                } else {
                    MyToast.toast(AddKeywordActivity.this, "创建数据失败：" + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
