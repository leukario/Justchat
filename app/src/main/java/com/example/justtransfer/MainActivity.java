package com.example.justtransfer;


import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //initializer();
    }

     public void send_page(View v) {
         Intent i = new Intent(this, sendpage.class);
         startActivity(i);
         Intent i1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
         startActivityForResult(i1, 1);
     }

     public void recieve_page(View v)
     {
         Intent i = new Intent(this, recievepage.class);
         startActivity(i);
         Intent i1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
         startActivityForResult(i1, 1);
     }


}
