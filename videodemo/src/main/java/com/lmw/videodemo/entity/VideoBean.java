package com.lmw.videodemo.entity;

public class VideoBean {
    private String title;
    private String video_path;



    public VideoBean(String title, String video_path) {
        this.title = title;
        this.video_path = video_path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "title='" + title + '\'' +
                ", video_path='" + video_path + '\'' +
                '}';
    }
}
