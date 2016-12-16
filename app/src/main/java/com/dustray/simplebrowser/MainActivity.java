package com.dustray.simplebrowser;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dustray.entity.UrlEntity;
import com.dustray.source.SiteFilter;
import com.dustray.tools.GetKeyword;
import com.dustray.tools.MyToast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;

import static android.support.v7.appcompat.R.attr.height;
import static android.support.v7.appcompat.R.id.top;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Intent intent;
    private WebView webView;
    private String searchStr = "";
    private String nowUrl = "", nowTitle = "";
    private EditText editText;
    private AppCompatImageView goBackBtn, goForwardBtn, refreshBtn, linkBtn, gohomeBtn;
    private ContentLoadingProgressBar progressBar;
    private SQLiteDatabase db;
    private List<UrlEntity> urlList = new ArrayList<UrlEntity>();
    private Vibrator vibrator;//震动
    private Button searchBtn, showSearchBtn, cleanSearchBtn;
    private CheckBox checkNeverShow;
    private SharedPreferences mSharedPreferences;
    private SwipeRefreshLayout swipeLayout;
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
        //键盘初始化
        imm = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
        //动画设置时间
        showAnimation.setDuration(100);
        hideAnimation.setDuration(100);
        loadWebView();//加载SwipeRefreshLayout和WebView
        initializeControl();//初始化其他控件
        initializeDatabase();//初始化sqlite
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // ATTENTION: This was auto-generated t o implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        vibrator = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);
        mSharedPreferences = getSharedPreferences("localSetting", 0);
        if (mSharedPreferences.getInt("ifNeverShowMsgForWei", 0) == 0) {
            dialog();
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
        db.execSQL("create table if not exists allkeyword(_id integer primary key autoincrement, keyword text not null)");//所有历史表
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
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progress_bar);
        goBackBtn = (AppCompatImageView) findViewById(R.id.goback_btn);
        goForwardBtn = (AppCompatImageView) findViewById(R.id.goforward_btn);
        refreshBtn = (AppCompatImageView) findViewById(R.id.refresh_btn);
        gohomeBtn = (AppCompatImageView) findViewById(R.id.gohome_btn);
        linkBtn = (AppCompatImageView) findViewById(R.id.link_btn);
        goBackBtn.setOnClickListener(this);
        goForwardBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        gohomeBtn.setOnClickListener(this);
        linkBtn.setOnClickListener(this);

        //toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    public void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_never_show, null);
        checkNeverShow = (CheckBox) linearLayout.findViewById(R.id.check_never_show);
        builder.setView(linearLayout);
        builder.setTitle("提示");
        builder.setMessage("您有一封信，是否阅读");
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (checkNeverShow.isChecked()) {
                    SharedPreferences.Editor mEditor = mSharedPreferences.edit();
                    mEditor.putInt("ifNeverShowMsgForWei", 1);
                    mEditor.commit();
                }
                Intent intent = new Intent(MainActivity.this, MessageForWeiActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("不看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (checkNeverShow.isChecked()) {
                    SharedPreferences.Editor mEditor = mSharedPreferences.edit();
                    mEditor.putInt("ifNeverShowMsgForWei", 1);
                    mEditor.commit();
                }
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
        //下拉刷新控件
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_orange_dark, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

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

            if (!sf.filterWebsite(url)) {
                Toast.makeText(MainActivity.this, "已拦截，关键字：" + sf.getFilterKey(), Toast.LENGTH_SHORT).show();
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
            if (!sf.filterWebsite(url)) {
                //Toast.makeText(MainActivity.this, "已拦截，关键字："+sf.getFilterKey(), Toast.LENGTH_SHORT).show();
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
                Snackbar.make(webView, "此网站是否为新闻网站，关键词：" + sf.getFilterKey(), Snackbar.LENGTH_LONG)
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            //用Bundle携带数据
            Bundle bundle = new Bundle();
            //传递name参数为tinyphp
            bundle.putInt("type", 0);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            //用Bundle携带数据
            Bundle bundle = new Bundle();
            //传递name参数为tinyphp
            bundle.putInt("type", 1);
            intent.putExtras(bundle);
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
}
