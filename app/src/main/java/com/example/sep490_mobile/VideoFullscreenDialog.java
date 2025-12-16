package com.example.sep490_mobile;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.ImageButton;

public class VideoFullscreenDialog extends Dialog {
    private final String videoUrl;

    public VideoFullscreenDialog(Context context, String videoUrl) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.videoUrl = videoUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_fullscreen_dialog);

        VideoView videoView = findViewById(R.id.fullscreenVideoView);
        videoView.setVideoURI(Uri.parse(videoUrl));
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();

        // Nút thoát video
        ImageButton btnClose = findViewById(R.id.btnCloseVideo);
        btnClose.setOnClickListener(v -> dismiss());

        videoView.setOnCompletionListener(mp -> dismiss());
        setCanceledOnTouchOutside(true);
    }
}