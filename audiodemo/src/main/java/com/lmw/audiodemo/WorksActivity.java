package com.lmw.audiodemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lmw.audiodemo.R;
import com.lmw.audiodemo.listener.WorksPlayListener;
import com.lmw.audiodemo.manager.FloatServiceManager;
import com.lmw.audiodemo.manager.WorksPlayManager;
import com.lmw.audiodemo.model.Works;

import java.util.ArrayList;
import java.util.List;

public class WorksActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private Button btnInitData, btnPlayOrPause, btnNext, btnPre, btnLoop, btnFloat;
    private TextView tvMsg;

    private List<Works> worksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_works);
        initView();
        initData();
        setListener();


        WorksPlayManager.getInstance().setWorksPlayListener(new WorksPlayListener() {
            @Override
            public void onProgress(long currentPosition, long duration, int position) {
                seekBar.setMax((int) duration);
                seekBar.setProgress((int) currentPosition);
            }

            @Override
            public void onBufferingUpdate(int percent, int position) {
                int max = seekBar.getMax();
                seekBar.setSecondaryProgress((int) Math.ceil(max * percent * 0.01));
            }

            @Override
            public void onWorksChange(Works model, int position) {
                StringBuilder sb = new StringBuilder();
                sb.append(position).append("\n").append(model.nickName).append("\n").append(model.title).append("\n").append(model.voiceUrl).append("\n");
                tvMsg.setText(sb.toString());
            }

            @Override
            public void onPlayStateChange(boolean isPlaying, int position) {
                if (isPlaying) {
                    btnPlayOrPause.setText("暂停");
                } else {
                    btnPlayOrPause.setText("播放");
                }
            }
        });
    }

    private void setListener() {
        btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorksPlayManager.getInstance().playOrPause();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorksPlayManager.getInstance().nextSong();
            }
        });

        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorksPlayManager.getInstance().preSong();
            }
        });

        btnInitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorksPlayManager.getInstance().setListData(worksList);
                WorksPlayManager.getInstance().playSong(0);
            }
        });
        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean looping = WorksPlayManager.getInstance().isLooping();
                if (looping) {
                    WorksPlayManager.getInstance().setLooping(false);
                    btnLoop.setText("设置单曲循环");
                } else {
                    WorksPlayManager.getInstance().setLooping(true);
                    btnLoop.setText("取消单曲循环");
                }
            }
        });
        btnFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showFloatView = FloatServiceManager.getInstance().isShowFloatView();
                if (showFloatView) {
                    FloatServiceManager.getInstance().hideFloatView();
                    btnFloat.setText("显示浮标");
                } else {
                    FloatServiceManager.getInstance().showFloatView();
                    btnFloat.setText("隐藏浮标");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                WorksPlayManager.getInstance().seekTo(seekBar.getProgress());
            }
        });
    }


    private void initData() {
        worksList.clear();
        worksList.add(new Works("阿桑", "一直很安静", "https://oijmns1ch.qnssl.com/antiwork_today.mp3","图片地址1111"));
        worksList.add(new Works("音阙诗听", "红昭愿", "https://oijmns1ch.qnssl.com/antiwork_today.mp3","图片地址2222"));
        worksList.add(new Works("彭清", "起风了", "https://oijmns1ch.qnssl.com/antiwork_today.mp3","图片地址3333"));
        worksList.add(new Works("李袁杰", "离人愁", "https://oijmns1ch.qnssl.com/antiwork_today.mp3","图片地址4444"));
        worksList.add(new Works("广东雨神", "广东爱情故事", "https://oijmns1ch.qnssl.com/antiwork_today.mp3","图片地址5555"));
    }

    private void initView() {
        seekBar = findViewById(R.id.seekBar);
        btnInitData = findViewById(R.id.btnInitData);
        btnPlayOrPause = findViewById(R.id.btnPlayOrPause);
        btnNext = findViewById(R.id.btnNext);
        btnPre = findViewById(R.id.btnPre);
        tvMsg = findViewById(R.id.tvMsg);
        btnLoop = findViewById(R.id.btnLoop);
        btnFloat = findViewById(R.id.btnFloat);
    }
}
