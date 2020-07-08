package com.lmw.audiodemo.model;


public class Works {
    public int payType;
    public int listenTotal;
    public String headPicUrl;
    public String categoryTag;
    public String nickName;
    public String topic;
    public int category;
    public int age;
    public int authType;
    public String title;
    public String content;
    public String createTime;
    public String forwardTotal;
    public int status; //0= 待审核 1=审核通过  2= 审核不通过
    public String coverUrl;
    public int opusId;
    public String voiceUrl;

    public Works(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }


    public Works(String title, String voiceUrl) {
        this.title = title;
        this.voiceUrl = voiceUrl;
    }

    public Works(String nickName, String title, String voiceUrl) {
        this.nickName = nickName;
        this.title = title;
        this.voiceUrl = voiceUrl;
    }
    public Works(String nickName, String title, String voiceUrl,String coverUrl) {
        this.nickName = nickName;
        this.title = title;
        this.voiceUrl = voiceUrl;
        this.coverUrl = coverUrl;
    }
}
