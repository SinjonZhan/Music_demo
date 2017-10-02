package com.soleil.music_demo.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.soleil.music_demo.conf.Constants;
import com.soleil.music_demo.utils.MediaUtil;

import java.util.Timer;
import java.util.TimerTask;

import static com.soleil.music_demo.conf.Constants.MSG_ONPREPARE;

/**
 * @作者 xinrong
 * @创建日期 2017/5/23 13:14
 */

public class MusicService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mplayer;
    private Messenger messenger;
    private Timer mTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {//多次启动只调用一次
        mplayer = new MediaPlayer();
        mplayer.setOnErrorListener(this);//设置资源出错
        mplayer.setOnPreparedListener(this);//准备操作
        mplayer.setOnCompletionListener(this);//完成操作
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//每次启动调用一次

        String option = intent.getStringExtra("option");
        if (messenger == null) {
            messenger = (Messenger) intent.getExtras().get("msg_onprepare");
        }
        if ("播放".equals(option)) {
            String path = intent.getStringExtra("path");
            play(path);
        } else if ("暂停".equals(option)) {
            pause();
        } else if ("继续".equals(option)) {
            continuePlay();
        } else if ("停止".equals(option)) {
            stop();
        } else if ("进度".equals(option)) {
            int progress = intent.getIntExtra("progress", -1);
            seekPlay(progress);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 进度播放
     *
     * @param progress
     */
    private void seekPlay(int progress) {
        if (mplayer != null && mplayer.isPlaying()) {
            mplayer.seekTo(progress);
        }
    }


    @Override
    public void onDestroy() {//销毁
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


    /**---------------封装方法begin---------------**/

    /**
     * 播放音乐
     *
     * @param path
     */
    public void play(String path) {
        try {
            mplayer.reset();//idle
            mplayer.setDataSource(path);//设置歌曲路径
            mplayer.prepare();//准备
            mplayer.start();//开始播放
            MediaUtil.CURSTATE = Constants.STATE_PLAY;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mplayer != null && mplayer.isPlaying()) {
            mplayer.pause();
            MediaUtil.CURSTATE = Constants.STATE_PAUSE;
        }


    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        if (mplayer != null && !mplayer.isPlaying()) {
            mplayer.start();
            MediaUtil.CURSTATE = Constants.STATE_PLAY;
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mplayer != null) {
            mplayer.stop();
            MediaUtil.CURSTATE = Constants.STATE_STOP;
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }
    }


    /**---------------封装方法end---------------**/
    /**
     * ---------------监听相关回调方法---------------
     **/
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "亲,资源错误", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int currentPosition = mplayer.getCurrentPosition();
                int totalDuration = mplayer.getDuration();
                Message msg = Message.obtain();
                msg.what = MSG_ONPREPARE;
                msg.arg1 = currentPosition;//当前歌曲时间
                msg.arg2 = totalDuration;//总时间
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Message msg = Message.obtain();
        msg.what = Constants.MSG_ONCOMPLETION;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ---------------音乐焦点处理---------------
     **/
    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mplayer.start();
                mplayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mplayer.isPlaying()) {
                    mplayer.stop();
                }
                mplayer.release();
                mplayer = null;

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mplayer.isPlaying()) {
                    mplayer.pause();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mplayer.isPlaying()) {
                    mplayer.setVolume(0.1f, 0.1f);
                }
                break;

        }
    }
}
