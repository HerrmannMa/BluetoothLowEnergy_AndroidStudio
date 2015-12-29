package com.vogella.ble_fragmentsV5.fragments.ConnectionFragmentClasses;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vogella.ble_fragmentsV5.MainActivity;
import com.vogella.ble_fragmentsV5.R;
import com.vogella.ble_fragmentsV5.general.BroadcastFilters;
import com.vogella.ble_fragmentsV5.services.BluetoothLeService;

import java.util.ArrayList;

/**
 * Created by Matthias on 15.12.2015.
 */
public class Fragment_Connection extends android.support.v4.app.Fragment implements View.OnClickListener, BluetoothAdapter.LeScanCallback, AdapterView.OnItemClickListener, DialogInterface.OnClickListener{

    private final String TAG = Fragment_Connection.class.getSimpleName();

    //references for view elements
    private Button btnStart;
    private Button btnStop;
    private ListView list;

    //Own list adapter for custom list vies
    private SimpleArrayListAdapter adp;

    //Bluetooth specific variables
    private ArrayList<OwnBluetoothDevice> scannedDevices;
    private STATES currentState;
    private BluetoothAdapter btAdapter;
    private OwnBluetoothDevice connectedDevice;
    private OwnBluetoothDevice selectedDevice;


    //Keys for SharedPreferences-Storage (Key-Value-Principle)
    private final String KEY_STARTSCAN = "KEY_STARTSCAN_" + Fragment_Connection.class.getSimpleName();
    private final String KEY_STOPSCAN = "KEY_STOPSCAN_" + Fragment_Connection.class.getSimpleName();
    private final String KEY_STATE = "KEY_STATE_" + Fragment_Connection.class.getSimpleName();
    private final String KEY_CONNECTEDDEVICE = "KEY_CONNECTEDDEVICE_" + Fragment_Connection.class.getSimpleName();
    private final String KEY_SELECTEDDEVICE = "KEY_SELECTEDDEVICE_" + Fragment_Connection.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "Entering: onCreate");
        super.onCreate(savedInstanceState);

        //Get Bluetooth adapter
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = manager.getAdapter();

        //Check if Bluetooth adapter was received successfully
        if (btAdapter == null) {
            //Device does not support Bluetooth
            Toast.makeText(getActivity(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            //Finish activity
            getActivity().finish();
        }

        //Initialize array
        scannedDevices = new ArrayList<>();
        Log.d(TAG, "Leaving: onCreate");
    }

    @Override
    public void onResume(){
        Log.d(TAG, "Entering: onResume");
        super.onResume();

        //Check if bluetooth has already been enabled on this device
        if (btAdapter.isEnabled() == false) {
            //Bluetooth was not enabled --> Enable now
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Log.d(TAG, "BT enable started");
            //Start activity for user interaction to enable bluetooth (Active user permission required)
            //TODO: Was passiert wenn User die Freigabe verweigert
            startActivity(enableBluetooth);
        }

        //Check if device supports Bluetooth Low Energy (Every device that uses Android 4.3(API 18) == Jellybean should support BLE ==> min API version in manifest = 18)
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == false) {
            //Device does not support Bluetooth Low Energy
            Toast.makeText(getActivity(), "Device does not support Bluetooth Low Energy", Toast.LENGTH_SHORT).show();
            //Finish activity
            getActivity().finish();
        }

        //Search in SharedPreferences for last user settings, use default values for initialization if nothing was found
        //Primitive values (bool, int, ...) can be stored in SharedPreferences by default
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //TODO genau Unterschiede nochmal überlegen! Was genau ist das wichtige am Reset?
        if((((MainActivity)getActivity()).isPreviouslyConnected() == true) && ((MainActivity)getActivity()).isConnected() == false){
            Log.d(TAG, "From Background");
            reset();
            currentState = STATES.NOT_CONNECTED;
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);

            String serializedDevice;
            serializedDevice = p.getString(KEY_CONNECTEDDEVICE, "");
            connectedDevice = getOwnBluetoothDeviceFromJson(serializedDevice);
            selectedDevice = connectedDevice;
            //If deserialization was successfull add device to list view
            if (connectedDevice != null){
                scannedDevices.add(connectedDevice);
                updateScannedDevices();
            }
            //TODO genau Unterschiede nochmal überlegen! Was genau ist das wichtige am Reset?
        }else if(((MainActivity)getActivity()).isConnected() == true){
            Log.d(TAG, "Swipe");
            currentState = STATES.CONNECTED;
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);

            String serializedDevice;
            serializedDevice = p.getString(KEY_CONNECTEDDEVICE, "");
            connectedDevice = getOwnBluetoothDeviceFromJson(serializedDevice);
            //If deserialization was successfull add device to list view
            if (connectedDevice != null){
                scannedDevices.add(connectedDevice);
                updateScannedDevices();
            }

        }
        else{
            reset();
        }
        /*//Search for last state. If nothing was found initialize state with NOT_CONNECTED
        //currentState = STATES.fromInteger(p.getInt(KEY_STATE, STATES.NOT_CONNECTED.getValue()));
        if (currentState != STATES.NOT_CONNECTED){
            btnStart.setEnabled(p.getBoolean(KEY_STARTSCAN, true) );
            btnStop.setEnabled(p.getBoolean(KEY_STOPSCAN, false));

        }




        //To store and reload user specific values (Arrays, objects, ...) the Gson-lib was used.
        //Gson uses the JSON (JavaScript Object Notation) to serialize objects and store all the information in one string
        //Strings can be stored in SharedPreferences

        String serializedDevice;*//* = p.getString(KEY_SELECTEDDEVICE, "");
        selectedDevice = getOwnBluetoothDeviceFromJson(serializedDevice);*//*

        serializedDevice = p.getString(KEY_CONNECTEDDEVICE, "");
        connectedDevice = getOwnBluetoothDeviceFromJson(serializedDevice);
        //If deserialization was successfull add device to list view
        if (connectedDevice != null){
            scannedDevices.add(connectedDevice);
        }*/



        //Fragment_Connection only needs to listen to connection changes as it has to change data that should be changed or UI
        getActivity().registerReceiver(mGattUpdateReceiver, BroadcastFilters.getFragment_ConnectionFilter());
        Log.d(TAG, "Leaving: onResume");
    }

    private OwnBluetoothDevice getOwnBluetoothDeviceFromJson(String json) {
        OwnBluetoothDevice retValue;
        Gson gson = new Gson();

        try{
            retValue = gson.fromJson(json, OwnBluetoothDevice.class);
        }
        //Catch Exception if deserialization of OwnBluetoothDevice was not successful
        catch (JsonSyntaxException jsexc){
            retValue = null;
        }
        return retValue;
    }

    //Set everything to its initial values
    public void reset() {
        Log.d(TAG, "Entering: reset");
        scannedDevices.clear();
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        updateScannedDevices();
        stopScan();
        currentState = STATES.NOT_CONNECTED;
        Log.d(TAG, "Leaving: reset");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.d(TAG, "Entering: onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        adp = new SimpleArrayListAdapter(getContext(), R.layout.list_view_row_item, scannedDevices);
        list.setAdapter(adp);
        Log.d(TAG, "Leaving: onActivityCreated");
    }

    public void onPause(){
        Log.d(TAG, "Entering: onPause");
        super.onPause();
        btAdapter.stopLeScan(this);


        if (getActivity().isFinishing() == true) {
            Log.d(TAG, "isFinishing() == true");
        } else {
            Log.d(TAG, "isFinishing() == false");

            /*editor.putBoolean(KEY_STARTSCAN, btnStart.isEnabled());
            editor.putBoolean(KEY_STOPSCAN, btnStop.isEnabled());*/
            if (currentState == STATES.CONNECTED){
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = p.edit();
                Gson gson = new Gson();
                String json = gson.toJson(connectedDevice);
                editor.putString(KEY_CONNECTEDDEVICE, json);
                editor.commit();
            }
        }
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        Log.d(TAG, "Leaving: onPause");
    }

    public void onDestroy(){
        Log.d(TAG, "Entering: onDestroy");
        super.onDestroy();
        Log.d(TAG, "Leaving: onDestroy");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Entering: onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);

        //Assign view elements to internal references and init Listeners
        btnStart = (Button)rootView.findViewById(R.id.btnStart);
        btnStop = (Button)rootView.findViewById(R.id.btnStop);
        list = (ListView)rootView.findViewById(R.id.list);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        list.setOnItemClickListener(this);

        Log.d(TAG, "Leaving: onCreateView");
        return rootView;
    }

    private void startScan(){
        Log.d(TAG, "Entering: startScan");

        //Configure buttons corresponding to user input
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        //Start a completely new scan --> delete any old data
        scannedDevices.clear();
        btAdapter.startLeScan(this);
        Log.d(TAG, "Leaving: startScan");
    }
    @Override
    public void onClick(View v) {

        //On click listener for both buttons
        Log.d(TAG, "Entering: onClick");
        switch (v.getId()){
            case (R.id.btnStart):
                startScan();
                break;
            case (R.id.btnStop):
                stopScan();
                break;

        }
        Log.d(TAG, "Leaving: onClick");
    }

    private void stopScan() {
        Log.d(TAG, "Entering: stopScan");

        //Configure buttons corresponding to user input
        btnStop.setEnabled(false);
        btnStart.setEnabled(true);
        btAdapter.stopLeScan(this);
        Log.d(TAG, "Leaving: stopScan");
    }

/*  Callback method for BluetoothLE
    this method is called everytime a new device was found during scan */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, "Entering: onLeScan");
        scannedDevices.add(new OwnBluetoothDevice(device, rssi));
        updateScannedDevices();
        Log.d(TAG, "Leaving: onLeScan");

    }
    private void updateScannedDevices(){
        Log.d(TAG, "Entering: updateScannedDevices");
        //Changing view elemts needs to run on the UI Thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //scannedDevices.add(device);
                adp.notifyDataSetChanged();
            }
        });
        Log.d(TAG, "Leaving: updateScannedDevices");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Entering: onItemClick");
        String msg = null;
        //AlertDialog.Builder is needed if the application needs user interaction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Object obj;
        obj = parent.getAdapter().getItem(position);

        //As there are only objects of the class OwnbluetoothDevice listed in the array no instanceof-operator was used
        selectedDevice = (OwnBluetoothDevice)obj;
        if (scannedDevices.contains(selectedDevice)) {
            Log.d(TAG, "Reaching this point");
            switch (currentState) {
                case NOT_CONNECTED:
                    msg = "Möchten Sie sich mit " + selectedDevice.getDevice().getName() + " verbinden?";
                    break;
                case CONNECTED:
                    if (connectedDevice == selectedDevice) {
                        msg = "Möchten Sie sich von " + connectedDevice.getDevice().getName() + " trennen?";
                        currentState = STATES.CONNECTED_SAME_DEVICE_SELECTED;
                    } else {
                        msg = "Möchten Sie sich von " + connectedDevice.getDevice().getName() + " trennen und mit " + selectedDevice.getDevice().getName() + "verbinden?";
                        currentState = STATES.CONNECTED_NEW_DEVICE_SELECTED;
                    }
                    break;
                default:
                    Log.d(TAG, "Error state machine item listener");
                    getActivity().finish();
                    break;
            }
            builder.setMessage(msg).setPositiveButton("Ja", this).setNegativeButton("Nein", this).show();
        }else{
            Log.d(TAG, "A device was selected that is not a member of the scannedDevices-Array -- ERROR");
            getActivity().finish();
        }
        Log.d(TAG, "Leaving: onItemClick");
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            //The user selected YES
            case DialogInterface.BUTTON_POSITIVE:
                switch (currentState){
                    case NOT_CONNECTED:
                        ((MainActivity) getActivity()).connectToDevice(selectedDevice.getDevice());
                        break;
                    case CONNECTED_NEW_DEVICE_SELECTED:
                    case CONNECTED_SAME_DEVICE_SELECTED:
                        ((MainActivity) getActivity()).disconnect();
                        break;
                }
                break;
            //The user selected NO
            case DialogInterface.BUTTON_NEGATIVE:
                connectedDevice = null;
                selectedDevice = null;
                break;
            default:
                Log.d(TAG, "Error on dialog click state machine");
                getActivity().finish();
                break;
        }

    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Entering: onReceive() in " + Fragment_Connection.class.getSimpleName());
            final String action = intent.getAction();
            switch (action){
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    //If connection was successful update internal state und reference
                    currentState = STATES.CONNECTED;
                    connectedDevice = selectedDevice;
                    break;

                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    //Check if you disconnected from the selected device or from a previous device
                    if (currentState == STATES.CONNECTED_SAME_DEVICE_SELECTED){
                        reset();
                    }else{
                        //TODO Was passiert wenn man ein 2. Bluetooth-Gerät findet und sich stattdessen damit verbinden will
                        //((MainActivity) getActivity()).connectToDevice(selectedDevice.getDevice());
                    }
                    break;

            }
            Log.d(TAG, "Leaving: onReceive() in " + Fragment_Connection.class.getSimpleName());

        }
    };
    private enum STATES {
        NOT_CONNECTED(0), CONNECTED_SAME_DEVICE_SELECTED(1), CONNECTED_NEW_DEVICE_SELECTED(2), CONNECTED(3);

        //Get and Set-methods are only necessary to save currentState with the primitive methods of SharedPreferences and not use overhead from Json
        private final int value;
        private STATES(int value) {
            this.value = value;
        }
        public int getValue(){
            return value;
        }
        public static STATES fromInteger(int x){
            switch (x){
                case 0:
                    return NOT_CONNECTED;
                case 1:
                    return CONNECTED_SAME_DEVICE_SELECTED;
                case 2:
                    return CONNECTED_NEW_DEVICE_SELECTED;
                case 3:
                    return CONNECTED;
                default:
                    return null;
            }
        }


    };
}
