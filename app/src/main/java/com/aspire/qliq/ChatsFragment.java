package com.aspire.qliq;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth auth;
    RecyclerView recyclerView;
    CircleImageView userPicRecycler;
    FirestoreRecyclerAdapter<FirebaseRecyclerModel,NoteViewHolder> chatAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chats, container, false);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_view);

        //Query query = firebaseFirestore.collection("Users");
        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid",auth.getUid());
        FirestoreRecyclerOptions<FirebaseRecyclerModel> allusername = new FirestoreRecyclerOptions.Builder<FirebaseRecyclerModel>().setQuery(query, FirebaseRecyclerModel.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<FirebaseRecyclerModel, NoteViewHolder>(allusername) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull FirebaseRecyclerModel model) {

                holder.usersNameTv.setText(model.name);
                String uri = model.getImage();
                Picasso.get().load(uri).into(userPicRecycler);
                if(model.getStatus().equals("Online")){
                    holder.usersStatusTv.setText(model.getStatus());
                    //holder.usersStatusTv.setTextColor(Color.GREEN);
                }else{
                    holder.usersStatusTv.setText(model.getStatus());
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(),ChatRoom.class);
                        i.putExtra("name",model.getName());
                        i.putExtra("receiveruid",model.getUid());
                        i.putExtra("imgUri",model.getImage());
                        startActivity(i);
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_recycler_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatAdapter);
        return view;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        private TextView usersNameTv,usersStatusTv;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            usersNameTv = itemView.findViewById(R.id.users_name_tv);
            usersStatusTv = itemView.findViewById(R.id.users_status_tv);
            userPicRecycler = itemView.findViewById(R.id.users_pic_recycler);

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter != null){
            chatAdapter.stopListening();
        }
    }
}