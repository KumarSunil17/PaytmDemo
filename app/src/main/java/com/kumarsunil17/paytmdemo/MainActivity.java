package com.kumarsunil17.paytmdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.kumarsunil17.paytmhelper.PaytmHelper;
import com.kumarsunil17.paytmhelper.utils.PaytmHelperTransactionCallback;
import com.kumarsunil17.paytmhelper.utils.pojo.Paytm;

public class MainActivity extends AppCompatActivity {
    private EditText amount, orderid, customerid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        PaytmHelper p = PaytmHelper.initialize(this, "","",12,12,12);

        amount = findViewById(R.id.amount);
        orderid = findViewById(R.id.orderid);
        customerid = findViewById(R.id.customer_id);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
    }

    public void proceedToPay(View view) {
        if (!TextUtils.isEmpty(amount.getText().toString()) && !TextUtils.isEmpty(orderid.getText().toString()) && !TextUtils.isEmpty(customerid.getText().toString())) {

//            Intent i = new Intent(MainActivity.this, PaytmActivity.class);
//            i.putExtra("amount", amount.getText().toString().trim());
//            i.putExtra("orderid", orderid.getText().toString().trim());
//            i.putExtra("custid", customerid.getText().toString().trim());

//            startActivity(i);

            Paytm paytm = new Paytm(amount.getText().toString(),
                    "qwertyqwerty123",
                    "skmuduli17@gmail.com",
                    "9438295102");

            PaytmHelper p = PaytmHelper.initialize(MainActivity.this, "","",1,1,1);
            p.startPayment(paytm, new PaytmHelperTransactionCallback() {
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
            });

            amount.setText("");
            orderid.setText("");
            customerid.setText("");
        }
    }
}