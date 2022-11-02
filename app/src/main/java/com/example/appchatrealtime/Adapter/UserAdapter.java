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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> {
    private Context context;
    private List<User> mUsers;
    private boolean isChat;
    private onClickItemListener listener;

    public UserAdapter(Context context, List<User> mUsers, boolean isChat, onClickItemListener listener) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;
        this.listener = listener;
    }

    String theLastMessage;


    public UserAdapter(Context context, List<User> mUsers, boolean isChat) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }


    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.UserVH(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UserVH holder, int position) {
        User user = mUsers.get(position);
        holder.tvUsername.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.img_profile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.img_profile);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
                ViewGroup  parent =  (ViewGroup) view.getParent();
                parent.removeView(view);



            }
        });
        if (isChat) {
            lastMessage(user.getId(), holder.tvLastMsg);

        }else{
            holder.tvLastMsg.setVisibility(View.GONE);
        }
        if (isChat) {
            if (user.getStatus().equals("online")) {

                holder.img_onl.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_onl.setVisibility(View.GONE);
            }
        } else {
            holder.img_off.setVisibility(View.GONE);
            holder.img_onl.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class UserVH extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public ImageView img_profile;
        public ImageView img_onl;
        public ImageView img_off;
        public TextView tvLastMsg;

        public UserVH(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            img_profile = itemView.findViewById(R.id.img_profile);
            img_off = itemView.findViewById(R.id.img_off);
            img_onl = itemView.findViewById(R.id.img_onl);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
        }
    }

    public interface onClickItemListener {
        void onClickItem();
    }

    public void lastMessage(String userid, TextView tvLastMsg) {
        theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(firebaseUser != null ){
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userid)) {
                            theLastMessage = chat.getMessage();
                        }
                    }

                }
                switch (theLastMessage) {
                    case "default":
                        tvLastMsg.setText("No Message");
                        break;
                    default:
                        tvLastMsg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
