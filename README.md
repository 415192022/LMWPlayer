

## LMWPlayer

#### gradle接入

```
	//工程根目录build.gradle
	allprojects {
		repositories {
			maven { url 'https://www.jitpack.io' }
		}
	}
	
	//项目build.gradle
	dependencies {
	        implementation 'com.github.415192022:LMWPlayer:latest.release'
	}
```

#### 视频播放

##### 清晰度切换播放器

```xml
    <com.lmw.ijkplayer.video.SmartPickVideo
        android:id="@+id/video_player"
        android:layout_width="match_parent"
        android:layout_height="220dp">

    </com.lmw.ijkplayer.video.SmartPickVideo>
```

```java
  //配置两种视频源
  videoModelList = new ArrayList<>();
  videoModelList.add(new SwitchVideoModel("标清", url_low));
  videoModelList.add(new SwitchVideoModel("超清", url_high));

  //配置视频
  videoPlayer.setUp(videoModelList, true, name);
```

详情配置参考 [AloneActivity](videodemo/src/main/java/com/lmw/videodemo/AloneActivity.java)



##### 列表播放器

1、`RecycleView.ViewHolder `实现`com.lmw.ijkplayer.videoList.items.ListItem`接口，重写item进入屏幕与移出屏幕的方法

```java
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
```

2、`Adapter`实现`com.lmw.ijkplayer.videoList.scroll.ItemsProvider`，重写接口方法

```java
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
```

3、配置RecycleView

```java
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
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) 			{
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
```

具体配置参考[ListActivity](videodemo/src/main/java/com/lmw/videodemo/ListActivity.java)

#### 音频播放

1、通过start或bind方式启动音频播放service

```java
public void startService() {
    if (!mIsBound || mPlayService == null) {
            Intent intent = new Intent(CoreLib.instance.getContext(), AudioPlayService.class);
            mConnection = new AloneServiceConnection();
            mIsBound = CoreLib.instance.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
     }
}

private class AloneServiceConnection implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      	//获取service对象
        mPlayService =((AudioPlayService.AudioPlayBinder)service).getAudioPlayService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
```

2、启动播放

```java
mPlayService.setAutoPlaying(mAutoPlay); //是否自动播放
mPlayService.setAudioPlayListener(new AudioPlayListener() {
            @Override
            public void onError(String msg) {
                
            }

            @Override
            public void onPreparing() {

            }

            @Override
            public void onPlaying() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onProgress(long currentPosition, long duration) {

            }

            @Override
            public void onBufferingUpdate(int percent) {

            }
});
mPlayService.initDataSource(url);
```

详细控制逻辑可参考[AlonePlayManager](audiodemo/src/main/java/com/lmw/audiodemo/manager/AlonePlayManager.java)

`audiodemo`还提供了列表音频播放的参考示例[WorksPlayManager](audiodemo/src/main/java/com/lmw/audiodemo/manager/WorksPlayManager.java)与[WorksPlayService](audiodemo/src/main/java/com/lmw/audiodemo/service/WorksPlayService.java)


#### 视频播放

基于`GSYVideoPlayer`封装，继承`StandardGSYVideoPlayer`实现`SmartPickVideo`，切换视频源时，保存当前视频源播放进度，切换视频播放地址后移动到该进度。

列表视频播放控制，通告对RecyclerView滚动位置的监听，回调通知当前ViewHolder的显示状态。

#### 音频播放

创建Service，在Service中使用`ijkplayer`进行音频播放，并进行控制。
