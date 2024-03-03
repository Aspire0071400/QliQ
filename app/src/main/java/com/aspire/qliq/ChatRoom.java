package com.aspire.qliq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatRoom extends AppCompatActivity {

    EditText chatroomMsgEdt;
    ImageButton chatroomMsgSendIb,chatroomBackIb;
    Toolbar chatroomToolbar;
    ImageView chatroomContactPicIv;
    TextView chatroomContactNameTv;
    private String currentMessage;
    Intent intent;
    String receiverName,senderName,receiverUID,senderUID;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    String senderRoom,receiverRoom;
    RecyclerView chatroomRecycler;
    String currentTime;
    Calendar calendar;
    SimpleDateFormat dateFormat;
    MessagesAdapter msgAdapter;
    ArrayList<MessagesModel> msgArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.main_color));

        chatroomBackIb = findViewById(R.id.chatroom_back_ib);
        chatroomRecycler = findViewById(R.id.chatroom_recycler);
        chatroomToolbar = findViewById(R.id.chatroom_toolbar);
        chatroomMsgEdt = findViewById(R.id.chatroom_msg_edt);
        chatroomContactNameTv = findViewById(R.id.chatroom_contact_name_tv);
        chatroomContactPicIv = findViewById(R.id.chatroom_contact_pic_iv);
        chatroomMsgSendIb = findViewById(R.id.chatroom_send_msg_ib);
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        msgArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatroomRecycler.setLayoutManager(linearLayoutManager);
        msgAdapter = new MessagesAdapter(ChatRoom.this,msgArrayList);
        chatroomRecycler.setAdapter(msgAdapter);

        intent = getIntent();
        setSupportActionBar(chatroomToolbar);

        senderUID = auth.getUid();
        receiverUID = getIntent().getStringExtra("receiveruid");
        receiverName = getIntent().getStringExtra("name");

        senderRoom = senderUID+receiverUID;
        receiverRoom = receiverUID+senderUID;


        chatroomContactNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"user Profile , fetching Data",Toast.LENGTH_SHORT).show();
            }
        });

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("hh:mm a");



        DatabaseReference databaseReference =   firebaseDatabase.getReference().child("chats")
                .child(senderRoom).child("messages");

        msgAdapter = new MessagesAdapter(ChatRoom.this,msgArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                msgArrayList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){

                    MessagesModel msg = snapshot1.getValue(MessagesModel.class);
                    msgArrayList.add(msg);
                }
                msgAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        chatroomBackIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        chatroomContactNameTv.setText(receiverName);
        String receiverImgURI = getIntent().getStringExtra("imgUri");
        if(receiverImgURI.isEmpty()){
            Toast.makeText(getApplicationContext(),"This user doesn't have a Pic",Toast.LENGTH_SHORT).show();
        }else {
            Picasso.get().load(receiverImgURI).into(chatroomContactPicIv);
        }



        chatroomMsgSendIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                currentMessage = chatroomMsgEdt.getText().toString();
                if(currentMessage.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Empty Field",Toast.LENGTH_SHORT).show();
                }else {

                    Date date =new Date();
                    currentTime = dateFormat.format(calendar.getTime());

                    MessagesModel messagesModel = new MessagesModel(currentMessage,auth.getUid(),currentTime, date.getTime());

                    firebaseDatabase.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messagesModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    firebaseDatabase.getReference().child("chats")
                                            .child(receiverRoom).child("messages").push()
                                            .setValue(messagesModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //sent receipt required but not added.
                                                    //must provide.
                                                }
                                            });
                                }
                            });

                    chatroomMsgEdt.setText(null);
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        msgAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(msgAdapter != null){
            msgAdapter.notifyDataSetChanged();
        }
    }

}