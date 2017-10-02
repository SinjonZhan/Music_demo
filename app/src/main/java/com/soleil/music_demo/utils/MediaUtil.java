package com.soleil.music_demo.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.soleil.music_demo.bean.Music;
import com.soleil.music_demo.conf.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @作者 xinrong
 * @创建日期 $data$ $time$
 */
public class MediaUtil {

    public static List<Music> songList = new ArrayList<Music>();
    public static int CURPOSITION = 0;
    public static int CURSTATE = Constants.STATE_STOP;
    public static int CURMODEL = Constants.MODEL_NORMAL;

    /**
     * 加载本地音乐
     *
     * @param context
     */
    //加载手机里的本地音乐---->sqlite---->contentProvider
    public static void initSongList(Context context) {

        songList.clear();
        /**
         *  private String title;//歌曲名
         private String artist;//歌手
         private String path;//路径
         */

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            Music music = new Music(title, artist, path);
            songList.add(music);
        }
    }


    public static String duration2Str(int duration) {
        String result = "";
        int i = duration / 1000;
        int min = i / 60;
        int sec = i % 60;
        if (min > 9) {
            if (sec > 9) {
                result = min + ":" + sec;
            } else {
                result = min + ":0" + sec;
            }
        } else {
            if (sec > 9) {
                result = "0" + min + ":" + sec;
            } else {
                result = "0" + min + ":0" + sec;
            }
        }
        return result;
    }

    /**
     * 根据歌曲的路径,得到对应的lrc
     *
     * @param path
     * @return
     */
    public static File getLrcFile(String path) {
        File file;
        String lrcName = path.replace(".mp3", ".lrc");//找歌曲名称相同的.lrc文件
        file = new File(lrcName);
        if (!file.exists()) {
            lrcName = path.replace(".mp3", ".txt");//歌词可能是.txt结尾
            file = new File(lrcName);
            if (!file.exists()) {
                return null;
            }
        }
        return file;

    }
}
