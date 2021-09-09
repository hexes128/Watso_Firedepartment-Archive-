package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class mylend_fixmanagement extends AppCompatActivity {

    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    private HashMap<String, Boolean> idSet;


    private HashMap<String, String> nameSet, placeSet, statusSet, change_date, staffSet, postscriptSet;
    Global gv;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylend_fixmanagement);
        gv = (Global) getApplicationContext();
        setTitle("我的修/借管理");

        idSet = new HashMap<>();
        nameSet = new HashMap<>();
        statusSet = new HashMap<>();
        placeSet = new HashMap<>();
        change_date = new HashMap<>();
        staffSet = new HashMap<>();

        postscriptSet = new HashMap<>();
        mAdapter = new MyAdapter(idSet);


        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        service.submit(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/StatusChanged/GetChangeRecord?staff=" + gv.UserName).build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();

                    Log.e("", resStr);

                    JSONObject jsonObject = new JSONObject(resStr);
                    JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");

                    for (int i = jsonArray.length() - 1; i >= 0; i--) {


                        idSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), false);

                        nameSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("item_name").trim());
                        placeSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("item_place").trim());
                        change_date.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("change_date").trim().substring(0, 4) + "/" +
                                jsonArray.getJSONObject(i).getString("change_date").trim().substring(4, 6) + "/" +
                                jsonArray.getJSONObject(i).getString("change_date").trim().substring(6, 8) + " " +
                                jsonArray.getJSONObject(i).getString("change_date").trim().substring(8, 10) + ":" +
                                jsonArray.getJSONObject(i).getString("change_date").trim().substring(10, 12));
                        staffSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("staff").trim());
                        statusSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("change_type").trim());
                        postscriptSet.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("postscript").trim());

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == resultCode) {
            idSet.putAll(gv.idset);
            mAdapter.notifyDataSetChanged();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.camera): {
                gv.intentfrom = "itemback";

                gv.idset.putAll(idSet);
                gv.placeset.putAll(placeSet);
                startActivityForResult(new Intent(mylend_fixmanagement.this, QRCodeScanner.class), 1);
            }
            break;
            case (R.id.send): {


                if (!gv.checkNetwork()) {
                    return false;
                }
                if (!gv.TokenVerification()) {

                    gv.TokenExpired = true;
                    new AlertDialog.Builder(mylend_fixmanagement.this).setCancelable(false).
                            setTitle("認證時間逾期").
                            setMessage("請重新登入").
                            setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                                public void onClick(DialogInterface dialog, int i) {

                                    finish();//關閉activity
                                }
                            }).show();
                    return false;
                }


                String senddata = "";
                final ArrayList<String> checkedid = new ArrayList<>();
                for (String id : idSet.keySet()) {
                    if (idSet.get(id)) {
                        senddata = senddata + id + " " + nameSet.get(id) + "\n";
                        checkedid.add(id);
                    }
                }

                if (senddata.equals("")) {

                    Toast.makeText(getApplicationContext(), "尚未選擇任何物品", Toast.LENGTH_SHORT).show();
                    return false;
                }

                new AlertDialog.Builder(mylend_fixmanagement.this).setCancelable(false).
                        setTitle("確認送出以下清單?").
                        setMessage(senddata).
                        setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                            public void onClick(DialogInterface dialog, int i) {


                                service.submit(new Runnable() {

                                    @Override
                                    public void run() {

                                        Request request = new Request.Builder().url("http://140.133.78.44/StatusChanged/ChangeStatus?item_id=" + checkedid.toString() + "&staff=" + gv.UserName + "&change_type=return" + "&postscript=[]").build();
                                        try {
                                            Log.e("網址", "http://140.133.78.44/StatusChanged/ChangeStatus?item_id=" + checkedid.toString() + "&staff=" + gv.UserName + "&change_type=return" + "&postscript=[]");
                                            final Response response = client.newCall(request).execute();
                                            final String resStr = response.body().string();

                                            Log.e("回應", resStr);

                                            if (resStr.equals("新增成功")) {


                                                for (String idkey : new ArrayList<>(idSet.keySet())) {

                                                    if (idSet.get(idkey)) {
                                                        idSet.remove(idkey);


                                                    }

                                                }


                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), resStr, Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }


                                        } catch (IOException e) {
                                            Log.e("有", "問題");
                                        }

                                    }

                                });


                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();


            }
            break;

        }
        return true;
    }


    public class MyAdapter extends RecyclerView.Adapter<mylend_fixmanagement.MyAdapter.ViewHolder> {
        HashMap<String, Boolean> mid;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView idView;
            public TextView nameView;
            public TextView placeView;
            public TextView statusView;
            public CheckBox checkBox;
            public TextView dateView;

            public ViewHolder(View v) {
                super(v);
                idView = v.findViewById(R.id.text_item_id);
                nameView = v.findViewById(R.id.text_item_name);
                placeView = v.findViewById(R.id.text_item_place);
                statusView = v.findViewById(R.id.text_item_status);
                checkBox = v.findViewById(R.id.checkBox);
                dateView = v.findViewById(R.id.text_date);
            }
        }

        public MyAdapter(HashMap<String, Boolean> id) {


            mid = id;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mylend_fix_itemview, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            final String item_id = new ArrayList<>(mid.keySet()).get(position);
            final String item_status = statusSet.get(item_id);


            holder.idView.setText(item_id);
            holder.nameView.setText(nameSet.get(item_id));
            holder.placeView.setText(placeSet.get(item_id));
            switch (item_status) {
                case ("lend"): {
                    holder.statusView.setTextColor(Color.rgb(0, 0, 255));
                    break;
                }
                case ("fix"): {
                    holder.statusView.setTextColor(Color.rgb(255, 0, 0));
                }
            }
            holder.statusView.setText(item_status);

            holder.dateView.setText(change_date.get(item_id));

            holder.checkBox.setClickable(false);

            holder.checkBox.setChecked(idSet.get(item_id));


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(mylend_fixmanagement.this).setCancelable(false).
                            setTitle(item_id + " 備註").

                            setMessage(postscriptSet.get(item_id))
                            .setNeutralButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }

                            }).show();


                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mid.size();
        }


        @Override
        public int getItemViewType(int position) {
            return position;
        }


    }
}
