package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Record_month extends AppCompatActivity {


    String[] feature = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
    List<String> featureList;
    RecyclerView mRecyclerView;
    MyAdapter mAdapter;
    Global gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_month);

        gv = (Global)getApplicationContext();
        mRecyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        featureList = new ArrayList<>(Arrays.asList(feature));
        mAdapter = new MyAdapter(featureList);
        mRecyclerView.setAdapter(mAdapter);

    }

    public class MyAdapter extends RecyclerView.Adapter<Record_month.MyAdapter.ViewHolder> {

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
                    if (!gv.TokenVerification()) {
                        gv.TokenExpired = true;
                        new AlertDialog.Builder(Record_month.this).setCancelable(false).
                                setTitle("認證時間逾期").
                                setMessage("請重新登入").
                                setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                                    public void onClick(DialogInterface dialog, int i) {

                                        finish();//關閉activity
                                    }
                                }).show();
                        return;
                    }

//                    gv.month = position + 1;
                    startActivity(new Intent(Record_month.this, Inventory_Record.class));

                }


            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }


    }

}
