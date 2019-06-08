package com.kumarsunil17.paytmhelper.utils.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Paytm implements Parcelable {
    private String amount, email, phone;

    public Paytm(String amount, String email, String phone) {
        this.amount = amount;
        this.email = email;
        this.phone = phone;
    }

    private Paytm(Parcel in) {
        amount = in.readString();
        email = in.readString();
        phone = in.readString();
    }

    public static final Creator<Paytm> CREATOR = new Creator<Paytm>() {
        @Override
        public Paytm createFromParcel(Parcel in) {
            return new Paytm(in);
        }

        @Override
        public Paytm[] newArray(int size) {
            return new Paytm[size];
        }
    };

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(amount);
        dest.writeString(email);
        dest.writeString(phone);
    }
}
