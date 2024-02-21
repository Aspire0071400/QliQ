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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class AuthOTP extends AppCompatActivity {
    Button verifyOtpBtn;
    EditText otpEdt;
    TextView changeNumberTv;
    ProgressBar progressBarOtpAuth;
    View loadingBgOtpAuth;
    FirebaseAuth auth;
    String enteredOtp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_otp);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        verifyOtpBtn = findViewById(R.id.verify_otp_btn);
        otpEdt = findViewById(R.id.otp_edt);
        progressBarOtpAuth = findViewById(R.id.progressBar_otp_auth);
        loadingBgOtpAuth = findViewById(R.id.loadingbg_otp_auth);
        auth = FirebaseAuth.getInstance();
        changeNumberTv = findViewById(R.id.change_number_tv);


        changeNumberTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AuthOTP.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredOtp = otpEdt.getText().toString();
                if(enteredOtp.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter OTP", Toast.LENGTH_SHORT).show();
                }else{
                    progressBarOtpAuth.setVisibility(View.VISIBLE);
                    loadingBgOtpAuth.setVisibility(View.VISIBLE);

                    String codeRecieved = getIntent().getStringExtra("otp");
                    PhoneAuthCredential cred = PhoneAuthProvider.getCredential(codeRecieved,enteredOtp);
                    signInWithPhoneAuthCredential(cred);

                }
            }
        });


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential cred){
        auth.signInWithCredential(cred).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBarOtpAuth.setVisibility(View.GONE);
                    loadingBgOtpAuth.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"LogIn Success",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(AuthOTP.this, SetProfile.class);
                    startActivity(i);
                    finish();
                }else{
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        progressBarOtpAuth.setVisibility(View.GONE);
                        loadingBgOtpAuth.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "LogIn Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}