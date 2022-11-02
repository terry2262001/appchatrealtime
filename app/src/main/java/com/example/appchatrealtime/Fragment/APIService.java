package com.example.appchatrealtime.Fragment;

import com.example.appchatrealtime.Notification.MyResponse;
import com.example.appchatrealtime.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({"Content-Type:application/json",
            "Authorization:key=AAAAjpDyIEY:APA91bHWKLQnwveNcKDfs2TF-dbaW47-nUg_vzE-2IUZhvsNeGKzYMkxSNOUmA08NLgzOvMIzazde2JC-B4jbCdCh7mKYt64urVZ1FLYU53FQh_2x9Q1-iwQuwzsgVgWKvyNS_7ptagH"})
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

