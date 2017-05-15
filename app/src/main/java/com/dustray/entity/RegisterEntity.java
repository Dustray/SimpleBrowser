package com.dustray.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Dustray on 2017/4/7 0007.
 */

public class RegisterEntity extends BmobObject {
    private  String guardianEmail;//监护人邮箱
    private  String guardianPassword;//监护人密码
    private  String unguardianUsername;//被监护人用户名
    private  String unguardianPassword;//被监护人密码
    private  String unguardianDeviceIMEI;//被监护人设备IMEI号,用于设备唯一登陆
    private  String guardianPhone;//监护人手机号


    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getGuardianPassword() {
        return guardianPassword;
    }

    public void setGuardianPassword(String guardianPassword) {
        this.guardianPassword = guardianPassword;
    }

    public String getUnguardianUsername() {
        return unguardianUsername;
    }

    public void setUnguardianUsername(String unguardianUsername) {
        this.unguardianUsername = unguardianUsername;
    }

    public String getUnguardianPassword() {
        return unguardianPassword;
    }

    public void setUnguardianPassword(String unguardianPassword) {
        this.unguardianPassword = unguardianPassword;
    }

    public String getUnguardianDeviceIMEI() {
        return unguardianDeviceIMEI;
    }

    public void setUnguardianDeviceIMEI(String unguardianDeviceIMEI) {
        this.unguardianDeviceIMEI = unguardianDeviceIMEI;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }




}
