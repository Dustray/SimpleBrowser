package com.dustray.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dustray.simplebrowser.HistoryFragment;

/**
 * Created by Dustray on 2017/4/2 0002.
 */

public class FragmentPagerHistoryAdapter extends FragmentPagerAdapter {

    public final int COUNT = 2;
    private String[] titles = new String[]{"所有历史纪录", "本次历史纪录"};
    private Context context;

    public FragmentPagerHistoryAdapter(FragmentManager fm, Context mcontext) {
        super(fm);
        context = mcontext;
    }

    @Override
    public Fragment getItem(int position) {
        return HistoryFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
