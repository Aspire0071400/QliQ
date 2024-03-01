package com.aspire.qliq;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {

    private ImageButton updateProfileBackBtn;
    private CircleImageView updateProfilePicTv;
    private EditText updateUsernameEdt;
    private Button updateProfileBtn;
    private ProgressBar progressbarUpdateProfile;
    private View loadingBgUpdateProfile;
    private String imageUriAccessToken;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private Toolbar updateProfileToolbar;
    private Uri imgPath;
    Intent i;
    private static int PickImage = 123;
    String newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);


        updateProfileBtn = findViewById(R.id.update_profile_btn);
        updateProfileBackBtn = findViewById(R.id.update_profile_back_btn);
        updateProfileToolbar = findViewById(R.id.update_profile_toolbar);
        updateUsernameEdt = findViewById(R.id.update_username_edt);
        updateProfilePicTv = findViewById(R.id.update_profile_pic_iv);
        loadingBgUpdateProfile = findViewById(R.id.loadingbg_update_profile);
        progressbarUpdateProfile = findViewById(R.id.progressBar_update_profile);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        i = getIntent();

        setSupportActionBar(updateProfileToolbar);

        updateProfileBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateUsernameEdt.setText(i.getStringExtra("name"));

        DatabaseReference databaseReference = firebaseDatabase.getReference(auth.getUid());
        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newName = updateUsernameEdt.getText().toString();
                if(newName.isEmpty()){
                    Toast.makeText(UpdateProfile.this, "Name is Empty", Toast.LENGTH_SHORT).show();
                }else if(imgPath!=null){

                    progressbarUpdateProfile.setVisibility(View.VISIBLE);
                    loadingBgUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName,auth.getUid());
                    databaseReference.setValue(userProfile);

                    updateImageTOStorage();
                    Toast.makeText(getApplicationContext(),"Updated",Toast.LENGTH_SHORT).show();
                    progressbarUpdateProfile.setVisibility(View.GONE);
                    loadingBgUpdateProfile.setVisibility(View.GONE);
                    Intent i = new Intent(UpdateProfile.this, HomeScreen.class);
                    startActivity(i);
                    finish();

                }else {

                    progressbarUpdateProfile.setVisibility(View.VISIBLE);
                    loadingBgUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName,auth.getUid());
                    databaseReference.setValue(userProfile);

                    updateNameOnCloudFirestore();
                    Toast.makeText(getApplicationContext(),"Updated",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(UpdateProfile.this, HomeScreen.class);
                    startActivity(i);
                    finish();

                }

            }
        });

        updateProfilePicTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i,PickImage);
            }
        });

        storageReference = firebaseStorage.getReference();
        storageReference.child("Images")
                .child(auth.getUid()).child("Profile Pic")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Picasso.get().load(uri).into(updateProfilePicTv);
            }
        });


    }

    private void updateNameOnCloudFirestore() {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(auth.getUid());
        Map<String,Object> userData = new HashMap<>();
        userData.put("name",newName);
        userData.put("image",imageUriAccessToken);
        userData.put("uid",auth.getUid());
        userData.put("status","online");
        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Data Update success",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Internal Error",Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void updateImageTOStorage() {

        StorageReference imgref = storageReference.child("Images").child(auth.getUid()).child("Profile Pic");
        //Image compression
        Bitmap bitmap =null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgPath);
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
                        updateNameOnCloudFirestore();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to get URI",Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(),"Image Updated",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image not Updated",Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PickImage && resultCode==RESULT_OK){
            assert data != null;
            imgPath = data.getData();
            updateProfilePicTv.setImageURI(imgPath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStop() {
        super.onStop();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(auth.getUid());

        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"User Offline",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(auth.getUid());

        documentReference.update("status","Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"User Online",Toast.LENGTH_SHORT).show();
            }
        });

    }

}