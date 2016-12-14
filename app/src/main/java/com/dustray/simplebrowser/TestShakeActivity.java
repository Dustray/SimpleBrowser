package com.dustray.simplebrowser;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.dustray.tools.MyToast;

public class TestShakeActivity extends AppCompatActivity implements View.OnClickListener {
    private Vibrator vibrator;//震动
    private android.support.v7.widget.AppCompatButton shakeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_shake);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("测试");
        shakeBtn = (android.support.v7.widget.AppCompatButton) findViewById(R.id.shake_btn);
        shakeBtn.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(TestShakeActivity.this.VIBRATOR_SERVICE);

    }

    @Override
    public void onClick(View view) {
        long[] pattern = {0, 30, 0, 1, 0, 5};   // 停止 开启
        vibrator.vibrate(pattern, -1);
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
