package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoredPlace extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    ArrayList<String> myDataset;
    Global gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_place);

        gv = (Global) getApplicationContext();
        setTitle("請選擇欲盤點地點");

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);


        myDataset = new ArrayList<>();
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);


        service.submit(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/Getplace/Selectplace").build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();

                    JSONObject jsonObject = new JSONObject(resStr);
                    
                    JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        myDataset.add(jsonArray.getJSONObject(i).getString("item_place").trim());
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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position).trim());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (gv.TokenVerification()) {
                        gv.Selectedplace = mData.get(position);
                        Intent intent = new Intent(StoredPlace.this, ItemActivity.class);
                        startActivity(intent);
                    } else {
                        gv.TokenExpired = true;
                        new AlertDialog.Builder(StoredPlace.this).setCancelable(false).
                                setTitle("認證時間逾期").
                                setMessage("請重新登入").
                                setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                                    public void onClick(DialogInterface dialog, int i) {

                                        finish();//關閉activity
                                    }
                                }).show();
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



