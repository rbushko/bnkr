package com.teamsh.bnkr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.reimaginebanking.api.nessieandroidsdk.NessieError;
import com.reimaginebanking.api.nessieandroidsdk.NessieResultsListener;
import com.reimaginebanking.api.nessieandroidsdk.models.Account;
import com.reimaginebanking.api.nessieandroidsdk.models.Customer;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private NessieClient nessieClient;
    final UUID appUuid = UUID.fromString("140862c0-5447-4504-a65c-b82c01736218");

    private EditText phoneNumberEdit;
    private EditText passwordEdit;
    private Button signInButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nessieClient = NessieClient.getInstance("f657ed9d5d6ce1209eb7703117b752e0");

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        phoneNumberEdit = (EditText) findViewById(R.id.accountNumberEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        signInButton = (Button) findViewById(R.id.signInButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        setUpListeners();

        nessieClient.CUSTOMER.getCustomers(new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                List<Customer> customers = (List<Customer>) result;
                // do something with the list of customers here
            }

            @Override
            public void onFailure(NessieError error) {
                // handle error
            }
        });
    }

    public void setUpListeners() {

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nessieClient.ACCOUNT.getAccounts(new NessieResultsListener() {
                    @Override
                    public void onSuccess(Object result) {
                        List<Account> accounts = (List<Account>) result;

                        for (Account a : accounts) {
                            Log.d("Account", a.toString());

                            Log.d("Entered Number", "000000" + phoneNumberEdit.getText().toString());
                            Log.d("Entered Password", passwordEdit.getText().toString());

                            if (a.getAccountNumber().equals("000000" + phoneNumberEdit.getText().toString())
                                    && a.getNickname().equals(passwordEdit.getText().toString())) {
                                Log.d("Sign in", "Success");
                                sendSignalToPebble();

                                // Start the pebble service
                                Intent service = new Intent(getApplicationContext(), BankDataService.class);
                                service.putExtra("id", a.getId().toString());
                                Log.d("Service ID", a.getId().toString());
                                startService(service);

                                Intent intent = new Intent(getApplicationContext(), WorkingActivity.class);
                                startActivity(intent);
                            } else {
                                Log.d("Sign in", "Failure - Phone Number/Password not valid");
                            }
                        }
                    }

                    @Override
                    public void onFailure(NessieError error) {
                        Log.d("Sign in", "Failure - " + error.getMessage());
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public void sendSignalToPebble() {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addInt32(PebbleCode.LOGGED_IN, 1);

        PebbleKit.sendDataToPebble(getApplicationContext(), appUuid, dict);
    }

    public void onBackPressed() {
        //void
    }
}
