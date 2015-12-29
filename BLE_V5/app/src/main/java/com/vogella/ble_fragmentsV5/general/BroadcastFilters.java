package com.vogella.ble_fragmentsV5.general;

import android.content.IntentFilter;

import com.vogella.ble_fragmentsV5.services.BluetoothLeService;

/**
 * Created by Matthias on 12.12.2015.
 */
/*BroadcasFilters is a static class that returns the corresponding broadcast/intent filters for each fragment/activity
  Not every object needs to listen to all broadcast messages, e. g. fragment for data display does not care about connection change events*/
public class BroadcastFilters {

    //DISCONNECTED: To reset its view
    //DATA_AVAILABLE: To update its view with data values
    public static IntentFilter getGeneralDisplayFragmentFilter(){
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //MainActivity needs to listen to every bluetooth event so far
    //DATA_AVAILABLE: To store the data in the datamodel
    //Connection events: To update the status action bar
    public static IntentFilter getMainActivityFilter(){
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    //Fragment_Connection needs to listen to connection change events as it needs to update the UI
    public static IntentFilter getFragment_ConnectionFilter(){
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;

    }
    //TODO: Model-Intentfilter wird  momentan nicht benutzt. Ggf. Löschen
    public static IntentFilter getModelFilter(){
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
