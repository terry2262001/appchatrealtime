package com.example.appchatrealtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appchatrealtime.Adapter.MessageAdapter;
import com.example.appchatrealtime.Fragment.APIService;
import com.example.appchatrealtime.Model.Chat;
import com.example.appchatrealtime.Model.User;
import com.example.appchatrealtime.Notification.Client;
import com.example.appchatrealtime.Notification.Data;
import com.example.appchatrealtime.Notification.MyResponse;
import com.example.appchatrealtime.Notification.Sender;
import com.example.appchatrealtime.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    //final Context  context = (MainActivity)getApplication().getApplicationContext();
    final Context context = MessageActivity.this;


    CircleImageView img_profile;
    TextView tvUsername;
    FirebaseUser fUser;
    DatabaseReference reference;
    //
    Intent intent;


    ImageButton btSend;
    EditText etSend;

    MessageAdapter messageAdapter;
    List<Chat>mChat;
    RecyclerView rvChatBox;

    ValueEventListener seenListener;
    String userid ;

    APIService apiService;
    boolean notify = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        //
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        //
        rvChatBox = findViewById(R.id.rvChatBox);
        rvChatBox.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rvChatBox.setLayoutManager(linearLayoutManager);


        //
        img_profile = findViewById(R.id.img_profile);
        tvUsername = findViewById(R.id.tvUsername);
        btSend = findViewById(R.id.btSend);
        etSend = findViewById(R.id.etSend);
        //


        intent = getIntent();
        userid= intent.getStringExtra("userid");
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;
                String msg = etSend.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fUser.getUid(),userid,msg);
                }else{
                    Toast.makeText(MessageActivity.this, "You can't send empty message ", Toast.LENGTH_SHORT).show();
                }
                etSend.setText("");
                etSend.setHint("Aa");
            }
        });



        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvUsername.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    img_profile.setImageResource(R.mipmap.ic_launcher);
                }else{
                   // Glide.with(MessageActivity.this).load(user.getImageURL()).into(img_profile);

                    //c1
//                    if(isValidContextForGlide(context)){
//                        Glide.with(context).load(user.getImageURL()).into(img_profile);
//                    }
                    //c2
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(img_profile);

                }
                readMessage(fUser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(userid);
    }
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender,final String receiver,String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        reference.child("Chats").push().setValue(hashMap);

        //add user to chats fragment
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fUser.getUid())
                .child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                        chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final  String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("User").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if(notify){
                    sendNotification(receiver,user.getUsername(),msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendNotification(String receiver, String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query= tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(),R.mipmap.ic_launcher,username+": "+msg,"New Message",userid);
                    Sender sender = new Sender(data,token.getToken());
                    System.out.println("tho_1_sender_token: "+ token.getToken());
                   // Sender sender = new Sender(data,"d2iT75ucSyuvmn5PeO8ajK:APA91bGG_PTIaBIUPm_o-QzrbTcpCigOZKlDqDk3HvBK1jyVWT-LmaW6tT6POvXbZOCjUY-QDsPHrK-ow9gbbRXcBH2f1QbEf0jGtmTwD4F6y8KYD65yKsRCOm0X-Oc_tpMstDLTOAtv");
                    System.out.println("tho_1_sender"+sender.toString()+"data:====="+data.toString());
                    System.out.println("tho_2_sender"+sender.toString()+"data:====="+data.getBody());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1 ){

                                            Toast.makeText(MessageActivity.this, "Failed can not notification", Toast.LENGTH_SHORT).show();

                                       }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessage(final String myId,final String userId,final String imageURL){
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myId) && chat.getSender().equals(userId)||
                    chat.getReceiver().equals(userId) && chat.getSender().equals(myId)){
                        mChat.add(chat);

                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this,mChat,imageURL);
                    rvChatBox.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void  currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentUser", userid);
        editor.apply();

    }
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("User").child(fUser.getUid());

        HashMap<String,Object> hashMap =  new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

}