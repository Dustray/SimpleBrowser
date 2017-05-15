package com.dustray.simplebrowser;

import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dustray.tools.MyToast;

import java.util.Locale;

public class TestShakeActivity extends AppCompatActivity implements View.OnClickListener {
    private Vibrator vibrator;//震动
    private AppCompatButton shakeBtn, btnSpeak;
    private TextInputEditText edtToSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_shake);
        initShake();//震动初始化
        initSpeak();
    }

    private void initSpeak() {
        btnSpeak = (AppCompatButton) findViewById(R.id.btn_speak);
        btnSpeak.setOnClickListener(this);
        edtToSpeak = (TextInputEditText) findViewById(R.id.edt_to_speak);

    }

    private void initShake() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("测试");
        shakeBtn = (AppCompatButton) findViewById(R.id.shake_btn);
        shakeBtn.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(TestShakeActivity.this.VIBRATOR_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shake_btn:
                long[] pattern = {0, 30, 0, 1, 0, 5};   // 停止 开启
                vibrator.vibrate(pattern, -1);
                break;
            case R.id.btn_speak:

                break;

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        vibrator.cancel();
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
