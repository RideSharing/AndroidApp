package com.halley.message;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.halley.app.AppController;
import com.halley.helper.CustomNetworkImageView;
import com.halley.itinerary.list.item.ItineraryItem;
import com.halley.registerandlogin.R;

import java.util.List;

/**
 * Created by enclaveit on 5/23/15.
 */
public class MessageAdapter extends BaseAdapter {
    private List<MessageItem> messageItems;
    private Activity activity;
    private LayoutInflater inflater;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    MessageAdapter(Activity activity, List<MessageItem> messageItems) {
        this.activity = activity;
        this.messageItems = messageItems;
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public Object getItem(int location) {
        return messageItems.get(location);
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
            convertView = inflater.inflate(R.layout.message_item, null);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        CustomNetworkImageView avatar = (CustomNetworkImageView) convertView
                .findViewById(R.id.img_author);
        TextView tv_subject = (TextView) convertView.findViewById(R.id.tv_subject);
        TextView tv_email_author = (TextView) convertView.findViewById(R.id.tv_email_author);
        TextView tv_created_at = (TextView) convertView.findViewById(R.id.tv_created_at);

        MessageItem m = messageItems.get(position);

        byte[] decodeString = Base64.decode(m.getLink_avatar(), Base64.DEFAULT);
        Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
                decodeString.length);

        avatar.setLocalImageBitmap(decodeByte);
        tv_subject.setText(m.getSubject());
        tv_email_author.setText(m.getEmail_author());
        tv_created_at.setText(m.getCreated_at());
        return convertView;
    }
}
