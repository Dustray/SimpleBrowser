package com.dustray.tools;

import android.os.CountDownTimer;


/**
 * Created by Dustray on 2017/4/9 0009.
 */

public class MyTimer extends CountDownTimer{


        public MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
        }

}
