package com.hq.nwjsahq.models;

import com.hq.nwjsahq.DM;

import java.util.Date;

public class NotificationComment {
    public int notificationId;
    public int notificationCommentId;
    public int memberId;
    public String memberName;
    public Date commentAdded;
    public String comment;
    public String memberAvatar;

    public String getTimeAgo()
    {
        return DM.getTimeAgo(this.commentAdded);
    }
}
