package com.vogella.ble_fragmentsV5;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vogella.ble_fragmentsV5.general.BroadcastFilters;
import com.vogella.ble_fragmentsV5.general.SensorTag;
import com.vogella.ble_fragmentsV5.model.Model;
import com.vogella.ble_fragmentsV5.services.BluetoothLeService;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {


    private BluetoothLeService btService;
    private final String TAG = MainActivity.class.getSimpleName();
    private ActionBar actionBar;
    private ViewPager Tab;
    private TabPageAdapter TabAdapter;
    private TextView status;
    private Model model;
    private boolean connected;
    private boolean previouslyConnected;
    private BluetoothDevice connectedDevice;

    private final String KEY_CONNECTED = "KEY_CONNECTED_" + MainActivity.class.getSimpleName();
    private final String KEY_PREV_CONNECTED = "KEY_PREV_CONNECTED_" + MainActivity.class.getSimpleName();
    private final String KEY_CONNECTEDDEVICE = "KEY_CONNECTEDDEVICE_" + MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the viewpager as layout
        setContentView(R.layout.activity_main);
        //The actionbar is needed to show the different tabs - SupportActionBar because the minimal API version is 18 in this application
        actionBar = getSupportActionBar();
        //Implement an own page adapter for the viewpager to connect view with the corresponding data/fragments
        TabAdapter = new TabPageAdapter(getSupportFragmentManager());
        //The reference for the viewpager is only needed for configuraton purposes
        Tab = (ViewPager)findViewById(R.id.pager);
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //Assign the adapter to the view pager
        Tab.setAdapter(TabAdapter);

        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });




        actionBar.addTab(actionBar.newTab().setText("Connect").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Anzeige").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Trend").setTabListener(this));

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_custom_layout);

        status = (TextView)findViewById(R.id.valueStatus);


        model = new Model();


        //Register Broadcast receiver for main activity

        /*SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        if(p.getBoolean(KEY_REACHEDONSTOP, false)){
            p.edit().clear().commit();
        }*/
        /*connected = p.getBoolean(KEY_CONNECTED, false);
        if (connected){
            Gson gson = new Gson();
            connectedDevice = gson.fromJson(p.getString(KEY_CONNECTEDDEVICE, ""), BluetoothDevice.class);
            connectToDevice(connectedDevice);
        }*/

    }



    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    protected void onResume() {
        //parent method call
        super.onResume();
        Log.d(TAG, "Entering: onResume");
        Intent bleServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(bleServiceIntent, mConnection, BIND_AUTO_CREATE);
        startService(bleServiceIntent);
        registerReceiver(mGattUpdateReceiver, BroadcastFilters.getMainActivityFilter());
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        previouslyConnected = p.getBoolean(KEY_PREV_CONNECTED, false);
        connected = false;
        Log.d(TAG, "Previously connected: " + String.valueOf(previouslyConnected));
        if (previouslyConnected){
            Gson gson = new Gson();
            try {
                connectedDevice = gson.fromJson(p.getString(KEY_CONNECTEDDEVICE, ""), BluetoothDevice.class);
                Log.d(TAG, "Device name and address from storage: " + connectedDevice.getName());
            }catch (JsonSyntaxException jsexc){
                connectedDevice = null;
            }
        }

        Log.d(TAG, "Leaving: onResume");
    }

    public boolean isPreviouslyConnected() {
        return previouslyConnected;
    }

    public boolean isConnected() {
        return connected;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Entering: onServiceConnected");
            BluetoothLeService.InternalBinder b = (BluetoothLeService.InternalBinder) service;
            btService = b.getService();
            if(!btService.initService()){
                Log.d(TAG, "Unable to initialize Bluetooth service");
                finish();
            }
            if (connectedDevice != null){
                btService.connectToDevice(connectedDevice);
            }
            Log.d(TAG, "Leaving: onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Entering: onServiceDisconnected");
            btService = null;
            Log.d(TAG, "Leaving: onServiceDisconnected");
        }
    };
    //Actual connection method
    public void connectToDevice(final BluetoothDevice device) {
        Log.d(TAG, "Entering: connectToDevice");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Try to connect to " + device.getName());
                if (btService != null){
                    btService.connectToDevice(device);
                }

            }
        });
        Log.d(TAG, "Leaving: connectToDevice");
    }

    public void disconnect(){
        Log.d(TAG, "Entering: disconnect");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btService.disconnect();
            }
        });
        Log.d(TAG, "Leaving: disconnect");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Entering: onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = p.edit();
       /* if (connected == true){
            editor.putBoolean(KEY_CONNECTED,connected);
            Gson gson = new Gson();
            editor.putString(KEY_CONNECTEDDEVICE, gson.toJson(connectedDevice));
        }
*/
        Log.d(TAG, "Currently connected: " + String.valueOf(connected));
        if (isFinishing() == true){
            editor.clear();
            editor.commit();
        }else{
            if(connected == true){
                editor.putBoolean(KEY_PREV_CONNECTED,connected);
                Gson gson = new Gson();
                editor.putString(KEY_CONNECTEDDEVICE, gson.toJson(connectedDevice));
            }else{
                editor.putBoolean(KEY_PREV_CONNECTED,false);
                editor.putString(KEY_CONNECTEDDEVICE, "");
            }
            editor.commit();
        }
        Log.d(TAG, "Leaving: onPause");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Entering: onStop");
        super.onStop();
        Log.d(TAG, "On stop");
        if (btService != null) {
            btService.disconnect();
            stopService(new Intent(this, BluetoothLeService.class));
            unbindService(mConnection);
        }
        Log.d(TAG, "Leaving: onStop");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Entering: onDestroy");
        super.onDestroy();
        btService.disableBluetooth();

        Log.d(TAG, "Leaving: onDestroy");

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Entering: onReceive() in " + MainActivity.class.getSimpleName());
            final String action = intent.getAction();
            switch (action){
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    model.clear();
                    connected = true;
                    previouslyConnected = true;
                    connectedDevice = btService.getConnectedDevice();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText(getResources().getString(R.string.connected));
                            status.setTextColor(getResources().getColor(R.color.signalGood));

                        }
                    });

                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    String UUID = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    if (UUID.equals(SensorTag.IR_TEMPERATURE_DATA.toString())){
                        //Data from temperature sensor was received
                        double tempData = Double.parseDouble(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                        model.addTemperatureData(tempData);

                    }
                    break;

                case BluetoothLeService.ACTION_GATT_CONNECTING:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText(getResources().getString(R.string.connecting));
                            status.setTextColor(getResources().getColor(R.color.connecting));
                        }
                    });
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    connected = false;
                    previouslyConnected = false;
                    connectedDevice = null;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText(getResources().getString(R.string.notConnected));
                            status.setTextColor(getResources().getColor(R.color.notConnected));

                        }
                    });

            }
            Log.d(TAG, "Leaving: onReceive() in " + MainActivity.class.getSimpleName());

        }
    };


    @Override
    //Whenever a new tab in the actionbar is selected, change the corresponding fragment in the viewpager
    //Add some fragment initialization code here
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Tab.setCurrentItem(tab.getPosition());

    }

    @Override
    //Add some cleanup code here
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public Model getModel() {
        return model;
    }
}
