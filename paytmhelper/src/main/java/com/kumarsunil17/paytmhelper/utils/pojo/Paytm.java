package com.kumarsunil17.paytmhelper.utils.pojo;

public class Paytm {
    String amount, userId, email, phone;

    public Paytm(String amount, String userId, String email, String phone) {
        this.amount = amount;
        this.userId = userId;
        this.email = email;
        this.phone = phone;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
