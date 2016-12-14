package com.dustray.tools;

import android.app.AlertDialog;
import android.content.Context;

import com.dustray.simplebrowser.R;

/**
 * Created by Dustray on 2016/12/8 0008.
 */

public class MyDialog {

    public void dialog(Context context,String title,String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        //builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
