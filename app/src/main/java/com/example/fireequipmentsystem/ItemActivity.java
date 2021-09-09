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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

public class ItemActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    HashMap<String, Boolean> idset;
    HashMap<String, String> nameSet, buyDateSet, priceSet, placeSet, custosSet, statusSet,statuscopySet, postscriptSet, change_type;
    Global gv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        gv = (Global) getApplicationContext();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);
        setTitle(gv.Selectedplace + " 設備清單");

        idset = new HashMap<>();
        nameSet = new HashMap<>();
        buyDateSet = new HashMap<>();
        priceSet = new HashMap<>();
        placeSet = new HashMap<>();
        change_type = new HashMap<>();
        custosSet = new HashMap<>();
        statusSet = new HashMap<>();
        postscriptSet = new HashMap<>();
        statuscopySet= new HashMap<>();
        mAdapter = new MyAdapter(idset);
        mRecyclerView.setAdapter(mAdapter);

        service.submit(new Runnable() {

            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/Selectitembyplace/Selectitembyplace?item_place="
                        + gv.Selectedplace).build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();

                    JSONObject jsonObject = new JSONObject(resStr);
                    JSONArray item = jsonObject.getJSONArray("JsonResult");

                    for (int i = 0; i < item.length(); i++) {
                        idset.put(item.getJSONObject(i).getString("item_id").trim(), false);
                        nameSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_name").trim());
                        buyDateSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_buydate").trim());
                        priceSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_price").trim());
                        placeSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_place").trim());
                        custosSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_custos").trim());
                        statusSet.put(item.getJSONObject(i).getString("item_id").trim(),
                                item.getJSONObject(i).getString("item_status").trim());
                    }
                    statuscopySet.putAll(statusSet);
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
        if (resultCode == requestCode) {
            String senddata = "";
            int i=1;
            for (String key : new ArrayList<>(gv.idset.keySet())) {
                if (gv.idset.get(key)) {
                    senddata +=i+"  "+ key + " " + nameSet.get(key) + "\n";
                    i++;
                }
            }
            if (senddata.equals("")) {
                return;
            }
            Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(ItemActivity.this).setCancelable(false).
                    setTitle("剛剛掃描的").
                    setMessage(senddata).
                    setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                        public void onClick(DialogInterface dialog, int i) {
                            idset.putAll(gv.idset);
                            gv.idset.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    gv.idset.clear();
                }
            }).show();
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
                gv.intentfrom = "inventory";
                gv.idset.putAll(idset);
                for(String id: idset.keySet()){
                    if(!idset.get(id)){
                        gv.idset.put(id,idset.get(id));
                    }
                }
                startActivityForResult(new Intent(ItemActivity.this, QRCodeScanner.class), 1);
            }
            break;
            case (R.id.send): {
                if (!gv.checkNetwork()) {
                    return false;
                }
                if (!gv.TokenVerification()) {
                    gv.TokenExpired = true;
                    new AlertDialog.Builder(ItemActivity.this).setCancelable(false).
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
                for (String key : new ArrayList<>(idset.keySet())) {
                    if (idset.get(key)) {
                        senddata += key + " " + nameSet.get(key) + "\n";
                    } else {
                        senddata = "";
                        break;
                    }
                }
                if (senddata.equals("")) {
                    Toast.makeText(getApplicationContext(), "尚有物品未處理", Toast.LENGTH_SHORT).show();
                    return false;
                }
                new AlertDialog.Builder(ItemActivity.this).setCancelable(false).
                        setTitle("確認送出以下清單?").
                        setMessage(senddata).
                        setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                            public void onClick(DialogInterface dialog, int i) {
                                Log.e("狀態", statusSet.values().toString());
                                service.submit(new Runnable() {

                                    @Override
                                    public void run() {
                                        Request request = new Request.Builder().url("http://140.133.78.44/inventory/Inventory?item_id="
                                                + idset.keySet() + "&Inventory_staff="
                                                + gv.UserName + "&item_status="
                                                + statuscopySet.values()).build();
                                        try {
                                            final Response response = client.newCall(request).execute();
                                            final String resStr = response.body().string();
                                            if (resStr.equals("新增成功")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        idset.clear();
                                                        nameSet.clear();
                                                        buyDateSet.clear();
                                                        priceSet.clear();
                                                        placeSet.clear();
                                                        custosSet.clear();
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
                                        }
                                        if (change_type.values().contains("fix")) {
                                            request = new Request.Builder().url("http://140.133.78.44/StatusChanged/ChangeStatus?item_id=" + change_type.keySet() + "&staff=" + gv.UserName + "&change_type=fix" + "&postscript=" + postscriptSet.values()).build();
                                            try {
                                                client.newCall(request).execute();
                                            } catch (IOException e) {
                                            }
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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private HashMap<String, Boolean> mid;
        public MyAdapter(HashMap<String, Boolean> id) {
            mid = id;
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

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_data_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String item_id = new ArrayList<>(mid.keySet()).get(position);
            final String item_name = nameSet.get(item_id);
            final String item_status = statusSet.get(item_id);
            holder.item_id.setText(item_id);
            holder.item_name.setText(item_name);
            if (item_status.equals("lend") || item_status.equals("fix")) {
                idset.put(item_id, true);
                holder.checkBox.setVisibility(View.GONE);
                switch (item_status) {
                    case ("lend"): {
                        holder.item_name.setText(item_name + "(借出中)");
                        holder.item_id.setTextColor(Color.rgb(0, 0, 255));
                        holder.item_name.setTextColor(Color.rgb(0, 0, 255));
                        break;
                    }
                    case ("fix"): {
                        holder.item_name.setText(item_name + "(維修中)");
                        holder.item_id.setTextColor(Color.rgb(255, 0, 0));
                        holder.item_name.setTextColor(Color.rgb(255, 0, 0));
                        break;
                    }
                }
            } else {
                holder.checkBox.setChecked(idset.get(item_id));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(change_type.keySet().contains(item_id)){
                            return;
                        }
                        idset.put(item_id, !holder.checkBox.isChecked());
                        holder.checkBox.setChecked(!holder.checkBox.isChecked());
                        Log.e("狀態", statusSet.get(item_id));
                    }
                });
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final String info = "動產/非消耗品編號 : " + item_id +
                            "\n" + "動產/非消耗品名稱 : " + nameSet.get(item_id) +
                            "\n" + "購置日期 : " + buyDateSet.get(item_id) +
                            "\n" + "價值 : " + priceSet.get(item_id) +
                            "\n" + "存置地點 : " + placeSet.get(item_id) +
                            "\n" + "保管人 : " + custosSet.get(item_id) +
                            "\n" + "物品狀態 : " + statusSet.get(item_id);
                    new AlertDialog.Builder(ItemActivity.this).setCancelable(false).
                            setTitle("詳細資訊").
                            setMessage(info)
                            .setNeutralButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).setNegativeButton("報修", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (item_status.equals("lend") || item_status.equals("fix")) {
                                return;
                            }
                            final EditText editText = new EditText(ItemActivity.this);
                            editText.setHint("輸入備註");
                            new AlertDialog.Builder(ItemActivity.this).setTitle("新增備註").setCancelable(false).setView(editText)
                                    .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (editText.getText().toString().isEmpty()) {
                                                postscriptSet.put(item_id, "無");
                                                statuscopySet.put(item_id,"fix");
                                            }
                                            else {
                                                postscriptSet.put(item_id, editText.getText().toString().trim());
                                            }
                                            idset.put(item_id, true);
                                            change_type.put(item_id, "fix");
                                            statuscopySet.put(item_id,"fix");
                                            holder.item_name.setTextColor(Color.rgb(0, 255, 0));
                                            holder.checkBox.setVisibility(View.GONE);
                                        }
                                    }).setNegativeButton("略過", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    postscriptSet.put(item_id, "無");
                                    idset.put(item_id, true);
                                    change_type.put(item_id, "fix");
                                    statuscopySet.put(item_id,"fix");
                                    holder.item_name.setTextColor(Color.rgb(0, 255, 0));
                                    holder.checkBox.setVisibility(View.GONE);
                                }
                            }).show();
                        }

                    }).setPositiveButton("回復", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (item_status.equals("lend") || item_status.equals("fix")) {
                                return;
                            }
                            postscriptSet.remove(item_id);
                            change_type.remove(item_id);
                            holder.item_name.setTextColor(Color.rgb(0, 0, 0));
                            holder.checkBox.setVisibility(View.VISIBLE);
                            idset.put(item_id, false);
                            statuscopySet.put(item_id,"normal");
                            holder.checkBox.setChecked(false);
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
