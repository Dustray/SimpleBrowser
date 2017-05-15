package com.dustray.simplebrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.dustray.entity.NoFilterEntity;
import com.dustray.tools.MyToast;
import com.dustray.tools.SharedPreferencesHelper;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class ApplyNoFilterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button nofilterTimeBtn, startNofilterBtn;
    private TextView nofilterTimeText, tvNoFilterState;
    private TextInputEditText applyPwdEdt, applyTimeEdt;

    //private int noFilterTime = 0;//单位秒
    private boolean isSubmit = false, isApply = false;

    private int userType = 0;
    SharedPreferencesHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_no_filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("免屏蔽");
        getSupportActionBar().setElevation(0);
        spHelper = new SharedPreferencesHelper(this);

        initControl();//初始化控件
        initUser();//初始化用户及用户所属控件
        getAllNoFilterTimeFromNet();
    }

    private void initUser() {

        userType = spHelper.getUserType();
        if (userType == 1) {
            startNofilterBtn.setVisibility(View.GONE);
        } else if (userType == 2) {
            applyTimeEdt.setHint("申请免屏蔽时间");
            applyPwdEdt.setVisibility(View.GONE);
        }
        judgeIsNoFilter();
    }

    private void initControl() {
        nofilterTimeBtn = (Button) findViewById(R.id.btn_nofilter_time);
        startNofilterBtn = (Button) findViewById(R.id.btn_start_nofilter);
        nofilterTimeBtn.setOnClickListener(this);
        startNofilterBtn.setOnClickListener(this);

        nofilterTimeText = (TextView) findViewById(R.id.text_nofilter_time);
        tvNoFilterState = (TextView) findViewById(R.id.tv_no_filter_state);

        applyPwdEdt = (TextInputEditText) findViewById(R.id.edt_apply_pwd);
        applyTimeEdt = (TextInputEditText) findViewById(R.id.edt_apply_time);
    }

    /**
     * 查询一条Nofilter Time
     */
    private void getNoFilterTimeFromNet() {

        BmobQuery<NoFilterEntity> bmobQuery = new BmobQuery<NoFilterEntity>();
        bmobQuery.getObject(spHelper.getNoFilterID(), new QueryListener<NoFilterEntity>() {
            @Override
            public void done(NoFilterEntity object, BmobException e) {
                if (e == null) {
                    spHelper.setNoFilterTime(object.getNoFilterTime() * 60 * 1000);
                    nofilterTimeText.setText(object.getNoFilterTime() + "分钟");
                    //spHelper.setNoFilterTime(noFilterTime*1000);

                    judgeIsNoFilter();

                    //MyToast.toast(ApplyNoFilterActivity.this, "成功");
                } else {
                    MyToast.toast(ApplyNoFilterActivity.this, "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 查询多条Nofilter Time
     */

    private void getAllNoFilterTimeFromNet() {
        BmobQuery<NoFilterEntity> query = new BmobQuery<NoFilterEntity>();
        query.addWhereEqualTo("userID", spHelper.getBmobObjectID());

        //执行查询方法
        query.findObjects(new FindListener<NoFilterEntity>() {
            @Override
            public void done(List<NoFilterEntity> object, BmobException e) {
                if (e == null) {
                    if (isSubmit && object.size() != 0) {
                        // MyToast.toast(ApplyNoFilterActivity.this, "成功");
                        updateNoFilterTimeToNet();
                        isSubmit = false;
                    } else if (isSubmit && object.size() == 0) {
                        submitNoFilterTimeToNet();
                        isSubmit = false;
                    }

                    for (NoFilterEntity noFilter : object) {
                        //获得playerName的信息
                        spHelper.setNoFilterID(noFilter.getObjectId());
                        nofilterTimeText.setText(noFilter.getNoFilterTime() + "分钟");
                        spHelper.setNoFilterTime(noFilter.getNoFilterTime() * 60 * 1000);
                        //spHelper.setNoFilterTime(noFilterTime*1000);

                        //显示申请时长详情
                        if (noFilter.getWaitingForApplyTime() > 0) {
                            nofilterTimeBtn.setBackgroundResource(R.drawable.xml_btn_color_warning);
                            applyTimeEdt.setText("" + noFilter.getWaitingForApplyTime());
                            applyPwdEdt.setText("");
                            if (spHelper.getUserType() == 1) {//监督者
                                nofilterTimeBtn.setText("修改");
                            } else if (spHelper.getUserType() == 2) {//被监督者
                                nofilterTimeBtn.setText("取消");
                                isApply = true;
                            }
                        } else {
                            nofilterTimeBtn.setBackgroundResource(R.drawable.xml_btn_color_accent);
                            nofilterTimeBtn.setText("确定");
                            applyTimeEdt.setText("");
                            applyPwdEdt.setText("");
                            isApply = false;
                        }

                        judgeIsNoFilter();
                    }
                } else {
                    if (isSubmit) {
                        submitNoFilterTimeToNet();
                        isSubmit = false;
                    }
                    MyToast.toast(ApplyNoFilterActivity.this, "失败1：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 提交time到Bmob
     */
    private void submitNoFilterTimeToNet() {
        NoFilterEntity nfe = new NoFilterEntity();
        nfe.setNoFilterTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        if (isApply) {
            nfe.setWaitingForApplyTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        } else {
            nfe.setWaitingForApplyTime(0);
        }
        nfe.setUserID(spHelper.getBmobObjectID());
        nfe.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    spHelper.setNoFilterID(objectId);
                    getAllNoFilterTimeFromNet();
                    //getNoFilterTimeFromNet();
                    MyToast.toast(ApplyNoFilterActivity.this, "添加数据成功");
                } else {
                    MyToast.toast(ApplyNoFilterActivity.this, "创建数据失败：" + e.getMessage());
                }
            }
        });

    }

    /**
     * 修改time到Bmob
     */
    private void updateNoFilterTimeToNet() {
        NoFilterEntity nfe = new NoFilterEntity();
        nfe.setNoFilterTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        if (isApply) {
            nfe.setWaitingForApplyTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        } else {
            nfe.setWaitingForApplyTime(0);
        }
        nfe.update(spHelper.getNoFilterID(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    getAllNoFilterTimeFromNet();
                    MyToast.toast(ApplyNoFilterActivity.this, "修改数据成功");
                } else {
                    submitNoFilterTimeToNet();
                    MyToast.toast(ApplyNoFilterActivity.this, "修改数据失败：" + e.getMessage());
                }
            }

        });
    }


    /**
     * 修改applyTtime到Bmob
     */
    private void updateApplyTimeToNet(int applyTime) {
        NoFilterEntity nfe = new NoFilterEntity();
        //nfe.setNoFilterTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        nfe.setWaitingForApplyTime(applyTime);
        nfe.update(spHelper.getNoFilterID(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    getAllNoFilterTimeFromNet();//查询申请
                    MyToast.toast(ApplyNoFilterActivity.this, "提交成功");

                } else {

                    MyToast.toast(ApplyNoFilterActivity.this, "修改数据失败,请通知监护者添加一次开通：" + e.getMessage());
                }
            }
        });
    }

    public void judgeIsNoFilter() {
        if (spHelper.getNoFilterTime() == 0) {//时长用尽
            startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_warning);
            startNofilterBtn.setText("时长已用尽");
            tvNoFilterState.setText("免屏蔽时长已用尽");
            spHelper.setIsNoFilter(false);
        } else if (spHelper.getIsNoFilter()) {//免屏蔽状态已开启
            startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_accent);
            startNofilterBtn.setText("暂停");
            tvNoFilterState.setText("免屏蔽已开启");
            spHelper.setIsNoFilter(true);
        } else if (!spHelper.getIsNoFilter()) {//免屏蔽状态已关闭
            startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_primary);
            startNofilterBtn.setText("开始");
            tvNoFilterState.setText("免屏蔽已关闭");
            spHelper.setIsNoFilter(false);
        }
    }

    public void applyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("警告");
        builder.setMessage("申请免屏蔽时长将清空当前已有时长，是否继续？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateApplyTimeToNet(Integer.parseInt(applyTimeEdt.getText().toString()));
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_nofilter_time:
                if (spHelper.getUserType() == 1)
                    if (TextUtils.isEmpty(applyPwdEdt.getText())) {
                        MyToast.toast(ApplyNoFilterActivity.this, "密码不能为空");
                    } else if (applyPwdEdt.getText().toString().equals(spHelper.getRegisterPassword())) {
                        isSubmit = true;
                        getAllNoFilterTimeFromNet();
                    } else {
                        MyToast.toast(ApplyNoFilterActivity.this, "密码错误" + applyPwdEdt.getText());
                    }
                else if (spHelper.getUserType() == 2) {
                    if (applyTimeEdt.getText().toString().equals("")) {
                        MyToast.toast(ApplyNoFilterActivity.this, "未执行任何操作");
                    } else if (Integer.parseInt(applyTimeEdt.getText().toString()) == 0) {
                        MyToast.toast(ApplyNoFilterActivity.this, "未执行任何操作");
                    } else if (isApply) {
                        updateApplyTimeToNet(0);
                        applyTimeEdt.setText("");
                        applyPwdEdt.setText("");
                        nofilterTimeBtn.setBackgroundResource(R.drawable.xml_btn_color_accent);
                        nofilterTimeBtn.setText("确认");
                        isApply = false;
                    } else {
                        applyDialog();
                    }
                }
                break;
            case R.id.btn_start_nofilter:
                if (spHelper.getNoFilterTime() == 0) {//时长用尽
                    MyToast.toast(ApplyNoFilterActivity.this, "您的免屏蔽时长已用尽");
                    startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_warning);
                    startNofilterBtn.setText("时长已用尽");
                    tvNoFilterState.setText("免屏蔽时长已用尽");
                    spHelper.setIsNoFilter(false);
                } else if (!spHelper.getIsNoFilter()) {//免屏蔽开启
                    MyToast.toast(ApplyNoFilterActivity.this, "免屏蔽已开启");
                    startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_accent);
                    startNofilterBtn.setText("暂停");
                    tvNoFilterState.setText("免屏蔽已开启");
                    spHelper.setIsNoFilter(true);
                } else if (spHelper.getIsNoFilter()) {//关闭
                    MyToast.toast(ApplyNoFilterActivity.this, "免屏蔽已关闭");
                    startNofilterBtn.setBackgroundResource(R.drawable.xml_btn_color_primary);
                    startNofilterBtn.setText("开始");
                    tvNoFilterState.setText("免屏蔽已关闭");
                    spHelper.setIsNoFilter(false);
                }
                break;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_no_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.item_btn_no_filter_refresh:
                //getNoFilterTimeFromNet();
                getAllNoFilterTimeFromNet();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
