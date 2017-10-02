package com.soleil.music_demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.soleil.music_demo.adapter.MyAdapter;
import com.soleil.music_demo.conf.Constants;
import com.soleil.music_demo.service.MusicService;
import com.soleil.music_demo.utils.LrcUtil;
import com.soleil.music_demo.utils.MediaUtil;
import com.soleil.music_demo.views.LyricShow;
import com.soleil.music_demo.views.ScrollableViewGroup;

import java.io.File;
import java.util.Random;

import static android.graphics.Color.WHITE;
import static com.soleil.music_demo.conf.Constants.MSG_ONPREPARE;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    MyBroadcastReceiver receiver = new MyBroadcastReceiver();
    private TextView mtv_curduration;
    private LyricShow mTv_lyricshow;
    private TextView mtv_minilrc;
    private TextView mtv_totalduration;
    private SeekBar msk_duration;
    private ImageView mIv_bottom_model;
    private ImageView mIv_bottom_play;
    private ListView mlv_list;
    private ScrollableViewGroup msvg_main;
    private LrcUtil mLrcUtil;
    private MyAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private MainActivity mInstance;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ONPREPARE:
                    int currentPosition = msg.arg1;
                    int totalDuration = msg.arg2;
                    mtv_curduration.setText(MediaUtil.duration2Str(currentPosition));
                    mtv_totalduration.setText(MediaUtil.duration2Str(totalDuration));
                    msk_duration.setMax(totalDuration);
                    msk_duration.setProgress(currentPosition);

                    if (mLrcUtil == null) {
                        mLrcUtil = new LrcUtil(mInstance);

                    }
                    File file = MediaUtil.getLrcFile(MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                    if (file != null) {
                        mLrcUtil.ReadLRC(file);
                        mLrcUtil.RefreshLRC(currentPosition);
                    }
                    //1.设置集合
                    mTv_lyricshow.SetTimeLrc(LrcUtil.lrclist);
                    mTv_lyricshow.SetNowPlayIndex(currentPosition);

                    break;
                case Constants.MSG_ONCOMPLETION:
                    if (MediaUtil.CURMODEL == Constants.MODEL_NORMAL) {
                        if (MediaUtil.CURPOSITION < MediaUtil.songList.size() - 1) {
                            changeColorWhite();
                            MediaUtil.CURPOSITION++;
                            changeColorGreen();
                            startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                        } else {
                            startMediaService("停止");
                        }
                    } else if (MediaUtil.CURMODEL == Constants.MODEL_RANDOM) {
                        Random random = new Random();
                        int position = random.nextInt(MediaUtil.songList.size());
                        changeColorWhite();
                        MediaUtil.CURPOSITION = position;
                        changeColorGreen();
                        startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());

                    } else if (MediaUtil.CURMODEL == Constants.MODEL_REPEAT) {
                        if (MediaUtil.CURPOSITION < MediaUtil.songList.size() - 1) {
                            changeColorWhite();
                            MediaUtil.CURPOSITION++;
                            changeColorGreen();
                            startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                        } else {
                            changeColorWhite();
                            MediaUtil.CURPOSITION = 0;
                            changeColorGreen();
                            startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                        }
                    } else if (MediaUtil.CURMODEL == Constants.MODEL_SINGLE) {
                        startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                    }
                    break;
                default:
                    break;

            }
        }
    };

    /*
    * 初始化界面
    * */
    private int[] topArr = {R.id.ib_top_play, R.id.ib_top_list, R.id.ib_top_lrc, R.id.ib_top_volumn};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInstance = this;
        initView();
        initData();
        initListener();

    }

    /*
    * 初始化监听
    * */

    private void initView() {
        /**
         * tv_minilrc
         tv_curduration
         tv_totalduration
         sk_duration
         lv_list
         tv_lrc
         */

        mtv_curduration = (TextView) findViewById(R.id.tv_curduration);
        mTv_lyricshow = (LyricShow) findViewById(R.id.tv_lrc);
        mtv_minilrc = (TextView) findViewById(R.id.tv_minilrc);
        mtv_totalduration = (TextView) findViewById(R.id.tv_totalduration);
        msk_duration = (SeekBar) findViewById(R.id.sk_duration);
        mlv_list = (ListView) findViewById(R.id.lv_list);
        msvg_main = (ScrollableViewGroup) findViewById(R.id.svg_main);
        mIv_bottom_model = (ImageView) findViewById(R.id.iv_bottom_model);
        mIv_bottom_play = (ImageView) findViewById(R.id.iv_bottom_play);

        //默认选中第一个
        findViewById(R.id.ib_top_play).setSelected(true);

    }

    /*
    * 初始化数据
    * */
    private void initData() {
        MediaUtil.initSongList(this);
        mAdapter = new MyAdapter(this);
        mlv_list.setAdapter(mAdapter);
        
    }

    private void initListener() {
        findViewById(R.id.ib_top_play).setOnClickListener(this);
        findViewById(R.id.ib_top_list).setOnClickListener(this);
        findViewById(R.id.ib_top_lrc).setOnClickListener(this);
        findViewById(R.id.ib_top_volumn).setOnClickListener(this);
        findViewById(R.id.ib_bottom_model).setOnClickListener(this);
        findViewById(R.id.ib_bottom_last).setOnClickListener(this);
        findViewById(R.id.ib_bottom_play).setOnClickListener(this);
        findViewById(R.id.ib_bottom_next).setOnClickListener(this);
        findViewById(R.id.ib_bottom_update).setOnClickListener(this);
        msvg_main.setOnCurrentViewChangedListener(new ScrollableViewGroup.OnCurrentViewChangedListener() {
            @Override
            public void onCurrentViewChanged(View view, int currentview) { /*                Log.d(TAG, "----------------" + currentview + "----------------");*/
                setTopSelected(topArr[currentview]);
            }
        });
        mlv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { /*修改当前position*/
                changeColorWhite();
                MediaUtil.CURPOSITION = position;
                changeColorGreen(); /*播放*/
                startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                mIv_bottom_play.setImageResource(R.drawable.appwidget_pause);
            }
        });

        msk_duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                msk_duration.setProgress(seekBar.getProgress());
                startMediaService("进度", seekBar.getProgress());

            }
        });
    }

    /**
     * 设置选中效果
     *
     * @param selected
     */
    private void setTopSelected(int selected) {
        //取消之前的选中效果
        findViewById(R.id.ib_top_play).setSelected(false);
        findViewById(R.id.ib_top_list).setSelected(false);
        findViewById(R.id.ib_top_lrc).setSelected(false);
        findViewById(R.id.ib_top_volumn).setSelected(false);

        //为所选中的按钮添加按下效果
        findViewById(selected).setSelected(true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_top_play:
                msvg_main.setCurrentView(0);
                setTopSelected(R.id.ib_top_play);
                break;
            case R.id.ib_top_list:
                msvg_main.setCurrentView(1);
                setTopSelected(R.id.ib_top_list);

                break;
            case R.id.ib_top_lrc:
                msvg_main.setCurrentView(2);
                setTopSelected(R.id.ib_top_lrc);

                break;
            case R.id.ib_top_volumn:
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

                int maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumn / 2, AudioManager.FLAG_PLAY_SOUND);

                break;
            case R.id.ib_bottom_model:
                if (MediaUtil.CURMODEL == Constants.MODEL_NORMAL) {
                    MediaUtil.CURMODEL = Constants.MODEL_RANDOM;

                    mIv_bottom_model.setImageResource(R.drawable.icon_playmode_shuffle);
                } else if (MediaUtil.CURMODEL == Constants.MODEL_RANDOM) {
                    MediaUtil.CURMODEL = Constants.MODEL_REPEAT;

                    mIv_bottom_model.setImageResource(R.drawable.icon_playmode_repeat);
                } else if (MediaUtil.CURMODEL == Constants.MODEL_REPEAT) {
                    MediaUtil.CURMODEL = Constants.MODEL_SINGLE;

                    mIv_bottom_model.setImageResource(R.drawable.icon_playmode_single);
                } else if (MediaUtil.CURMODEL == Constants.MODEL_SINGLE) {
                    MediaUtil.CURMODEL = Constants.MODEL_NORMAL;

                    mIv_bottom_model.setImageResource(R.drawable.icon_playmode_normal);
                }
                break;
            case R.id.ib_bottom_last:
                if (MediaUtil.CURPOSITION > 0) {
                    changeColorWhite();
                    MediaUtil.CURPOSITION--;
                    changeColorGreen();
                    startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                    //3.修改图标
                    mIv_bottom_play.setImageResource(R.drawable.appwidget_pause);
                }
                break;
            case R.id.ib_bottom_play://播放按钮,点击同一个按钮.有两个操作.需要定义一个变量进行控制
                //启动服务.而且让服务播放音乐
                if (MediaUtil.CURSTATE == Constants.STATE_STOP) {//默认是停止,点击就变播放
                    startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                    //修改图标
                    mIv_bottom_play.setImageResource(R.drawable.appwidget_pause);
                } else if (MediaUtil.CURSTATE == Constants.STATE_PLAY) {//第二次点击的时候.当前的状态是播放
                    startMediaService("暂停");
                    //修改图标
                    mIv_bottom_play.setImageResource(R.drawable.img_playback_bt_play);
                } else if (MediaUtil.CURSTATE == Constants.STATE_PAUSE) {//第三次点击的时候.当前的状态是暂停
                    startMediaService("继续");
                    //修改图标
                    mIv_bottom_play.setImageResource(R.drawable.appwidget_pause);
                }

                break;
            case R.id.ib_bottom_next:
                if (MediaUtil.CURPOSITION < MediaUtil.songList.size() - 1) {

                    changeColorWhite();
                    MediaUtil.CURPOSITION++;
                    changeColorGreen();
                    startMediaService("播放", MediaUtil.songList.get(MediaUtil.CURPOSITION).getPath());
                    mIv_bottom_play.setImageResource(R.drawable.appwidget_pause);

                }
                break;
            case R.id.ib_bottom_update:
                reflesh();
                break;

            default:
                break;
        }
    }

    public void startMediaService(String option) {
        Intent service = new Intent(MainActivity.this, MusicService.class);
        service.putExtra("option", option);
        service.putExtra("msg_onprepare", new Messenger(handler));
        startService(service);
    }

    public void startMediaService(String option, String path) {
        Intent service = new Intent(MainActivity.this, MusicService.class);
        service.putExtra("option", option);
        service.putExtra("path", path);
        service.putExtra("msg_onprepare", new Messenger(handler));
        startService(service);
    }

    public void startMediaService(String option, int progress) {
        Intent service = new Intent(MainActivity.this, MusicService.class);
        service.putExtra("option", option);
        service.putExtra("progress", progress);
        service.putExtra("msg_onprepare", new Messenger(handler));
        startService(service);
    }

    private void changeColorWhite() {
        TextView tvPosition = (TextView) mlv_list.findViewWithTag(MediaUtil.CURPOSITION);
        if (tvPosition != null)
            tvPosition.setTextColor(WHITE);
    }

    @Override
    public void onBackPressed() {
        //取消finish
        //回到桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        //发一个通知
        showNotification();
    }

    private void changeColorGreen() {
        TextView tvPosition = (TextView) mlv_list.findViewWithTag(MediaUtil.CURPOSITION);
        if (tvPosition != null)
            tvPosition.setTextColor(Color.GREEN);
    }

    /**
     * 显示迷你歌词
     *
     * @param lrcString
     */
    public void setMiniLrc(String lrcString) {
        mtv_minilrc.setText(lrcString);
    }

    public void reflesh() {
        Toast.makeText(MainActivity.this, "hhh", Toast.LENGTH_SHORT).show();
        /**---------------接受系统扫描完成的广播---------------**/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(receiver, intentFilter);

        /**---------------发送广播更新媒体库---------------**/
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.parse("file://" + Environment.getExternalStorageDirectory()));
        sendBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.ic_launcher;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, "哈喽酷狗", null, pi);

        manager.notify(9, notification);

    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(receiver);

            MediaUtil.initSongList(MainActivity.this);

            //刷新操作
            new MyScanTask().execute();
        }
    }

    class MyScanTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(MainActivity.this, "提示", "努力更新中...");
            mAdapter.notifyDataSetChanged();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            MediaUtil.initSongList(MainActivity.this);
            return null;
        }
    }
}
