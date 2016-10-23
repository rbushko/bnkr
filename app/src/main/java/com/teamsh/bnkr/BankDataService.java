package com.teamsh.bnkr;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.reimaginebanking.api.nessieandroidsdk.NessieError;
import com.reimaginebanking.api.nessieandroidsdk.NessieResultsListener;
import com.reimaginebanking.api.nessieandroidsdk.models.ATM;
import com.reimaginebanking.api.nessieandroidsdk.models.Account;
import com.reimaginebanking.api.nessieandroidsdk.models.PaginatedResponse;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

import java.io.IOException;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class BankDataService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {

    private NessieClient nessieClient;
    private GoogleApiClient googleApiClient;

    private Location mLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        nessieClient = NessieClient.getInstance("f657ed9d5d6ce1209eb7703117b752e0");
        googleApiClient = new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

        String id = intent.getStringExtra("id");
        Log.d("Service Status", "Running with ID " + id);

        nessieClient.ACCOUNT.getAccount(id, new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                Account account = (Account) result;
                Log.d("Account Balance", "$" + account.getBalance());
            }

            @Override
            public void onFailure(NessieError error) {
                Log.d("Account Balance", "Failure - " + error.getMessage());
            }
        });


        return Service.START_STICKY;
    }

    public void getMap(float latitude, float longitude) throws IOException {
        String mapURL = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=16&size=144x168&maptype=roadmap" +
                "&markers=color:blue%7Clabel:A%7C" + latitude + "," + longitude +
                "&key=AIzaSyAUqtN0SYduBia8NuugzPF46ma01dOLgEQ";

        Log.d("Map URL", mapURL);
        //Bitmap bmp = Picasso.with(getApplicationContext()).load(mapURL).get();
    }

    public static float getDistance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371000; //meters

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    public void getATM() {

        nessieClient.ATM.getATMs((float) mLocation.getLatitude(), (float) mLocation.getLongitude(), 8f, new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                PaginatedResponse<ATM> response = (PaginatedResponse<ATM>) result;

                if (response.getObjectList().size() > 0) {
                    ATM nearestATM = response.getObjectList().get(0);
                    for (ATM atm : response.getObjectList()) {
                        if (getDistance(mLocation.getLatitude(), mLocation.getLongitude(), atm.getGeocode().getLat(), atm.getGeocode().getLng()) <
                                getDistance(mLocation.getLatitude(), mLocation.getLongitude(), nearestATM.getGeocode().getLat(), nearestATM.getGeocode().getLng())) {
                            nearestATM = atm;
                            //Log.d("New Nearest ATM", nearestATM.toString());
                        }
                        //Log.d("ATM Result", atm.toString());
                    }

                    Log.d("Nearest ATM Result", nearestATM.toString());
                    try {
                        getMap(nearestATM.getGeocode().getLat(), nearestATM.getGeocode().getLng());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(NessieError error) {
                // handle error
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(new Activity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLocation != null) {
            Log.d("Location", mLocation.getLatitude() + ", " + mLocation.getLongitude());
            getATM();
        } else {
            Log.d("Location", "Not Connected");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
