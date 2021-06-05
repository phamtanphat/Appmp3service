package com.example.boundservice23032021.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.boundservice23032021.R;
import com.example.boundservice23032021.activity.MainActivity;
import com.example.boundservice23032021.constant.Constant;
import com.example.boundservice23032021.interfaces.OnChangeTimeCurrent;
import com.example.boundservice23032021.model.Song;


public class MyService extends Service implements Runnable{

    String CHANNEL_ID = "CHANNEL_ID";
    NotificationManager mNotificationManager;
    MediaPlayer mMediaPlayer;
    boolean mIsPlaying;
    Song mSong;
    OnChangeTimeCurrent mOnChangeTimeCurrent;

    Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalIBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Song song = (Song) intent.getSerializableExtra("objectsong");
        int action = intent.getIntExtra("actionmusic", 0);

        if (song != null) {
            mSong = song;
            mIsPlaying = true;
            startMusic(mSong);
            createNotification(this, mSong.getTitle());
        }

        if (action > 0) {
            handleActionMusic(action);
        }
        return START_NOT_STICKY;
    }

    private void startMusic(Song song) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), song.getData());
        }
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), song.getData());
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mIsPlaying = true;
                perSecondUpdateTime();
            }
        });
    }

    private void handleActionMusic(int action) {
        switch (action) {
            case Constant.ACTION_PAUSE:
                pauseMusic();
                break;
            case Constant.ACTION_RESUME:
                resumeMusic();
                break;
            case Constant.ACTION_CLOSE:
                if (mHandler != null){
                    mHandler.removeCallbacks(this);
                }
                stopSelf();
                break;
        }
    }

    private void pauseMusic() {
        if (mMediaPlayer != null && mIsPlaying) {
            mMediaPlayer.pause();
            mIsPlaying = false;
            createNotification(this, mSong.getTitle());
        }
    }

    private void resumeMusic() {
        if (mMediaPlayer != null && !mIsPlaying) {
            mMediaPlayer.start();
            mIsPlaying = true;
            createNotification(this, mSong.getTitle());
            perSecondUpdateTime();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void createNotification(Context context, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification_music);
        remoteViews.setTextViewText(R.id.textViewNotificationTitleSong, title);
        remoteViews.setImageViewResource(R.id.imagePlayOrPause, R.drawable.ic_pause_black);

        if (mIsPlaying) {
            remoteViews.setOnClickPendingIntent(R.id.imagePlayOrPause, getPendingIntent(context, Constant.ACTION_PAUSE));
            remoteViews.setImageViewResource(R.id.imagePlayOrPause, R.drawable.ic_pause_black);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.imagePlayOrPause, getPendingIntent(context, Constant.ACTION_RESUME));
            remoteViews.setImageViewResource(R.id.imagePlayOrPause, R.drawable.ic_play_black);
        }

        remoteViews.setOnClickPendingIntent(R.id.imageClose, getPendingIntent(context, Constant.ACTION_CLOSE));


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentIntent(pendingIntent);
        builder.setCustomContentView(remoteViews);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(null);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(null, null);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        startForeground(1, builder.build());
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra("actionmusic", action);
        return PendingIntent.getService(context, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setOnChangeTimeCurrent(OnChangeTimeCurrent onChangeTimeCurrent) {
        mOnChangeTimeCurrent = onChangeTimeCurrent;
    }

    private void perSecondUpdateTime(){
        if (mOnChangeTimeCurrent != null){
            if (mIsPlaying){
                mHandler.postDelayed(this,1000);
                mOnChangeTimeCurrent.isPlaying(true);
            }else{
                mOnChangeTimeCurrent.isPlaying(false);
            }
        }
    }

    @Override
    public void run() {
        if (mMediaPlayer != null){
            if (mMediaPlayer.getCurrentPosition() <= mMediaPlayer.getDuration()){
                mOnChangeTimeCurrent.onChangeTime(mMediaPlayer.getCurrentPosition());
                mHandler.postDelayed(this::run ,1000);
            }
        }
    }


    public class LocalIBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }
}
