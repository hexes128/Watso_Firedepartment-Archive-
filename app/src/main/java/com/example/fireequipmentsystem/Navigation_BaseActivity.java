package com.example.fireequipmentsystem;

import android.content.DialogInterface;
import android.content.Intent;

import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;



import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;



public class Navigation_BaseActivity extends AppCompatActivity {
    private DrawerLayout DL;
    private FrameLayout FL;
    ActionBarDrawerToggle actionBarDrawerToggle;
    protected NavigationView NV;
    protected int CurrentMenuItem = 0;//紀錄目前User位於哪一個項目

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        DL = (DrawerLayout) getLayoutInflater().inflate(R.layout.navigation_drawer, null);
        FL = DL.findViewById(R.id.content_frame);
        NV = DL.findViewById(R.id.Left_Navigation);
        getLayoutInflater().inflate(layoutResID, FL, true);
        super.setContentView(DL);
        setHeaderLayout();
        setUpNavigation();
    }

    private void setHeaderLayout() {
        Global gv;
        gv = (Global) getApplicationContext();
        View headerLayout = NV.getHeaderView(0);
        TextView headerText = headerLayout.findViewById(R.id.navigation_header_userID);
        headerText.setText(gv.UserName);
    }

    private void setUpNavigation() {
        // Set navigation item selected listener
        NV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                final Global gv = (Global) getApplicationContext();
                Log.e("press", menuItem.toString());
                if (!(menuItem == NV.getMenu().getItem(CurrentMenuItem))) {//判斷使者者是否點擊當前畫面的項目，若不是，根據所按的項目做出分別的動作
                    Log.e("press2", NV.getMenu().getItem(CurrentMenuItem).toString());
                    switch (menuItem.getItemId()) {
                        case R.id.Logout: {

                            finish();

                            break;
                        }
                        case R.id.userInfo: {

                            startActivity(new Intent(getApplicationContext(), UserInfo.class));
                            finish();
                            break;

                        }
                        case R.id.mainPage: {

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                            break;

                        }
                        case R.id.inventory: {

                            startActivity(new Intent(getApplicationContext(), InventoryDashboard.class));
                            finish();
                            break;

                        }
                        case R.id.manager: {
                            if(gv.Admin.equals("1")) {


                                startActivity(new Intent(getApplicationContext(), ManagerDashboard.class));
                                finish();
                            }else {
                                new AlertDialog.Builder(Navigation_BaseActivity.this).setCancelable(false).
                                        setTitle("權限不足").
                                        setPositiveButton("確認", new DialogInterface.OnClickListener() {//退出按鈕
                                            public void onClick(DialogInterface dialog, int i) {

                                            }
                                        }).show();
                            }
                            break;
                        }

                    }

                } else {//點擊當前項目時，收起avigation
                    DL.closeDrawer(GravityCompat.START);
                }
                return false;
            }
        });

    }

    public void setUpToolBar() {//設置ToolBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, DL, R.string.app_name, R.string.app_name);
        DL.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
