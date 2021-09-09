package com.example.fireequipmentsystem;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Global extends Application {

    public String Token = "", TokenStatus, UserName;
    public String userID;
    public String Selectedplace;
    public String Admin;
    public HashMap<String, String> placeset = new HashMap<>();
    public HashMap<String, Boolean> idset = new HashMap<>();
    public String Record;

    public boolean TokenExpired = false;
    public static Object lockObject = new Object();
    public String intentfrom = "";
    final ExecutorService service = Executors.newSingleThreadExecutor();

    Runnable postToken = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("http://140.133.78.44/TokenVerification/TokenVerification?Token=" + Token.trim()).build();

            synchronized (lockObject) {
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
                    JSONObject jsonObject = new JSONObject(resStr);

                    TokenStatus = jsonObject.getString("Tokenstatus").trim();

                    switch (TokenStatus) {
                        case ("認證成功"): {
                            Token = jsonObject.getString("NewToken").trim();
                            break;
                        }
                        case ("認證過期"): {
                            TokenExpired = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lockObject.notifyAll();
            }


        }
    };


    public boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "請檢查網路連線是否正常", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public boolean TokenVerification() {
        service.submit(postToken);


        synchronized (lockObject) {

            try {
                lockObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        return TokenStatus.equals("認證成功");
    }


}