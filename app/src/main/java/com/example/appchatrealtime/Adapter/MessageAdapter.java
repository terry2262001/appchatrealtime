package com.example.appchatrealtime.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appchatrealtime.MessageActivity;
import com.example.appchatrealtime.Model.Chat;
import com.example.appchatrealtime.Model.User;
import com.example.appchatrealtime.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageURl;
    private UserAdapter.onClickItemListener listener;
    FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageURl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageURl = imageURl;
    }


    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.MessageVH(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.MessageVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {
        Chat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
        if (imageURl.equals("default")) {
            holder.img_profile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageURl).into(holder.img_profile);
        }
        if(position == mChat.size()-1){
            if(chat.isIsseen()){ // check for last message
                holder.tvSeen.setText("Seen");
            }else{
                holder.tvSeen.setText("Delivered");
            }
        }else {
            holder.tvSeen.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }

    class MessageVH extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView img_profile;
        public TextView  tvSeen;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            img_profile = itemView.findViewById(R.id.img_profile);
            tvSeen = itemView.findViewById(R.id.tvSeen);
        }
    }

    public interface onClickItemListener {
        public void onClickItem();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }
}
