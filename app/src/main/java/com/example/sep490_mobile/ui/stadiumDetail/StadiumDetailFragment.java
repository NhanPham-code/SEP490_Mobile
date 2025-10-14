package com.example.sep490_mobile.ui.stadiumDetail;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StadiumDetailFragment extends Fragment {

    private FragmentStadiumDetailBinding binding;
    private StadiumViewModel stadiumViewModel;
    private ImageButton nextButton;
    private ImageButton backButton;
    private ImageView stadiumImage;
    private ImageButton backToHomeButton;
    private VideoView stadiumVideo;
    private Uri videoUri;
    private int imgPosition;
    private StadiumDTO stadiumDTO;
    private int stadiumId;

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

        backToHomeButton = root.findViewById(R.id.btn_back);
        stadiumImage = root.findViewById(R.id.iv_stadium_image);
        stadiumVideo = root.findViewById(R.id.vv_stadium_video);
        nextButton = root.findViewById(R.id.btn_next);
        backButton = root.findViewById(R.id.btn_prev);

        // 1. Thiết lập tham số OData
        Map<String, String> odataUrl = new HashMap<>();
        odataUrl.put("$expand", "Courts,StadiumImages,StadiumVideos");
        // SỬA: Lọc chính xác theo ID
        odataUrl.put("$filter", "Id eq " + stadiumId);

        // 2. Thiết lập Observer trước
        observeStadiumListResponse();

        // 3. Gọi API để tải dữ liệu
        stadiumViewModel.fetchStadium(odataUrl);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StadiumImagesDTO[] stadiumImagesDTOS = stadiumDTO.getStadiumImages().toArray(new StadiumImagesDTO[0]);
                if(imgPosition < stadiumImagesDTOS.length - 1){
                    imgPosition ++;
                    Glide.with(getContext()).load(ImageUtils.getFullUrl(stadiumImagesDTOS.length > 0 ? "img/" + stadiumImagesDTOS[imgPosition].imageUrl : "")).centerCrop().into(binding.ivStadiumImage);
                }
                Toast.makeText(getContext(), "img: " + imgPosition, Toast.LENGTH_SHORT).show();
                setNextButton(stadiumImagesDTOS);
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
                Toast.makeText(getContext(), "img: " + imgPosition, Toast.LENGTH_SHORT).show();
                setBackButton();
            }
        });
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


        return root;
    }

    private void switchToVideo() {
        // 1. Ẩn ImageView
        stadiumImage.setVisibility(View.GONE);

        // 2. Hiển thị VideoView
        stadiumVideo.setVisibility(View.VISIBLE);

        // 3. Bắt đầu phát video (Tùy chọn)
//        stadiumVideo.start();
    }

    /**
     * Chuyển sang hiển thị ảnh
     */
    private void switchToImage() {
        // 1. Dừng video và ẩn VideoView
        if (stadiumVideo.isPlaying()) {
            stadiumVideo.stopPlayback();
        }
        stadiumVideo.setVisibility(View.GONE);

        // 2. Hiển thị ImageView
        stadiumImage.setVisibility(View.VISIBLE);
    }
    private void setNextButton(StadiumImagesDTO[] stadiumImagesDTOS){
        if(imgPosition >= stadiumImagesDTOS.length - 1){
            binding.btnNext.setVisibility(View.GONE);
        }
            binding.btnPrev.setVisibility(View.VISIBLE);

    }
    private void setBackButton(){
        if(imgPosition <= 0){
            binding.btnPrev.setVisibility(View.GONE);
        }
            binding.btnNext.setVisibility(View.VISIBLE);

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

            String completeVideoUrl = "";

            if (videosDTOS != null && videosDTOS.length > 0) {
                // 1. Lấy tên file video
                String fileName = videosDTOS[0].videoUrl;

                // 2. Tạo đường dẫn tương đối (đảm bảo là thư mục chứa video trên server)
                String videoUrlPath = "img/" + fileName;
                // 3. Lấy URL hoàn chỉnh (ví dụ: "https://server.com/video/file.mp4")
                completeVideoUrl = ImageUtils.getFullUrl(videoUrlPath);
            }

// 4. Thiết lập đường dẫn video (chỉ khi có URL hợp lệ)
            if (!completeVideoUrl.isEmpty()) {
                switchToVideo();
                // setVideoURI thường được khuyến nghị hơn setVideoPath khi dùng URL mạng
                // setVideoPath chấp nhận cả đường dẫn cục bộ và URL mạng, nhưng Uri rõ ràng hơn
                String videoUrl = "https://x.com/i/status/1977967621158117672";
                videoUri = Uri.parse(videoUrl);

                System.out.println("url: " + completeVideoUrl);

                // 2. Gán Uri cho VideoView
                stadiumVideo.setVideoURI(videoUri);

                // 3. Tạo và thiết lập MediaController
                MediaController mediaController = new MediaController(this.getContext());
                mediaController.setAnchorView(stadiumVideo);
                stadiumVideo.setMediaController(mediaController);

                // 4. Bắt đầu phát video
                stadiumVideo.start();

                // Xử lý khi video gặp lỗi
                stadiumVideo.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(this.getContext(), "Lỗi phát video: Không thể kết nối hoặc định dạng không được hỗ trợ.", Toast.LENGTH_LONG).show();
                    return true;
                });
                // Tự động chuyển sang chế độ video và bắt đầu phát

            } else {
                // Trường hợp không có video: Đảm bảo chỉ hiển thị ảnh/placeholder
                switchToImage(); // Giả sử bạn có hàm này để ẩn VideoView và hiện ImageView
            }

            Glide.with(this.getContext()).load(ImageUtils.getFullUrl(imagesList.length > 0 ? "img/" + imagesList[0].imageUrl : "")).centerCrop().into(binding.ivStadiumImage);
            imgPosition = 0;
            if (imagesList.length <= 1){
                binding.btnNext.setVisibility(View.GONE);
                binding.btnPrev.setVisibility(View.GONE);
            }else{
                setNextButton(imagesList);
                setBackButton();
            }

        } else {
            binding.ivStadiumImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // TODO: Thêm logic cho nút Đặt sân ngay
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
}