package com.gz.yunhaipiaodi.httpmediaplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yunhaipiaodi on 2018/1/27.
 */

public class BasePlayer extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnInfoListener,
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener{

    /*
    * 参数集
    * */

    // 计时器
    private Timer timer_video_time;
    private TimerTask task_video_timer;

    //UI组件
    TextureView playerTexture;

    FrameLayout beforePlayShow;             //在播放前显示的界面
    ImageView videoCoverImage;              //视频预览封面
    TextView videoNameBeforePlay;           //在播放前显示的标题
    ImageButton firstPlayBtn;               //在播放前显示的播放按钮
    TextView videoDurationBeforePlay;       //在播放前显示的视频时长

    LinearLayout topBar;                        //顶部栏
    TextView videoNamePlay;                     //开始播放后显示的标题栏

    ImageButton centerPlayBtn;                //在暂停过程中显示的播放按钮

    LinearLayout bottomBar;                        //顶部栏
    ImageButton playOrPauseBtn;                   //“开始/暂停”键
    TextView playCurrentTime;                     //播放进度
    SeekBar seekBar;                               //播放进度条
    TextView playDuration;                         //播放后显示的视频时长

    FrameLayout replayLayout;                       //重播界面
    ImageButton replayBtn;                          //重播按钮

    ProgressWheel loadingPrgress;                     //加载提示框

    //类一般参数
    Context mContext;
    MediaPlayer mediaPlayer;
    String TAG = "HttpMediaPlayer";

    String videoUrl = "";
    String videoCoverUrl = "";
    String videoDuration = "";
    String videoTitle = "";
    /*
    * 初始化view
    * */
    public BasePlayer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BasePlayer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BasePlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    /* *
    *
    * 设置视频信息
    *
    * */

    //设置视频链接
    public void setVideoUrl(@Nullable String url){
        this.videoUrl = url;
    }

    //设置视频封面图片
    public void setVideoCoverUrl(@Nullable String url){
        this.videoCoverUrl = url;
        if(videoCoverImage != null){
            Glide.with(mContext)
                    .load(url)
                    .into(videoCoverImage);
        }
    }

    //设置视频时长显示
    public void setVideoDuration (String duration){
        this.videoDuration = duration;
        videoDurationBeforePlay.setText(duration);
    }

    //设置视频标题
    public void setVideoTitle(String title){
        this.videoTitle = title;
        videoNameBeforePlay.setText(title);
        videoNamePlay.setText(title);
    }

    //设置视频信息
    public void setVideoInfo(@Nullable String videoUrl,@Nullable String coverUrl,String duration,String title){
        setVideoUrl(videoUrl);
        setVideoCoverUrl(coverUrl);
        setVideoDuration(duration);
        setVideoTitle(title);
    }

     /* *
    *
    * 初始化视图
    *
    * */

    //可供子视图继承自定义布局
    protected View setPlayLayout(){
        return inflate(mContext, R.layout.video_player, (ViewGroup) getRootView());
    }

    private void init(){
        View view = setPlayLayout();
        initComponent(view);
    }

    //初始化组件
    private void initComponent(View view){
        playerTexture = (TextureView) view.findViewById(R.id.player_textureView);
        //播放前
        beforePlayShow = (FrameLayout) view.findViewById(R.id.before_play_show);
        videoCoverImage = (ImageView)view.findViewById(R.id.video_cover);
        videoNameBeforePlay = (TextView)view.findViewById(R.id.video_name_before_play);
        firstPlayBtn = (ImageButton)view.findViewById(R.id.first_play_btn);
        videoDurationBeforePlay = (TextView)view.findViewById(R.id.video_duration_before_play);

        centerPlayBtn = (ImageButton)view.findViewById(R.id.center_play_btn);

        //顶部栏
        topBar = (LinearLayout)view.findViewById(R.id.top_bar);
        videoNamePlay = (TextView)view.findViewById(R.id.video_name_play);

        //底部栏
        bottomBar = (LinearLayout)view.findViewById(R.id.bottom_bar);
        playOrPauseBtn = (ImageButton)view.findViewById(R.id.play_or_pause_btn);
        playCurrentTime = (TextView)view.findViewById(R.id.current_time);
        seekBar = (SeekBar)view.findViewById(R.id.seek_bar);
        playDuration = (TextView)view.findViewById(R.id.duration);

        //重播
        replayLayout = (FrameLayout)view.findViewById(R.id.replay_container);
        replayBtn = (ImageButton)view.findViewById(R.id.replay_btn);

        loadingPrgress = (ProgressWheel)view.findViewById(R.id.load_progressBar);


        //添加组件事件
        firstPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playFirstTime();
            }
        });

        centerPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });

        replayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                replay();
            }
        });

        //监听texture是否可用
        playerTexture.setSurfaceTextureListener(this);

        //点击播放texture显示或隐藏上下栏
        playerTexture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHiddenBar();
            }
        });

        playOrPauseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });

        seekBar.setOnSeekBarChangeListener(this);
    }

    //首次播放
    private void playFirstTime(){
        startToPlay();
        beforePlayShow.setVisibility(GONE);         //播放前界面隐藏
        loadingPrgress.setVisibility(VISIBLE);      //显示加载框
    }

    //重新播放视频
    private void replay(){
        replayLayout.setVisibility(GONE);
        startToPlay();
    }

    //显示或隐藏上下栏
    private void showOrHiddenBar(){
        if(bottomBar.getVisibility() == GONE){
            showBar();
        }else{
            hiddenBar();
        }
    }

    private void showBar(){
        topBar.setVisibility(VISIBLE);
        bottomBar.setVisibility(VISIBLE);
        initUpdatePlayTimeTask();
    }

    private void hiddenBar(){
        topBar.setVisibility(GONE);
        bottomBar.setVisibility(GONE);
        destroyUpdatePlayTimeTask();
    }

    private void initUpdatePlayTimeTask(){
        //启动更新视频进度任务
        timer_video_time = new Timer();
        task_video_timer = new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer == null){
                    return;
                }

                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                String currentPlayTime = convertLongTimeToStr(mediaPlayer.getCurrentPosition());        //获得当前播放时间
                                playCurrentTime.setText(currentPlayTime);

                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                );
            }
        };
        timer_video_time.schedule(task_video_timer,0,1000);

    }

    private void destroyUpdatePlayTimeTask(){
        if(task_video_timer != null){
            task_video_timer.cancel();
        }
    }

    public  String convertLongTimeToStr(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        if (hour > 0) {
            return strHour + ":" + strMinute + ":" + strSecond;
        } else {
            return strMinute + ":" + strSecond;
        }
    }

    private void playOrPause(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playOrPauseBtn.setImageResource(R.drawable.play);
            centerPlayBtn.setVisibility(VISIBLE);
        }else{
            mediaPlayer.start();
            playOrPauseBtn.setImageResource(R.drawable.pause);
            centerPlayBtn.setVisibility(GONE);
        }
    }

    private void startToPlay(){
        try{
            if(mediaPlayer != null){
                mediaPlayer.reset();
                mediaPlayer.setDataSource(videoUrl);
                mediaPlayer.prepareAsync();
            }else{
                Log.i(TAG,"播放器还未准备好");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if(percent>=0 && percent<=100){
            int secondProgress = 0;
            secondProgress = mp.getDuration()*percent/100;
            seekBar.setSecondaryProgress(secondProgress);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        hiddenBar();
        replayLayout.setVisibility(VISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                loadingPrgress.setVisibility(VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                loadingPrgress.setVisibility(GONE);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        loadingPrgress.setVisibility(GONE);      //隐藏加载框
        mediaPlayer.start();                      //开始播放
        showOrHiddenBar();                         //显示上下栏
        //三秒后影藏上下栏
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showOrHiddenBar();
            }
        },3000);

        //设置seekBar时长
        seekBar.setMax(mp.getDuration());

        //设置播放时的视频时长
        playDuration.setText(convertLongTimeToStr(mp.getDuration()));
    }


    private SurfaceTexture mSurfaceTexture;
    private Surface surface;
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if(mSurfaceTexture  == null){
            mSurfaceTexture = surfaceTexture;
            surface = new Surface(surfaceTexture);
        }

        initMediaPlayer();
        mediaPlayer.setSurface(surface);
    }

    //初始化MediaPlayer
    private void initMediaPlayer(){
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置MediaPlayer的监听
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
    }

    //销毁Player
    public void onDestroy(){
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        surface = null;
        destroyUpdatePlayTimeTask();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            int maxCanSeekTo = seekBar.getMax() - 5*1000;
            if(seekBar.getProgress() < maxCanSeekTo){
                mediaPlayer.seekTo(seekBar.getProgress());
            }else{
                //不能拖到尽头
                mediaPlayer.seekTo(maxCanSeekTo);
            }
        }
    }

}
