package com.example.sep490_mobile;

import org.webrtc.*;
import android.util.Log;

public class CustomPeerConnectionObserver implements PeerConnection.Observer {
    private String logTag;
    public CustomPeerConnectionObserver(String logTag) { this.logTag = logTag; }

    @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) { Log.d(logTag, "onSignalingChange: " + signalingState); }
    @Override public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) { Log.d(logTag, "onIceConnectionChange: " + iceConnectionState); }
    @Override public void onIceConnectionReceivingChange(boolean receiving) { Log.d(logTag, "onIceConnectionReceivingChange: " + receiving); }
    @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) { Log.d(logTag, "onIceGatheringChange: " + iceGatheringState); }
    @Override public void onIceCandidate(IceCandidate iceCandidate) { Log.d(logTag, "onIceCandidate: " + iceCandidate); }
    @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) { Log.d(logTag, "onIceCandidatesRemoved"); }
    @Override public void onAddStream(MediaStream mediaStream) { Log.d(logTag, "onAddStream: id=" + mediaStream.getId()); }
    @Override public void onRemoveStream(MediaStream mediaStream) { Log.d(logTag, "onRemoveStream: id=" + mediaStream.getId()); }
    @Override public void onDataChannel(DataChannel dataChannel) { Log.d(logTag, "onDataChannel: " + dataChannel.label()); }
    @Override public void onRenegotiationNeeded() { Log.d(logTag, "onRenegotiationNeeded"); }
    @Override public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) { Log.d(logTag, "onAddTrack: " + receiver.id()); }
    @Override public void onTrack(RtpTransceiver transceiver) { Log.d(logTag, "onTrack: " + transceiver.getMid()); }
}