package com.lmw.audiodemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.lmw.audiodemo.R;
import com.lmw.audiodemo.manager.FloatServiceManager;
import com.lmw.audiodemo.manager.WorksPlayManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WorksPlayManager.getInstance().startService();
        FloatServiceManager.getInstance().startService();
    }

    public void playAlone(View view) {
        startActivity(new Intent(this, AloneActivity.class));
    }


    public void playList(View view) {
        startActivity(new Intent(this, WorksActivity.class));
    }


    @Override
    protected void onDestroy() {
        FloatServiceManager.getInstance().stopService();
        WorksPlayManager.getInstance().stopService();
        super.onDestroy();
    }
}
