package com.teamsh.bnkr;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

public class WorkingActivity extends AppCompatActivity {

    private NessieClient nessieClient;
    private GoogleApiClient googleApiClient;

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}
