package com.soleil.music_demo.bean;

/**
 * @作者 xinrong
 * @创建日期 $data$ $time$
 */

public class Music {
    private String title;//歌曲名
    private String artist;//歌手
    private String path;//路径

    public Music(String title, String artist, String path) {
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }
}
