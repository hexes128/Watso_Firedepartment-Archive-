package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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

public class alllend_fix_record extends AppCompatActivity {

    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();

    List<String> idset;
    HashMap<String, String> nameSet, change_date, return_date, placeSet, staffSet, statusSet, postscriptSet;

    Global gv;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alllend_fix_record);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);
        gv = (Global) getApplicationContext();
        setTitle("查詢結果");
        idset = new ArrayList<>();
        nameSet = new HashMap<>();
        staffSet = new HashMap<>();
        placeSet = new HashMap<>();
        change_date = new HashMap<>();
        statusSet = new HashMap<>();
        return_date = new HashMap<>();
        mAdapter = new MyAdapter(idset);
        postscriptSet = new HashMap<>();
        mRecyclerView.setAdapter(mAdapter);


        try {


            JSONObject jsonObject = new JSONObject(gv.Record);
            JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                idset.add(jsonArray.getJSONObject(i).getString("item_id").trim());
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
                if (jsonArray.getJSONObject(i).getString("return_date").trim().equals("none")) {
                    return_date.put(jsonArray.getJSONObject(i).getString("item_id").trim(), "尚未歸還");
                } else {
                    return_date.put(jsonArray.getJSONObject(i).getString("item_id").trim(), jsonArray.getJSONObject(i).getString("return_date").trim().substring(0, 4) + "/" +
                            jsonArray.getJSONObject(i).getString("return_date").trim().substring(4, 6) + "/" +
                            jsonArray.getJSONObject(i).getString("return_date").trim().substring(6, 8) + " " +
                            jsonArray.getJSONObject(i).getString("return_date").trim().substring(8, 10) + ":" +
                            jsonArray.getJSONObject(i).getString("return_date").trim().substring(10, 12));
                }

            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();

                }
            });


        } catch (JSONException e) {
            Log.e("json", e.getMessage());
        }


    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<String> midset;


        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView idView;
            public TextView nameView;
            public TextView placeView;
            public TextView statusView;
            public TextView staffview;
            public TextView outdate, returndate;

            public ViewHolder(View v) {
                super(v);
                idView = v.findViewById(R.id.item_id);
                nameView = v.findViewById(R.id.item_name);
                placeView = v.findViewById(R.id.item_place);
                statusView = v.findViewById(R.id.item_status);
                staffview = v.findViewById(R.id.staff);
                outdate = v.findViewById(R.id.outdate);
                returndate = v.findViewById(R.id.returndate);
            }
        }

        public MyAdapter(List<String> idset) {

            midset = idset;

        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.all_lend_fix_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final String item_id = idset.get(position);


            holder.idView.setText(item_id);
            holder.nameView.setText(nameSet.get(item_id));
            holder.placeView.setText(placeSet.get(item_id));
            holder.statusView.setText(statusSet.get(item_id));
            holder.staffview.setText(staffSet.get(item_id));
            holder.outdate.setText(change_date.get(item_id));
            holder.returndate.setText(return_date.get(item_id));

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(alllend_fix_record.this).setCancelable(false).
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
            return idset.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

}
