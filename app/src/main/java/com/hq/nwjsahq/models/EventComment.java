package com.hq.nwjsahq.models;

import com.hq.nwjsahq.DM;

import java.util.Date;

public class EventComment {
    public int eventCommentId;
    public int  eventId;
    public String comment;
    public Date commentDate;
    public int memberId;
    public String memberName;
    public String memberAvatar;

    public String getTimeAgo()
    {
        return DM.getTimeAgo(commentDate);
    }
}
