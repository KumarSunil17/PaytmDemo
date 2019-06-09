package com.kumarsunil17.paytmhelper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kumarsunil17.paytmhelper.utils.Constants;
import com.kumarsunil17.paytmhelper.utils.PaytmHelperTransactionCallback;
import com.kumarsunil17.paytmhelper.utils.VolleySingleton;
import com.kumarsunil17.paytmhelper.utils.pojo.Paytm;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PaytmActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback{
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverurl + "/generateChecksum.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("generate : ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);// if (jsonObject.getBoolean("result")) {
                            String checksumhash = jsonObject.getString("CHECKSUMHASH");
                            orderid = jsonObject.getString("ORDER_ID");

                            PaytmPGService service = PaytmPGService.getStagingService();

                            HashMap<String, String> paramMap = new HashMap<>();
                            paramMap.put("MID", merchantID);
                            paramMap.put("ORDER_ID", orderid);
                            paramMap.put("CUST_ID", paytm.getCust_id());
                            paramMap.put("CHANNEL_ID", "WAP");
                            //paramMap.put("MOBILE", "89797");
                            //paramMap.put("EMAIL", "as@as.com");
                            paramMap.put("TXN_AMOUNT", paytm.getAmount());
                            paramMap.put("WEBSITE", "WEBSTAGING");
                            paramMap.put("CALLBACK_URL", "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp");
                            paramMap.put("CHECKSUMHASH", checksumhash);
                            paramMap.put("INDUSTRY_TYPE_ID", "Retail");

                            PaytmOrder Order = new PaytmOrder(paramMap);

                            service.initialize(Order, null);
                            service.startPaymentTransaction(PaytmActivity.this, true, true, PaytmActivity.this);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.getMessage());
                Toast.makeText(PaytmActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> m=new HashMap<>();
                m.put("MID" , merchantID);
                //m.put("email","skmuduli17@gmail.com");
                //m.put("phone","9438295102");
                m.put("ORDER_ID", "order"+paytm.getCust_id());
                m.put("CUST_ID", paytm.getCust_id());
                m.put("INDUSTRY_TYPE_ID" ,"Retail");
                //m.put("txn_id","qer13");
                m.put("CHANNEL_ID" , "WAP");
                m.put("TXN_AMOUNT" , paytm.getAmount());
                m.put("WEBSITE" , "WEBSTAGING");
                m.put("CALLBACK_URL" , "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp");
                return m;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest,this);
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
        loaderLayoutId = Integer.parseInt(getIntent().getStringExtra("loader"));
        successLayoutId = Integer.parseInt(getIntent().getStringExtra("success"));
        failureLayoutId = Integer.parseInt(getIntent().getStringExtra("failure"));
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        Log.e("paytm", inResponse.toString());

        if (inResponse.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS")){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, serverurl + "/verifyChecksum.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("verify : ", response);
//                            setContentView(successLayout);

                            try {
                                JSONObject jsonObject = new JSONObject(response);// if (jsonObject.getBoolean("result")) {
//                            String is_checksum_valid = jsonObject.getString("IS_CHECKSUM_VALID");
//                            String txn_id = jsonObject.getString("ORDER_ID");
//
                                Bundle bundle = new Bundle();
                                Iterator iter = jsonObject.keys();
                                while(iter.hasNext()){
                                    String key = (String)iter.next();
                                    String value = jsonObject.getString(key);
                                    bundle.putString(key,value);
                                }
                                paytmHelperTransactionCallback.onTransactionResponse(bundle);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error", error.getMessage());

                    Toast.makeText(PaytmActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> m=new HashMap<>();
                    m.put("MID" , merchantID);
                    //m.put("email","skmuduli17@gmail.com");
                    //m.put("phone","9438295102");
                    m.put("ORDER_ID", "order"+paytm.getPhone());
                    m.put("CUST_ID", paytm.getEmail());
                    m.put("INDUSTRY_TYPE_ID" ,"Retail");
                    //m.put("txn_id","qer13");
                    m.put("CHANNEL_ID" , "WAP");
                    m.put("TXN_AMOUNT" , paytm.getAmount());
                    m.put("WEBSITE" , "WEBSTAGING");
                    m.put("CALLBACK_URL" , "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp");
                    return m;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest,this);
        }else{
            Toast.makeText(this, inResponse.getString("RESPMSG"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void networkNotAvailable() {
        paytmHelperTransactionCallback.networkNotAvailable();
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        paytmHelperTransactionCallback.clientAuthenticationFailed(inErrorMessage);
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        paytmHelperTransactionCallback.someUIErrorOccurred(inErrorMessage);
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        paytmHelperTransactionCallback.onErrorLoadingWebPage(iniErrorCode, inErrorMessage, inFailingUrl);
    }

    @Override
    public void onBackPressedCancelTransaction() {
        paytmHelperTransactionCallback.onBackPressedCancelTransaction();

    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        paytmHelperTransactionCallback.onTransactionCancel(inErrorMessage, inResponse);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
