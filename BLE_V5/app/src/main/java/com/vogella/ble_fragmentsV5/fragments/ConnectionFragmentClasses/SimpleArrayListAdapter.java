package com.vogella.ble_fragmentsV5.fragments.ConnectionFragmentClasses;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vogella.ble_fragmentsV5.R;

import java.util.ArrayList;

/**
 * Created by Matthias on 16.12.2015.
 */

//SimpleArrayListAdapter is the connection between the data source and the corresponding view
public class SimpleArrayListAdapter extends ArrayAdapter<OwnBluetoothDevice>{

    private final Context context;
    private final ArrayList<OwnBluetoothDevice> devices;
    private final int layoutResourceId;

    //for debug purposes only
    private final String TAG = SimpleArrayListAdapter.class.getSimpleName();

    public SimpleArrayListAdapter(Context context, int layoutResourceId,  ArrayList<OwnBluetoothDevice> devices) {
        super(context, layoutResourceId, devices);
        this.context = context;
        this.devices = devices;
        this.layoutResourceId = layoutResourceId;
    }


    //GetView was overwritten to use the view recycling mechanism. Layout inflation usually makes heavy use of CPU.
    //Therefore old views are recycled during scrolling
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Log.d(TAG, "Entering: getView");

        View row = convertView;
        ViewHolder holder;


        //Check if the row is created for the first time
        if (row == null){
            //A completely new row should be created
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            //Get references for views
            holder.deviceName = (TextView)row.findViewById(R.id.deviceName);
            holder.deviceRSSI = (TextView)row.findViewById(R.id.deviceRSSI);

            //Viewholder is stored within the row so you don't have to call "findViewById" the next time -->Performance optimization
            //Now holder is directly assigned to this view object
            row.setTag(holder);


        }else{
            //Get prevously stored view holder ==> Now you can easily access text views
            holder = (ViewHolder)row.getTag();
        }

        //Actual data for view object can be accessed independently from the view recycling mechanism
        OwnBluetoothDevice device = devices.get(position);


        //Write all the values to the view object. All the references to the text views are stored in the ViewHolder class
        //The ViewHolder object for this row was saved as a tag
        holder.deviceName.setText(device.getDevice().getName());
        holder.deviceRSSI.setText(String.valueOf(device.getRssi()) + " dBm");
        Log.d(TAG, "Device name: " + device.getDevice().getName() + "RSSI: " + device.getRssi());
        Log.d(TAG, "Leaving: getView");
        return row;

    }

    //ViewHolder design pattern = storage for repeatably used view elements
    static class ViewHolder{
        TextView deviceName;
        TextView deviceRSSI;
    }
}
