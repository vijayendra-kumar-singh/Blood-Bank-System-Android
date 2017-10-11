package com.example.mohan.bbms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;

        SharedPreferences sharedPreferences = getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("isLoggedIn", false)){
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, availableRequests.class);
        }
        startActivity(intent);
        finish();
    }
}