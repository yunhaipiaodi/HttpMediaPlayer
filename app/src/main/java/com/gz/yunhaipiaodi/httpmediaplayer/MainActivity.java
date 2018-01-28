package com.gz.yunhaipiaodi.httpmediaplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    BasePlayer basePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        basePlayer = findViewById(R.id.player);

        basePlayer.setVideoInfo("http://vodpapcn8vk.vod.126.net/vodpapcn8vk/3cb2ea99-dc28-46a4-aca8-86c8d262dbab.mp4",
                "http://tv.gzhaochuan.com/Public/Upload/Article/image/ede4bc9933c6b1b21225c1285416ee4a.0x384x216.jpg",
                "4:10",
                "你追的上我的残影吗？？新版本蓝猫25级天赋！"
        );
    }

    @Override
    protected void onDestroy(){
        basePlayer.onDestroy();
        super.onDestroy();
    }
}
