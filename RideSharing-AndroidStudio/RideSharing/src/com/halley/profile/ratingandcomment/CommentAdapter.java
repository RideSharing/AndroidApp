package com.halley.profile.ratingandcomment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.halley.registerandlogin.R;

import java.util.List;

/**
 * Created by enclaveit on 5/19/15.
 */
public class CommentAdapter extends BaseAdapter {

    private  List<CommentItem> comments;
    private Activity activity;
    private LayoutInflater inflater;

    public CommentAdapter(Activity activity, List<CommentItem> comments){
        this.activity = activity;
        this.comments = comments;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int location) {
        return comments.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_comment_item, null);
        TextView content = (TextView) convertView.findViewById(R.id.content);
        CommentItem m = comments.get(position);
        content.setText(m.getComment());
//
        return convertView;
    }
}
