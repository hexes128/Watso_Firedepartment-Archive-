package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Navigation_BaseActivity {

    Global gv;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;
    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    private ArrayList titleList, contentList, staffList, dateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolBar();
        setTitle("首頁");

        NV.getMenu().getItem(CurrentMenuItem).setChecked(true);
        CurrentMenuItem = 0;
        gv = (Global) getApplicationContext();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);
        titleList = new ArrayList();
        contentList = new ArrayList();
        staffList = new ArrayList();
        dateList = new ArrayList();

        mAdapter = new MyAdapter(staffList, titleList, contentList, dateList);
        mRecyclerView.setAdapter(mAdapter);

        service.submit(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/Billboard/getBillBoard").build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();

                    Log.e("", resStr);

                    JSONObject jsonObject = new JSONObject(resStr);
                    JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");

                    for (int i = jsonArray.length() - 1; i >= 0; i--) {

                        titleList.add(jsonArray.getJSONObject(i).getString("title").trim());
                        contentList.add(jsonArray.getJSONObject(i).getString("content").trim());
                        staffList.add(jsonArray.getJSONObject(i).getString("staff").trim());
                        dateList.add(jsonArray.getJSONObject(i).getString("date").trim().substring(0, 4) + "/" +
                                jsonArray.getJSONObject(i).getString("date").trim().substring(4, 6) + "/" +
                                jsonArray.getJSONObject(i).getString("date").trim().substring(6, 8) + " " +
                                jsonArray.getJSONObject(i).getString("date").trim().substring(8, 10) + ":" +
                                jsonArray.getJSONObject(i).getString("date").trim().substring(10, 12));


                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();

                        }
                    });


                } catch (IOException e) {
                    Log.e("IO", e.getMessage());
                } catch (JSONException e) {
                    Log.e("json", e.getMessage());
                }

            }
        });


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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(MainActivity.this).setCancelable(false).
                    setTitle("登出").
                    setMessage("確定登出?").
                    setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                        public void onClick(DialogInterface dialog, int i) {

                            finish();//關閉activity
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {

                }
            }).show();

        }
        return super.onKeyDown(keyCode, event);
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList myStaff;
        private ArrayList myTitle;
        private ArrayList myContent;
        private ArrayList myDate;


        public MyAdapter(ArrayList staff, ArrayList title, ArrayList content, ArrayList date) {
            myStaff = staff;
            myTitle = title;
            myContent = content;
            myDate = date;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView title, billBoardContent, staff, date;

            public ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.title);
                billBoardContent = v.findViewById(R.id.textView);
                staff = v.findViewById(R.id.staff);
                date = v.findViewById(R.id.time);

            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bill_board_data_view, parent, false);
            MainActivity.MyAdapter.ViewHolder vh = new MainActivity.MyAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.staff.setText(myStaff.get(position).toString());
            holder.title.setText(myTitle.get(position).toString());
            holder.billBoardContent.setText(myContent.get(position).toString());
            holder.date.setText(myDate.get(position).toString());

        }

        @Override
        public int getItemCount() {
            return myStaff.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }


}
