package com.kumarsunil17.paytmhelper.utils;

import android.os.Bundle;

import java.io.Serializable;

public interface PaytmHelperTransactionCallback extends Serializable {
    public void onTransactionResponse(Bundle inResponse);
    public void networkNotAvailable();
    public void clientAuthenticationFailed(String inErrorMessage);
    public void someUIErrorOccurred(String inErrorMessage);
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl);
    public void onBackPressedCancelTransaction();
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse);
}
