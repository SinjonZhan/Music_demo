package com.soleil.music_demo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.soleil.music_demo.R;
import com.soleil.music_demo.bean.Music;
import com.soleil.music_demo.utils.MediaUtil;

/**
 * @作者 xinrong
 * @创建日期 2017/5/22 22:23
 */

public class MyAdapter extends BaseAdapter {

    private Context context;

    public MyAdapter(Context context) {
        super();
        this.context = context;
    }

    //1.数据源在什么地方datasource
    @Override
    public int getCount() {
        if (MediaUtil.songList != null)
            return MediaUtil.songList.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (MediaUtil.songList != null)
            return MediaUtil.songList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /**---------------视图的初始化---------------**/
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_music, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /**---------------拿到数据---------------**/
        Music music = MediaUtil.songList.get(position);
        /**---------------设置数据---------------**/
        viewHolder.tv_title.setText(music.getTitle());
        viewHolder.tv_artist.setText(music.getArtist());

        if (MediaUtil.CURPOSITION == position) {
//            viewHolder.tv_title.setTextColor(必须是一个16进制的颜色值0xFF00FF00||Color.GREEN);
            viewHolder.tv_title.setTextColor(Color.GREEN);
        } else {
            viewHolder.tv_title.setTextColor(Color.WHITE);
        }
        viewHolder.tv_title.setTag(position);
        return convertView;
    }

    class ViewHolder {
        private TextView tv_title;
        private TextView tv_artist;
    }
}
