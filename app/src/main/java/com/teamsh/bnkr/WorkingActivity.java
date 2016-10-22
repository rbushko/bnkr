package com.teamsh.bnkr;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.reimaginebanking.api.nessieandroidsdk.NessieError;
import com.reimaginebanking.api.nessieandroidsdk.NessieResultsListener;
import com.reimaginebanking.api.nessieandroidsdk.models.ATM;
import com.reimaginebanking.api.nessieandroidsdk.models.Address;
import com.reimaginebanking.api.nessieandroidsdk.models.Customer;
import com.reimaginebanking.api.nessieandroidsdk.models.PaginatedResponse;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class WorkingActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private NessieClient nessieClient;
    private GoogleApiClient googleApiClient;

    private final int SCREEN_W = 144;
    private final int SCREEN_H = 168;

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        nessieClient = NessieClient.getInstance("f657ed9d5d6ce1209eb7703117b752e0");
        googleApiClient = new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();

        //createCustomer();
        //getATM();
        //getBranch();
    }

    public void getMap(float latitude, float longitude) {
        String mapURL = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=16&size=600x300&maptype=roadmap" +
                "&markers=color:blue%7Clabel:A%7C" + latitude + "," + longitude +
                "&key=AIzaSyAUqtN0SYduBia8NuugzPF46ma01dOLgEQ";

        Log.d("Map URL", mapURL);
        //Picasso.with(getApplicationContext())
        //.load(mapURL)
        //.resize(SCREEN_W, SCREEN_H)
        //.into((ImageView) findViewById(R.id.map));
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

    public void getBranch() {

    }

    public void getATM() {

        nessieClient.ATM.getATMs((float) mLocation.getLatitude(), (float) mLocation.getLongitude(), 8f, new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                PaginatedResponse<ATM> response = (PaginatedResponse<ATM>) result;

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
                getMap(nearestATM.getGeocode().getLat(), nearestATM.getGeocode().getLng());
            }

            @Override
            public void onFailure(NessieError error) {
                // handle error
            }
        });
    }

    public void createCustomer() {

        Address customerAddress = new Address.Builder()
                .city("Baltimore")
                .state("MD")
                .streetName("Wyman Park Dr")
                .streetNumber("3101")
                .zip("21211")
                .build();

        Customer newCustomer = new Customer.Builder()
                .address(customerAddress)
                .firstName("John")
                .lastName("Doe")
                .build();

        Log.d("Customer", newCustomer.toString());

        nessieClient.CUSTOMER.createCustomer(newCustomer, new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                Log.d("Create Customer", "Success");
            }

            @Override
            public void onFailure(NessieError error) {
                Log.d("Create Customer", "Failure - " + error.getMessage() + " Culprit - " + error.getCulprit());
            }
        });
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient.connect();
        AppIndex.AppIndexApi.start(googleApiClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(googleApiClient, getIndexApiAction());
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLocation != null) {
            Toast.makeText(this, mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            getATM();
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
