package com.kumarsunil17.paytmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kumarsunil17.paytmdemo.utils.Constants;
import com.kumarsunil17.paytmdemo.utils.VolleySingleton;
import com.paytm.pgsdk.Log;
import com.paytm.pgsdk.PaytmConstants;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaytmActivity extends AppCompatActivity {

    private TextView paymentMessage;
    private ImageView paymentImage;
    private LinearLayout paymentProcessing, paymentResult;
    private String CHECKSUMHASH, amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paytm);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        paymentMessage = findViewById(R.id.payment_message);
        paymentImage = findViewById(R.id.payment_image);
        paymentProcessing = findViewById(R.id.payment_processing_lin);
        paymentResult = findViewById(R.id.payment_result_lin);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProcessing();

        amount = getIntent().getStringExtra("rupees");
        getCheckSum();
    }

    private void getCheckSum() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GENERATE_CHECKSUM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            CHECKSUMHASH = jsonObject.has("CHECKSUMHASH")?jsonObject.getString("CHECKSUMHASH"):"";
                            Log.e("CHECKSUMHASH", CHECKSUMHASH);

                            placeOrder();
                        } catch (JSONException e) {
                            showResult(e.getMessage(), false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showResult("Network error", false);
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  map = new HashMap<>();
                map.put("MID", Constants.MERCHANT_ID);
                map.put("ORDER_ID", "order123");
                map.put("CUST_ID", "cust123");
                map.put("CHANNEL_ID", Constants.CHANNEL_ID);
                map.put("TXN_AMMOUNT", amount);
                map.put("WEBSITE", "WEBSTAGING");
                map.put("CALLBACK_URL", Constants.CALLBACK_URL);
                map.put("INDUSTRY_TYPE_ID","Retail");

                return map;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest,this);
    }

    private void placeOrder() {

        PaytmPGService service = PaytmPGService.getStagingService();

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", Constants.MERCHANT_ID); //MID provided by paytm
        paramMap.put("ORDER_ID", "order123");
        paramMap.put("CUST_ID", "cust123");
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("TXN_AMOUNT", amount);
        paramMap.put("WEBSITE", "WEBSTAGING");
        paramMap.put("CALLBACK_URL" ,Constants.CALLBACK_URL);

        //paramMap.put( "EMAIL" , "abc@gmail.com");   // no need
        // paramMap.put( "MOBILE_NO" , "9144040888");  // no need
        //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
        paramMap.put("CHECKSUMHASH" ,CHECKSUMHASH);
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");

        PaytmOrder order = new PaytmOrder(paramMap);
        service.initialize(order, null);

        service.startPaymentTransaction(PaytmActivity.this, true, true, new PaytmPaymentTransactionCallback() {
            public void someUIErrorOccurred(String inErrorMessage) {
                showResult(inErrorMessage, false);
            }
            public void onTransactionResponse(Bundle inResponse) {
                Log.e("PaymentResponse",inResponse.toString());

                String response = inResponse.getString("RESPMSG");
                if (inResponse.getString("STATUS").equalsIgnoreCase("TXN_FAILURE")){
                    showResult(response, false);
                }else
                    showResult(response, true);
//                if (! inResponse.get("STATUS").equals("TXN_FAILURE"))
//                    verifyTransaction(inResponse.getString("ORDER_ID"));
            }
            public void networkNotAvailable() {
                showResult("Network not available", false);
            }
            public void clientAuthenticationFailed(String inErrorMessage) {
                showResult(inErrorMessage, false);
            }
            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                showResult(inErrorMessage,false);
            }
            public void onBackPressedCancelTransaction() {
                showResult("Transaction cancelled",false);
                //finish();
            }
            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                showResult(inErrorMessage, false);
            }
        });
    }

    private void verifyTransaction(String orderid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.VERIFY_CHECKSUM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            showResult(response,true);
                            JSONObject jsonObject = new JSONObject(response);

                            String checksumhash = jsonObject.getString("IS_CHECKSUM_VALID");
//                            String orderid = jsonObject.getString("ORDER_ID");
//                            String paymentStatus = jsonObject.getString("payt_STATUS");
                            Log.e("is_checked",checksumhash);

                        } catch (JSONException e) {
                            showResult("Cant verify", false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showResult("Network error", false);
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                return map;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest,this);
    }

    private void showProcessing(){
        paymentResult.setVisibility(View.GONE);
        paymentProcessing.setVisibility(View.VISIBLE);
    }
    private void showResult(String message, boolean isSuccess){
        paymentImage.setImageDrawable(getResources().getDrawable(isSuccess ? R.drawable.checked_vector:R.drawable
        .unchecked_vector));
        paymentMessage.setText(message);
        paymentProcessing.setVisibility(View.GONE);
        paymentResult.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
