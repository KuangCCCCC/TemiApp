package com.example.temiapp;

import android.os.Bundle;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlaybackActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);

        videoView = findViewById(R.id.videoView); // 影片視圖

        // 設定影片路徑
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.people_1;
        videoView.setVideoPath(videoPath);

        // 加入控制器，提供播放控制
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // 播放影片
        videoView.start();
    }
}
