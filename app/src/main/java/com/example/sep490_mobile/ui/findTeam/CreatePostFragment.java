package com.example.sep490_mobile.ui.findTeam;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FindTeamAdapter;
import com.example.sep490_mobile.adapter.SelectBookingAdapter;
import com.example.sep490_mobile.adapter.StadiumAdapter;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.CreateTeamPostDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.booking.BookingDetailViewModelDTO;
import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.databinding.FragmentCreatePostBinding;
import com.example.sep490_mobile.databinding.FragmentSelectBookingBinding;
import com.example.sep490_mobile.ui.schedule.ScheduleViewModel;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.HtmlConverter;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;


public class CreatePostFragment extends Fragment {

    private FindTeamViewModel model;
    private List<ScheduleBookingDTO> bookingList;
    private int count;
    private FragmentCreatePostBinding binding;
    private RecyclerView recyclerView;
    private SelectBookingAdapter adapter;
    private CreateTeamPostDTO createTeamPostDTO;
    private CreateTeamMemberDTO createTeamMemberDTO;
    private BookingReadDTO booking;
    private StadiumDTO stadium;

    public CreatePostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        model = new ViewModelProvider(this).get(FindTeamViewModel.class);
        setSelectBooking();

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                model.createNewPost(collectFormData(), getContext());
                model.created.observe(getViewLifecycleOwner(), aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(getContext(), "Tạo bài viết thành công", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    else {
                        Toast.makeText(getContext(), "Tạo bài viết thất bại", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        binding.backButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getParentFragmentManager() != null){
                    getParentFragmentManager().popBackStack("SelectBookingFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
        return root;
    }

    private void setSelectBooking(){
            // 1. Validate response and data
            ShareFilterFindTeamViewModel shareFilterFindTeamViewModel = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        booking = shareFilterFindTeamViewModel.getBooking().getValue();

        stadium = shareFilterFindTeamViewModel.getStadium().getValue();

            if (booking == null) {
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu booking." , Toast.LENGTH_SHORT).show();
                return;
            }


                if (stadium != null) {
                    // Assuming CourtsDTO[] can be correctly retrieved from StadiumDTO
                    CourtsDTO[] courtsDTOS = stadium.getCourts().toArray(new CourtsDTO[0]);

                    // Assuming BookingDetailViewModelDTO is not null and has at least one element
                    if (booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
                        BookingDetailViewModelDTO bookingDetail = booking.getBookingDetails().get(0);

                        // 3. Set data to UI
                        binding.newLocationEditText.setText(stadium.getAddress());

                        if (courtsDTOS.length > 0) {
                            binding.newSport.setText(courtsDTOS[0].getSportType());
                        }

                        // You still need to implement how to set the Date and Time from bookingDetail
                        // Example (Assuming bookingDetail has a getTimeSlot and Date property):
                        // binding.newPlayDateEditText.setText(bookingDetail.getDateString());
                        // binding.newTimePlayAutoCompleteTextView.setText(bookingDetail.getTimeSlot());

                        // Display info section and fill it out

                            System.out.println("datetime: " + bookingDetail.getStartTime().toString());
                            String playDate = DurationConverter.convertCustomToReadable(bookingDetail.getStartTime().toString(), "dd/MM/yyyy");
                            String playTime = DurationConverter.convertCustomToReadable(bookingDetail.getStartTime().toString(), "HH:mm");
                        binding.selectedBookingInfo.setVisibility(View.VISIBLE);
                        String bookingInfo = String.format("Sân: %s\nNgày: %s",
                                stadium.getName(), playDate); // Update with actual properties

                        binding.bookingInfoContent.setText(bookingInfo);


                        List<BookingDetailViewModelDTO> bookingDetailViewModelDTOS = booking.getBookingDetails();
                        List<String> timeOptions = new ArrayList<>();
                        timeOptions.add(playTime);
                        Date date = bookingDetailViewModelDTOS.get(0).getStartTime();
                        for (BookingDetailViewModelDTO detail : bookingDetailViewModelDTOS) {
                            if(detail.getStartTime() != date){
                                timeOptions.add(DurationConverter.convertCustomToReadable(detail.getStartTime().toString(), "HH:mm"));
                            }
                        }
                        // date
                        List<String> DateOptions = new ArrayList<>();
                        DateOptions.add(playDate);
                        Date datePlay = bookingDetailViewModelDTOS.get(0).getStartTime();
                        for (BookingDetailViewModelDTO detail : bookingDetailViewModelDTOS) {
                            if(detail.getStartTime() != datePlay){
                                DateOptions.add(DurationConverter.convertCustomToReadable(detail.getStartTime().toString(), "dd/MM/yyyy"));
                            }
                        }

                        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(
                                this.getContext(),
                                android.R.layout.simple_spinner_item,
                                timeOptions
                        );
                        ArrayAdapter<String> adapterDate = new ArrayAdapter<>(
                                this.getContext(),
                                android.R.layout.simple_spinner_item,
                                DateOptions
                        );

// 3. Đặt layout cho danh sách thả xuống (Dropdown)
                        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// 4. Gán (Binding) Adapter cho Spinner
                        binding.newTimePlaySpinner.setAdapter(adapterTime);
                        binding.newPlayDateEditText.setAdapter(adapterDate);

                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy chi tiết booking.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin sân vận động.", Toast.LENGTH_SHORT).show();
                }

    }


    private CreateTeamPostDTO collectFormData() {
        if (binding == null) {
            Toast.makeText(getContext(), "Lỗi hệ thống: Binding chưa được khởi tạo.", Toast.LENGTH_SHORT).show();
            return null;
        }

        // --- 1. Thu thập Chuỗi Thô từ View ---
        String titleStr = binding.newTitleEditText.getText().toString().trim();
        String locationStr = binding.newLocationEditText.getText().toString().trim();
        String sportStr = binding.newSport.getText().toString().trim();
        String priceStr = binding.newPricePerPersonEditText.getText().toString().trim();
        String descriptionStr = HtmlConverter.convertSpannedToEscapedHtml(binding.newDescriptionEditText);
        String neededPlayersStr = binding.newNeededPlayersEditText.getText().toString().trim();

        Object selectedDateItem = binding.newPlayDateEditText.getSelectedItem();
        String playDateStr = (selectedDateItem != null) ? selectedDateItem.toString() : ""; // Ví dụ: "19/02/2026"

        Object selectedTimeItem = binding.newTimePlaySpinner.getSelectedItem();
        String timeSpinnerStr = (selectedTimeItem != null) ? selectedTimeItem.toString() : ""; // Ví dụ: "16:00"

        // --- SỬA LỖI ĐỊNH DẠNG (FIX 400 ERROR) ---

        // ⭐ FIX 1: Chuyển đổi PlayDate (DD/MM/YYYY) sang ISO DateTime đầy đủ
        // Ví dụ: "2026-02-19T00:00:00+07:00"
        String isoPlayDateStr = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isoPlayDateStr = DurationConverter.convertDateToIsoDateTime(playDateStr, "dd/MM/yyyy", ZoneId.systemDefault());
        }

        // ⭐ FIX 2: Chuyển đổi TimePlay (HH:mm) sang định dạng "HH:mm:ss"
        String isoTimePlayStr = "";
        if (!timeSpinnerStr.isEmpty()) {
            if (timeSpinnerStr.matches("\\d{2}:\\d{2}")) { // Nếu định dạng là HH:mm
                isoTimePlayStr = timeSpinnerStr + ":00"; // Thêm giây
            } else {
                isoTimePlayStr = timeSpinnerStr; // Giữ nguyên nếu đã có định dạng khác
            }
        }

        // --- 2. KIỂM TRA LỖI NULL / RỖNG (Validation) ---

        // Kiểm tra Tiêu đề
        if (titleStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Tiêu đề bài đăng (*).", Toast.LENGTH_SHORT).show();
            binding.newTitleEditText.requestFocus();
            return null;
        }
        // ... (Các kiểm tra khác: Location, NeededPlayers)

        // Kiểm tra Ngày chơi
        if (playDateStr.isEmpty()) { // Vẫn dùng chuỗi gốc để kiểm tra
            Toast.makeText(getContext(), "Vui lòng chọn Ngày chơi (*).", Toast.LENGTH_SHORT).show();
            binding.newPlayDateEditText.performClick();
            return null;
        }

        // Kiểm tra Thời gian chơi
        if (isoTimePlayStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn Thời gian chơi (*).", Toast.LENGTH_SHORT).show();
            binding.newTimePlaySpinner.performClick();
            return null;
        }

        // --- 3. Ánh xạ dữ liệu sau khi Validation thành công ---
        CreateTeamPostDTO postDTO = new CreateTeamPostDTO();

        // Ánh xạ chuỗi
        postDTO.setTitle(titleStr);
        postDTO.setLocation(locationStr);
        postDTO.setSportType(sportStr);
        postDTO.setDescription(descriptionStr);
        postDTO.setNeededPlayers(Integer.parseInt((neededPlayersStr)));
        postDTO.setPricePerPerson(Double.parseDouble(priceStr));

        // ⭐ Gán giá trị đã sửa lỗi
        postDTO.setPlayDate(isoPlayDateStr);
        postDTO.setTimePlay(isoTimePlayStr);

        // ... (Ánh xạ PricePerPerson, NeededPlayers)

        // Ánh xạ CreatedBy (Bắt buộc)
        // Giả định bạn đã có logic lấy User ID
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("user_id", 0);
        postDTO.setCreatedBy(currentUserId);

        // ... (Ánh xạ StadiumId, BookingId, JoinedPlayers)

        if (stadium != null) {
            postDTO.setStadiumName(stadium.getName());
            postDTO.setStadiumId(stadium.getId());
        }
        if (booking != null) {
            postDTO.setBookingId(booking.getId());
        }
        postDTO.setJoinedPlayers(0); // Gán 1 thay vì 0 (nếu logic yêu cầu)

        // --- 4. IN DỮ LIỆU RA LOGCAT (ĐÃ SỬA LỖI) ---
        Log.d("CreatePostData", "--- DỮ LIỆU BÀI ĐĂNG GỬI ĐI (FIX 2) ---");
        Log.d("CreatePostData", "PlayDate (ISO DateTime): " + postDTO.getPlayDate());
        Log.d("CreatePostData", "TimePlay (HH:mm:ss): " + postDTO.getTimePlay());
        Log.d("CreatePostData", "CreatedBy: " + postDTO.getCreatedBy());
        Log.d("CreatePostData", "--------------------------------------");

        return postDTO;
    }
}