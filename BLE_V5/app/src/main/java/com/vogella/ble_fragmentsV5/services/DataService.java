package com.vogella.ble_fragmentsV5.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Matthias on 23.12.2015.
 */
public class DataService extends Service {

    private final String TAG = DataService.class.getSimpleName();
    private final IBinder mBinder = new InternalBinder();


    public DataService(){

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class InternalBinder extends Binder {
        public DataService getService(){
            return DataService.this;
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Entering: onReceive() in " + DataService.class.getSimpleName());
            final String action = intent.getAction();
            switch (action){
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                   break;
            }
            Log.d(TAG, "Leaving: onReceive() in " + DataService.class.getSimpleName());

        }
    };
}
