package com.kumarsunil17.paytmhelper.utils;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public interface PaytmHelperTransactionCallback extends Parcelable {
    @Override
    int describeContents();

    @Override
    void writeToParcel(Parcel dest, int flags);

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public PaytmHelperTransactionCallback createFromParcel(Parcel in) {
            return new PaytmHelperTransactionCallback() {
                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {

                }

                @Override
                public void onTransactionResponse(Bundle inResponse) {

                }

                @Override
                public void networkNotAvailable() {

                }

                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {

                }

                @Override
                public void someUIErrorOccurred(String inErrorMessage) {

                }

                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {

                }

                @Override
                public void onBackPressedCancelTransaction() {

                }

                @Override
                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

                }
            };
        }

        public PaytmHelperTransactionCallback[] newArray(int size) {
            return new PaytmHelperTransactionCallback[size];
        }
    };

    public void onTransactionResponse(Bundle inResponse);
    public void networkNotAvailable();
    public void clientAuthenticationFailed(String inErrorMessage);
    public void someUIErrorOccurred(String inErrorMessage);
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl);
    public void onBackPressedCancelTransaction();
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse);
}
