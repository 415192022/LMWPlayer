package com.lmw.videodemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void aloneVideo(View view) {
        startActivity(new Intent(this, AloneActivity.class));
    }


    public void listVideo(View view) {
        startActivity(new Intent(this, ListActivity.class));
    }
}
