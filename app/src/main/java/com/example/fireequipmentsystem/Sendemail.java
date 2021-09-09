package com.example.fireequipmentsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Sendemail extends AppCompatActivity {


    final ExecutorService service = Executors.newSingleThreadExecutor();
    final OkHttpClient client = new OkHttpClient();
    List<String> placelist;
    List<String> year, month;
    private Spinner yearspinner, monthspinner, placespinner;
    private Button send;

    Global gv;
    private Handler handler;


    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendemail);
        setTitle("匯出紀錄");

        placelist = new ArrayList<>();
        year = new ArrayList<>();
        month = new ArrayList<>();

        yearspinner = findViewById(R.id.yearspinner);
        monthspinner = findViewById(R.id.monthspinner);
        placespinner = findViewById(R.id.place);
        send = findViewById(R.id.send);
        gv = (Global) getApplicationContext();
        ArrayAdapter<String> yearadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, year);
        yearadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        progressDialog = new ProgressDialog(Sendemail.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        handler = new Handler();
        final ArrayAdapter<String> monthadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, month);
        monthadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Runnable timeout = new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "逾時", Toast.LENGTH_SHORT).show();
            }
        };

        service.submit(new Runnable() {

            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/RecordFilter/getInfo").build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();

                    JSONObject jsonObject = new JSONObject(resStr);
                    JSONArray place = jsonObject.getJSONArray("place");
                    for (int i = 0; i < place.length(); i++) {
                        placelist.add(place.getJSONObject(i).getString("item_place"));

                    }
                    final ArrayAdapter<String> placeadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, placelist);
                    placeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
        yearspinner.setAdapter(yearadapter);
        monthspinner.setAdapter(monthadapter);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("寄發中");
                progressDialog.show();
                progressDialog.setCancelable(false);

                handler.postDelayed(timeout, 20000);
                service.submit(new Runnable() {

                    String time = yearspinner.getSelectedItem().toString().trim() + monthspinner.getSelectedItem().toString().trim();
                    String place = placespinner.getSelectedItem().toString().trim();

                    @Override
                    public void run() {
                        Request request = new Request.Builder().url("http://140.133.78.44/RecordFilter/sendmail?username=" + gv.UserName + "&place=" + place + "&time=" + time).build();
                        try {
                            final Response response = client.newCall(request).execute();
                            final String resStr = response.body().string();

                            progressDialog.dismiss();
                            handler.removeCallbacks(timeout);


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
