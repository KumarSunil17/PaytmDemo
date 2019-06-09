package com.kumarsunil17.paytmhelper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kumarsunil17.paytmhelper.utils.Constants;
import com.kumarsunil17.paytmhelper.utils.JSONParser;
import com.kumarsunil17.paytmhelper.utils.PaytmHelperTransactionCallback;
import com.kumarsunil17.paytmhelper.utils.pojo.Paytm;
import com.paytm.pgsdk.Log;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PaytmActivity extends AppCompatActivity {
    private RelativeLayout loaderLayout, failureLayout, successLayout;
    private Paytm paytm;
    private PaytmHelperTransactionCallback paytmHelperTransactionCallback;
    private String merchantID, serverurl, orderid;
    private int loaderLayoutId, successLayoutId, failureLayoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paytm);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checkPermission();

        getIntentData();

        loaderLayout = findViewById(loaderLayoutId);
        failureLayout = findViewById(successLayoutId);
        successLayout = findViewById(failureLayoutId);

        GetChecksumServer dl = new GetChecksumServer(paytm);
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(PaytmActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PaytmActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getIntentData(){
        merchantID = getIntent().getStringExtra("paytmmid");
        serverurl = getIntent().getStringExtra("serverurl");
        paytm = getIntent().getParcelableExtra("paytmdata");
        paytmHelperTransactionCallback = getIntent().getParcelableExtra("paytmlistener");

        loaderLayoutId = getIntent().getIntExtra("loader", R.layout.loader_layout);
        successLayoutId = getIntent().getIntExtra("success", R.layout.success_layout);
        failureLayoutId = getIntent().getIntExtra("failure", R.layout.failure_layout);
    }

    private class GetChecksumServer extends AsyncTask<ArrayList<String>, Void, String> {
        private String checksumhash = "";
        private Paytm paytm;

        GetChecksumServer(Paytm paytm) {
            this.paytm = paytm;
        }

        @Override
        protected void onPreExecute() {
            setContentView(loaderLayoutId);
        }

        protected final String doInBackground(ArrayList<String>... arrayLists) {
            JSONParser jsonParser = new JSONParser(PaytmActivity.this);
            String param=
                    "MID=" + merchantID+
                            "&email" + paytm.getEmail()+
                            "&phone" + paytm.getPhone()+
//                            "&ORDER_ID=" + orderid+
                            "&CUST_ID="+ paytm.getCust_id()+
                            "&CHANNEL_ID=" + Constants.CHANNEL_ID +
                            "&TXN_AMOUNT=" + paytm.getAmount()+
                            "&WEBSITE=" + Constants.WEBSITE+
                            "&CALLBACK_URL=" + Constants.CALLBACK_URL+
                            "&INDUSTRY_TYPE_ID=" + Constants.INDUSTRY_TYPE_ID;
            Log.e("server",serverurl+"/generateChecksum.php");
            JSONObject jsonObject = jsonParser.makeHttpRequest(serverurl+"/generateChecksum.php","POST",param);
            //Log.e("CheckSum result ",jsonObject.toString());
            try {
                if (jsonObject.getBoolean("result")) {
                    checksumhash = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    orderid = jsonObject.getString("order_id");

                    Log.e("CHECKSUMHASH ", checksumhash);
                }else{
                    Toast.makeText(PaytmActivity.this, jsonObject.getString("reason"), Toast.LENGTH_SHORT).show();
                }
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
            paramMap.put("CUST_ID", paytm.getCust_id());
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
            service.startPaymentTransaction(PaytmActivity.this, true, true,
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
            JSONParser jsonParser = new JSONParser(PaytmActivity.this);
            String param = "CHECKSUMHASH="+checksum+
                    "MID=" + merchantID+
                    "&email" + paytm.getEmail()+
                    "&phone" + paytm.getPhone()+
                    "&ORDER_ID=" + orderid+
                    "&CUST_ID="+ paytm.getCust_id()+
                    "&CHANNEL_ID=" + Constants.CHANNEL_ID +
                    "&TXN_AMOUNT=" + paytm.getAmount()+
                    "&WEBSITE=" + Constants.WEBSITE+
                    "&CALLBACK_URL=" + Constants.CALLBACK_URL+
                    "&INDUSTRY_TYPE_ID=" + Constants.INDUSTRY_TYPE_ID;

            JSONObject jsonObject = jsonParser.makeHttpRequest(serverurl+"/verifyChecksum.php","POST",param);
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
                paytmHelperTransactionCallback.onTransactionResponse(null);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
