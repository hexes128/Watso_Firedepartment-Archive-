package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Announcement extends AppCompatActivity {

    EditText staff, title, billBoardContent;
    Button submit;
    Global gv;
    final OkHttpClient client = new OkHttpClient();
    final ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        gv = (Global) getApplicationContext();
        setTitle("發布公告");
        staff = findViewById(R.id.name);
        billBoardContent = findViewById(R.id.billBoardContent);
        title = findViewById(R.id.title);
        submit = findViewById(R.id.submit);
        staff.setText(gv.UserName);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (gv.TokenVerification()) {
                            Request request = new Request.Builder().url("http://140.133.78.44/Billboard/billboard?staff=" + staff.getText() + "&title=" + title.getText() + "&content=" + billBoardContent.getText()).build();
                            try {
                                Response response = client.newCall(request).execute();
                                String resStr = response.body().string().trim();
                                if (resStr.equals("成功")) {
                                    Log.e("公告狀態", "成功");
                                    finish();
                                }
                            } catch (Exception e) {

                            }
                        }

                    }
                });


            }
        });
    }
}
