package com.aspire.qliq;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfile extends AppCompatActivity {
    private Button saveProfileBtn;
    private EditText setUsernameEdt;
    CircleImageView setProfilePic;
    private static int profilePic = 123;
    private Uri imagePath;
    private FirebaseAuth auth;
    private String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String imageUriAccessToken;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    ProgressBar progressBarSetProfile;
    View loadingBgSetProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        auth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(auth.getUid());

        saveProfileBtn = findViewById(R.id.save_profile_btn);
        setUsernameEdt = findViewById(R.id.set_username_edt);
        setProfilePic = findViewById(R.id.set_profile_pic_iv);
        loadingBgSetProfile = findViewById(R.id.loadingbg_set_profile);
        progressBarSetProfile = findViewById(R.id.progressBar_set_profile);


        setProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i,profilePic);
            }
        });


        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = setUsernameEdt.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Username can't be empty",Toast.LENGTH_SHORT).show();
                    
                } else if (imagePath == null) {
                    Toast.makeText(getApplicationContext(),"profile Image can't be empty",Toast.LENGTH_SHORT).show();
                }else {

                    progressBarSetProfile.setVisibility(View.VISIBLE);
                    loadingBgSetProfile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    progressBarSetProfile.setVisibility(View.GONE);
                    loadingBgSetProfile.setVisibility(View.GONE);
                    Intent i = new Intent(SetProfile.this,HomeScreen.class);
                    startActivity(i);
                    finish();

                }

            }
        });

    }

    private void sendDataForNewUser() {

        sendDataToRealtimeDatabase();

    }

    private void sendDataToRealtimeDatabase() {

        name = setUsernameEdt.getText().toString().trim();
        UserProfile userProfile = new UserProfile(name, auth.getUid());
        databaseReference.setValue(userProfile);
        Toast.makeText(getApplicationContext(),"Profile added",Toast.LENGTH_SHORT).show();
        sendImageToStorage();

    }

    private void sendImageToStorage() {
        StorageReference imgref = storageReference.child("Images").child(auth.getUid()).child("Profile Pic");
        //Image compression
        Bitmap bitmap =null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
        }catch (IOException e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //uploading image to storage
        UploadTask uploadTask = imgref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();
                        sendDatatoCloudFirestore();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to get URI",Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(),"image upload successful",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image not uploaded",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendDatatoCloudFirestore() {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(auth.getUid());
        Map<String,Object> userData = new HashMap<>();
        userData.put("name",name);
        userData.put("image",imageUriAccessToken);
        userData.put("uid",auth.getUid());
        userData.put("status","online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Data uploaded to cloud",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Internal Error",Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == profilePic && resultCode==RESULT_OK){
            assert data != null;
            imagePath = data.getData();
            setProfilePic.setImageURI(imagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}