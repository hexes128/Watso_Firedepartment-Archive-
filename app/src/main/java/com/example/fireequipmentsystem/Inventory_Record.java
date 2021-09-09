package com.example.fireequipmentsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class Inventory_Record extends AppCompatActivity {

    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    Global gv;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private List<String> item_id, item_status, inventory_staff, inventory_date, item_place, item_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory__record);
        gv = (Global) getApplicationContext();
        setTitle("設備盤點紀錄");

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);

        item_id = new ArrayList<>();
        item_name = new ArrayList<>();
        item_status = new ArrayList<>();
        inventory_date = new ArrayList<>();
        inventory_staff = new ArrayList<>();
        item_place = new ArrayList<>();


        mAdapter = new MyAdapter(item_id, item_name, item_place, item_status, inventory_staff, inventory_date);
        mRecyclerView.setAdapter(mAdapter);


        try {


            JSONObject jsonObject = new JSONObject(gv.Record);
            JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");
            if (jsonArray.length() == 0) {
                Toast.makeText(getApplicationContext(), "查無紀錄", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                item_id.add(jsonArray.getJSONObject(i).getString("item_id").trim());
                item_name.add(jsonArray.getJSONObject(i).getString("item_name").trim());
                item_place.add(jsonArray.getJSONObject(i).getString("item_place").trim());
                inventory_date.add(jsonArray.getJSONObject(i).getString("inventory_date").trim().substring(0, 4) + "/" +
                        jsonArray.getJSONObject(i).getString("inventory_date").trim().substring(4, 6) + "/" +
                        jsonArray.getJSONObject(i).getString("inventory_date").trim().substring(6, 8) + " " +
                        jsonArray.getJSONObject(i).getString("inventory_date").trim().substring(8, 10) + ":" +
                        jsonArray.getJSONObject(i).getString("inventory_date").trim().substring(10, 12));
                inventory_staff.add(jsonArray.getJSONObject(i).getString("inventory_staff").trim());
                item_status.add(jsonArray.getJSONObject(i).getString("item_status").trim());

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

    public class MyAdapter extends RecyclerView.Adapter<Inventory_Record.MyAdapter.ViewHolder> {

        private List<String> mitem_id;
        private List<String> mitem_name;
        private List<String> mitem_place;
        private List<String> mitem_status;
        private List<String> minventory_staff;
        private List<String> minventory_date;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView idView;
            public TextView nameView;
            public TextView placeView;
            public TextView statusView;
            public TextView staffView;
            public TextView dateView;

            public ViewHolder(View v) {
                super(v);
                idView = v.findViewById(R.id.item_id);
                nameView = v.findViewById(R.id.item_name);
                placeView = v.findViewById(R.id.item_place);
                statusView = v.findViewById(R.id.item_status);
                staffView = v.findViewById(R.id.staff);
                dateView = v.findViewById(R.id.date);
            }
        }

        public MyAdapter(List<String> item_id, List<String> item_name, List<String> item_place, List<String> item_status, List<String> inventory_staff, List<String> inventory_date) {
            mitem_id = item_id;
            mitem_name = item_name;
            mitem_place = item_place;
            mitem_status = item_status;
            minventory_staff = inventory_staff;
            minventory_date = inventory_date;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.record_itemview, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.idView.setText(mitem_id.get(position).trim());
            holder.nameView.setText(mitem_name.get(position).trim());
            holder.placeView.setText(mitem_place.get(position).trim());
            holder.statusView.setText(mitem_status.get(position).trim());
            holder.staffView.setText(minventory_staff.get(position).trim());
            holder.dateView.setText(minventory_date.get(position).trim());


        }

        @Override
        public int getItemCount() {
            return mitem_id.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }


}
