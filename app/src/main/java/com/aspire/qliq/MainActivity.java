package com.aspire.qliq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button sendOtp;
    EditText phoneNumberEdt;
    CountryCodePicker ccp;
    String countryCode,phoneNumber,codeSent;
    FirebaseAuth auth;
    ProgressBar progressBarOtpSend;
    View loadingBgOtpSend;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        auth = FirebaseAuth.getInstance();
        phoneNumberEdt = findViewById(R.id.phone_number_edt);
        ccp = findViewById(R.id.ccp);
        progressBarOtpSend = findViewById(R.id.progressBar_otp_send);
        loadingBgOtpSend = findViewById(R.id.loadingbg_otp_send);
        sendOtp = findViewById(R.id.send_otp_btn);


        countryCode = ccp.getSelectedCountryCodeWithPlus();
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode=ccp.getSelectedCountryCodeWithPlus();
            }
        });


        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num;
                num = phoneNumberEdt.getText().toString();

                if(num.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please Enter Your Number",Toast.LENGTH_SHORT).show();
                    
                } else if (num.length() < 10) {
                    Toast.makeText(getApplicationContext(),"Please Correct Number",Toast.LENGTH_SHORT).show();
                    
                }else{
                    progressBarOtpSend.setVisibility(View.VISIBLE);
                    loadingBgOtpSend.setVisibility(View.VISIBLE);
                    phoneNumber=countryCode+num;

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(callback)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(),"OTP sent",Toast.LENGTH_SHORT).show();
                progressBarOtpSend.setVisibility(View.GONE);
                loadingBgOtpSend.setVisibility(View.GONE);
                codeSent = s;
                Intent i = new Intent(MainActivity.this,AuthOTP.class);
                i.putExtra("otp", codeSent);
                startActivity(i);
            }
        };



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getInstance().getCurrentUser() != null){
            Intent i = new Intent(MainActivity.this, HomeScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }


    }
}