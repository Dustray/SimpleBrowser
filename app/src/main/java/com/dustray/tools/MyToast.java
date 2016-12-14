package com.dustray.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Dustray on 2016/11/27 0027.
 */

public class MyToast {
    public static void toast(Context context, String msg){
        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
    }
}
