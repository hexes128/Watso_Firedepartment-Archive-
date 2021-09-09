package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class LendActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    Global gv;
    HashMap<String, Boolean> idset;
    HashMap<String, String> nameSet, buyDateSet, priceSet, placeSet, custosSet, statusSet, postscriptSet;
    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lend);
        gv = (Global) getApplicationContext();
        setTitle("借出");
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);


        idset = new HashMap<>();

        nameSet = new HashMap<>();

        buyDateSet = new HashMap<>();
        priceSet = new HashMap<>();
        placeSet = new HashMap<>();


        custosSet = new HashMap<>();
        statusSet = new HashMap<>();
        postscriptSet = new HashMap<>();


        mAdapter = new MyAdapter(idset);
        mRecyclerView.setAdapter(mAdapter);
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

            idset.putAll(gv.idset);
            gv.idset.clear();
            service.submit(new Runnable() {

                @Override
                public void run() {
                    Request request = new Request.Builder().url("http://140.133.78.44/StatusChanged/GetNormalItem?item_id=" + idset.keySet()).build();
                    try {
                        final Response response = client.newCall(request).execute();
                        final String resStr = response.body().string();


                        Log.e("回應", resStr);
                        JSONObject jsonObject = new JSONObject(resStr);


                        JSONArray item = jsonObject.getJSONArray("JsonResult");

                        idset.clear();
                        for (int i = 0; i < item.length(); i++) {

                            idset.put(item.getJSONObject(i).getString("item_id").trim(), false);

                            nameSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_name").trim());

                            buyDateSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_buydate").trim());
                            priceSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_price").trim());
                            placeSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_place").trim());

                            custosSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_custos").trim());
                            statusSet.put(item.getJSONObject(i).getString("item_id").trim(), item.getJSONObject(i).getString("item_status").trim());
                            postscriptSet.put(item.getJSONObject(i).getString("item_id").trim(), "無");

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (IOException e) {

                    } catch (JSONException e) {

                    }

                }

            });

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
                gv.intentfrom = "lend";
                gv.idset.putAll(idset);
                startActivityForResult(new Intent(LendActivity.this, QRCodeScanner.class), 1);
            }
            break;
            case (R.id.send): {
                String tmp = "";
                for (String key : idset.keySet()) {
                    tmp = tmp + key + " " + nameSet.get(key) + "\n";
                }

                if (tmp.equals("")) {
                    Toast.makeText(getApplicationContext(), "尚未選擇任何物品", Toast.LENGTH_SHORT).show();
                    return false;
                }
                new AlertDialog.Builder(LendActivity.this).setCancelable(false).
                        setTitle("確定送出以下物品?").
                        setMessage(tmp).
                        setNeutralButton("關閉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setNegativeButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        service.submit(new Runnable() {

                            @Override
                            public void run() {
                                Request request = new Request.Builder().url("http://140.133.78.44/StatusChanged/ChangeStatus?item_id=" + idset.keySet() + "&staff=" + gv.UserName + "&change_type=lend"  + "&postscript=" + postscriptSet.values()).build();
                                try {
                                    final Response response = client.newCall(request).execute();
                                    final String resStr = response.body().string();

                                    if (resStr.equals("新增成功")) {

                                        idset.clear();
                                        nameSet.clear();

                                        buyDateSet.clear();
                                        priceSet.clear();
                                        placeSet.clear();

                                        custosSet.clear();
                                        postscriptSet.clear();
                                        statusSet.clear();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });

                                    }


                                } catch (IOException e) {

                                }

                            }

                        });


                    }
                }).show();


            }
            break;

        }
        return true;
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private HashMap<String, Boolean> mid;


        public MyAdapter(HashMap<String, Boolean> id) {
            mid = id;
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final String item_id = new ArrayList<>(mid.keySet()).get(position);


            holder.item_id.setText(item_id);

            holder.item_name.setText(nameSet.get(item_id));

            holder.checkBox.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    idset.put(item_id, !holder.checkBox.isChecked());
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());

                }
            });


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    new AlertDialog.Builder(LendActivity.this).setCancelable(false).
                            setTitle("詳細資訊").
                            setMessage("動產/非消耗品編號 : " + item_id +
                                    "\n" + "動產/非消耗品名稱 : " + nameSet.get(item_id) +

                                    "\n" + "購置日期 : " + buyDateSet.get(item_id) +
                                    "\n" + "價值 : " + priceSet.get(item_id) +
                                    "\n" + "存置地點 : " + placeSet.get(item_id) +


                                    "\n" + "保管人 : " + custosSet.get(item_id) +
                                    "\n" + "物品狀態 : " + statusSet.get(item_id)).
                            setNeutralButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).setNegativeButton("新增備註", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            final EditText editText = new EditText(LendActivity.this);
                            editText.setHint("輸入備註");
                            new AlertDialog.Builder(LendActivity.this).setTitle("新增備註").setCancelable(false).setView(editText)
                                    .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (editText.getText().toString().trim().isEmpty()) {
                                                postscriptSet.put(new ArrayList<>(idset.keySet()).get(position), "無");
                                            } else {
                                                postscriptSet.put(new ArrayList<>(idset.keySet()).get(position), editText.getText().toString().trim());
                                            }

                                        }
                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView item_id;
            public TextView item_name;
            public CheckBox checkBox;

            public ViewHolder(View v) {
                super(v);
                item_id = v.findViewById(R.id.item_id);
                item_name = v.findViewById(R.id.item_name);
                checkBox = v.findViewById(R.id.checkBox);
            }
        }

    }


}
