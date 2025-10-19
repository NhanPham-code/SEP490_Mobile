package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.repository.FindTeamRepository;
import com.example.sep490_mobile.data.repository.ScheduleRepository;
import com.example.sep490_mobile.data.repository.StadiumRepository;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindTeamViewModel extends AndroidViewModel {

    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;
    private final FindTeamRepository findTeamRepository;
    private final ScheduleRepository scheduleRepository;
    private final MutableLiveData<FindTeamDTO> _findTeam = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<StadiumDTO>> _stadiums = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<PublicProfileDTO>> _publicProfiles = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<ReadTeamPostDTO>> _teamPosts = new MutableLiveData<>();
    private final MutableLiveData<SelectBookingDTO> _selectBooking = new MutableLiveData<>();
    private final MutableLiveData<Integer> _totalCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<FindTeamDTO> findTeam = _findTeam;
    public final LiveData<ODataResponse<StadiumDTO>> stadiums = _stadiums;
    public final LiveData<ODataResponse<PublicProfileDTO>> publicProfiles = _publicProfiles;
    public final LiveData<ODataResponse<ReadTeamPostDTO>> teamPosts = _teamPosts;
    public final LiveData<SelectBookingDTO> selectBooking = _selectBooking;
    public final LiveData<Integer> totalCount = _totalCount;
    public final LiveData<Boolean> isLoading = _isLoading;
    public final LiveData<String> errorMessage = _errorMessage;
    public FindTeamViewModel(@NonNull Application application) {
        super(application);

        this.stadiumRepository = new StadiumRepository(application);
        this.userRepository = new UserRepository(application);
        this.findTeamRepository = new FindTeamRepository(application);
        this.scheduleRepository = ScheduleRepository.getInstance(application);
    }

    public void fetchFindTeamList(Map<String, String> odataUrl) {
        _isLoading.setValue(true);

        findTeamRepository.getTeamPost(odataUrl).enqueue(new Callback<ODataResponse<ReadTeamPostDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<ReadTeamPostDTO>> call, Response<ODataResponse<ReadTeamPostDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    FindTeamDTO currentData = new FindTeamDTO(null, null, null);

                    List<ReadTeamPostDTO> teamPosts = response.body().getItems();
                    currentData.setTeamPostDTOS(teamPosts); // Set dữ liệu chính

                    // 1. Tạo bộ đếm cho 2 cuộc gọi API phụ
                    // Chúng ta cần chờ 2 cuộc gọi (Stadiums và Users) hoàn thành
                    AtomicInteger pendingCalls = new AtomicInteger(2);

                    // 2. Lấy ra danh sách IDs
                    List<Integer> listStadiumIds = teamPosts.stream()
                            .map(ReadTeamPostDTO::getStadiumId)
                            .collect(Collectors.toList());
                    List<Integer> listUserIds = teamPosts.stream()
                            .map(ReadTeamPostDTO::getCreatedBy)
                            .collect(Collectors.toList());

                    String stadiumIDs = listStadiumIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                    String userIDs = listUserIds.stream().map(String::valueOf).collect(Collectors.joining(","));

                    // 3. GỌI API PHỤ (truyền 'currentData' và 'pendingCalls' vào)
                    fetchStadiums(stadiumIDs, currentData, pendingCalls);
                    fetchUserProfiles(userIDs, currentData, pendingCalls);

                    // *** XÓA DÒNG NÀY: _findTeam.setValue(currentData); ***
                    // Chúng ta sẽ chỉ setValue khi cả 2 cuộc gọi phụ xong

                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Lỗi tải Team Posts. Mã: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ODataResponse<ReadTeamPostDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Team Posts: " + t.getMessage());
            }
        });
    }

    // Phương thức phụ trợ 1: Lấy Stadiums
    private void fetchStadiums(String stadiumIDs, FindTeamDTO currentData, AtomicInteger pendingCalls) {
        stadiumRepository.getStadiumByListId(stadiumIDs).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, StadiumDTO> stadiums = new Hashtable<>();
                    for (StadiumDTO stadium : response.body().getItems()) {
                        stadiums.put(stadium.id, stadium);
                    }
                    currentData.setStadiums(stadiums); // Set dữ liệu vào DTO

                } else {
                    _errorMessage.setValue("Lỗi tải Stadiums. Mã: " + response.code());
                }
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong
                checkAndNotifyCompletion(currentData, pendingCalls);
            }

            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) {
                _errorMessage.setValue("Lỗi mạng khi tải Stadiums: " + t.getMessage());
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong (kể cả khi thất bại)
                checkAndNotifyCompletion(currentData, pendingCalls);
            }
        });
    }

    // Phương thức phụ trợ 2: Lấy User Profiles
    private void fetchUserProfiles(String userIDs, FindTeamDTO currentData, AtomicInteger pendingCalls) {
        userRepository.getPublicProfileByListId(userIDs).enqueue(new Callback<ODataResponse<PublicProfileDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<PublicProfileDTO>> call, Response<ODataResponse<PublicProfileDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, PublicProfileDTO> users = new Hashtable<>();
                    for (PublicProfileDTO uPublicProfileDTO : response.body().getItems()) {
                        users.put(uPublicProfileDTO.id, uPublicProfileDTO);
                    }
                    currentData.setUsers(users); // Set dữ liệu vào DTO

                } else {
                    _errorMessage.setValue("Lỗi tải User Profiles. Mã: " + response.code());
                }
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong
                checkAndNotifyCompletion(currentData, pendingCalls);
            }

            @Override
            public void onFailure(Call<ODataResponse<PublicProfileDTO>> call, Throwable t) {
                _errorMessage.setValue("Lỗi mạng khi tải User Profiles: " + t.getMessage());
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong (kể cả khi thất bại)
                checkAndNotifyCompletion(currentData, pendingCalls);
            }
        });
    }
    private void fetchStadiumsForBooking(String stadiumIDs) {
        stadiumRepository.getStadiumByListId(stadiumIDs).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, StadiumDTO> stadiums = new Hashtable<>();
                    for (StadiumDTO stadium : response.body().getItems()) {
                        stadiums.put(stadium.id, stadium);
                    }
                    selectBooking.getValue().setStadiums(stadiums); // Set dữ liệu vào DTO

                } else {
                    _errorMessage.setValue("Lỗi tải Stadiums. Mã: " + response.code());
                }
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) {
                _errorMessage.setValue("Lỗi mạng khi tải Stadiums: " + t.getMessage());
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong (kể cả khi thất bại)
                _isLoading.setValue(false);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchBooking(Context context){
        _isLoading.setValue(true);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
        ZonedDateTime dateNow = ZonedDateTime.now(ZoneOffset.UTC);

// 2. Format it to ISO 8601
// ISO_OFFSET_DATE_TIME is correct and essential for OData DateTimeOffset.
        String formatted = dateNow.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

// Replace 'myUserId' with the actual variable holding the user's ID
// Note: Assuming sharedPreferences is available here
        String myUserId = sharedPreferences.getInt("user_id", 0) + "";

// 3. Construct the OData URL string - FINAL FIX APPLIED HERE
// Use the ISO 8601 formatted string WITHOUT quotes or the 'datetimeoffset' prefix.
        String url = String.format(
                "UserId eq %s and BookingDetails/any(m: m/StartTime ge %s)",
                myUserId,
                formatted
        );
        List<ScheduleBookingDTO> booking = scheduleRepository.getBookings(url).getValue().stream().collect(Collectors.toList());
        selectBooking.getValue().setScheduleBookingDTOS(booking);
        List<Integer> listStadiumIds = booking.stream()
                .map(ScheduleBookingDTO::getStadiumId)
                .collect(Collectors.toList());
        String stadiumIDs = listStadiumIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        if(booking.size() > 0){
            fetchStadiumsForBooking(stadiumIDs);
        }else{
            _isLoading.setValue(false);
        }

    }

    /**
     * Phương thức mới để kiểm tra xem tất cả các cuộc gọi phụ đã hoàn tất chưa.
     * Nếu đã hoàn tất (bộ đếm = 0), cập nhật LiveData để thông báo cho UI.
     */
    private void checkAndNotifyCompletion(FindTeamDTO currentData, AtomicInteger pendingCalls) {
        // Giảm bộ đếm đi 1 và kiểm tra xem nó đã về 0 chưa
        if (pendingCalls.decrementAndGet() == 0) {
            // Cả 2 cuộc gọi (Stadiums và Users) đã xong
            _isLoading.setValue(false);
            _findTeam.setValue(currentData); // Bây giờ mới kích hoạt Observer!
        }
    }
}
