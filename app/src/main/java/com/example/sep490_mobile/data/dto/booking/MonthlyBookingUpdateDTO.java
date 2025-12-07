package com.example.sep490_mobile.data.dto.booking;


public class MonthlyBookingUpdateDTO {

    private String status;
    private Double totalPrice;
    private String paymentMethod;
    private Double originalPrice;
    private String note;

    public MonthlyBookingUpdateDTO() {
    }

    public MonthlyBookingUpdateDTO(String status, Double totalPrice, String paymentMethod, Double originalPrice, String note) {
        this.status = status;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.originalPrice = originalPrice;
        this.note = note;
    }

    // Getters & Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "MonthlyBookingUpdateDTO{" +
                "status='" + status + '\'' +
                ", totalPrice=" + totalPrice +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", originalPrice=" + originalPrice +
                ", note='" + note + '\'' +
                '}';
    }
}
