package com.hq.nwjsahq.models;

import android.net.Uri;

import java.util.Date;
import java.util.List;

public class Media {
    public String url;
    public Uri getUrl;
    public String thumbnail;
    public String caption;
    public Date dateAdded;
    public List<MediaComment> comments;
    public int mediaId;
}
