package com.halley.registeritinerary;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.halley.vehicle.VehicleItem;

import java.util.List;

/**
 * Created by enclaveit on 5/13/15.
 */
public class VehicleAdapter extends BaseAdapter implements SpinnerAdapter {

    /**
     * The internal data (the ArrayList with the Objects).
     */
    private final List<VehicleItem> data;
    private final Activity activity;
    public VehicleAdapter(List<VehicleItem> data,Activity activity){
        this.data = data;
        this.activity=activity;
    }

    /**
     * Returns the Size of the ArrayList
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * Returns one Element of the ArrayList
     * at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    /**
     * Returns the View that is shown when a element was
     * selected.
     */
    @Override
    public View getView(int position, View recycle, ViewGroup parent) {
        TextView text;
        if (recycle != null){
            // Re-use the recycled view here!
            text = (TextView) recycle;
        } else {
            // No recycled view, inflate the "original" from the platform:
            text = (TextView) activity.getLayoutInflater().inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false
            );
        }
//        text.setTextColor(Color.BLACK);
        text.setText(data.get(position).getType());
        return text;
    }


}

