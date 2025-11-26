package com.example.sep490_mobile.ui.findTeam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ShareFilterFindTeamViewModel extends ViewModel {
    private final MutableLiveData<Map<String, String>> selected = new MutableLiveData<Map<String, String>>();
    private MutableLiveData<String> address = new MutableLiveData<String>();
    private MutableLiveData<BookingViewDTO> booking= new MutableLiveData<BookingViewDTO>();
    private MutableLiveData<StadiumDTO> stadium = new MutableLiveData<StadiumDTO>();
    private MutableLiveData<String> playDate = new MutableLiveData<String>();
    private MutableLiveData<String> playTime = new MutableLiveData<String>();
    private MutableLiveData<Integer> needPlayer = new MutableLiveData<Integer>(1);
    private MutableLiveData<Integer> minPlayer = new MutableLiveData<Integer>(1);
    private MutableLiveData<Integer> maxPlayer = new MutableLiveData<Integer>(10);
    private MutableLiveData<List<String>> sportType = new MutableLiveData<List<String>>();

    // Khai báo giá trị mặc định để dễ dàng reset
    private static final String DEFAULT_PLAY_TIME = "15:00";
    private static final Integer DEFAULT_NEED_PLAYER = 1;
    private static final Integer DEFAULT_MAX_PLAYER = 10;

// ... (Các khai báo MutableLiveData của bạn)
private String getCurrentDateString() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return today.format(formatter);
    } else {
        // Dùng Calendar cho các phiên bản Android cũ hơn
        // Calendar calendar = Calendar.getInstance();
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // return sdf.format(calendar.getTime());
        return null; // Tùy chọn: trả về null nếu không muốn hỗ trợ dưới API 26
    }
}
    public void clearAllData() {
        // 1. Reset các biến phức tạp (Map, List, String không có giá trị mặc định) về null
        //    Đặt về null có nghĩa là "chưa có gì được chọn"
        selected.setValue(null);
        address.setValue(null);
        playDate.setValue(null);
        sportType.setValue(null);

        // 2. Reset các biến có giá trị mặc định về giá trị ban đầu đó
        playTime.setValue(DEFAULT_PLAY_TIME);
        needPlayer.setValue(DEFAULT_NEED_PLAYER);
        maxPlayer.setValue(DEFAULT_MAX_PLAYER);
    }

    public LiveData<StadiumDTO> getStadium() {
        return stadium;
    }

    public void setStadium(StadiumDTO stadium) {
        this.stadium.setValue(stadium);
    }

    public LiveData<BookingViewDTO> getBooking() {
        return booking;
    }

    public void setBooking(BookingViewDTO booking) {
        this.booking.setValue(booking);
    }

    public LiveData<Map<String, String>> getSelected() {
        return selected;
    }

    public void setSelected(Map<String, String> odata) {
        this.selected.setValue(odata);
    }

    public LiveData<String> getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address.setValue(address);
    }

    public LiveData<String> getPlayDate() {
        return playDate;
    }

    public void setPlayDate(String playDate) {
        this.playDate.setValue(playDate);
    }

    public LiveData<String> getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime.setValue(playTime);
    }

    public LiveData<Integer> getMinPlayerr() {
        return minPlayer;
    }

    public void setMinPlayer(int minPlayer) {
        this.minPlayer.setValue(minPlayer);
    }
    public LiveData<Integer> getNeedPlayer() {
        return needPlayer;
    }

    public void setNeedPlayer(int needPlayer) {
        this.needPlayer.setValue(needPlayer);
    }

    public LiveData<Integer> getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer.setValue(maxPlayer);
    }

    public LiveData<List<String>> getSportType() {
        return sportType;
    }

    public void setSportType(List<String> sportType) {
        this.sportType.setValue(sportType);
    }
}
