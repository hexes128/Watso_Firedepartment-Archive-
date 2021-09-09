package com.example.fireequipmentsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InventoryDashboard extends Navigation_BaseActivity {


    String[] feature = new String[]{"借出", "報修", "我的修/借 管理", "設備盤點", "紀錄查詢", "匯出紀錄"};
    List<String> featureList;
    RecyclerView mRecyclerView;
    MyAdapter mAdapter;
    Global gv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_dashboard);
        setUpToolBar();
        NV.getMenu().getItem(CurrentMenuItem).setChecked(true);
        CurrentMenuItem = 2;
        gv = (Global) getApplicationContext();
        setTitle("設備管理");


        mRecyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        featureList = new ArrayList<>(Arrays.asList(feature));
        mAdapter = new MyAdapter(featureList);
        mRecyclerView.setAdapter(mAdapter);






    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            new AlertDialog.Builder(InventoryDashboard.this).setCancelable(false).
                    setTitle("登出").
                    setMessage("確定登出?").
                    setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                        public void onClick(DialogInterface dialog, int i) {

                            finish();
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {

                }
            }).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {

        super.onResume();


        if (gv.TokenExpired) {
            finish();
        } else {
            if (!gv.TokenVerification()) {
                gv.TokenExpired = true;
                finish();
            }
        }


    }

    public class MyAdapter extends RecyclerView.Adapter<InventoryDashboard.MyAdapter.ViewHolder> {

        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.place_name);
            }
        }

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.stored_list, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position).trim());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!gv.checkNetwork()) {
                        return;
                    }

                    switch (mData.get(position)) {
                        //借出

//                        String[] feature = new String[]{"借出",  "報修","修/借 管理", "修/借 查詢", "設備盤點", "盤點紀錄"};
                        case ("借出"): {


                            startActivity(new Intent(getApplicationContext(), LendActivity.class));

                            break;
                        }
                        //歸還
                        case ("我的修/借 管理"): {
                            startActivity(new Intent(getApplicationContext(), mylend_fixmanagement.class));
                            break;

                        }
                        //借出查詢
                        case ("全體修/借 查詢"): {

                            startActivity(new Intent(getApplicationContext(), alllend_fix_record.class));
                            break;

                        }
                        //報修
                        case ("報修"): {


                            startActivity(new Intent(getApplicationContext(), FixActivity.class));

                            break;
                        }

                        //設備盤點
                        case ("設備盤點"): {


                            startActivity(new Intent(getApplicationContext(), StoredPlace.class));

                            break;

                        }

                        //紀錄過濾
                        case ("紀錄查詢"): {
                            startActivity(new Intent(getApplicationContext(), RecordFilter.class));

                            break;

                        }

                        case ("匯出紀錄"): {
                            startActivity(new Intent(getApplicationContext(), Sendemail.class));

                            break;

                        }

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }


    }

}
