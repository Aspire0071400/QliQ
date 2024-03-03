package com.aspire.qliq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MessagesModel> mesArrayList;
    int ITEM_SEND = 1;
    int ITEM_RECEIVED = 2;

    public MessagesAdapter(Context context, ArrayList<MessagesModel> mesArrayList) {
        this.context = context;
        this.mesArrayList = mesArrayList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND){

            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat_layout,parent,false);
            return new SenderViewHolder(view);

        }else {

            View view = LayoutInflater.from(context).inflate(R.layout.receiver_chat_layout,parent,false);
            return new ReceiverViewHolder(view);

        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessagesModel messagesModel = mesArrayList.get(position);
        if(holder.getClass()==SenderViewHolder.class){

            SenderViewHolder sender = (SenderViewHolder)holder;
            sender.senderTextDisplay.setText(messagesModel.getMsg());
            sender.senderTextSendTime.setText(messagesModel.getCurrentTime());

        }else {

            ReceiverViewHolder receiver = (ReceiverViewHolder)holder;
            receiver.receiverTextDisplay.setText(messagesModel.getMsg());
            receiver.receiverTextSendTime.setText(messagesModel.getCurrentTime());

        }
    }


    @Override
    public int getItemViewType(int position) {

        MessagesModel messagesModel = mesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messagesModel.getSenderId())){
            return ITEM_SEND;
        }else {
            return ITEM_RECEIVED;
        }

    }


    @Override
    public int getItemCount() {
        return mesArrayList.size();
    }






    class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView senderTextDisplay,senderTextSendTime;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderTextDisplay = itemView.findViewById(R.id.sender_text_display);
            senderTextSendTime = itemView.findViewById(R.id.sender_text_send_time);



        }
    }


    class ReceiverViewHolder extends RecyclerView.ViewHolder{

        TextView receiverTextDisplay,receiverTextSendTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverTextDisplay = itemView.findViewById(R.id.receiver_text_display);
            receiverTextSendTime = itemView.findViewById(R.id.receiver_text_send_time);

        }
    }


}
