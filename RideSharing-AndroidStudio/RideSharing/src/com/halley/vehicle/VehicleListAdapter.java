package com.halley.vehicle;

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
import com.halley.registerandlogin.R;

import java.util.List;

/**
 * Created by enclaveit on 5/5/15.
 */
public class VehicleListAdapter extends BaseAdapter {
    private Activity activity;
    private List<VehicleItem> vehicleItems;
    private LayoutInflater inflater;
    private Context context;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public VehicleListAdapter(Activity activity, List<VehicleItem> vehicleItems, Context context) {
        this.activity = activity;
        this.vehicleItems = vehicleItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return vehicleItems.size();
    }

    @Override
    public Object getItem(int location) {
        return vehicleItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.vehicle_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        CustomNetworkImageView img_VehicleImage = (CustomNetworkImageView) convertView
                .findViewById(R.id.img_VehicleImage);
        TextView lvType = (TextView) convertView
                .findViewById(R.id.lvType);
        TextView lvLicensePlate = (TextView) convertView.findViewById(R.id.lvLicensePlate);
        TextView lvRegCertificate = (TextView) convertView
                .findViewById(R.id.lvRegCertificate);
        VehicleItem m = vehicleItems.get(position);

        // thumbnail image
        byte[] decodeString = Base64.decode(m.getVehicle_img(), Base64.DEFAULT);
        Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
                decodeString.length);

        img_VehicleImage.setLocalImageBitmap(decodeByte);

        lvType.setText("Type: " + m.getType());
        lvLicensePlate.setText("License Plate: " + m.getLicense_plate());
        lvRegCertificate.setText("Registration Certificate: " + m.getReg_certificate());
        return convertView;
    }
}
