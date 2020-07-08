package com.lmw.videodemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lmw.ijkplayer.video.SimpleControlVideo;
import com.lmw.ijkplayer.videoList.items.ListItem;
import com.lmw.ijkplayer.videoList.scroll.ItemsProvider;
import com.lmw.videodemo.R;
import com.lmw.videodemo.entity.VideoBean;

import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder> implements ItemsProvider {

    private Context mContext;
    private List<VideoBean> dataList;
    private RecyclerView mRecyclerView;

    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
    }

    public VideoListAdapter(Context mContext, List<VideoBean> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    public List<VideoBean> getDataList(){
        return dataList;
    }
    @NonNull
    @Override
    public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.layout_video_item, viewGroup, false);
        return new VideoListViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListViewHolder myViewHolder, int i) {
        VideoBean videoBean = dataList.get(i);
        myViewHolder.tvTitle.setText(videoBean.getTitle());
        myViewHolder.videoPlayer.setUp(videoBean.getVideo_path(),true,videoBean.getTitle());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public ListItem getListItem(int position) {
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof ListItem) {
            return (ListItem) viewHolder;
        } else {
            return null;
        }
    }

    @Override
    public int listItemSize() {
        return dataList == null ? 0 : dataList.size();
    }


    public class VideoListViewHolder extends RecyclerView.ViewHolder implements ListItem {
        private TextView tvTitle;
        private SimpleControlVideo videoPlayer;

        public VideoListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            videoPlayer = itemView.findViewById(R.id.video_player);
        }

        //进入屏幕
        @Override
        public void setActive(View newActiveView, int newActiveViewPosition) {
            videoPlayer.startPlayLogic();
        }

        //移出屏幕
        @Override
        public void deactivate(View currentView, int position) {
            videoPlayer.release();
        }
    }


}
