package com.vogella.ble_fragmentsV5.fragments.ConnectionFragmentClasses;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Matthias on 16.12.2015.
 */

/*
This class does not only store a specific bluetooth device.
It also stores the corresponding rssi value that was received during bluetooth scan
*/

public class OwnBluetoothDevice {
    private BluetoothDevice device;
    private int rssi;

    public OwnBluetoothDevice(BluetoothDevice device, int rssi){
        this.rssi = rssi;
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    //Setter is only necessary to update the rssi if the device was already found during a previous scan
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
