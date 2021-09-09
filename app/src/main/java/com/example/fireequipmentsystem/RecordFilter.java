package com.example.fireequipmentsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class RecordFilter extends AppCompatActivity {

    List<String> stafflist, placelist, recordtype;
    List<String> year, month, date;
    private Spinner yearspinner0, monthspinner0, datespinner0, yearspinner1, monthspinner1, datespinner1, staffspinner, placespinner, recordspinner, returnyetspinner;
    private Button send;

    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    String[] recordarray = new String[]{"盤點紀錄", "報修紀錄", "借出紀錄"};
    String[] returnyetarray = new String[]{"不限", "是", "否"};
    Global gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_filter);
        setTitle("紀錄查詢");
        year = new ArrayList<>();
        month = new ArrayList<>();
        date = new ArrayList<>();
        stafflist = new ArrayList<>();
        placelist = new ArrayList<>();
        recordtype = new ArrayList<>();
        yearspinner0 = findViewById(R.id.yearspinner);
        yearspinner1 = findViewById(R.id.endyear);
        monthspinner0 = findViewById(R.id.monthspinner);
        monthspinner1 = findViewById(R.id.endmonth);
        datespinner0 = findViewById(R.id.startdate);
        datespinner1 = findViewById(R.id.enddate);
        send = findViewById(R.id.send);
        staffspinner = findViewById(R.id.staff);
        placespinner = findViewById(R.id.place);
        recordspinner = findViewById(R.id.record_type);
        returnyetspinner = findViewById(R.id.returnyet);

        ArrayAdapter<String> yearadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, year);
        yearadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> monthadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, month);
        monthadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> dateadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, date);
        dateadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> returnyetadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, returnyetarray);
        returnyetadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> recordadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recordarray);
        recordadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> placeadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, placelist);
        placeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> staffadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, stafflist);
        staffadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        gv = (Global) getApplicationContext();
        service.submit(new Runnable() {

            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/RecordFilter/getInfo").build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();


                    JSONObject jsonObject = new JSONObject(resStr);


                    JSONArray place = jsonObject.getJSONArray("place");
                    JSONArray users = jsonObject.getJSONArray("users");
                    placelist.add("不限");
                    for (int i = 0; i < place.length(); i++) {
                        placelist.add(place.getJSONObject(i).getString("item_place"));

                    }
                    stafflist.add("不限");
                    for (int i = 0; i < users.length(); i++) {
                        stafflist.add(users.getJSONObject(i).getString("Name"));
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staffspinner.setAdapter(staffadapter);
                            placespinner.setAdapter(placeadapter);
                        }
                    });


                } catch (IOException e) {
                    Log.e("IO", e.getMessage());
                } catch (JSONException e) {
                    Log.e("json", e.getMessage());
                }

            }

        });

        for (int i = 2020; i <= 2029; i++) {

            year.add(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            month.add(String.format("%02d", i));

        }
        for (int i = 1; i <= 31; i++) {
            date.add(String.format("%02d", i));
        }


        yearspinner0.setAdapter(yearadapter);
        monthspinner0.setAdapter(monthadapter);
        datespinner0.setAdapter(dateadapter);
        yearspinner1.setAdapter(yearadapter);
        monthspinner1.setAdapter(monthadapter);
        datespinner1.setAdapter(dateadapter);
        recordspinner.setAdapter(recordadapter);
        returnyetspinner.setAdapter(returnyetadapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String min = yearspinner0.getSelectedItem().toString() + monthspinner0.getSelectedItem().toString() + datespinner0.getSelectedItem().toString();
                final String max = yearspinner1.getSelectedItem().toString() + monthspinner1.getSelectedItem().toString() + datespinner1.getSelectedItem().toString();
                if (Integer.parseInt(max) < Integer.parseInt(min)) {
                    Toast.makeText(getApplicationContext(), "日期選擇錯誤", Toast.LENGTH_SHORT).show();
                    return;
                }


                service.submit(new Runnable() {

                    @Override
                    public void run() {


                        String recordtype = "";
                        switch (recordspinner.getSelectedItem().toString()) {
                            case ("盤點紀錄"): {
                                recordtype = "inventory";
                                break;
                            }
                            case ("報修紀錄"): {
                                recordtype = "fix";
                                break;
                            }
                            case ("借出紀錄"): {
                                recordtype = "lend";
                                break;
                            }
                        }
                        String returnyet = "";
                        switch (returnyetspinner.getSelectedItem().toString()) {
                            case ("是"): {
                                returnyet = "yes";
                                break;
                            }
                            case ("否"): {
                                returnyet = "no";
                            }

                        }

                        Request request = new Request.Builder().url("http://140.133.78.44/RecordFilter/searchInfo?recordType=" + recordtype + "&staff=" + staffspinner.getSelectedItem() + "&place=" + placespinner.getSelectedItem() + "&min=" + min + "&max=" + max + "&returnyet=" + returnyet).build();
                        try {

                            Log.e("網址", "http://140.133.78.44/RecordFilter/searchInfo?recordType=" + recordtype + "&staff=" + staffspinner.getSelectedItem() + "&place=" + placespinner.getSelectedItem() + "&min=" + min + "&max=" + max + "&returnyet=" + returnyet);

                            final Response response = client.newCall(request).execute();
                            final String resStr = response.body().string();
                            gv.Record = resStr;


                            switch (recordspinner.getSelectedItem().toString()) {
                                case ("盤點紀錄"): {
                                    startActivity(new Intent(RecordFilter.this, Inventory_Record.class));
                                    break;
                                }

                                case ("報修紀錄"):
                                case ("借出紀錄"): {
                                    startActivity(new Intent(RecordFilter.this, alllend_fix_record.class));
                                    break;
                                }

                            }


                        } catch (IOException e) {
                            Log.e("IO", e.getMessage());
                        }

                    }

                });
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
}
