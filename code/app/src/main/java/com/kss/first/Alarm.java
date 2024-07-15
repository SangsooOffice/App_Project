package com.kss.first;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Alarm extends BroadcastReceiver { //특정날짜의 알람을 설정하기 위해서 방송수신자를 사용한다.
    public Alarm(){ }

    NotificationManager manager;
    NotificationCompat.Builder builder;

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동한다.
    private static String CHANNEL_ID = "eat_food";  //채널 id를 지정한다.
    private static String CHANNEL_NAME = "eat_food"; //채널 이름을 지정한다


    @Override
    public void onReceive(Context context, Intent intent) {

        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){                 //오레오보다 같거나 큰 경우
            manager.createNotificationChannel(                              //알림채널 객체 만든다.
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);  //알림건축가 객체도 생성한다.
        } else {
            builder = new NotificationCompat.Builder(context);
        }


        //알림창 제목
        builder.setContentTitle("유통기한 임박 알림");
        //알림창 텍스트
        builder.setContentText("유통기한이 하루 남은 음식이 있습니다.");
        //알림창 아이콘
        builder.setSmallIcon(R.drawable.logo);


        Notification notification = builder.build();  //건축가에게 알림 객체 생성 할수 있도록
        manager.notify(1,notification);            //알림매니저에게 알림(Notify)을 요청

    }

}