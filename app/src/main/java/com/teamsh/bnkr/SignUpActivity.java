package com.teamsh.bnkr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.reimaginebanking.api.nessieandroidsdk.NessieError;
import com.reimaginebanking.api.nessieandroidsdk.NessieResultsListener;
import com.reimaginebanking.api.nessieandroidsdk.constants.AccountType;
import com.reimaginebanking.api.nessieandroidsdk.models.Account;
import com.reimaginebanking.api.nessieandroidsdk.models.Address;
import com.reimaginebanking.api.nessieandroidsdk.models.Customer;
import com.reimaginebanking.api.nessieandroidsdk.models.PostResponse;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

public class SignUpActivity extends AppCompatActivity {

    private NessieClient nessieClient;

    EditText firstNameEdit;
    EditText lastNameEdit;
    EditText phoneNumberEdit;
    EditText passwordEdit;
    EditText confirmPasswordEdit;

    TextView error;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        nessieClient = NessieClient.getInstance("f657ed9d5d6ce1209eb7703117b752e0");

        firstNameEdit = (EditText) findViewById(R.id.firstNameText);
        lastNameEdit = (EditText) findViewById(R.id.lastNameText);
        phoneNumberEdit = (EditText) findViewById(R.id.phoneNumText);
        passwordEdit = (EditText) findViewById(R.id.passwordText);
        confirmPasswordEdit = (EditText) findViewById(R.id.confirmPasswordText);

        error = (TextView) findViewById(R.id.error);
        signUpButton = (Button) findViewById(R.id.signInButton);

        setListener();
    }

    public void setListener() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstNameEdit.getText().toString().isEmpty()
                        && !lastNameEdit.getText().toString().isEmpty()
                        && !phoneNumberEdit.getText().toString().isEmpty()
                        && !passwordEdit.getText().toString().isEmpty()
                        && !confirmPasswordEdit.getText().toString().isEmpty()) {
                    if (passwordEdit.getText().toString().equals(confirmPasswordEdit.getText().toString())) {
                        createAccount();
                    } else {
                        error.setText("Passwords do not match");
                    }
                } else {
                    error.setText("All fields required");
                }
            }
        });
    }

    public void createAccount() {

        Address address = new Address.Builder()
                .streetNumber("0")
                .streetName("N Charles St")
                .city("Baltimore")
                .state("MD")
                .zip("21218")
                .build();

        Customer customer = new Customer.Builder()
                .firstName(firstNameEdit.getText().toString())
                .lastName(lastNameEdit.getText().toString())
                .address(address)
                .build();

        nessieClient.CUSTOMER.createCustomer(customer, new NessieResultsListener() {
            @Override
            public void onSuccess(Object result) {
                PostResponse<Customer> newCustomer = (PostResponse<Customer>) result;

                Account account = new Account.Builder()
                        .accountNumber("000000" + phoneNumberEdit.getText().toString())
                        .balance(0)
                        .rewards(0)
                        .type(AccountType.CHECKING)
                        .nickname(passwordEdit.getText().toString())
                        .build();

                nessieClient.ACCOUNT.createAccount(newCustomer.getObjectCreated().getId(), account, new NessieResultsListener() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("Account Creation", "Success");
                        Intent intent = new Intent(getApplicationContext(), WorkingActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(NessieError error) {
                        Log.d("Account Creation", "Failure - " + error.getMessage() + ". Culprit - " + error.getCulprit());
                    }
                });
            }

            @Override
            public void onFailure(NessieError error) {
                Log.d("Customer Creation", "Failure - " + error.getMessage() + ". Culprit - " + error.getCulprit());
            }
        });
    }
}
