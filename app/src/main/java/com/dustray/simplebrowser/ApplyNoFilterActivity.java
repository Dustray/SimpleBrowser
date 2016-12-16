package com.dustray.simplebrowser;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dustray.entity.NoFilterEntity;
import com.dustray.tools.MyToast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class ApplyNoFilterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button nofilterTimeBtn, startNofilterBtn;
    private TextView nofilterTimeText;
    private TextInputEditText applyPwdEdt, applyTimeEdt;

    private String myId = "";
    private int noFilterTime = 0;
    private boolean isSubmit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_no_filter);
        init();
        getAllNoFilterTimeFromNet();

    }

    private void init() {
        nofilterTimeBtn = (Button) findViewById(R.id.btn_nofilter_time);
        startNofilterBtn = (Button) findViewById(R.id.btn_start_nofilter);
        nofilterTimeBtn.setOnClickListener(this);
        startNofilterBtn.setOnClickListener(this);
        nofilterTimeText = (TextView) findViewById(R.id.text_nofilter_time);
        applyPwdEdt = (TextInputEditText) findViewById(R.id.edt_apply_pwd);
        applyTimeEdt = (TextInputEditText) findViewById(R.id.edt_apply_time);
    }

    /**
     * 查询一条Nofilter Time
     */
    private void getNoFilterTimeFromNet() {

        //查找Person表里面id为6b6c11c537的数据
        BmobQuery<NoFilterEntity> bmobQuery = new BmobQuery<NoFilterEntity>();
        bmobQuery.getObject(myId, new QueryListener<NoFilterEntity>() {
            @Override
            public void done(NoFilterEntity object, BmobException e) {
                if (e == null) {

                    noFilterTime = object.getNoFilterTime();
                    nofilterTimeText.setText("" + noFilterTime);
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
        query.addWhereEqualTo("userID", 111111);

        //执行查询方法
        query.findObjects(new FindListener<NoFilterEntity>() {
            @Override
            public void done(List<NoFilterEntity> object, BmobException e) {
                if (e == null) {
                    if (isSubmit && object.size() != 0) {
                        updateNoFilterTimeToNet();
                        isSubmit = false;
                    } else if (isSubmit && object.size() == 0) {
                        submitNoFilterTimeToNet();
                        isSubmit = false;
                    }
                    //MyToast.toast(ApplyNoFilterActivity.this, "成功");
                    for (NoFilterEntity noFilter : object) {
                        //获得playerName的信息
                        myId = noFilter.getObjectId();
                        noFilterTime = noFilter.getNoFilterTime();
                        nofilterTimeText.setText("" + noFilterTime);

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
        nfe.setUserID(111111);
        nfe.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    myId = objectId;
                    getNoFilterTimeFromNet();
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
        //更新Person表里面id为6b6c11c537的数据，address内容更新为“北京朝阳”
        NoFilterEntity nfe = new NoFilterEntity();
        nfe.setNoFilterTime(Integer.parseInt(applyTimeEdt.getText().toString()));
        nfe.setUserID(111111);
        nfe.update(myId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    getAllNoFilterTimeFromNet();
                    MyToast.toast(ApplyNoFilterActivity.this, "修改数据成功");
                } else {
                    MyToast.toast(ApplyNoFilterActivity.this, "修改数据失败：" + e.getMessage());
                }
            }

        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_nofilter_time:
                if (TextUtils.isEmpty(applyPwdEdt.getText())) {
                    MyToast.toast(ApplyNoFilterActivity.this, "密码不能为空");
                } else if (applyPwdEdt.getText().toString().equals("pbi")) {
                    isSubmit = true;
                    getAllNoFilterTimeFromNet();
                    //submitNoFilterTimeToNet();

                } else {
                    MyToast.toast(ApplyNoFilterActivity.this, "密码错误" + applyPwdEdt.getText());
                }
                break;
            case R.id.btn_start_nofilter:
                break;
        }
    }
}
