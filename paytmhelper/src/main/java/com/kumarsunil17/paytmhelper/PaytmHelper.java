package com.kumarsunil17.paytmhelper;

import android.content.Context;
import android.content.Intent;

import com.kumarsunil17.paytmhelper.utils.PaytmHelperTransactionCallback;
import com.kumarsunil17.paytmhelper.utils.pojo.Paytm;

public class PaytmHelper {

    private static PaytmHelper mInstance;
    private Context context;
    private String merchantID, serverurl, orderid, customerid;
    private int loaderLayoutId, successLayoutId, failureLayoutId;

    private PaytmHelper(Context context, String MID, String serverUrl, int loaderLayoutId, int failureLayoutId, int successLayoutId) {
        this.context = context;
        this.merchantID = MID;
        this.serverurl = serverUrl;
        this.failureLayoutId = failureLayoutId;
        this.successLayoutId = successLayoutId;
        this.loaderLayoutId = loaderLayoutId;
    }

    public static synchronized PaytmHelper initialize(Context context, String MID, String serverUrl, int loaderLayoutId, int failureLayoutId, int successLayoutId){
        if (mInstance == null) {
            mInstance = new PaytmHelper(context, MID, serverUrl, loaderLayoutId, failureLayoutId, successLayoutId);
        }
        return mInstance;
    }

    public void startPayment(Paytm paytm, PaytmHelperTransactionCallback paytmHelperTransactionCallback){

        context.startActivity(new Intent(context, PaytmActivity.class)
            .putExtra("paytmdata",paytm)
            .putExtra("paytmlistener", paytmHelperTransactionCallback)
                .putExtra("loader", loaderLayoutId)
                .putExtra("failure", failureLayoutId)
                .putExtra("success", successLayoutId)
                .putExtra("serverurl", serverurl)
                .putExtra("paytmmid", merchantID)
        );

    }

    public String getServerurl() {
        return serverurl;
    }

    public int getLoaderLayoutId() {
        return loaderLayoutId;
    }

    public void setLoaderLayoutId(int loaderLayoutId) {
        this.loaderLayoutId = loaderLayoutId;
    }

    public int getSuccessLayoutId() {
        return successLayoutId;
    }

    public void setSuccessLayoutId(int successLayoutId) {
        this.successLayoutId = successLayoutId;
    }

    public int getFailureLayoutId() {
        return failureLayoutId;
    }

    public void setFailureLayoutId(int failureLayoutId) {
        this.failureLayoutId = failureLayoutId;
    }
}