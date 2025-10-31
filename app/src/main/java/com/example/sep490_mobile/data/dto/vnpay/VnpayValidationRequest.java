package com.example.sep490_mobile.data.dto.vnpay;

import java.util.Map;
// DTO này chứa các tham số callback VNPay để gửi lên backend xác thực
public class VnpayValidationRequest {
    public Map<String, String> vnpayParams;
    public VnpayValidationRequest(Map<String, String> params) { this.vnpayParams = params; }
}
