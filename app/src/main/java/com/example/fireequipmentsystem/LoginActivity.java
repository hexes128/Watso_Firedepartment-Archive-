package com.example.fireequipmentsystem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Button login, read;
    private EditText userid, password;
    Global gv;
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final OkHttpClient client = new OkHttpClient();
        login = findViewById(R.id.login);
        userid = findViewById(R.id.userId);
        password = findViewById(R.id.password);
//        checkBox = findViewById(R.id.checkBox);
//        checkBox.setText("記住密碼");
        Log.e("phoneID", getUniquePsuedoID());
        setTitle("消防設備管理系統");
        sharedPreferences = getSharedPreferences("Token", getApplicationContext().MODE_PRIVATE);

        final TextInputLayout textInputLayout = findViewById(R.id.til_et_name);
        final ExecutorService service = Executors.newSingleThreadExecutor();
        gv = (Global) getApplicationContext();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                if (userid.length() == 0) {
                    textInputLayout.setError("Error in name input");

                } else {
                    textInputLayout.setError(null);
                }

                if (!gv.checkNetwork()) {
                    return;
                }

                gv.userID = userid.getText().toString();
                service.submit(new Runnable() {
                    @Override
                    public void run() {

                        Request request = new Request.Builder().url("http://140.133.78.44/Login/login?id=" + userid.getText() + "&password=" + password.getText() + "&phoneid=" + getUniquePsuedoID()).build();
                        try {
                            final Response response = client.newCall(request).execute();
                            final String resStr = response.body().string();
                            JSONObject jsonObject = new JSONObject(resStr);

                            String Access = jsonObject.getString("Access").trim();

                            switch (Access) {
                                case ("登入成功"): {

                                    gv.Token = jsonObject.getString("Token").trim();
                                    gv.UserName = jsonObject.getString("UserName").trim();
                                    gv.Admin = jsonObject.getString("Admin").trim();


                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                    break;
                                }
                                case ("帳號或密碼錯誤"): {
                                    Snackbar.make(view, "帳號或密碼錯誤", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    break;
                                }
                                case ("此帳號已被其他裝置綁定"): {
                                    Snackbar.make(view, "此帳號已被其他裝置綁定", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                    break;
                                }


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(LoginActivity.this).setCancelable(false).
                    setTitle("離開").setCancelable(false).
                    setMessage("確定離開此程式?").
                    setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                        public void onClick(DialogInterface dialog, int i) {

                            finish();//關閉activity
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    //不退出不用執行任何操作
                }
            }).show();


        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("Token", gv.Token);
        edit.putString("UserName", gv.UserName);

        edit.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (gv.TokenExpired) {
            gv.TokenExpired = false;
            new AlertDialog.Builder(this).setCancelable(false).
                    setTitle("認證時間逾期").
                    setMessage("請重新登入").
                    setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                        public void onClick(DialogInterface dialog, int i) {


                        }
                    }).show();


        }

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
}
