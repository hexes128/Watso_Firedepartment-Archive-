package com.example.fireequipmentsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.fireequipmentsystem.LoginActivity.getUniquePsuedoID;

public class UserInfo extends Navigation_BaseActivity {
    private Button bindphoneBtn;
    Global gv;
    Date date;
    private TextView name;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;
    String bindPhone;
    ArrayList<String> userInfoList;
    private NotificationManager notificationManager;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setTitle("個人資料");
        setUpToolBar();
        gv = (Global) getApplicationContext();
        NV.getMenu().getItem(CurrentMenuItem).setChecked(true);
        CurrentMenuItem = 1;
        context = this;
        final ExecutorService service = Executors.newSingleThreadExecutor();
        final OkHttpClient client = new OkHttpClient();
        final OkHttpClient client2 = new OkHttpClient();
        mRecyclerView = findViewById(R.id.userInfoRecycleView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        name = findViewById(R.id.name);

        bindphoneBtn = findViewById(R.id.bindphone);
        service.submit(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("http://140.133.78.44/UserInfo/selectUserInfo?Token=" + gv.Token).build();
                try {
                    final Response response = client2.newCall(request).execute();
                    final String resStr = response.body().string();
                    final JSONObject jsonObject = new JSONObject(resStr);
                    final JSONArray jsonArray = jsonObject.getJSONArray("JsonResult");

                    Log.e("", jsonArray.getJSONObject(0).getString("Name").trim());


                    if (jsonArray.getJSONObject(0).getString("phoneid").trim().equals("none")) {
                        bindPhone = "尚未綁定手機";
                    } else {
                        bindPhone = "已綁定手機";
                    }
                    userInfoList = new ArrayList<>();

                    userInfoList.add("姓名 : " + jsonArray.getJSONObject(0).getString("Name").trim());
                    userInfoList.add("信箱 : " + jsonArray.getJSONObject(0).getString("email").trim());
                    userInfoList.add("手機號碼 : " + jsonArray.getJSONObject(0).getString("phone").trim());
                    userInfoList.add("綁定手機 : " + bindPhone);
                    userInfoList.add("美簽管制號碼 : " + jsonArray.getJSONObject(0).getString("US_VISA").trim());
                    userInfoList.add("美簽到期日 : " + jsonArray.getJSONObject(0).getString("US_VISA_deadline").trim());

                    mAdapter = new MyAdapter(userInfoList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                        }
                    });


                    date = new Date();
                    SimpleDateFormat ft =
                            new SimpleDateFormat("yyyyMMdd");
                    Log.e("time", ft.format(date));
                    if (Integer.parseInt(jsonArray.getJSONObject(0).getString("US_VISA_deadline").trim()) - Integer.parseInt(ft.format(date)) < 180) {
                        Log.e("", "到期");
                        showNotify(jsonArray.getJSONObject(0).getString("US_VISA_deadline").trim().substring(0, 4) + "/" +
                                jsonArray.getJSONObject(0).getString("US_VISA_deadline").trim().substring(4, 6) + "/" +
                                jsonArray.getJSONObject(0).getString("US_VISA_deadline").trim().substring(6, 8) + " ", context);
                    } else {
                        Log.e("", "沒到期");
                    }

                } catch (IOException e) {
                    Log.e("IO", e.getMessage());
                } catch (JSONException e) {
                    Log.e("json", e.getMessage());
                }
            }
        });


        bindphoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder ad = new AlertDialog.Builder(UserInfo.this);
                ad.setTitle("綁定手機");
                ad.setMessage("將此帳號綁定手機");
                ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                    public void onClick(DialogInterface dialog, int i) {
                        service.submit(new Runnable() {
                            @Override
                            public void run() {
                                Request request = new Request.Builder().url("http://140.133.78.44/BindPhone/BindPhone?Token=" + gv.Token + "&phoneid=" + getUniquePsuedoID()).build();
                                try {
                                    final Response response = client.newCall(request).execute();
                                    final String resStr = response.body().string();

                                    if (resStr.equals("綁定成功")) {
                                        Snackbar.make(view, "綁定成功", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                ad.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        //不退出不用執行任何操作
                    }
                });
                ad.setNeutralButton("解除綁定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        service.submit(new Runnable() {
                            @Override
                            public void run() {
                                //TODO 後端
                                Request request = new Request.Builder().url("http://140.133.78.44/BindPhone/BindPhone?Token=" + gv.Token + "&phoneid=none").build();
                                try {
                                    final Response response = client.newCall(request).execute();
                                    final String resStr = response.body().string();

                                    if (resStr.equals("綁定成功")) {
                                        Snackbar.make(view, "解除綁定", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                ad.show();//顯示對話框

            }
        });
    }

    public void showNotify(String usVisaDate, Context context) {
        String CHANNEL_ONE_ID = "Channel One";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH); //IMPORTANCE_HIGH 用戶通知級別(發出通知並顯示提示通知)
            notificationChannel.enableLights(true); //閃爍指示燈
            notificationChannel.setLightColor(Color.RED); //指示燈顏色設置(不是每一台手機都支援)
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel); //綁定Channel


            Notification notification = new Notification.Builder(context)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ONE_ID)
                    .setSmallIcon(R.drawable.information)
                    .setTicker("美簽即將到期")
                    .setContentTitle("美簽即將到期")
                    .setContentText("到期日 :" + usVisaDate)
                    .setWhen(System.currentTimeMillis()).build();
            manager.notify(1, notification);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause", "onPause");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void ConfirmExit() {//退出確認
        AlertDialog.Builder ad = new AlertDialog.Builder(UserInfo.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                finish();//關閉activity
            }
        });
        ad.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//顯示對話框
    }

    public static String getUniquePsuedoID() {
        String serial = null;
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.USER.length() % 10;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    public class MyAdapter extends RecyclerView.Adapter<UserInfo.MyAdapter.ViewHolder> {

        private ArrayList myData;


        public MyAdapter(ArrayList data) {

            myData = data;

        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView userInfo;

            public ViewHolder(View v) {
                super(v);
                userInfo = v.findViewById(R.id.textView);

            }
        }





        @Override
        public UserInfo.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_info_data_view, parent, false);
            UserInfo.MyAdapter.ViewHolder vh = new UserInfo.MyAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final UserInfo.MyAdapter.ViewHolder holder, final int position) {

            holder.userInfo.setText(myData.get(position).toString());

        }

        @Override
        public int getItemCount() {
            return myData.size();
        }
    }

}
