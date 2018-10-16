package com.hq.nwjsahq;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hq.nwjsahq.models.Media;
import com.hq.nwjsahq.models.MediaAlbum;

public class FullScreen extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    String url;


    private Media selectedMedia;
    public static MediaAlbum mediaAlbum;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = findViewById(R.id.videoView);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            url = extras.getString("url");
            Log.d("data" , ":" + url);
        }

        String fullScreen =  getIntent().getStringExtra("fullScreenInd");
        if("y".equals(fullScreen)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }

        Uri videoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/baseball-qld.appspot.com/o/Videos%2F120%2FOptional(%22120%22)1539175361.mp4?alt=media&token=95df206d-793d-46b9-adf6-f203ec254fef");

        videoView.setVideoURI(videoUri);

        mediaController = new FullScreenMediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.start();
    }
}

