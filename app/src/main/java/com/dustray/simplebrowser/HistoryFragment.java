package com.dustray.simplebrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dustray.adapter.HistoryAdapter;
import com.dustray.entity.UrlEntity;
import com.dustray.tools.MyToast;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment {

    private List<UrlEntity> historyList = new ArrayList<UrlEntity>();
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private SQLiteDatabase db;
    private int mPage;

    public static final String ARGS_PAGE = "args_page";

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(int page) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARGS_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);

        rvHistory = (RecyclerView) view.findViewById(R.id.rv_history);
        db = getActivity().openOrCreateDatabase("browser.db", android.content.Context.MODE_PRIVATE, null);

        //获取历史纪录操作
        initData();
        initRecyclerView();
        return view;
    }

    /**
     * 初始化/刷新Adapter，初始化/刷新RecyclerView
     */
    private void initRecyclerView() {
        historyAdapter = new HistoryAdapter(getActivity(), historyList);
        //item长按/短按事件
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onLongClick(int position) {
                MyToast.toast(getActivity(), "长按还没啥用。。。");
            }

            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                String passString = historyList.get(position).getTheURL();
                intent.putExtra("resultUrl", passString);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });

        //MyToast.toast(getActivity(), "查询成功：共" + historyList.size() + "条数据。");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //设置布局管理器
        rvHistory.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        rvHistory.setAdapter(historyAdapter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        getHistory();
    }

    /**
     * 获取历史纪录
     */
    private void getHistory() {
                /*查询所有信息*/
        Cursor c = null;
        if (mPage == 2) {
            c = db.rawQuery("select * from temphistory order by _id desc", null);
        } else if (mPage == 1) {
            c = db.rawQuery("select * from allhistory order by _id desc", null);
        }
        if (c != null) {
            while (c.moveToNext()) {
                UrlEntity ue = new UrlEntity();
                ue.setTheTitle(c.getString(c.getColumnIndex("title")));
                ue.setTheURL(c.getString(c.getColumnIndex("url")));
                historyList.add(ue);
            }
            c.close();//释放游标
        }

    }

}
