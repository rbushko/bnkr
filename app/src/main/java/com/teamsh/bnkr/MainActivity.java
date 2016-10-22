package com.teamsh.bnkr;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

public class MainActivity extends AppCompatActivity {

    private NessieClient nessieClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        nessieClient = NessieClient.getInstance("f657ed9d5d6ce1209eb7703117b752e0");


    }
}
