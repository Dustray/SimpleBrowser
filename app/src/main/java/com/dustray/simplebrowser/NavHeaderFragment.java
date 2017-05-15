package com.dustray.simplebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dustray.tools.SharedPreferencesHelper;

public class NavHeaderFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextView tvHeaderMyAccount, tvHeaderMyState;
    private SharedPreferencesHelper spHelper;

    public NavHeaderFragment() {
        // Required empty public constructor
    }

    public static NavHeaderFragment newInstance(String param1, String param2) {
        NavHeaderFragment fragment = new NavHeaderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_header, container, false);
        spHelper = new SharedPreferencesHelper(getActivity());

        tvHeaderMyAccount = (TextView) view.findViewById(R.id.tv_header_my_account);
        tvHeaderMyAccount.setOnClickListener(this);
        tvHeaderMyState = (TextView) view.findViewById(R.id.tv_header_my_state);
       resetHeader();
        return view;
    }

    private void resetHeader() {
        tvHeaderMyAccount.setText(spHelper.getUserAccount());
        if (spHelper.getUserType() == 1) {
            tvHeaderMyState.setText("正在进行监控");
        } else if (spHelper.getUserType() == 2) {
            tvHeaderMyState.setText("正在接受保护");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resetHeader();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_header_my_account:
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
