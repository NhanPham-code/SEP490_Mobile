package com.example.sep490_mobile;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import org.webrtc.*;

import com.google.firebase.database.*;

import java.util.*;

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

    private List<PeerConnection.IceServer> getIceServers() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:ss-turn2.xirsys.com").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:ss-turn2.xirsys.com:80?transport=udp")
                .setUsername("J4iMiMv9g1r-ZJjiFIVJr1OOztQWCW3p2zYqF-cFtHp0FDFx30CFkDnZegxwX-J5AAAAAGkMTGtEYWlzeQ==")
                .setPassword("2e09fc6a-bae1-11f0-a5a8-0242ac140004")
                .createIceServer());
        // Có thể thêm TURN khác nếu cần
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

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        );

        factory = PeerConnectionFactory.builder()
                .setOptions(new PeerConnectionFactory.Options())
                .createPeerConnectionFactory();

        // Bật loa ngoài để nghe rõ
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        // Khởi tạo video/audio source
        videoCapturer = createCameraCapturer(context);
        videoSource = factory.createVideoSource(false);
        SurfaceTextureHelper textureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoCapturer.initialize(textureHelper, context, videoSource.getCapturerObserver());
        try {
            videoCapturer.startCapture(1280, 720, 30);
        } catch (Exception e) {
            Log.e("RtcManager", "Camera start error: " + e.getMessage());
        }

        audioSource = factory.createAudioSource(new MediaConstraints());

        rtcRef = FirebaseDatabase.getInstance().getReference("webrtc").child(chatId);
    }

    /** Helper lấy camera trước **/
    private VideoCapturer createCameraCapturer(Context ctx) {
        Camera2Enumerator enumerator = new Camera2Enumerator(ctx);
        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }
        throw new RuntimeException("Không tìm thấy camera trước!");
    }

    /** Bắt đầu gọi (CALLER) **/
    public void startCall() {
        peerConnection = factory.createPeerConnection(getIceServers(), new CustomPeerConnectionObserver("peerConn") {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                super.onIceCandidate(candidate);
                Map<String, Object> data = new HashMap<>();
                // Web đang dùng "candidate.candidate" raw string → Android ghi key "candidate"
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
                } else if (track instanceof AudioTrack) {
                    AudioTrack audioTrack = (AudioTrack) track;
                    audioTrack.setEnabled(true);
                    Log.d("RtcManager", "✅ Attached remote audio track");
                }
            }
        });

        // Tạo track local
        VideoTrack localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);

        peerConnection.addTrack(localVideoTrack, Collections.singletonList("ARDAMS"));
        peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));
        localVideoTrack.addSink(localView);

        // Tạo offer
        MediaConstraints constraints = new MediaConstraints();
        peerConnection.createOffer(new CustomSdpObserver("createOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);

                Map<String, Object> offer = new HashMap<>();
                offer.put("sdp", sessionDescription.description);
                offer.put("type", sessionDescription.type.canonicalForm()); // "offer"
                offer.put("from", currentUserId);
                offer.put("callerName", (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserId);
                offer.put("isVideo", isVideoCall);
                offer.put("timestamp", System.currentTimeMillis());

                rtcRef.child("offer").setValue(offer);
                Log.d("RtcManager", "📤 Sent offer with callerName=" + offer.get("callerName"));
            }
        }, constraints);

        // Lắng nghe answer từ callee
        rtcRef.child("answer").addValueEventListener(new ValueEventListener() {
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
                    Log.d("RtcManager", "📥 Set remote answer, calleeName=" + snap.child("calleeName").getValue(String.class));
                }
            }

            @Override public void onCancelled(DatabaseError dbError) {
                Log.e("RtcManager", "Answer listener cancelled: " + dbError.getMessage());
            }
        });

        // Lắng nghe ICE của callee
        rtcRef.child("calleeCandidates").addChildEventListener(new ChildEventListener() {
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
        });
    }

    /** Nhận cuộc gọi (CALLEE) **/
    public void receiveCall(String offerSdp, String offerType) {
        peerConnection = factory.createPeerConnection(getIceServers(), new CustomPeerConnectionObserver("peerConn") {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                super.onIceCandidate(candidate);
                Map<String, Object> data = new HashMap<>();
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
                } else if (track instanceof AudioTrack) {
                    AudioTrack audioTrack = (AudioTrack) track;
                    audioTrack.setEnabled(true);
                    Log.d("RtcManager", "✅ Attached remote audio track");
                }
            }
        });

        // Tạo track local
        VideoTrack localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        AudioTrack localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);

        peerConnection.addTrack(localVideoTrack, Collections.singletonList("ARDAMS"));
        peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));
        localVideoTrack.addSink(localView);

        // Set remote offer
        peerConnection.setRemoteDescription(new CustomSdpObserver("setRemote"),
                new SessionDescription(SessionDescription.Type.fromCanonicalForm(offerType), offerSdp));

        // Tạo answer
        peerConnection.createAnswer(new CustomSdpObserver("createAnswer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);

                Map<String, Object> answer = new HashMap<>();
                answer.put("sdp", sessionDescription.description);
                answer.put("type", sessionDescription.type.canonicalForm()); // "answer"
                answer.put("from", currentUserId);
                answer.put("calleeName", (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserId);
                answer.put("timestamp", System.currentTimeMillis());

                rtcRef.child("answer").setValue(answer);
                Log.d("RtcManager", "📤 Sent answer with calleeName=" + answer.get("calleeName"));
            }
        }, new MediaConstraints());

        // Lắng nghe ICE của caller
        rtcRef.child("callerCandidates").addChildEventListener(new ChildEventListener() {
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
        });
    }

    public void cleanUp() {
        try {
            if (peerConnection != null) {
                peerConnection.close();
                peerConnection = null;
            }
            if (videoCapturer != null) videoCapturer.dispose();
            if (videoSource != null) videoSource.dispose();
            if (audioSource != null) audioSource.dispose();
            if (localView != null) localView.release();
            if (remoteView != null) remoteView.release();
            if (rtcRef != null) rtcRef.removeValue();
        } catch (Exception e) {
            Log.e("RtcManager", "Cleanup error: " + e.getMessage());
        }
    }
}