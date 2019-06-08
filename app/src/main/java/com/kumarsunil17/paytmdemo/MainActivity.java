package com.kumarsunil17.paytmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.kumarsunil17.paytmdemo.utils.Constants;
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

        amount = findViewById(R.id.amount);
        orderid = findViewById(R.id.orderid);
        customerid = findViewById(R.id.customer_id);

    }

    public void proceedToPay(View view) {
        if (!TextUtils.isEmpty(amount.getText().toString()) && !TextUtils.isEmpty(orderid.getText().toString()) && !TextUtils.isEmpty(customerid.getText().toString())) {

            Paytm paytm = new Paytm(amount.getText().toString(),
                    "skmuduli17@gmail.com",
                    "9438295102");

            PaytmHelper p = PaytmHelper.initialize(this, Constants.MERCHANT_ID,Constants.SERVER_URL, R.layout.loader_layout, R.layout.failure_layout, R.layout.success_layout);
            p.startPayment(paytm, new PaytmHelperTransactionCallback() {
                @Override
                public void onTransactionResponse(Bundle inResponse) {
                    Toast.makeText(MainActivity.this, "Transaction success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void networkNotAvailable() {
                    Toast.makeText(MainActivity.this, "Network not available", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {
                    Toast.makeText(MainActivity.this, inErrorMessage, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void someUIErrorOccurred(String inErrorMessage) {
                    Toast.makeText(MainActivity.this, inErrorMessage, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                    Toast.makeText(MainActivity.this, inErrorMessage, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBackPressedCancelTransaction() {
                    Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                    Toast.makeText(MainActivity.this, inErrorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            amount.setText("");
            orderid.setText("");
            customerid.setText("");
        }
    }
}