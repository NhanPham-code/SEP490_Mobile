package com.example.sep490_mobile;

import org.webrtc.*;

public class CustomSdpObserver implements SdpObserver {
    private String logTag;
    public CustomSdpObserver(String logTag) { this.logTag = logTag; }
    @Override public void onCreateSuccess(SessionDescription sessionDescription) { android.util.Log.d(logTag, "onCreateSuccess: " + sessionDescription.type); }
    @Override public void onSetSuccess() { android.util.Log.d(logTag, "onSetSuccess"); }
    @Override public void onCreateFailure(String error) { android.util.Log.e(logTag, "onCreateFailure: " + error); }
    @Override public void onSetFailure(String error) { android.util.Log.e(logTag, "onSetFailure: " + error); }
}