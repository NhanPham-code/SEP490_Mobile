package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.CreateTeamPostDTO;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberForDetailDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostResponse;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.TeamMemberDetailDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamPostDTO;
import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.notification.CreateNotificationDTO;
import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.example.sep490_mobile.data.repository.BookingRepository;
import com.example.sep490_mobile.data.repository.FindTeamRepository;
import com.example.sep490_mobile.data.repository.NotificationRepository;
import com.example.sep490_mobile.data.repository.StadiumRepository;
import com.example.sep490_mobile.data.repository.UserRepository;
import com.example.sep490_mobile.utils.DurationConverter;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindTeamViewModel extends AndroidViewModel {

    private String TAG = "BookingReadDTO";
    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;
    private final FindTeamRepository findTeamRepository;
    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<ReadTeamMemberForDetailDTO>> _listMember = new MutableLiveData<>();
    private final MutableLiveData<TeamMemberDetailDTO> _teamMemberDetail = new MutableLiveData<>();
    private final MutableLiveData<FindTeamDTO> _findTeam = new MutableLiveData<>();
    private final MutableLiveData<ReadTeamPostDTO> _teamPostData = new MutableLiveData<>();
    private final MutableLiveData<ReadTeamMemberForDetailDTO> _teamMemberData = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<StadiumDTO>> _stadiums = new MutableLiveData<>();
    private final MutableLiveData<ReadTeamPostResponse> _teamPost = new MutableLiveData<ReadTeamPostResponse>();
    private final MutableLiveData<ODataResponse<PublicProfileDTO>> _publicProfiles = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<ReadTeamPostDTO>> _teamPosts = new MutableLiveData<>();
    private final MutableLiveData<SelectBookingDTO> _selectBooking = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> _totalCount = new MutableLiveData<>();
    private final MutableLiveData<ScheduleBookingDTO> _scheduleBooking = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _created = new MutableLiveData<Boolean>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _success = new MutableLiveData<>(false);
    public final LiveData<FindTeamDTO> findTeam = _findTeam;
    public final LiveData<Boolean> success = _success;
    public final LiveData<ReadTeamPostDTO> teamPostData = _teamPostData;
    public final LiveData<List<ReadTeamMemberForDetailDTO>> listMember = _listMember;
    public final LiveData<TeamMemberDetailDTO> teamMemberDetail = _teamMemberDetail;
    public final LiveData<ReadTeamMemberForDetailDTO> teamMemberData = _teamMemberData;
    public final LiveData<ODataResponse<StadiumDTO>> stadiums = _stadiums;
    public final LiveData<ReadTeamPostResponse> teamPost = _teamPost;
    public final LiveData<ODataResponse<PublicProfileDTO>> publicProfiles = _publicProfiles;
    public final LiveData<ODataResponse<ReadTeamPostDTO>> teamPosts = _teamPosts;
    public final LiveData<SelectBookingDTO> selectBooking = _selectBooking;
    public final LiveData<Boolean> created = _created;
    public final LiveData<Integer> totalCount = _totalCount;
    public final LiveData<Boolean> isLoading = _isLoading;
    public final LiveData<String> errorMessage = _errorMessage;
    public FindTeamViewModel(@NonNull Application application) {
        super(application);

        this.stadiumRepository = new StadiumRepository(application);
        this.userRepository = new UserRepository(application);
        this.findTeamRepository = new FindTeamRepository(application);
        this.bookingRepository = new BookingRepository(application);
        this.notificationRepository = new NotificationRepository(application);
    }

    public void fetchFindTeamList(Map<String, String> odataUrl) {
        _isLoading.setValue(true);

        findTeamRepository.getTeamPost(odataUrl).enqueue(new Callback<ODataResponse<ReadTeamPostDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<ReadTeamPostDTO>> call, Response<ODataResponse<ReadTeamPostDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _totalCount.setValue(Integer.parseInt(response.body().getCount() + ""));
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
                    if(response.code() == 401){
                        _errorMessage.setValue("Vui lòng đăng nhập để sửa dụng!");
                    }else{
                        _errorMessage.setValue("Lỗi tải Team Post: " + response.code());
                    }
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
                    Log.d(TAG, "fetchStadiums: " + response.body().getItems());
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
                _isLoading.setValue(false);
                Log.d(TAG, "onFailure: " + t.getMessage());
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
    private void fetchStadiumsForBooking(String stadiumIDs, SelectBookingDTO selectBookingDTO) {
        stadiumRepository.getStadiumByListId(stadiumIDs).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, StadiumDTO> stadiums = new Hashtable<>();
                    for (StadiumDTO stadium : response.body().getItems()) {
                        stadiums.put(stadium.id, stadium);
                    }
                    selectBookingDTO.setStadiums(stadiums);
                    _selectBooking.setValue(selectBookingDTO); // Set dữ liệu vào DTO

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


    // Trong ViewModel của bạn (hoặc class tương đương)

    // 1. Khai báo hàm fetchBooking ban đầu (Chỉ gọi API 1)
    public void fetchBooking(Context context){
        _isLoading.setValue(true);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);

        // Đảm bảo LiveData không null trước khi gán
        if (_selectBooking.getValue() == null) {
            _selectBooking.setValue(new SelectBookingDTO(null, null));
        }

        // 1. Lấy User ID
        int currentUserId = sharedPreferences.getInt("user_id", 0);
        String myUserId = String.valueOf(currentUserId);

        // 2. Chuẩn bị filter cho API thứ nhất (fetchFindTeamList)
        Map<String, String> odataUrl = new HashMap<>();

        odataUrl.put("$filter", "CreatedBy eq " + currentUserId);

        // 3. Gọi hàm mới để thực hiện API chuỗi
        fetchFindTeamListAndThenFetchBookingHistory(odataUrl, currentUserId, myUserId);
    }

    // 2. Hàm mới để thực hiện API chuỗi (API 1 và API 2)
    private void fetchFindTeamListAndThenFetchBookingHistory(Map<String, String> odataUrl, int currentUserId, String myUserId) {

        // Thực hiện API 1: Tải danh sách Team Posts
        findTeamRepository.getTeamPost(odataUrl).enqueue(new Callback<ODataResponse<ReadTeamPostDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<ReadTeamPostDTO>> call, Response<ODataResponse<ReadTeamPostDTO>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    // --- BƯỚC 1A: Dữ liệu API FindTeam đã về an toàn ---
                    List<ReadTeamPostDTO> postList = response.body().getItems();
                    List<Integer> id = new ArrayList<>();

                    if (postList != null) {
                        for (ReadTeamPostDTO post : postList) {
                            // Kiểm tra an toàn cho null và giá trị
                            if (post != null && post.getCreatedBy() == currentUserId && post.getBookingId() > 0) {
                                id.add(post.getBookingId());
                            }
                        }
                    }

                    // 1B. Chuẩn bị URL cho API thứ 2 (getBookingsHistoryFindTeam)
                    String url;
                    String userFilter = String.format("UserId eq %s", myUserId);

                    if (!id.isEmpty()) {
                        // SỬA LỖI ODATA: Thay thế "Id not in (x,y)" bằng "(Id ne x and Id ne y)"
                        String exclusionFilter = id.stream()
                                .map(singleId -> String.format("Id ne %s", singleId))
                                .collect(Collectors.joining(" and "));

                        // Bao filter loại trừ trong ngoặc đơn để đảm bảo ưu tiên toán tử
                        // Kết quả: (Id ne 1 and Id ne 5) and UserId eq 3
                        url = String.format("(%s) and %s", exclusionFilter, userFilter);
                    } else {
                        // Không có gì để loại trừ, chỉ lọc theo User ID
                        url = userFilter;
                    }
                    ZonedDateTime dateNowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

// Định dạng theo chuẩn ISO 8601 (đã bao gồm múi giờ)
                    String formatted = dateNowInVietnam.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    // --- BƯỚC 2: Gọi API thứ hai (Tải Booking History) ---
                    fetchBookingHistory(url + " and Status eq 'accepted' and BookingDetails/any(m: m/StartTime ge " + DurationConverter.createCurrentISOSDateTime() + ")");

                } else {
                    // Xử lý lỗi API FindTeam
                    _isLoading.setValue(false);
                    if(response.code() == 401){
                        _errorMessage.setValue("Vui lòng đăng nhập để sửa dụng!");
                    }else{
                        _errorMessage.setValue("Lỗi tải Team Post: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ODataResponse<ReadTeamPostDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Team Post: " + t.getMessage());
            }
        });
    }


    // 3. Hàm riêng để thực hiện API thứ hai (Tải Booking History)
    private void fetchBookingHistory(String url) {

        findTeamRepository.getBookingsHistoryFindTeam(url).enqueue(new Callback<BookingHistoryODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingHistoryODataResponse> call,@NonNull Response<BookingHistoryODataResponse> response) {
                // Xóa _isLoading.setValue(false) ở đây. Chúng ta sẽ set nó trong fetchStadiumsForBooking nếu có data.
                System.out.println("BookingHistoryODataResponse: " + response.code());

                if (response.code() == 200) {
                    BookingHistoryODataResponse bookings = response.body();

                    if (bookings != null && bookings.getValue() != null && !bookings.getValue().isEmpty()) {

                        SelectBookingDTO selectBookingDTO = new SelectBookingDTO(null, null);
                        selectBookingDTO.setBookingReadDTOS(bookings.getValue());
                        _selectBooking.setValue(selectBookingDTO);

                        // Tối ưu: Thêm .distinct() để không gọi API trùng lặp stadium ID
                        List<Integer> listStadiumIds = bookings.getValue().stream()
                                .map(BookingViewDTO::getStadiumId)
                                .distinct()
                                .collect(Collectors.toList());
                        String stadiumIDs = listStadiumIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                        System.out.println("BookingHistoryODataResponse: " + selectBookingDTO.getBookingReadDTOS().size());

                        // Gọi API phụ để lấy thông tin stadium
                        fetchStadiumsForBooking(stadiumIDs, selectBookingDTO);

                    } else {
                        // Không có booking nào, hoàn thành tác vụ
                        _isLoading.setValue(false);
                    }
                } else {
                    // API lỗi, hoàn thành tác vụ
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Lỗi tải Booking History. Mã: " + response.code());
                    Log.d(TAG, "Lỗi tải Booking History. Mã:: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BookingHistoryODataResponse> call, Throwable t) {
                // Mạng lỗi, hoàn thành tác vụ
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
            }
        });
    }

    public void createNewPost(CreateTeamPostDTO createTeamPostDTO, Context context){
        _isLoading.setValue(true);
        findTeamRepository.createNewPost(createTeamPostDTO).enqueue(new Callback<ReadTeamPostResponse>() {

            @Override
            public void onResponse(Call<ReadTeamPostResponse> call, Response<ReadTeamPostResponse> response) {
                _isLoading.setValue(false); // Đặt isLoading ở đầu để đảm bảo nó được tắt

                if (response.isSuccessful() && response.body() != null){

                    ReadTeamPostResponse createdPost = response.body();
                    _teamPost.setValue(createdPost); // Cập nhật LiveData

                    Log.d(TAG, "onResponse: " + createdPost);

                    // --- VỊ TRÍ ĐÃ SỬA: CHUYỂN LOGIC TẠO LEADER VÀO ĐÂY ---
                    if(createdPost.getId() > 0){
                        CreateTeamMemberDTO createTeamMemberDTO = new CreateTeamMemberDTO();
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);

                        createTeamMemberDTO.setUserId(sharedPreferences.getInt("user_id", 0));
                        // Lấy ID trực tiếp từ response, KHÔNG phải từ LiveData
                        createTeamMemberDTO.setTeamPostId(createdPost.getId());

                        createTeamMemberDTO.setRole("Leader");
                        createTeamMemberDTO.setJoinedAt(DurationConverter.createCurrentISOString());

                        createMember(createTeamMemberDTO);

                        System.out.println("createTeamMemberDTO: " + createdPost.getId());
                    } else {
                        _errorMessage.setValue("Tạo bài đăng thành công nhưng ID không hợp lệ.");
                    }

                } else {
                    _errorMessage.setValue("Lỗi mạng khi tạo Post: " + response.code());
                    Log.d(TAG, "onFailure: " + response.code());
                    _teamPost.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ReadTeamPostResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối khi tạo Post: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

        // XÓA TẤT CẢ LOGIC TẠO LEADER KHỎI ĐÂY!
        // Nếu không nó sẽ chạy trước khi API trả lời.
    }
    public void createMember(CreateTeamMemberDTO createTeamMemberDTO){
        _isLoading.setValue(true);
        findTeamRepository.addNewMember(createTeamMemberDTO).enqueue(new Callback<ReadTeamMemberDTO>() {
            @Override
            public void onResponse(Call<ReadTeamMemberDTO> call, Response<ReadTeamMemberDTO> response) {
                if(response.isSuccessful()){
                    _created.setValue(true);
                    _isLoading.setValue(false);
                }else{
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + response.code());
                    Log.d(TAG, "onFailure: " + response.code());
                    _created.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<ReadTeamMemberDTO> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
                _created.setValue(false);
            }
        });
    }

    public void updateTeamPost(UpdateTeamPostDTO updateTeamPostDTO){
        _isLoading.setValue(true);
        findTeamRepository.updateTeamPost(updateTeamPostDTO).enqueue(new Callback<ReadTeamPostDTO>() {

            @Override
            public void onResponse(Call<ReadTeamPostDTO> call, Response<ReadTeamPostDTO> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()){
                    _teamPostData.setValue(response.body());
                    Log.d(TAG, "onResponse: " + response.body());
                    _success.setValue(true);
                }

            }

            @Override
            public void onFailure(Call<ReadTeamPostDTO> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public UpdateTeamPostDTO getUpdateMember(ReadTeamPostDTO readTeamPostDTO){
        UpdateTeamPostDTO updateTeamPostDTO = new UpdateTeamPostDTO();
        updateTeamPostDTO.setId(readTeamPostDTO.getId());
        updateTeamPostDTO.setTitle(readTeamPostDTO.getTitle());
        updateTeamPostDTO.setDescription(readTeamPostDTO.getDescription());
        updateTeamPostDTO.setJoinedPlayers(readTeamPostDTO.getJoinedPlayers() + 1);
        updateTeamPostDTO.setUpdatedAt(DurationConverter.createCurrentISOString());
        updateTeamPostDTO.setPricePerPerson(readTeamPostDTO.getPricePerPerson());
        updateTeamPostDTO.setNeededPlayers(readTeamPostDTO.getNeededPlayers());

        return updateTeamPostDTO;
    }

    private UpdateTeamPostDTO getUpdateMemberDelete(ReadTeamPostDTO readTeamPostDTO, String type){
        UpdateTeamPostDTO updateTeamPostDTO = new UpdateTeamPostDTO();
        updateTeamPostDTO.setId(readTeamPostDTO.getId());
        updateTeamPostDTO.setTitle(readTeamPostDTO.getTitle());
        updateTeamPostDTO.setDescription(readTeamPostDTO.getDescription());
        if(type.equalsIgnoreCase("member")){
            updateTeamPostDTO.setJoinedPlayers(readTeamPostDTO.getJoinedPlayers() - 1);
        }else{
            updateTeamPostDTO.setJoinedPlayers(readTeamPostDTO.getJoinedPlayers());
        }
        updateTeamPostDTO.setUpdatedAt(DurationConverter.createCurrentISOString());
        updateTeamPostDTO.setPricePerPerson(readTeamPostDTO.getPricePerPerson());
        updateTeamPostDTO.setNeededPlayers(readTeamPostDTO.getNeededPlayers());

        return updateTeamPostDTO;
    }
    public void updateTeamMember(UpdateTeamMemberDTO updateTeamMemberDTO, ReadTeamPostDTO readTeamPostDTO){
        _isLoading.setValue(true);
        findTeamRepository.updateTeamMember(updateTeamMemberDTO).enqueue(new Callback<ReadTeamMemberForDetailDTO>() {

            @Override
            public void onResponse(Call<ReadTeamMemberForDetailDTO> call, Response<ReadTeamMemberForDetailDTO> response) {
                _isLoading.setValue(false);
                if (response.code() == 200){
                    _teamMemberData.setValue(response.body());
                    Log.d(TAG, "onResponse: " + response.body());


                }
            }

            @Override
            public void onFailure(Call<ReadTeamMemberForDetailDTO> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void getTeamMember(int postId){
        _isLoading.setValue(true);

        findTeamRepository.getTeamMember(postId).enqueue(new Callback<List<ReadTeamMemberForDetailDTO>>() {

            @Override
            public void onResponse(Call<List<ReadTeamMemberForDetailDTO>> call, Response<List<ReadTeamMemberForDetailDTO>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()){
                    TeamMemberDetailDTO teamMemberDetailDTO = new TeamMemberDetailDTO();
                    _listMember.setValue(response.body());
                    teamMemberDetailDTO.setMember(response.body());
                    teamMemberDetailDTO.setUser(null);
                    AtomicInteger pendingCalls = new AtomicInteger(1);

                    List<Integer> listUserIds = teamMemberDetailDTO.getMember().stream()
                            .map(ReadTeamMemberForDetailDTO::getUserId)
                            .collect(Collectors.toList());

                    String userIDs = listUserIds.stream().map(String::valueOf).collect(Collectors.joining(","));

                    fetchUserProfilesForTeamMember(userIDs, teamMemberDetailDTO, pendingCalls);
                    Log.d(TAG, "onResponse: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ReadTeamMemberForDetailDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void fetchUserProfilesForTeamMember(String userIDs, TeamMemberDetailDTO currentData, AtomicInteger pendingCalls) {
        userRepository.getPublicProfileByListId(userIDs).enqueue(new Callback<ODataResponse<PublicProfileDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<PublicProfileDTO>> call, Response<ODataResponse<PublicProfileDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, PublicProfileDTO> users = new Hashtable<>();
                    for (PublicProfileDTO uPublicProfileDTO : response.body().getItems()) {
                        users.put(uPublicProfileDTO.id, uPublicProfileDTO);
                    }
                    currentData.setUser(users); // Set dữ liệu vào DTO

                } else {
                    _errorMessage.setValue("Lỗi tải User Profiles. Mã: " + response.code());
                }
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong
                checkAndNotifyCompletionTeamMember(currentData, pendingCalls);
            }

            @Override
            public void onFailure(Call<ODataResponse<PublicProfileDTO>> call, Throwable t) {
                _errorMessage.setValue("Lỗi mạng khi tải User Profiles: " + t.getMessage());
                // SỬA LỖI 2: Đánh dấu cuộc gọi này đã xong (kể cả khi thất bại)
                checkAndNotifyCompletionTeamMember(currentData, pendingCalls);
            }
        });
    }

    public void deleteTeamPost(int postId){
        _isLoading.setValue(true);
        _success.setValue(false);
        findTeamRepository.deleteTeamPost(postId).enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful()){
                    _success.setValue(true);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void deleteMember(int teamMemberId, int teamPostId, ReadTeamPostDTO readTeamPostDTO, String type){
        _isLoading.setValue(true);
        _success.setValue(false);
        findTeamRepository.deleteTeamMember(teamMemberId, teamPostId).enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                _isLoading.setValue(false);
                if(response.code() == 200) {
                    updateTeamPost(getUpdateMemberDelete(readTeamPostDTO, type));
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void checkAndNotifyCompletionTeamMember(TeamMemberDetailDTO currentData, AtomicInteger pendingCalls) {
        // Giảm bộ đếm đi 1 và kiểm tra xem nó đã về 0 chưa
        if (pendingCalls.decrementAndGet() == 0) {
            // Cả 2 cuộc gọi (Stadiums và Users) đã xong
            _isLoading.setValue(false);
            _teamMemberDetail.setValue(currentData); // Bây giờ mới kích hoạt Observer!
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

    public void loadMore(Map<String, String> odataUrl, FindTeamDTO findTeamDTO){
        _isLoading.setValue(true);
        findTeamRepository.getTeamPost(odataUrl).enqueue(new Callback<ODataResponse<ReadTeamPostDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<ReadTeamPostDTO>> call, Response<ODataResponse<ReadTeamPostDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    FindTeamDTO currentData = new FindTeamDTO(findTeamDTO.getTeamPostDTOS(), findTeamDTO.getStadiums(), findTeamDTO.getUsers());

                    List<ReadTeamPostDTO> teamPosts = response.body().getItems();
                    currentData.getTeamPostDTOS().addAll(teamPosts); // Set dữ liệu chính

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
                    fetchStadiumsLoadMore(stadiumIDs, currentData, pendingCalls);
                    fetchUserProfilesLoadMore(userIDs, currentData, pendingCalls);

                    // *** XÓA DÒNG NÀY: _findTeam.setValue(currentData); ***
                    // Chúng ta sẽ chỉ setValue khi cả 2 cuộc gọi phụ xong

                } else {
                    _isLoading.setValue(false);
                    if(response.code() == 401){
                        _errorMessage.setValue("Vui lòng đăng nhập để sửa dụng!");
                    }else{
                        _errorMessage.setValue("Lỗi tải Team Post: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ODataResponse<ReadTeamPostDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Team Posts: " + t.getMessage());
            }
        });
    }
    private void fetchStadiumsLoadMore(String stadiumIDs, FindTeamDTO currentData, AtomicInteger pendingCalls) {
        stadiumRepository.getStadiumByListId(stadiumIDs).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "fetchStadiums: " + response.body().getItems());
                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, StadiumDTO> stadiums = new Hashtable<>();
                    for (StadiumDTO stadium : response.body().getItems()) {
                        currentData.getStadiums().put(stadium.id, stadium);
                    }

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
                _isLoading.setValue(false);
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    // Phương thức phụ trợ 2: Lấy User Profiles
    private void fetchUserProfilesLoadMore(String userIDs, FindTeamDTO currentData, AtomicInteger pendingCalls) {
        userRepository.getPublicProfileByListId(userIDs).enqueue(new Callback<ODataResponse<PublicProfileDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<PublicProfileDTO>> call, Response<ODataResponse<PublicProfileDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // SỬA LỖI 1: Khởi tạo Dictionary
                    Dictionary<Integer, PublicProfileDTO> users = new Hashtable<>();
                    for (PublicProfileDTO uPublicProfileDTO : response.body().getItems()) {
                        currentData.getUsers().put(uPublicProfileDTO.id, uPublicProfileDTO);
                    }

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

    public void notifyToMember(CreateNotificationDTO createNotificationDTO){

        String jsonString = "{\"title\":\"FindTeam\",\"content\":\"/FindTeam/FindTeam\"}";
// 2. Thêm các cặp key-value bạn muốn vào trong JSON
//    Key là một String, Value có thể là String, Integer, Boolean, Double...

        createNotificationDTO.setParameters(jsonString);
        notificationRepository.createNotification(createNotificationDTO).enqueue(new Callback<NotificationDTO>() {
            @Override
            public void onResponse(Call<NotificationDTO> call, Response<NotificationDTO> response) {
                if (response.isSuccessful()){
                    System.out.println("da thong bao:");
                }
            }

            @Override
            public void onFailure(Call<NotificationDTO> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải Booking History: " + t.getMessage());
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

}
