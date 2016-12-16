package com.dustray.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Dustray on 2016/12/16 0016.
 */

public class NoFilterEntity extends BmobObject {
    private int userID;
    private int noFilterTime;

    public int getNoFilterTime() {
        return noFilterTime;
    }

    public void setNoFilterTime(int noFilterTime) {
        this.noFilterTime = noFilterTime;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
