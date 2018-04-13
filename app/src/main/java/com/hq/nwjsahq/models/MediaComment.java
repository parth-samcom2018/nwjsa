package com.hq.nwjsahq.models;

import com.hq.nwjsahq.DM;

import java.util.Date;

public class MediaComment {
    public int mediaId;
    public int mediaCommentId;
    public String comment;
    public int memberId;
    public String member;
    public String memberAvatar;
    public Date commentDate;

    public String getTimeAgo()
    {
        return DM.getTimeAgo(commentDate);
    }
}
