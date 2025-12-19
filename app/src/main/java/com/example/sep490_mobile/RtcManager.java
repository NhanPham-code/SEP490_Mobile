package com.example.sep490_mobile;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * FULL file: RtcManager.java
 * - Không dùng MediaStreamTrack.MediaType.VIDEO hay addTransceiver (tránh lỗi symbol).
 * - Nhận video từ web bằng OfferToReceiveVideo = true trong Offer/Answer constraints.
 * - Fallback: nếu không mở được camera, chỉ không gửi video (nhưng vẫn nhận remote video).
 * - Gỡ TẤT CẢ Firebase listeners trong cleanUp() để tránh crash khi gọi lần 2.
 */
public class RtcManager {
    private final String currentUserId;
    private final String currentUserName;
    private final boolean isVideoCall;

    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private SurfaceViewRenderer localView, remoteView;
    private EglBase eglBase;
    private DatabaseReference rtcRef;

    // Firebase listeners để gỡ khi cleanup
    private DatabaseReference answerRef;
    private ValueEventListener answerListener;
    private DatabaseReference callerCandidatesRef;
    private ChildEventListener callerCandidatesListener;
    private DatabaseReference calleeCandidatesRef;
    private ChildEventListener calleeCandidatesListener;

    // Mở được camera không
    private boolean canSendVideo = false;

    private List<PeerConnection.IceServer> getIceServers() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:ss-turn2.xirsys.com").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:ss-turn2.xirsys.com:80?transport=udp")
                .setUsername("J4iMiMv9g1r-ZJjiFIVJr1OOztQWCW3p2zYqF-cFtHp0FDFx30CFkDnZegxwX-J5AAAAAGkMTGtEYWlzeQ==")
                .setPassword("2e09fc6a-bae1-11f0-a5a8-0242ac140004")
                .createIceServer());
        return iceServers;
    }

    public RtcManager(Context context,
                      SurfaceViewRenderer localView,
                      SurfaceViewRenderer remoteView,
                      String chatId,
                      String currentUserId,
                      String currentUserName,
                      boolean isVideoCall) {
        this.localView = localView;
        this.remoteView = remoteView;
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
        this.isVideoCall = isVideoCall;

        eglBase = EglBase.create();
        localView.init(eglBase.getEglBaseContext(), null);
        remoteView.init(eglBase.getEglBaseContext(), null);
        localView.setEnableHardwareScaler(true);
        remoteView.setEnableHardwareScaler(true);
        localView.setZOrderMediaOverlay(true);
        localView.setMirror(true);

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        );

        factory = PeerConnectionFactory.builder()
                .setOptions(new PeerConnectionFactory.Options())
                .createPeerConnectionFactory();

        // Route audio
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        // Thử mở camera nếu là video call
        if (isVideoCall) {
            try {
                videoCapturer = createCameraCapturer(context);
                videoSource = factory.createVideoSource(false);
                SurfaceTextureHelper textureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                videoCapturer.initialize(textureHelper, context, videoSource.getCapturerObserver());
                videoCapturer.startCapture(1280, 720, 30);
                canSendVideo = true;
                Log.d("RtcManager", "✅ Camera initialized");
            } catch (Exception e) {
                canSendVideo = false;
                Log.e("RtcManager", "Camera init error, will not send local video: " + e.getMessage());
            }
        }

        audioSource = factory.createAudioSource(new MediaConstraints());
        rtcRef = FirebaseDatabase.getInstance().getReference("webrtc").child(chatId);
    }

    private VideoCapturer createCameraCapturer(Context ctx) {
        Camera2Enumerator enumerator = new Camera2Enumerator(ctx);
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }
        throw new RuntimeException("Không tìm thấy camera trước!");
    }

    public void startCall() {
        peerConnection = factory.createPeerConnection(getIceServers(), new CustomPeerConnectionObserver("peerConn") {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                super.onIceCandidate(candidate);
                HashMap<String, Object> data = new HashMap<>();
                data.put("candidate", candidate.sdp);
                data.put("sdpMid", candidate.sdpMid);
                data.put("sdpMLineIndex", candidate.sdpMLineIndex);
                rtcRef.child("callerCandidates").push().setValue(data);
            }

            @Override
            public void onTrack(RtpTransceiver transceiver) {
                MediaStreamTrack track = transceiver.getReceiver().track();
                if (track instanceof VideoTrack) {
                    ((VideoTrack) track).addSink(remoteView);
                    Log.d("RtcManager", "✅ Attached remote video track");
                } else if (track instanceof org.webrtc.AudioTrack) {
                    ((org.webrtc.AudioTrack) track).setEnabled(true);
                    Log.d("RtcManager", "✅ Attached remote audio track");
                }
            }

            @Override
            public void onAddTrack(RtpReceiver receiver, org.webrtc.MediaStream[] mediaStreams) {
                Log.d("RtcManager", "onAddTrack: " + receiver.id());
            }
        });

        // Thêm local tracks
        if (isVideoCall && canSendVideo && videoSource != null) {
            VideoTrack localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
            peerConnection.addTrack(localVideoTrack, Collections.singletonList("ARDAMS"));
            localVideoTrack.addSink(localView);
        }
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
        peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));

        // Offer constraints: bật nhận video nếu là video call
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", isVideoCall ? "true" : "false"));

        peerConnection.createOffer(new CustomSdpObserver("createOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);

                HashMap<String, Object> offer = new HashMap<>();
                offer.put("sdp", sessionDescription.description);
                offer.put("type", sessionDescription.type.canonicalForm());
                offer.put("from", currentUserId);
                offer.put("callerName", (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserId);
                offer.put("isVideo", isVideoCall);
                offer.put("timestamp", System.currentTimeMillis());

                rtcRef.child("offer").setValue(offer);
                Log.d("RtcManager", "📤 Sent offer (isVideo=" + isVideoCall + ", canSendVideo=" + canSendVideo + ")");
            }
        }, constraints);

        // Lắng nghe answer
        answerRef = rtcRef.child("answer");
        answerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                if (!snap.exists()) return;
                String sdp = snap.child("sdp").getValue(String.class);
                String type = snap.child("type").getValue(String.class);
                if (sdp != null && type != null) {
                    SessionDescription remoteDesc = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(type), sdp
                    );
                    peerConnection.setRemoteDescription(new CustomSdpObserver("setRemote"), remoteDesc);
                }
            }

            @Override public void onCancelled(DatabaseError dbError) {
                Log.e("RtcManager", "Answer listener cancelled: " + dbError.getMessage());
            }
        };
        answerRef.addValueEventListener(answerListener);

        // ICE của callee
        calleeCandidatesRef = rtcRef.child("calleeCandidates");
        calleeCandidatesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dsp, String prev) {
                String candidate = dsp.child("candidate").getValue(String.class);
                String sdpMid = dsp.child("sdpMid").getValue(String.class);
                Integer idxObj = dsp.child("sdpMLineIndex").getValue(Integer.class);
                int sdpMLineIndex = idxObj != null ? idxObj : 0;

                if (candidate != null && sdpMid != null) {
                    peerConnection.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, candidate));
                    Log.d("RtcManager", "📥 Added callee ICE candidate");
                }
            }
            public void onChildChanged(DataSnapshot dsp, String s) {}
            public void onChildRemoved(DataSnapshot dsp) {}
            public void onChildMoved(DataSnapshot dsp, String s) {}
            public void onCancelled(DatabaseError dbError) {
                Log.e("RtcManager", "calleeCandidates cancelled: " + dbError.getMessage());
            }
        };
        calleeCandidatesRef.addChildEventListener(calleeCandidatesListener);
    }

    public void receiveCall(String offerSdp, String offerType) {
        peerConnection = factory.createPeerConnection(getIceServers(), new CustomPeerConnectionObserver("peerConn") {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                super.onIceCandidate(candidate);
                HashMap<String, Object> data = new HashMap<>();
                data.put("candidate", candidate.sdp);
                data.put("sdpMid", candidate.sdpMid);
                data.put("sdpMLineIndex", candidate.sdpMLineIndex);
                rtcRef.child("calleeCandidates").push().setValue(data);
            }

            @Override
            public void onTrack(RtpTransceiver transceiver) {
                MediaStreamTrack track = transceiver.getReceiver().track();
                if (track instanceof VideoTrack) {
                    ((VideoTrack) track).addSink(remoteView);
                    Log.d("RtcManager", "✅ Attached remote video track");
                } else if (track instanceof org.webrtc.AudioTrack) {
                    ((org.webrtc.AudioTrack) track).setEnabled(true);
                    Log.d("RtcManager", "✅ Attached remote audio track");
                }
            }

            @Override
            public void onAddTrack(RtpReceiver receiver, org.webrtc.MediaStream[] mediaStreams) {
                Log.d("RtcManager", "onAddTrack: " + receiver.id());
            }
        });

        // Thêm local tracks
        if (isVideoCall && canSendVideo && videoSource != null) {
            VideoTrack localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
            peerConnection.addTrack(localVideoTrack, Collections.singletonList("ARDAMS"));
            localVideoTrack.addSink(localView);
        }
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
        peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));

        // Set remote offer
        peerConnection.setRemoteDescription(new CustomSdpObserver("setRemote"),
                new SessionDescription(SessionDescription.Type.fromCanonicalForm(offerType), offerSdp));

        // Answer constraints: bật nhận video nếu là video call
        MediaConstraints answerConstraints = new MediaConstraints();
        answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", isVideoCall ? "true" : "false"));

        peerConnection.createAnswer(new CustomSdpObserver("createAnswer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);

                HashMap<String, Object> answer = new HashMap<>();
                answer.put("sdp", sessionDescription.description);
                answer.put("type", sessionDescription.type.canonicalForm());
                answer.put("from", currentUserId);
                answer.put("calleeName", (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserId);
                answer.put("timestamp", System.currentTimeMillis());

                rtcRef.child("answer").setValue(answer);
                Log.d("RtcManager", "📤 Sent answer (isVideo=" + isVideoCall + ", canSendVideo=" + canSendVideo + ")");
            }
        }, answerConstraints);

        // ICE của caller
        callerCandidatesRef = rtcRef.child("callerCandidates");
        callerCandidatesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dsp, String prev) {
                String candidate = dsp.child("candidate").getValue(String.class);
                String sdpMid = dsp.child("sdpMid").getValue(String.class);
                Integer idxObj = dsp.child("sdpMLineIndex").getValue(Integer.class);
                int sdpMLineIndex = idxObj != null ? idxObj : 0;

                if (candidate != null && sdpMid != null) {
                    peerConnection.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, candidate));
                    Log.d("RtcManager", "📥 Added caller ICE candidate");
                }
            }
            public void onChildChanged(DataSnapshot dsp, String s) {}
            public void onChildRemoved(DataSnapshot dsp) {}
            public void onChildMoved(DataSnapshot dsp, String s) {}
            public void onCancelled(DatabaseError dbError) {
                Log.e("RtcManager", "callerCandidates cancelled: " + dbError.getMessage());
            }
        };
        callerCandidatesRef.addChildEventListener(callerCandidatesListener);
    }

    public void cleanUp() {
        try {
            // Gỡ mọi Firebase listeners
            if (answerRef != null && answerListener != null) {
                answerRef.removeEventListener(answerListener);
                answerRef = null; answerListener = null;
            }
            if (callerCandidatesRef != null && callerCandidatesListener != null) {
                callerCandidatesRef.removeEventListener(callerCandidatesListener);
                callerCandidatesRef = null; callerCandidatesListener = null;
            }
            if (calleeCandidatesRef != null && calleeCandidatesListener != null) {
                calleeCandidatesRef.removeEventListener(calleeCandidatesListener);
                calleeCandidatesRef = null; calleeCandidatesListener = null;
            }

            if (peerConnection != null) {
                peerConnection.close();
                peerConnection = null;
            }
            if (videoCapturer != null) {
                try { videoCapturer.stopCapture(); } catch (Exception ignored) {}
                videoCapturer.dispose();
                videoCapturer = null;
            }
            if (videoSource != null) { videoSource.dispose(); videoSource = null; }
            if (audioSource != null) { audioSource.dispose(); audioSource = null; }
            if (localView != null) localView.release();
            if (remoteView != null) remoteView.release();
        } catch (Exception e) {
            Log.e("RtcManager", "Cleanup error: " + e.getMessage());
        }
    }
}