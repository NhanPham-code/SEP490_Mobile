package com.example.sep490_mobile.ui.stadiumDetail;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.StadiumImagesDTO;
import com.example.sep490_mobile.data.dto.StadiumVideosDTO;
import com.example.sep490_mobile.databinding.FragmentStadiumDetailBinding;
import com.example.sep490_mobile.utils.ImageUtils;
import com.example.sep490_mobile.utils.PriceFormatter;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StadiumDetailFragment extends Fragment {

    private FragmentStadiumDetailBinding binding;
    private StadiumViewModel stadiumViewModel;
    private boolean isVideoPlaying = false;
    private Button viewImg;
    private Button viewVideo;
    private ImageButton nextButton;
    private ImageButton backButton;
    private ImageButton nextButtonVideo;
    private ImageButton backButtonVideo;
    private ImageView stadiumImage;
    private ImageButton backToHomeButton;
    private PlayerView playerView;
    private ExoPlayer player;
    private int imgPosition;
    private int videoPosition;
    private StadiumDTO stadiumDTO;
    private int stadiumId;
    private boolean isMuted = false;
    private ImageButton volumeButton;
    private ImageButton customFullscreenButton;
    private boolean isFullScreen = false;
    private int initialHeightPx;
    private ViewGroup originalParent;
    private ViewGroup activityRootView; // Root view của Activity
    private ConstraintLayout videoOverlayContainer; // 💡 Biến mới

    public static StadiumDetailFragment newInstance(int stadiumId) {
        StadiumDetailFragment fragment = new StadiumDetailFragment();
        Bundle args = new Bundle();
        args.putInt("stadiumId", stadiumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stadiumId = getArguments().getInt("stadiumId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStadiumDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        stadiumViewModel = new ViewModelProvider(this).get(StadiumViewModel.class);
        volumeButton = root.findViewById(R.id.btn_toggle_volume);
        nextButtonVideo = root.findViewById(R.id.btn_next_video);
        backButtonVideo = root.findViewById(R.id.btn_prev_video);
        viewImg = root.findViewById(R.id.view_img);
        viewVideo = root.findViewById(R.id.view_video);
        backToHomeButton = root.findViewById(R.id.btn_back);
        stadiumImage = root.findViewById(R.id.iv_stadium_image);
        playerView = root.findViewById(R.id.vv_stadium_video);
        nextButton = root.findViewById(R.id.btn_next);
        backButton = root.findViewById(R.id.btn_prev);
        customFullscreenButton = root.findViewById(R.id.btn_custom_fullscreen);
// ... (Trong phương thức onViewCreated hoặc setup)
        videoOverlayContainer = root.findViewById(R.id.video_full_screen_container); // 💡 THAY THẾ vv_stadium_video
        if (videoOverlayContainer != null) {
            originalParent = (ViewGroup) videoOverlayContainer.getParent();
        }
        if (getActivity() != null) {
            activityRootView = (ViewGroup) getActivity().findViewById(android.R.id.content);
        }

// Lưu chiều cao ban đầu (tính toán từ video_overlay_container, không cần PlayerView)
        float density = getResources().getDisplayMetrics().density;
        initialHeightPx = (int) (200 * density); // Giữ lại giá trị ban đầu 200dp

        // 1. Thiết lập tham số OData
        Map<String, String> odataUrl = new HashMap<>();
        odataUrl.put("$expand", "Courts,StadiumImages,StadiumVideos");
        // SỬA: Lọc chính xác theo ID
        odataUrl.put("$filter", "Id eq " + stadiumId);

        // 2. Thiết lập Observer trước
        observeStadiumListResponse();

        // 3. Gọi API để tải dữ liệu
        stadiumViewModel.fetchStadium(odataUrl);

        nextButtonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray(new StadiumVideosDTO[0]);
                if(videoPosition < videosDTOS.length - 1){
                    videoPosition ++;
                    player.seekToNextMediaItem();
                }
                setNextButton();
            }
        });
        backButtonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoPosition > 0){
                    videoPosition --;
                    player.seekToPreviousMediaItem();
                }
                setBackButton();
            }
        });

        // 4. Thiết lập sự kiện cho nút next và back
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumImagesDTO[] stadiumImagesDTOS = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);


                    if(imgPosition < stadiumImagesDTOS.length - 1){
                        imgPosition ++;
                        Glide.with(getContext()).load(ImageUtils.getFullUrl(stadiumImagesDTOS.length > 0 ? "img/" + stadiumImagesDTOS[imgPosition].imageUrl : "")).centerCrop().into(binding.ivStadiumImage);
                    }


                setNextButton();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumImagesDTO[] stadiumImagesDTOS = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);

                    if ( imgPosition > 0 ) {
                        imgPosition --;
                        Glide.with(getContext()).load(ImageUtils.getFullUrl(stadiumImagesDTOS.length > 0 ? "img/" + stadiumImagesDTOS[imgPosition].imageUrl : "")).centerCrop().into(binding.ivStadiumImage);
                    }

                setBackButton();
            }
        });

        // 5. Thiết lập sự kiện cho nút back
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();

                } else {
                    // Xử lý trường hợp không có gì trong back stack (hiếm)
                    Toast.makeText(getContext(), "Không thể đóng Fragment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 6. Thiết lập sự kiện cho nút viewImg và viewVideo
        viewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumImagesDTO[] stadiumImagesDTO = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);
                binding.btnNextVideo.setVisibility(View.GONE);
                binding.btnPrevVideo.setVisibility(View.GONE);
                isMuted = true;
                volumeButton.setVisibility(View.GONE);
                if(stadiumImagesDTO.length > 1){
                    binding.btnNext.setVisibility(View.VISIBLE);
                    binding.btnPrev.setVisibility(View.VISIBLE);
                }
                switchToImage();
            }
        });

        viewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray(new StadiumVideosDTO[0]);
                if(videosDTOS.length > 1){
                    binding.btnNextVideo.setVisibility(View.VISIBLE);
                    binding.btnPrevVideo.setVisibility(View.VISIBLE);
                }else{
                    binding.btnNextVideo.setVisibility(View.GONE);
                    binding.btnPrevVideo.setVisibility(View.GONE);
                }
                volumeButton.setVisibility(View.VISIBLE);
                switchToVideo();
            }
        });
        playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                // 'visibility' sẽ là View.VISIBLE hoặc View.GONE

                if (visibility == View.VISIBLE) {
                    // Bộ điều khiển đang HIỂN THỊ
                    // Ví dụ: Hiển thị các nút tùy chỉnh của bạn

                    customFullscreenButton.setVisibility(View.VISIBLE);
                } else {
                    // Bộ điều khiển đang ẨN
                    // Ví dụ: Ẩn các nút tùy chỉnh của bạn
                    customFullscreenButton.setVisibility(View.GONE);
                }
            }
        });
        volumeButton.setOnClickListener(v -> toggleVolume());
        setupCustomFullscreenButton();

        return root;
    }

    private void setupCustomFullscreenButton() {
        // Bạn cần tham chiếu và lưu các View khác vào 'otherContent' nếu có.

        // Lưu chiều cao ban đầu (200dp)
        // Đảm bảo initialHeightPx được tính toán chính xác và lưu trữ:
        float density = getResources().getDisplayMetrics().density;
        initialHeightPx = (int) (200 * density);

        customFullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void toggleFullscreen() {
        isFullScreen = !isFullScreen; // Đảo ngược trạng thái

        if (isFullScreen) {
            enterFullScreenMode();
        } else {
            exitFullScreenMode();
        }

        // Cập nhật biểu tượng cho nút tùy chỉnh
        int iconResource = isFullScreen ? R.drawable.exo_icon_fullscreen_exit : R.drawable.exo_icon_fullscreen_enter;
        customFullscreenButton.setImageResource(iconResource);
    }

// ----------------------------------------------------
// PHƯƠNG THỨC HỖ TRỢ FULLSCREEN
// ----------------------------------------------------

    private void enterFullScreenMode() {
        if (getActivity() == null || videoOverlayContainer == null || activityRootView == null) return;

        // 1. Xóa CONTAINER khỏi View cha ban đầu
        originalParent.removeView(videoOverlayContainer);

        // 2. Cấu hình LayoutParams cho toàn màn hình
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );

        // 3. Ẩn thanh Status Bar (Tùy chọn)
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 4. Thêm CONTAINER vào Activity Root View (Lớp phủ)
        activityRootView.addView(videoOverlayContainer, params);

        // 5. Chuyển sang màn hình ngang
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void exitFullScreenMode() {
        if (getActivity() == null || videoOverlayContainer == null || originalParent == null || activityRootView == null) return;

        // 1. Xóa CONTAINER khỏi Activity Root View
        activityRootView.removeView(videoOverlayContainer);

        // 2. Hiển thị lại thanh Status Bar
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 3. Đặt lại CONTAINER về kích thước ban đầu (200dp)
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                initialHeightPx // Giá trị 200dp
        );

        // 4. Thêm CONTAINER trở lại View cha ban đầu
        originalParent.addView(videoOverlayContainer, params);

        // 5. Chuyển về màn hình dọc
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void toggleVolume() {
        if (player == null) return;

        if (isMuted) {
            // Đang tắt tiếng -> Bật tiếng (Set âm lượng về 1.0f)
            player.setVolume(1.0f);
            isMuted = false;
            // Cập nhật biểu tượng thành Bật tiếng (Volume Up)
            volumeButton.setImageResource(R.drawable.ic_volume_up);
        } else {
            // Đang bật tiếng -> Tắt tiếng (Set âm lượng về 0.0f)
            player.setVolume(0.0f);
            isMuted = true;
            // Cập nhật biểu tượng thành Tắt tiếng (Volume Off/Mute)
            volumeButton.setImageResource(R.drawable.ic_volume_off);
        }
    }
    private void switchToVideo() {
        videoPosition = 0;
        isVideoPlaying = true;
        // Đặt lại trạng thái âm lượng
        isMuted = false;
        setNextButton();
        setBackButton();
        // 1. Ẩn ImageView
        stadiumImage.setVisibility(View.GONE);
        videoOverlayContainer.setVisibility(View.VISIBLE);
        // 2. Hiển thị VideoView
        playerView.setVisibility(View.VISIBLE);
        setVideoPlaying();
        toggleVolume();
    }

    /**
     * Chuyển sang hiển thị ảnh
     */
    private void switchToImage() {
        // 1. Dừng video và ẩn VideoView
        isVideoPlaying = false;
        videoOverlayContainer.setVisibility(View.GONE);
        playerView.setVisibility(View.GONE);
        if (player != null) {
            player.stop();
        }

        // 2. Hiển thị ImageView
        stadiumImage.setVisibility(View.VISIBLE);
    }
    private void setNextButton(){

        if(isVideoPlaying == true){
            StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray(new StadiumVideosDTO[0]);
            if(videoPosition >= videosDTOS.length - 1){
                binding.btnNextVideo.setVisibility(View.GONE);
            }
            binding.btnPrevVideo.setVisibility(View.VISIBLE);
        }else{
            StadiumImagesDTO[] stadiumImagesDTOS = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);
            if(imgPosition >= stadiumImagesDTOS.length - 1){
                binding.btnNext.setVisibility(View.GONE);
            }
            binding.btnPrev.setVisibility(View.VISIBLE);
        }
    }
    private void setBackButton(){
        if(isVideoPlaying == true){
            if(videoPosition <= 0){
                binding.btnPrevVideo.setVisibility(View.GONE);
            }
            binding.btnNextVideo.setVisibility(View.VISIBLE);
        }else{
            if(imgPosition <= 0){
                binding.btnPrev.setVisibility(View.GONE);
            }
            binding.btnNext.setVisibility(View.VISIBLE);

        }
    }

    private void observeStadiumListResponse() {
        stadiumViewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            // Kiểm tra tính hợp lệ của phản hồi
            if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {

                stadiumDTO = response.getItems().get(0);
                loadStadiumData(stadiumDTO);

                Toast.makeText(this.getContext(), stadiumDTO.getName(), Toast.LENGTH_LONG).show();

            } else {
                // Hiển thị thông báo khi không có dữ liệu
                Toast.makeText(this.getContext(), "Không tìm thấy chi tiết sân vận động.", Toast.LENGTH_LONG).show();
            }
        });
    }

    //play video
    private void setVideoPlaying(){
        StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray(new StadiumVideosDTO[0]);
        String completeVideoUrl = "";

        if (videosDTOS != null && videosDTOS.length > 0) {
            // 1. Lấy tên file video
            completeVideoUrl = videosDTOS[videoPosition].videoUrl;

            // 2. Tạo đường dẫn tương đối (đảm bảo là thư mục chứa video trên server)



            // 4. Chuẩn bị nguồn media và bắt đầu phát
        }

// 4. Thiết lập đường dẫn video (chỉ khi có URL hợp lệ)
        if (!completeVideoUrl.isEmpty()) {
            // setVideoURI thường được khuyến nghị hơn setVideoPath khi dùng URL mạng
            // setVideoPath chấp nhận cả đường dẫn cục bộ và URL mạng, nhưng Uri rõ ràng hơn
            player = new ExoPlayer.Builder(requireContext()).build();

            // 3. Liên kết Player với PlayerView
            playerView.setPlayer(player);
            // Tự động chuyển sang chế độ video và bắt đầu phát
            prepareVideo(completeVideoUrl);
        }
    }

    private void loadStadiumData(StadiumDTO stadiumDTO) {
        CourtsDTO[] courtsList = stadiumDTO.getCourts().toArray( new CourtsDTO[0]);
        StadiumImagesDTO[] imagesList = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);
        StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray( new StadiumVideosDTO[0]);

        binding.tvStadiumDescription.setText(stadiumDTO.getDescription());
        // Cập nhật thông tin cơ bản
        binding.tvStadiumName.setText(stadiumDTO.getName());
        binding.tvStadiumLocation.setText(stadiumDTO.getAddress());

        // Cập nhật giờ mở cửa
        binding.tvOpeningHours.setText(formatTime(stadiumDTO.getOpenTime().toString()) + " - " + formatTime(stadiumDTO.getCloseTime().toString()));

        // Cập nhật giá
        // 1. Đảm bảo danh sách không rỗng
        if (courtsList != null) {
            // 2. Khởi tạo minPrice và maxPrice bằng giá của phần tử đầu tiên
            int minPrice = Integer.parseInt(courtsList[0].getPricePerHour() + "");
            int maxPrice = Integer.parseInt(courtsList[0].getPricePerHour() + "");

            // 3. Duyệt qua danh sách để so sánh và cập nhật
            for (int i = 1; i < courtsList.length - 1; i++) {
                int currentPrice = Integer.parseInt(courtsList[i].getPricePerHour() + "");;

                if (currentPrice < minPrice) {
                    minPrice = currentPrice;
                }

                if (currentPrice > maxPrice) {
                    maxPrice = currentPrice;
                }
            }

            // 4. Định dạng và hiển thị kết quả
            String priceRange = "";
            if(minPrice != maxPrice){
                 priceRange = PriceFormatter.formatPrice(minPrice) + " - " + PriceFormatter.formatPrice(maxPrice) + " VND/giờ";

            }else{
                priceRange = PriceFormatter.formatPrice(minPrice) + " VND/giờ";
            }
            binding.tvPriceRange.setText(priceRange);

        } else {
            // Trường hợp danh sách rỗng hoặc null
            binding.tvPriceRange.setText("Giá chưa cập nhật");
        }

        // Tải hình ảnh
        if (imagesList != null && imagesList != null) {

// Khai báo kiểu dữ liệu cho videoUri là String hoặc Uri
// Nếu là Uri, bạn sẽ cần Uri.parse() sau khi có full URL.



            Glide.with(this.getContext()).load(ImageUtils.getFullUrl(imagesList.length > 0 ? "img/" + imagesList[0].imageUrl : "")).centerCrop().into(binding.ivStadiumImage);
            imgPosition = 0;
            if (imagesList.length <= 1){
                binding.btnNext.setVisibility(View.GONE);
                binding.btnPrev.setVisibility(View.GONE);
            }else{
                setNextButton();
                setBackButton();
            }

        } else {
            binding.ivStadiumImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // TODO: Thêm logic cho nút Đặt sân ngay
    }
    private void prepareVideo(String url) {
        // Tạo MediaItem từ URL
        MediaItem mediaItem = MediaItem.fromUri(url);
        StadiumVideosDTO[] videosDTOS = stadiumDTO.getStadiumVideos().toArray(new StadiumVideosDTO[0]);

        List<MediaItem> videoMediaItems = Arrays.stream(videosDTOS)
                .map(dto -> {
                    // Construct the full URI from the URL string provided by the DTO
                    String videoUrlPath = "img/" + dto.getVideoUrl();
                    // 3. Lấy URL hoàn chỉnh (ví dụ: "https://server.com/video/file.mp4")
                    Uri videoUri = Uri.parse(ImageUtils.getFullUrl(videoUrlPath));
                    System.out.println("url: " + videoUrlPath);

                    // Build the MediaItem
                    return MediaItem.fromUri(videoUri);
                })
                .collect(Collectors.toList());

        // Gán MediaItem vào Player
        player.setMediaItems(videoMediaItems);
//        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setPauseAtEndOfMediaItems(true);

        // Chuẩn bị Player
        player.prepare();

        // Tự động phát khi sẵn sàng
        player.setPlayWhenReady(true);
    }
    // Hàm tiện ích để chuyển đổi định dạng giờ OData (PT6H) sang hiển thị (06:00)
    private String formatTime(String odataTime) {
        if (odataTime == null || odataTime.length() < 3 || !odataTime.startsWith("PT")) {
            return "N/A";
        }

        // Loại bỏ tiền tố "PT"
        String timePart = odataTime.substring(2);

        int hours = 0;
        int minutes = 0;

        try {
            // Tìm và trích xuất giờ (trước 'H')
            int hIndex = timePart.indexOf('H');
            if (hIndex != -1) {
                String hourString = timePart.substring(0, hIndex);
                hours = Integer.parseInt(hourString);

                // Cập nhật timePart để chỉ còn lại phần phút (sau 'H')
                timePart = timePart.substring(hIndex + 1);
            }

            // Tìm và trích xuất phút (trước 'M')
            int mIndex = timePart.indexOf('M');
            if (mIndex != -1) {
                String minuteString = timePart.substring(0, mIndex);
                minutes = Integer.parseInt(minuteString);
            }

            // Nếu có giờ hoặc phút được phân tích, định dạng lại
            if (hours > 0 || minutes > 0) {
                return String.format("%02d:%02d", hours, minutes);
            } else {
                // Nếu chuỗi là "PT" hoặc "PT0H0M", trả về "00:00"
                return "00:00";
            }

        } catch (NumberFormatException e) {
            // Nếu việc phân tích số gặp lỗi, trả về chuỗi gốc
            return odataTime;
        }
    }
    @Override
    public void onStop(){
        super.onStop();
        if (player != null) {
            activityRootView.removeView(videoOverlayContainer);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            player.clearMediaItems();
            player.release();
            player = null;
        }

    }
}