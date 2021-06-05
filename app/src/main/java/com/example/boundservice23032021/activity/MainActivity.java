package com.example.boundservice23032021.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.boundservice23032021.R;
import com.example.boundservice23032021.adapter.SongAdapter;
import com.example.boundservice23032021.constant.Constant;
import com.example.boundservice23032021.interfaces.OnChangeTimeCurrent;
import com.example.boundservice23032021.interfaces.OnItemClick;
import com.example.boundservice23032021.model.Song;
import com.example.boundservice23032021.service.MyService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRcvSong;
    List<Song> mListSong;
    SongAdapter mSongAdapter;
    String[] arrNameSong = {"chieuthuhoabongnang", "codocvuong", "hoahaiduong", "hoyeuaimatroi", "tetdongday"};
    MediaMetadataRetriever mMetaRetriever;
    TextView mTvTimeCurrent,mTvToTalTime;
    SeekBar mSkSong;
    ImageButton mImgPre,mImgPlay,mImgNext;
    MyService mMyService;
    int mIndexSong = -1;
    boolean mPlaying;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRcvSong = findViewById(R.id.recyclerViewSong);
        mTvTimeCurrent = findViewById(R.id.textViewTimeCurrent);
        mTvToTalTime = findViewById(R.id.textViewTotalTime);
        mSkSong = findViewById(R.id.seekBarSong);
        mImgPre = findViewById(R.id.imageButtonPre);
        mImgPlay = findViewById(R.id.imageButtonPlay);
        mImgNext = findViewById(R.id.imageButtonNext);
        mListSong = new ArrayList<>();

        mMyService = new MyService();
        mListSong.add(new Song("Chiều thu họa bóng nàng", R.raw.chieuthuhoabongnang, 0));
        mListSong.add(new Song("Cô độc vương", R.raw.codocvuong, 0));
        mListSong.add(new Song("Hoa hải đường", R.raw.hoahaiduong, 0));
        mListSong.add(new Song("Họ yêu ai mất rồi", R.raw.hoyeuaimatroi, 0));
        mListSong.add(new Song("Tết đong đầy", R.raw.tetdongday, 0));

        for (int i = 0; i < arrNameSong.length; i++) {
            Uri mediaPath = Uri.parse("android.resource://com.example.boundservice23032021/raw/" + arrNameSong[i]);
            mMetaRetriever = new MediaMetadataRetriever();
            mMetaRetriever.setDataSource(this, mediaPath);
            long duration = Long.parseLong(mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mListSong.get(i).setDuration(duration);
        }

        mSongAdapter = new SongAdapter(mListSong);


        mRcvSong.setAdapter(mSongAdapter);

        mSongAdapter.setOnItemClick(new OnItemClick() {
            @Override
            public void onClick(int position) {
                Song song = mListSong.get(position);
                long minus = (song.getDuration() / 60000);
                long second = (song.getDuration() % 60000) / 1000;
                mTvToTalTime.setText("0" + minus + " : " + (second >= 10 ? second : "0" + second));
                mSkSong.setMax((int) song.getDuration());
                mIndexSong = position;

                Intent intent = new Intent(MainActivity.this, MyService.class);
                intent.putExtra("objectsong",song);
                startService(intent);
            }
        });

        mImgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlaying){
                    mPlaying = false;
                    mImgPlay.setImageResource(R.drawable.ic_play);
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    intent.putExtra("actionmusic", Constant.ACTION_PAUSE);
                    startService(intent);
                }else {
                    mPlaying = true;
                    mImgPlay.setImageResource(R.drawable.ic_pause);
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    intent.putExtra("actionmusic", Constant.ACTION_RESUME);
                    startService(intent);
                }
            }
        });


        mImgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIndexSong += 1;
                if (mIndexSong >= mListSong.size()){
                    mIndexSong = 0;
                }
                Song song = mListSong.get(mIndexSong);
                long minus = (song.getDuration() / 60000);
                long second = (song.getDuration() % 60000) / 1000;
                mTvToTalTime.setText("0" + minus + " : " + (second >= 10 ? second : "0" + second));
                mSkSong.setMax((int) song.getDuration());

                Intent intent = new Intent(MainActivity.this, MyService.class);
                intent.putExtra("objectsong",mListSong.get(mIndexSong));
                startService(intent);
            }
        });
        mImgPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIndexSong -= 1;
                if (mIndexSong < 0){
                    mIndexSong = mListSong.size() - 1;
                }
                Song song = mListSong.get(mIndexSong);
                long minus = (song.getDuration() / 60000);
                long second = (song.getDuration() % 60000) / 1000;
                mTvToTalTime.setText("0" + minus + " : " + (second >= 10 ? second : "0" + second));
                mSkSong.setMax((int) song.getDuration());
                Intent intent = new Intent(MainActivity.this, MyService.class);
                intent.putExtra("objectsong",mListSong.get(mIndexSong));
                startService(intent);
            }
        });
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.LocalIBinder localIBinder = (MyService.LocalIBinder) iBinder;
            mMyService = localIBinder.getService();
            mMyService.setOnChangeTimeCurrent(new OnChangeTimeCurrent() {
                @Override
                public void onChangeTime(long time) {
                    long minus = (time / 60000);
                    long second = (time % 60000) / 1000;
                    mTvTimeCurrent.setText("0" + minus + " : " + (second >= 10 ? second : "0" + second));
                    mSkSong.setProgress((int) time);
                }

                @Override
                public void isPlaying(boolean isPlaying) {
                    mPlaying = isPlaying;
                    if (isPlaying){
                        mImgPlay.setImageResource(R.drawable.ic_pause);
                    }else{
                        mImgPlay.setImageResource(R.drawable.ic_play);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onStart() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(connection);
        super.onStop();
    }
}