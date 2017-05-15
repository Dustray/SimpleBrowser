package com.dustray.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Dustray on 2016/12/16 0016.
 */

public class NoFilterEntity extends BmobObject {
    private String userID;
    private int noFilterTime;
    private int waitingForApplyTime;

    public int getWaitingForApplyTime() {
        return waitingForApplyTime;
    }

    public void setWaitingForApplyTime(int waitingForApplyTime) {
        this.waitingForApplyTime = waitingForApplyTime;
    }

    public int getNoFilterTime() {
        return noFilterTime;
    }

    public void setNoFilterTime(int noFilterTime) {
        this.noFilterTime = noFilterTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
