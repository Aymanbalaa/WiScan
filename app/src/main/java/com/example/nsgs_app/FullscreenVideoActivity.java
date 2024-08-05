package com.example.nsgs_app;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class FullscreenVideoActivity extends AppCompatActivity {

    private VideoView fullscreenVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        // Enable the action bar and set the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fullscreenVideoView = findViewById(R.id.fullscreenVideoView);

        // Get the video URI from the intent
        Uri videoUri = getIntent().getParcelableExtra("videoUri");
        if (videoUri != null) {
            fullscreenVideoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(this);
            fullscreenVideoView.setMediaController(mediaController);
            mediaController.setAnchorView(fullscreenVideoView);

            fullscreenVideoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                fullscreenVideoView.start();
            });

            fullscreenVideoView.setOnErrorListener((mp, what, extra) -> {
                // Handle the error here (you can show a Toast message or log the error)
                return true; // Returning true means we handled the error
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
