package com.example.sep490_mobile.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Map<String, String>> selected = new MutableLiveData<Map<String, String>>();
    private MutableLiveData<String> address = new MutableLiveData<String>();
    private MutableLiveData<String> startTime = new MutableLiveData<String>("09:00");
    private MutableLiveData<String> endTime = new MutableLiveData<String>("15:00");
    private MutableLiveData<List<String>> sportType = new MutableLiveData<List<String>>();
    private MutableLiveData<String> price = new MutableLiveData<String>("250000");
    public void setAddress(String address) {
        this.address.setValue(address);
    }
    public LiveData<String> getAddress() {
        return address;
    }
    public void setStartTime(String startTime) {
        this.startTime.setValue(startTime);
    }
    public LiveData<String> getStartTime() {
        return startTime;
    }
    public void setEndTime(String endTime) {
        this.endTime.setValue(endTime);
    }
    public LiveData<String> getEndTime() {
        return endTime;
    }
    public void setSportType(List<String> sportType) {
        this.sportType.setValue(sportType);
    }
    public LiveData<List<String>> getSportType() {
        return sportType;
    }
    public void setPrice(String price) {
        this.price.setValue(price);
    }
    public LiveData<String> getPrice() {
        return price;
    }
    public void select(Map<String, String> item) {
        selected.setValue(item);
    }

    public LiveData<Map<String, String>> getSelected() {
        return selected;
    }
    private static final String DEFAULT_START_TIME = "09:00";
    private static final String DEFAULT_END_TIME = "15:00";
    private static final String DEFAULT_PRICE = "250000";
    public void clearAllData() {
        // Đặt tất cả các MutableLiveData về giá trị mặc định/ban đầu (hoặc null)

        // Đặt về null hoặc Map/List rỗng nếu muốn xóa hoàn toàn
        selected.setValue(null);
        address.setValue(null);
        sportType.setValue(null);

        // Đặt về các giá trị String khởi tạo ban đầu
        startTime.setValue(DEFAULT_START_TIME);
        endTime.setValue(DEFAULT_END_TIME);
        price.setValue(DEFAULT_PRICE);
    }
}
