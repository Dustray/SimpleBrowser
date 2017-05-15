package com.dustray.simplebrowser;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dustray.db.LoginAndRegister;
import com.dustray.tools.AnimationHelper;
import com.dustray.tools.MyToast;
import com.dustray.tools.RandomNumber;
import com.dustray.tools.SendEmail;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnRegisterGoStep, btnRegister;
    private RelativeLayout llRegisterForm, rlNextBtnNear;
    private RelativeLayout rlRegisterForm1, rlRegisterForm2;
    private EditText etGuardianRegisterPassword1, etGuardianRegisterPassword2,
            etUnguardianRegisterPassword1, etUnguardianRegisterPassword2;
    private EditText etRegisterIdentifyingCode;//验证码
    private AutoCompleteTextView actvRegisterEmail, actvRegisterUsername;
    private boolean isGuardianInfoTrue = false, isNextRegister = false, isIdentifyTrue = false;
    private long verificationCode = 0;
    public static RegisterActivity instance = null;


    private timeCount tc = new timeCount(60000, 1000);

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("注册-第一步");
        getSupportActionBar().setElevation(0);
        instance = this;
        initControl();
        //sendVerificationCode();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //btnRegisterGoStep.setText("确定");
            if (msg.what == 0) {
                btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
                etRegisterIdentifyingCode.requestFocus();
                tc.start();
            } else if (msg.what == 1) {
                MyToast.toast(RegisterActivity.this, "获取验证码成功");
            } else if (msg.what == 2) {
                MyToast.toast(RegisterActivity.this, "获取验证码失败：" + msg.obj.toString());
            }
        }
    };

    private Handler handler_1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MyToast.toast(RegisterActivity.this, "获取验证码成功");
        }
    };

    private void initControl() {
        btnRegisterGoStep = (Button) findViewById(R.id.btn_register_go_step);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegisterGoStep.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        llRegisterForm = (RelativeLayout) findViewById(R.id.rl_register_form);

        rlNextBtnNear = (RelativeLayout) findViewById(R.id.rl_next_btn_near);

        rlRegisterForm1 = (RelativeLayout) findViewById(R.id.rl_register_form_1);
        rlRegisterForm2 = (RelativeLayout) findViewById(R.id.rl_register_form_2);

        etGuardianRegisterPassword1 = (EditText) findViewById(R.id.et_guardian_register_password_1);
        etGuardianRegisterPassword2 = (EditText) findViewById(R.id.et_guardian_register_password_2);
        etUnguardianRegisterPassword1 = (EditText) findViewById(R.id.et_unguardian_register_password_1);
        etUnguardianRegisterPassword2 = (EditText) findViewById(R.id.et_unguardian_register_password_2);

        etRegisterIdentifyingCode = (EditText) findViewById(R.id.et_register_identifying_code);
        etRegisterIdentifyingCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ((verificationCode + "").equals(etRegisterIdentifyingCode.getText().toString())) {

                    actvRegisterEmail.setEnabled(false);
                    etGuardianRegisterPassword1.setEnabled(false);
                    etGuardianRegisterPassword2.setEnabled(false);
                    etRegisterIdentifyingCode.setEnabled(false);
                    //btnRegisterGoStep.setEnabled(true);
                    tc.onFinish();
                    tc.cancel();

                    btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_primary);
                    btnRegisterGoStep.setText("下一步");
                    isIdentifyTrue = true;
                } else {
                    btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
                    //btnRegisterGoStep.setText("获取");
                    isIdentifyTrue = false;
                }
            }

        });

        actvRegisterEmail = (AutoCompleteTextView)

                findViewById(R.id.actv_register_email);

        actvRegisterUsername = (AutoCompleteTextView)

                findViewById(R.id.actv_register_username);

    }


    private void sendVerificationCode(final String email) {
        try {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        RandomNumber rn = new RandomNumber();
                        verificationCode = rn.getRandomNumber(6);
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                        SendEmail se = new SendEmail(email);
                        //se.sendTextEmail(verificationCode);//发送文本邮件
                        se.sendHtmlEmail(verificationCode);//发送html邮件

                        handler_1.sendEmptyMessage(0);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = "1" + e.toString();
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = "2" + e.toString();
            handler.sendMessage(msg);

            btnRegisterGoStep.setText("获取");
            e.printStackTrace();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * 登录或注册登录表单指定的帐户的尝试。如果有表单错误（无效的电子邮件，丢失的字段等），
     * 则会出现错误，没有实际的登录尝试。
     */
    private void attemptRegisterStep1() {
        // Reset errors.
        actvRegisterEmail.setError(null);
        etGuardianRegisterPassword1.setError(null);
        etGuardianRegisterPassword2.setError(null);
        // Store values at the time of the login attempt.
        //在登录尝试时存储值。
        String email = actvRegisterEmail.getText().toString();
        String gPassword1 = etGuardianRegisterPassword1.getText().toString();
        String gPassword2 = etGuardianRegisterPassword2.getText().toString();
        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        //检查有效的电子邮件地址。
        if (TextUtils.isEmpty(email)) {
            actvRegisterEmail.setError(getString(R.string.error_field_required));
            focusView = actvRegisterEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            actvRegisterEmail.setError(getString(R.string.error_invalid_email));
            focusView = actvRegisterEmail;
            cancel = true;
        } else if (!isEmailNotExist(email)) {
            actvRegisterEmail.setError(getString(R.string.error_exist_email));
            focusView = actvRegisterEmail;
            cancel = true;
        } else if (TextUtils.isEmpty(gPassword1)) {
            etGuardianRegisterPassword1.setError(getString(R.string.error_field_required));
            focusView = etGuardianRegisterPassword1;
            cancel = true;
        } else if (TextUtils.isEmpty(gPassword2)) {
            etGuardianRegisterPassword2.setError(getString(R.string.error_field_required));
            focusView = etGuardianRegisterPassword2;
            cancel = true;
        } else if (!TextUtils.isEmpty(gPassword1) && !isPasswordValid(gPassword1)) {
            // Check for a valid gPassword1, if the user entered one.
            //如果用户输入一个有效密码，请检查。
            etGuardianRegisterPassword1.setError(getString(R.string.error_invalid_password));
            focusView = etGuardianRegisterPassword1;
            cancel = true;
        } else if (!gPassword1.equals(gPassword2)) {
            etGuardianRegisterPassword2.setError(getString(R.string.error_insame_password));
            focusView = etGuardianRegisterPassword2;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            //有一个错误；不要尝试登录和聚焦第一表单字段的错误。
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //显示进度微调器，并启动后台任务以执行用户登录尝试。
            //showProgress(true);
            isGuardianInfoTrue = true;//信息正确为true
            sendVerificationCode(actvRegisterEmail.getText().toString());//像邮箱发送验证码
            etRegisterIdentifyingCode.setEnabled(true);

        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * 登录或注册登录表单指定的帐户的尝试。如果有表单错误（无效的电子邮件，丢失的字段等），
     * 则会出现错误，没有实际的登录尝试。
     */
    private void attemptRegisterStep2() {

        // Reset errors.
        actvRegisterUsername.setError(null);
        etUnguardianRegisterPassword1.setError(null);
        etUnguardianRegisterPassword2.setError(null);
        // Store values at the time of the login attempt.
        //在登录尝试时存储值。

        String username = actvRegisterUsername.getText().toString();
        String ugPassword1 = etUnguardianRegisterPassword1.getText().toString();
        String ugPassword2 = etUnguardianRegisterPassword2.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username) || !isUserNameValid(username)) {
            actvRegisterUsername.setError(getString(R.string.error_invalid_username));
            focusView = actvRegisterUsername;
            cancel = true;
        } else if (!isUserNotExist(username)) {
            actvRegisterUsername.setError(getString(R.string.error_exist_username));
            focusView = actvRegisterUsername;
            cancel = true;
        } else if (TextUtils.isEmpty(ugPassword1)) {
            etUnguardianRegisterPassword1.setError(getString(R.string.error_field_required));
            focusView = etUnguardianRegisterPassword1;
            cancel = true;
        } else if (TextUtils.isEmpty(ugPassword2)) {
            etUnguardianRegisterPassword2.setError(getString(R.string.error_field_required));
            focusView = etUnguardianRegisterPassword2;
            cancel = true;
        } else if (!TextUtils.isEmpty(ugPassword1) && !isPasswordValid(ugPassword1)) {
            // Check for a valid gPassword1, if the user entered one.
            //如果用户输入一个有效密码，请检查。
            etUnguardianRegisterPassword1.setError(getString(R.string.error_invalid_password));
            focusView = etUnguardianRegisterPassword1;
            cancel = true;
        } else if (!ugPassword1.equals(ugPassword2)) {
            etUnguardianRegisterPassword2.setError(getString(R.string.error_insame_password));
            focusView = etUnguardianRegisterPassword2;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            //有一个错误；不要尝试登录和聚焦第一表单字段的错误。
            focusView.requestFocus();
        } else {
            //MyToast.toast(RegisterActivity.this, "登陆成功！");
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //显示进度微调器，并启动后台任务以执行用户登录尝试。
            //showProgress(true);
            //isUnguardianInfoTrue = true;//信息正确为true
            //etRegisterIdentifyingCode.setEnabled(true);

            userRegisterTask(actvRegisterEmail.getText().toString(),
                    etGuardianRegisterPassword1.getText().toString(),
                    actvRegisterUsername.getText().toString(),
                    etUnguardianRegisterPassword1.getText().toString());
        }
    }

    private boolean isEmailValid(String email) {
        return (email.contains("@") && email.contains("."));
    }

    private boolean isEmailNotExist(String email) {
        LoginAndRegister lar = new LoginAndRegister();
        //return lar.isEmailNotExist(email);
        return true;

    }

    private boolean isUserNotExist(String username) {
        LoginAndRegister lar = new LoginAndRegister();
        //return lar.isUsernameNotExist(username);
        return true;
    }

    private boolean isUserNameValid(String username) {
        return username.length() >= 4;
    }

    private boolean isPasswordValid(String gPassword1) {
        return gPassword1.length() >= 6;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register_go_step:
                if (isGuardianInfoTrue && isIdentifyTrue) {
                    nextRegister();
                } else if (!isGuardianInfoTrue) {
                    //判断邮箱和密码是否正确
                    attemptRegisterStep1();
                } else if (isGuardianInfoTrue) {
                    sendVerificationCode(actvRegisterEmail.getText().toString());
                }

                break;
            case R.id.btn_register:
                attemptRegisterStep2();
                break;
            default:
                break;
        }
    }


    private void nextRegister() {
        //获取屏幕宽度
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        final int width = outMetrics.widthPixels;
        final float scale = outMetrics.density;
        final int dipToPx = (int) (82 * scale + 0.5f);

        AnimationHelper ah = new AnimationHelper();
        /*
        //*下一步
        final Animation translateAnim1Next = ah.getTranslateAnimation(0, -width, 0, 0);//1向左
        final Animation translateAnim2Next = ah.getTranslateAnimation(width, -width, 0, 0);//2向左
        //*上一步
        final Animation translateAnim1Last = ah.getTranslateAnimation(0, width, 0, 0);//1向右
        final Animation translateAnim2Last = ah.getTranslateAnimation(0, width, 0, 0);//2向右
        */
        final Animation translateAnimNext = ah.getTranslateAnimation(0, -width + dipToPx, 0, 0);//1向左
        final Animation translateAnimLast = ah.getTranslateAnimation(0, width - dipToPx, 0, 0);//1向右

        final Animation scaleAnim1Next = ah.getScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);//1向左
        final Animation scaleAnim2Next = ah.getScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);//2向左
        //*上一步
        final Animation scaleAnim1Last = ah.getScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);//1向右
        final Animation scaleAnim2Last = ah.getScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);//2向右
//打开按钮动画监听事件：动画结束时更改控件实际位置
        scaleAnim1Next.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //清除动画，否则会闪动
                rlRegisterForm1.clearAnimation();
                rlRegisterForm2.clearAnimation();
                rlNextBtnNear.clearAnimation();
                //移动控件
                /*
                rlRegisterForm1.layout(rlRegisterForm1.getLeft() - width, rlRegisterForm1.getTop(),
                        rlRegisterForm1.getRight() - width, rlRegisterForm1.getBottom());
                rlRegisterForm2.layout(rlRegisterForm2.getLeft() - width, rlRegisterForm2.getTop(),
                        rlRegisterForm2.getRight() - width, rlRegisterForm2.getBottom());
                */
//                btnRegisterGoStep.layout(btnRegisterGoStep.getLeft() - width + 16, btnRegisterGoStep.getTop(),
//                        btnRegisterGoStep.getRight() - width + 16, btnRegisterGoStep.getBottom());
                rlNextBtnNear.layout(rlNextBtnNear.getLeft() - width + dipToPx, rlNextBtnNear.getTop(),
                        rlNextBtnNear.getRight() - width + dipToPx, rlNextBtnNear.getBottom());
                rlRegisterForm1.setVisibility(View.GONE);

                isNextRegister = true;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //关闭按钮动画监听事件：动画结束时更改控件实际位置
        scaleAnim1Last.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //清除动画，否则会闪动
                rlRegisterForm1.clearAnimation();
                rlRegisterForm2.clearAnimation();
                rlNextBtnNear.clearAnimation();
                /*
                rlRegisterForm1.layout(rlRegisterForm1.getLeft() + width, rlRegisterForm1.getTop(),
                        rlRegisterForm1.getRight() + width, rlRegisterForm1.getBottom());
                rlRegisterForm2.layout(rlRegisterForm2.getLeft() + width, rlRegisterForm2.getTop(),
                        rlRegisterForm2.getRight() + width, rlRegisterForm2.getBottom());
                        */
//                btnRegisterGoStep.layout(btnRegisterGoStep.getLeft() + width - 16, btnRegisterGoStep.getTop(),
//                        btnRegisterGoStep.getRight() + width - 16, btnRegisterGoStep.getBottom());
                rlNextBtnNear.layout(rlNextBtnNear.getLeft() + width - dipToPx, rlNextBtnNear.getTop(),
                        rlNextBtnNear.getRight() + width - dipToPx, rlNextBtnNear.getBottom());
                rlRegisterForm2.setVisibility(View.GONE);
                btnRegister.setVisibility(View.INVISIBLE);

                isNextRegister = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (!isNextRegister) {
            //动画开始
            rlRegisterForm1.startAnimation(scaleAnim1Next);
            rlRegisterForm2.startAnimation(scaleAnim2Next);
            btnRegister.setVisibility(View.VISIBLE);
            btnRegisterGoStep.setText("上一步");
            getSupportActionBar().setTitle("注册-第二步");
            rlNextBtnNear.startAnimation(translateAnimNext);

            //btnRegisterGoStep.startAnimation(translateAnimNext);

            rlRegisterForm2.setVisibility(View.VISIBLE);
        } else {
            //动画开始
            rlRegisterForm1.startAnimation(scaleAnim1Last);
            rlRegisterForm2.startAnimation(scaleAnim2Last);
            rlNextBtnNear.startAnimation(translateAnimLast);
            btnRegisterGoStep.setText("下一步");
            getSupportActionBar().setTitle("注册-第一步");
            //btnRegisterGoStep.startAnimation(translateAnimLast);
            rlRegisterForm1.setVisibility(View.VISIBLE);


        }

    }


    public void userRegisterTask(String email, String gPassword1, String username, String ugPassword) {

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String IMEI = "000000000000000";
        try {
            IMEI = tm.getDeviceId();
        } catch (Exception e) {
            MyToast.toast(this, "获取IMEI失败，请重启软件后重试");
        }
        LoginAndRegister lar = new LoginAndRegister();
        lar.userRegister(email, gPassword1, username, ugPassword, IMEI, "", this);
    }

    public class timeCount extends CountDownTimer {

        public timeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            btnRegisterGoStep.setText(l / 1000 + "");
            btnRegisterGoStep.setEnabled(false);
            btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
        }

        @Override
        public void onFinish() {
            btnRegisterGoStep.setEnabled(true);
            btnRegisterGoStep.setText("获取");
            btnRegisterGoStep.setBackgroundResource(R.drawable.xml_btn_color_accent);
        }
    }
}
