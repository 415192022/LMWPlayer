package com.lmw.videodemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lmw.ijkplayer.videoList.calculator.SingleListViewItemActiveCalculator;
import com.lmw.ijkplayer.videoList.scroll.RecyclerViewItemPositionGetter;
import com.lmw.videodemo.adapter.VideoListAdapter;
import com.lmw.videodemo.entity.VideoBean;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoListAdapter mAdapter;
    private List<VideoBean> videoBeanList;
    private LinearLayoutManager linearLayoutManager;
    private SingleListViewItemActiveCalculator calculator;
    private int mScrollState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initData();

        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new VideoListAdapter(this, videoBeanList);
        mAdapter.setRecyclerView(recyclerView);
        recyclerView.setAdapter(mAdapter);

        calculator = new SingleListViewItemActiveCalculator(mAdapter, new RecyclerViewItemPositionGetter(linearLayoutManager, recyclerView));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                mScrollState = newState;
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !mAdapter.getDataList().isEmpty()) {
                    calculator.onScrollStateIdle();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                calculator.onScrolled(mScrollState);
            }
        });

    }

    private void initData() {
        videoBeanList = new ArrayList<>();
        videoBeanList.add(new VideoBean("第1个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第2个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第3个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第4个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第5个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第6个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第7个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第8个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第9个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoBeanList.add(new VideoBean("第10个", "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
    }
}
