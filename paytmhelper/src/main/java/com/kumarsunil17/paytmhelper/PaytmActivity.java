package com.kumarsunil17.paytmhelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class PaytmActivity extends AppCompatActivity {
    private RelativeLayout loaderLayout, failureLayout, successLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paytm);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loaderLayout = findViewById(R.id.loader_container);
        failureLayout = findViewById(R.id.failure_container);
        successLayout = findViewById(R.id.success_container);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
