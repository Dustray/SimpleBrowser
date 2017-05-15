package com.dustray.simplebrowser;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.webkit.JavascriptInterface;

import com.dustray.entity.NoFilterEntity;
import com.dustray.tools.MyToast;
import com.dustray.tools.SharedPreferencesHelper;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dustray.entity.UrlEntity;
import com.dustray.source.SiteFilter;
import com.dustray.tools.GetKeyword;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private int noFilterTime = 0, noFilterTimeTemp = 0;//单位秒
    private boolean isActivityPause = false;
    private Intent intent;
    private WebView webView;
    private String searchStr = "";
    private String nowUrl = "", nowTitle = "";
    private EditText editText;
    private TextView tvNoFilterToolbarHint;
    private LinearLayout llNoFilterToolbarHint;
    private AppCompatImageButton goBackBtn, goForwardBtn, refreshBtn, linkBtn, gohomeBtn;
    private ContentLoadingProgressBar progressBar;
    private SQLiteDatabase db;
    private List<UrlEntity> urlList = new ArrayList<UrlEntity>();
    private Vibrator vibrator;//震动
    private ButtonBarLayout ablFunctionBar;
    private Button searchBtn, showSearchBtn, cleanSearchBtn;
    private SwipeRefreshLayout swipeLayout;
    private SharedPreferencesHelper spHelper;
    private timeCount tc;
    Toolbar toolbar;
    //初始化动画
    Animation showAnimation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f);
    Animation hideAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f);
    //键盘
    InputMethodManager imm;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bmob.initialize(this, "1a67de83b1b060162bbad1b79bf1bd37");
        spHelper = new SharedPreferencesHelper(MainActivity.this);
        int a = spHelper.getUserType();
        if (a == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        //键盘初始化
        imm = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
        //动画设置时间
        showAnimation.setDuration(100);
        hideAnimation.setDuration(100);
        loadWebView();//加载SwipeRefreshLayout和WebView
        spHelper.setIsNoFilter(false);
        initNoFilterTimer();
        initializeControl();//初始化其他控件
        initializeDatabase();//初始化sqlite
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //取消滚动条
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        navigationMenuView.setVerticalScrollBarEnabled(false);
        // ATTENTION: This was auto-generated t o implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        vibrator = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);

    }

    private void initNoFilterTimer() {
        if (spHelper.getIsNoFilter()) {
            tc = new timeCount(spHelper.getNoFilterTime(), 1000);
            noFilterTimeTemp = spHelper.getNoFilterTime() / 1000;
            llNoFilterToolbarHint.setVisibility(View.VISIBLE);
            toolbar.setBackgroundResource(R.color.colorAccent);
            ablFunctionBar.setBackgroundResource(R.color.colorAccent);
            tc.start();
        }
    }

    /**
     * 创建数据库和表
     */
    private void initializeDatabase() {
         /*创建私有数据库*/
        db = openOrCreateDatabase("browser.db", MODE_PRIVATE, null);

         /*创建表，integer为整形，text为字符串，primary key代表主键，autoincrement代表自增，not null代表不为空*/
        db.execSQL("create table if not exists temphistory(_id integer primary key autoincrement, title text not null, url text not null)");//临时历史表
        db.execSQL("create table if not exists allhistory(_id integer primary key autoincrement, title text not null, url text not null)");//所有历史表
        db.execSQL("create table if not exists allkeyword(_id integer primary key autoincrement, keyword text not null)");//所有过滤关键字表
        //删除临时历史纪录表重新计算
        db.delete("temphistory", null, null);
        GetKeyword gk = new GetKeyword(MainActivity.this, db);
        gk.getKeywordFromNet();

    }

    /**
     * 初始化控件
     */
    private void initializeControl() {
        //常规控件初始化
        searchBtn = (Button) findViewById(R.id.search_btn);
        showSearchBtn = (Button) findViewById(R.id.show_search_btn);
        cleanSearchBtn = (Button) findViewById(R.id.clean_search_btn);
        showSearchBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cleanSearchBtn.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.search_edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    loadWeb();
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    // 此处为失去焦点时的处理内容

                    editText.startAnimation(hideAnimation);
                    //searchBtn.startAnimation(hideAnimation);
                    searchBtn.setVisibility(View.GONE);
                    cleanSearchBtn.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    showSearchBtn.setVisibility(View.VISIBLE);
                }
            }

        });
        editText.clearFocus();//不聚焦
        tvNoFilterToolbarHint = (TextView) findViewById(R.id.tv_no_filter_toolbar_hint);
        llNoFilterToolbarHint = (LinearLayout) findViewById(R.id.ll_no_filter_toolbar_hint);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progress_bar);

        ablFunctionBar = (ButtonBarLayout) findViewById(R.id.abl_function_bar);

        goBackBtn = (AppCompatImageButton) findViewById(R.id.goback_btn);
        goForwardBtn = (AppCompatImageButton) findViewById(R.id.goforward_btn);
        refreshBtn = (AppCompatImageButton) findViewById(R.id.refresh_btn);
        gohomeBtn = (AppCompatImageButton) findViewById(R.id.gohome_btn);
        linkBtn = (AppCompatImageButton) findViewById(R.id.link_btn);

        goBackBtn.setOnClickListener(this);
        goForwardBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        gohomeBtn.setOnClickListener(this);
        linkBtn.setOnClickListener(this);

        //toolbar初始化
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    public void noFilterTimeOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        //LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_never_show, null);
        //checkNeverShow = (CheckBox) linearLayout.findViewById(R.id.check_never_show);
        //builder.setView(linearLayout);
        builder.setTitle("提示");
        builder.setMessage("您的免屏蔽时长已到，是否重新申请？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, ApplyNoFilterActivity.class);
                startActivity(intent);

            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    /**
     * 加载web
     */
    public void loadWeb() {
        String str = editText.getText().toString();
        try {
            if (!str.substring(str.length() - 3, str.length()).equals(".cn")
                    && !str.substring(str.length() - 4, str.length()).equals(".com")
                    && !str.substring(str.length() - 4, str.length()).equals(".net")
                    && !str.substring(str.length() - 4, str.length()).equals(".edu")
                    && !str.substring(0, 7).equals("http:\\")
                    && !str.substring(0, 8).equals("https:\\")
                    && !str.substring(0, 4).equals("www.")
                    ) {
                webView.loadUrl("https://m.baidu.com/s?from=1012852p&word=" + str);
            } else {
                if (!str.substring(0, 4).equals("http")) {
                    webView.loadUrl("http://" + str);
                } else {
                    webView.loadUrl(str);
                }
            }
        } catch (Exception e) {
            webView.loadUrl("https://m.baidu.com/s?from=1012852p&word=" + str);
        }
    }

    /**
     * 加载WebView
     */
    public void loadWebView() {
        //解决网页中的视频，上屏幕的时候，可能出现闪烁的情况
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        //下拉刷新控件
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_orange_dark, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeLayout.setEnabled(false);
        webView = (WebView) findViewById(R.id.web_view);
        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        webView.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT > 17) {
            webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        }
        webView.setWebViewClient(new MyWebViewClient());

        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //获取网页title
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                nowTitle = title;
                //Toast.makeText(MainActivity.this, title, Toast.LENGTH_SHORT).show();

            }
        });
        webView.loadUrl("https://m.baidu.com/s?from=1012852p&word=" + searchStr);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                swipeLayout.setRefreshing(false);

                webView.reload();// 刷新页面
            }
        }, 100);

    }

    /**
     * WebView客户端类
     */
    final class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            nowUrl = url;

            recordHistoryToSqlite();

            SiteFilter sf = new SiteFilter(MainActivity.this, db);
            //Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();

            if (!spHelper.getIsNoFilter() && !sf.filterWebsite(url)) {
               // Toast.makeText(MainActivity.this, "已拦截，关键字：" + sf.getFilterKey(), Toast.LENGTH_SHORT).show();
                MyToast.toast(MainActivity.this, "已拦截，关键字："+ sf.getFilterKey());
                webView.stopLoading();
                webView.goBack();
            }
            progressBar.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            nowUrl = url;

            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" +
                    "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            SiteFilter sf = new SiteFilter(MainActivity.this, db);
            if (!spHelper.getIsNoFilter() && !sf.filterWebsite(url)) {
                //Toast.makeText(MainActivity.this, "已拦截，关键字："+sf.getFilterKey(), Toast.LENGTH_SHORT).show();
                MyToast.toast(MainActivity.this, "已拦截，关键字："+ sf.getFilterKey());
                webView.stopLoading();
                webView.goBack();
            }
            super.onPageFinished(view, url);
            progressBar.hide();
        }
    }

    /**
     * 获取历史纪录
     */
    private void getHistory() {
                /*查询所有信息*/
        Cursor c = db.rawQuery("select * from temphistory order by _id desc", null);
        if (c != null) {
            while (c.moveToNext()) {
                UrlEntity ue = new UrlEntity();
                ue.setTheTitle(c.getString(c.getColumnIndex("title")));
                ue.setTheURL(c.getString(c.getColumnIndex("url")));
                urlList.add(ue);
            }
            c.close();//释放游标
        }

    }

    /**
     * 向sqlite中添加信息
     */
    public void recordHistoryToSqlite() {
        Cursor c = db.rawQuery("select * from temphistory limit 1 offset (select count(*) - 1  from temphistory)", null);

        if (c != null) {
            c.moveToNext();
            if (!nowUrl.equals(c.getColumnIndex("title"))) {
                if (nowTitle != "" && nowUrl != "") {
                    /*添加方式一*/
                    //db.execSQL("insert into temphistory(title,url)values('"+nowTitle+"','"+nowUrl+"')");

                    /*添加方式二*/
                    ContentValues values = new ContentValues();
                    values.put("title", nowTitle);
                    values.put("url", nowUrl);
                    db.insert("temphistory", null, values);
                    db.insert("allhistory", null, values);
                    values.clear();//清空values值，便于后边继续使用该变量
                }
            }
        } else {
            if (nowTitle != "" && nowUrl != "") {
                    /*添加方式一*/
                //db.execSQL("insert into temphistory(title,url)values('"+nowTitle+"','"+nowUrl+"')");

                    /*添加方式二*/
                ContentValues values = new ContentValues();
                values.put("title", nowTitle);
                values.put("url", nowUrl);
                db.insert("temphistory", null, values);
                db.insert("allhistory", null, values);
                values.clear();//清空values值，便于后边继续使用该变量
            }
        }
    }

    /**
     * 页面javascript设置类
     */
    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            //Log.d("HTML", html);
            // Toast.makeText(MainActivity.this, html, Toast.LENGTH_SHORT).show();
            SiteFilter sf = new SiteFilter(MainActivity.this, db);
            if (!sf.filterKeyWord(html)) {
                Snackbar.make(webView, "此网站是否为新闻、娱乐网站，关键词：" + sf.getFilterKey(), Snackbar.LENGTH_LONG)
                        .setAction("是", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, AddKeywordActivity.class);
                                Bundle bundle = new Bundle();
                                //传递name参数为tinyphp
                                bundle.putString("url", nowUrl);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    /**
     * 硬件返回键事件
     */
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (editText.getVisibility() == View.VISIBLE) {
            editText.startAnimation(hideAnimation);
            //searchBtn.startAnimation(hideAnimation);
            searchBtn.setVisibility(View.GONE);
            cleanSearchBtn.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            showSearchBtn.setVisibility(View.VISIBLE);

            editText.clearFocus();//不聚焦
        } else if (webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
        } else {

            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            // Handle the camera action
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(MainActivity.this, AddKeywordActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, TestShakeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_letter) {
            Intent intent = new Intent(MainActivity.this, MessageForWeiActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_nofilter) {
            Intent intent = new Intent(MainActivity.this, ApplyNoFilterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 返回调用方法
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String result = data.getExtras().getString("resultUrl");
            webView.loadUrl(result);
        }
    }

    /**
     * 页面按钮点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        long[] pattern = {0, 30, 0, 5};   // 停止 开启
        vibrator.vibrate(pattern, -1);
        switch (v.getId()) {
            case R.id.goback_btn:
                webView.goBack();// 返回前一个页面
                break;
            case R.id.goforward_btn:
                webView.goForward();// 返回前一个页面
                break;
            case R.id.refresh_btn:
                webView.reload();// 刷新页面
                break;
            case R.id.gohome_btn:
                webView.loadUrl("https://m.baidu.com");//主页
                break;
            case R.id.link_btn:
                Snackbar.make(webView, nowUrl, Snackbar.LENGTH_LONG)
                        .setAction("复制", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData myClip = ClipData.newPlainText("text", nowUrl);//text是内容
                                cm.setPrimaryClip(myClip);
                                Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                break;
            case R.id.search_btn:
                loadWeb();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                break;
            case R.id.clean_search_btn:
                editText.setText("");
                break;
            case R.id.show_search_btn:
                editText.startAnimation(showAnimation);
                //searchBtn.startAnimation(showAnimation);
                editText.setVisibility(View.VISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                cleanSearchBtn.setVisibility(View.VISIBLE);
                showSearchBtn.setVisibility(View.GONE);
                editText.requestFocus();//获取焦点
            default:
                break;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
        //spHelper.setIsNoFilter(false);
        isActivityPause = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityPause = false;
        initNoFilterTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPause = true;
        if (spHelper.getIsNoFilter()) {
            tc.onFinish();
            tc.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
        vibrator.cancel();
    }


    /**
     * 修改time到Bmob
     */
    private void updateNoFilterTimeToNet() {
        NoFilterEntity nfe = new NoFilterEntity();
        nfe.setNoFilterTime(noFilterTime / 60);
        if (spHelper.getNoFilterID() != null) {
            nfe.update(spHelper.getNoFilterID(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {

                        //MyToast.toast(ApplyNoFilterActivity.this, "修改数据成功");
                    } else {
                        //MyToast.toast(MainActivity.this, "修改数据失败：" + e.getMessage());
                    }
                }

            });
        } else {
            MyToast.toast(this, "如果长时间没有出现此条Toast，请删除");
            tc.onFinish();
            tc.cancel();
        }
    }

    public class timeCount extends CountDownTimer {

        public timeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            noFilterTime = (int) (l / 1000);//单位秒

            if (noFilterTime == 1) {
                spHelper.setIsNoFilter(false);
                isActivityPause = true;
                noFilterTimeOverDialog();
                onFinish();
            }
//            if (noFilterTimeTemp - noFilterTime >= 1 && !isActivityPause && spHelper.getIsNoFilter()) {
//                if (noFilterTime < 59)
//                    MyToast.toast(MainActivity.this, "测试" + noFilterTimeTemp + "s" + noFilterTime);
//            }
            if (noFilterTimeTemp / 60 - noFilterTime / 60 >= 1 && !isActivityPause && spHelper.getIsNoFilter()) {
                spHelper.setNoFilterTime(noFilterTime * 1000);

                updateNoFilterTimeToNet();
                //MyToast.toast(MainActivity.this, "时长已刷新，还剩" + noFilterTime/60+"分钟");
                tvNoFilterToolbarHint.setText("剩余时长：" + (noFilterTime / 60 + 1) + "分钟");
                //MyToast.toast(MainActivity.this, "测试时长已刷新" + noFilterTimeTemp + "s" + noFilterTime);
                noFilterTimeTemp = noFilterTime;
            }
//            btnRegisterGoStep.setText(l / 1000 + "");
//            btnRegisterGoStep.setEnabled(false);
//            btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
        }

        @Override
        public void onFinish() {
            spHelper.setNoFilterTime(noFilterTime * 1000);
            llNoFilterToolbarHint.setVisibility(View.GONE);
            toolbar.setBackgroundResource(R.color.colorPrimary);
            ablFunctionBar.setBackgroundResource(R.color.colorPrimary);
//            btnRegisterGoStep.setEnabled(true);
//            btnRegisterGoStep.setText("获取");
//            btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
        }
    }
}

