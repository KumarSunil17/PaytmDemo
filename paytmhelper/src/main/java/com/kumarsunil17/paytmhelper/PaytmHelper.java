package com.kumarsunil17.paytmhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.kumarsunil17.paytmhelper.utils.Constants;
import com.kumarsunil17.paytmhelper.utils.JSONParser;
import com.kumarsunil17.paytmhelper.utils.PaytmHelperTransactionCallback;
import com.kumarsunil17.paytmhelper.utils.pojo.Paytm;
import com.paytm.pgsdk.Log;
import com.paytm.pgsdk.PaytmConstants;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PaytmHelper {

    private static PaytmHelper mInstance;
    private Context context;
    private String merchantID, serverurl, orderid, customerid;;
    private int loaderLayoutId, successLayoutId, failureLayoutId;
    private PaytmHelperTransactionCallback paytmHelperTransactionCallback;

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
        this.paytmHelperTransactionCallback = paytmHelperTransactionCallback;
        GetChecksumServer dl = new GetChecksumServer(paytm);
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetChecksumServer extends AsyncTask<ArrayList<String>, Void, String> {
        private String checksumhash = "";
        private Paytm paytm;

        GetChecksumServer(Paytm paytm) {
            this.paytm = paytm;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected final String doInBackground(ArrayList<String>... arrayLists) {
            JSONParser jsonParser = new JSONParser(context);
            String param=
                    "MID=" + merchantID+
                            "&uid" + paytm.getUserId()+
                            "&email" + paytm.getEmail()+
                            "&phone" + paytm.getPhone()+
//                            "&ORDER_ID=" + orderid+
//                            "&CUST_ID="+ custid+
                            "&CHANNEL_ID=" + Constants.CHANNEL_ID +
                            "&TXN_AMOUNT=" + paytm.getAmount()+
                            "&WEBSITE=" + Constants.WEBSITE+
                            "&CALLBACK_URL=" + Constants.CALLBACK_URL+
                            "&INDUSTRY_TYPE_ID=" + Constants.INDUSTRY_TYPE_ID;
            JSONObject jsonObject = jsonParser.makeHttpRequest(getServerurl()+"/generateChecksum.php","POST",param);
            Log.e("CheckSum result ",jsonObject.toString());
            try {
                checksumhash=jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                orderid = jsonObject.getString("order_id");
                customerid = jsonObject.getString("cust_id");

                Log.e("CHECKSUMHASH ",checksumhash);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return checksumhash;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e(" setup acc ","  signup result  " + s);

            PaytmPGService service = PaytmPGService.getStagingService();

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("MID", merchantID);
            paramMap.put("ORDER_ID", orderid);
            paramMap.put("CUST_ID", customerid);
            paramMap.put("CHANNEL_ID", Constants.CHANNEL_ID);
            paramMap.put("MOBILE", paytm.getPhone());
            paramMap.put("EMAIL", paytm.getEmail());
            paramMap.put("TXN_AMOUNT", paytm.getAmount());
            paramMap.put("WEBSITE", Constants.WEBSITE);
            paramMap.put("CALLBACK_URL" ,Constants.CALLBACK_URL);
            paramMap.put("CHECKSUMHASH" , checksumhash);
            paramMap.put("INDUSTRY_TYPE_ID",Constants.INDUSTRY_TYPE_ID);

            PaytmOrder Order = new PaytmOrder(paramMap);

            Log.e("checksum ", "param "+ paramMap.toString());

            service.initialize(Order,null);
            service.startPaymentTransaction(context, true, true,
                    new PaytmPaymentTransactionCallback() {
                        @Override
                        public void onTransactionResponse(Bundle inResponse) {
                            Log.e("checksum ", " respon true " + inResponse.toString());
                            String response = inResponse.getString("RESPMSG");

                            if (inResponse.getString("STATUS").equalsIgnoreCase("TXN_FAILURE")){
                                //failure
                            }else{
                                //success
                                String checksum = inResponse.getString("CHECKSUMHASH");
                                VerifyChecksumHelper v = new VerifyChecksumHelper(checksum);
                                v.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }

                        @Override
                        public void networkNotAvailable() {
                            //showResult("Network unavailable", false);
                            paytmHelperTransactionCallback.networkNotAvailable();
                        }

                        @Override
                        public void clientAuthenticationFailed(String inErrorMessage) {
                            //showResult(inErrorMessage, false);
                            paytmHelperTransactionCallback.clientAuthenticationFailed(inErrorMessage);
                        }

                        @Override
                        public void someUIErrorOccurred(String inErrorMessage) {
                            //showResult(inErrorMessage, false);
                            paytmHelperTransactionCallback.someUIErrorOccurred(inErrorMessage);
                        }

                        @Override
                        public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                            //showResult(inErrorMessage, false);
                            paytmHelperTransactionCallback.onErrorLoadingWebPage(iniErrorCode, inErrorMessage, inFailingUrl);
                        }

                        @Override
                        public void onBackPressedCancelTransaction() {
                            //showResult("Cancelled by user", false);
                            paytmHelperTransactionCallback.onBackPressedCancelTransaction();
                        }

                        @Override
                        public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                            //showResult(inErrorMessage, false);
                            paytmHelperTransactionCallback.onTransactionCancel(inErrorMessage,inResponse);
                        }
                    });
        }
    }

    private class VerifyChecksumHelper extends AsyncTask<ArrayList<String>, Void, String> {
        private String checksum, isValid;

        VerifyChecksumHelper(String checksum) {
            this.checksum = checksum;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {
            JSONParser jsonParser = new JSONParser(context);
            String param = "CHECKSUMHASH="+checksum;
            JSONObject jsonObject = jsonParser.makeHttpRequest(getServerurl()+"/verifyChecksum.php","POST",param);
            Log.e("verify result : ",jsonObject.toString());
            try {
                isValid = jsonObject.getString("IS_CHECKSUM_VALID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return isValid;
        }

        @Override
        protected void onPostExecute(String s) {
//            paytmHelperTransactionCallback.onTransactionResponse();

            if (isValid.equalsIgnoreCase("Y")){
                //transaction success
            }
        }
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