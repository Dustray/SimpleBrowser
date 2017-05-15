package com.dustray.db;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dustray.entity.RegisterEntity;
import com.dustray.simplebrowser.LoginActivity;
import com.dustray.simplebrowser.RegisterActivity;
import com.dustray.tools.MyToast;
import com.dustray.tools.SharedPreferencesHelper;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Dustray on 2017/4/7 0007.
 */

public class LoginAndRegister {

    /**
     * 用户注册，向Bmob添加数据
     *
     * @param guardianEmail
     * @param guardianPassword
     * @param unguardianUsername
     * @param unguardianPassword
     * @param unguardianDeviceIMEI
     * @param guardianPhone
     * @param context
     */
    public void userRegister(String guardianEmail,
                             String guardianPassword,
                             String unguardianUsername,
                             String unguardianPassword,
                             String unguardianDeviceIMEI,
                             String guardianPhone, final Context context) {
        RegisterEntity re = new RegisterEntity();

        re.setGuardianEmail(guardianEmail);
        re.setGuardianPassword(guardianPassword);
        re.setGuardianPhone(guardianPhone);

        re.setUnguardianUsername(unguardianUsername);
        re.setUnguardianPassword(unguardianPassword);
        re.setUnguardianDeviceIMEI(unguardianDeviceIMEI);

        re.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    MyToast.toast(context, "注册成功");
                    //SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
                    //spHelper.setRegisterID(""+objectId);
                    //登陆成功跳转
//                    Intent intent = new Intent(context, LoginActivity.class);
//                    context.startActivity(intent);
                    RegisterActivity.instance.finish();
                } else {
                    MyToast.toast(context, "注册失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 查询Email是否不存在
     *
     * @param email
     * @return
     */
    public final boolean isEmailNotExist(String email) {
        final boolean[] flag = {false};
        //查询username有值的数据
        BmobQuery<RegisterEntity> query = new BmobQuery<RegisterEntity>();
        query.addWhereEqualTo("guardianEmail", email);
        //执行查询方法
        query.findObjects(new FindListener<RegisterEntity>() {
            @Override
            public void done(List<RegisterEntity> object, BmobException e) {
                if (e == null) {
                    if (object.size() == 0) {
                        flag[0] = true;
                    }
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
        return flag[0];
    }

    /**
     * 查询用户名是否不存在
     *
     * @param username
     * @return
     */
    public boolean isUsernameNotExist(String username) {
        final boolean[] flag = {false};
        //查询username有值的数据
        BmobQuery<RegisterEntity> query = new BmobQuery<RegisterEntity>();
        query.addWhereEqualTo("unguardianUsername", username);
        //执行查询方法
        query.findObjects(new FindListener<RegisterEntity>() {
            @Override
            public void done(List<RegisterEntity> object, BmobException e) {
                if (e == null) {
                    if (object.size() == 0) {
                        flag[0] = true;
                    }
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
        return flag[0];
    }
}
