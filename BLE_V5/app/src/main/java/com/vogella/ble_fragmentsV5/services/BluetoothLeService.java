package com.vogella.ble_fragmentsV5.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.vogella.ble_fragmentsV5.StateMachine;
import com.vogella.ble_fragmentsV5.general.SensorTag;

import java.util.UUID;

/**
 * Created by Matthias on 12.12.2015.
 */
public class BluetoothLeService extends Service {

    public static final String ACTION_GATT_CONNECTED = "BluetoothLE_GATT_CONNECTION_ESTABLISHED";
    public static final String ACTION_GATT_DISCONNECTED = "BluetoothLE_GATT_DISCONNECTED";
    public static final String ACTION_GATT_CONNECTING = "BluetoothLE_GATT_CONNECTING";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "BluetoothLE_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_WRITE = "Bluetooth_GATT_WRITE_CHARACTERISTIC";
    public static final String ACTION_DATA_AVAILABLE ="BluetoothLE_GATT_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "BluetoothLE_EXTRA_DATA_AVAILABLE";
    public static final String EXTRA_UUID = "BluetoothLE_EXTRA_UUID_AVAILABLE";

    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;

    private StateMachine stateMachine;
    private BluetoothLeService btleService = this;
    private BluetoothGattService currentService;


    private final IBinder mBinder = new InternalBinder();

    public void enableNotification(UUID idForCharacterisitc, UUID idForService) {
        Log.d(TAG, "Entering: enableNotification");
        BluetoothGattCharacteristic characteristic = gatt.getService(idForService).getCharacteristic(idForCharacterisitc);
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        Log.d(TAG, "Leaving: enableNotification");
    }

    public class InternalBinder extends Binder {
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }



    public BluetoothDevice getConnectedDevice(){
        return gatt.getDevice();
    }
    public boolean initService(){
        boolean retValue = true;
        if(btAdapter == null){
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            btAdapter = manager.getAdapter();
            if(btAdapter == null){
                retValue = false;
            }
        }
        return retValue;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind called");
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        if (gatt == null) {
            return super.onUnbind(intent);
        }

        // make sure resources are released properly
        gatt.close();
        gatt = null;

        return super.onUnbind(intent);
    }



    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {


        //This method is called whenever a something changes in the current connection status
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Entering: onConnectionStateChanged");
            super.onConnectionStateChange(gatt, status, newState);

            String action;

            //A new connection has just been established
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.d(TAG, "Connection to GATT server successfully established");
                Log.d(TAG, "Trying to read services");
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){

                stateMachine = null;
                //Prepare action string for intent/broadcast receivers
                action = ACTION_GATT_DISCONNECTED;

                //notify broadcast receivers
                broadcastUpdate(action);
            }
            Log.d(TAG, "Leaving: onConnectionStateChanged");
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            //here you can add if the services you want to use are available on this system
            //if not, you cannot use this app with the connected device
            Log.d(TAG, "Entering onServiceDiscovered");
            for(BluetoothGattService service : gatt.getServices()){
                if (service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
                    Log.d(TAG, "Service found");
                    if(checkForCharacteristics(service)){
                        String action;

                        //Prepare action string for intent/broadcast receivers
                        action = ACTION_GATT_CONNECTED;

                        stateMachine = new StateMachine();
                        stateMachine.setBtleService(btleService);
                        stateMachine.advanceStateMachine(gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE), null);
                        //stateMachine.startInitializationSequence(gatt, gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE));

                        //notify broadcast receivers
                        broadcastUpdate(action);

                        break;
                    }
                }else{
                    Log.d(TAG, "Service not found");
                }
            }
            Log.d(TAG, "Leaving onServiceDiscovered");

        }

        private boolean checkForCharacteristics(BluetoothGattService service){
            boolean result;
            Log.d(TAG, "Entering checkForCharacterisitics");
            if (service.getCharacteristic(SensorTag.IR_TEMPERATURE_CONFIG) != null && service.getCharacteristic(SensorTag.IR_TEMPERATURE_DATA) != null){
                Log.d(TAG, "Found necessary characterisitcs for Temperature");
                result = true;
            }else{
                Log.d(TAG, "Could not find necessary characterisitics");
                result = false;

            }
            Log.d(TAG, "Leaving checkForCharacterisitics");
            return result;
        }


        //This method is called everytime a characteristic was read from the GATT server
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.d(TAG, "Entering: onCharacteristicRead");
            stateMachine.advanceStateMachine(gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE), characteristic);
            Log.d(TAG, "Leaving: onCharacteristicRead");
        }
        //This method is called everytime a characteristic was written to the GATT server
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "Entering: onCharacteristicWrite");
            /*if (status == BluetoothGatt.GATT_SUCCESS){

                //Update all Listeners that write was successful
                broadcastUpdate(ACTION_GATT_WRITE, characteristic);
            }*/
            stateMachine.advanceStateMachine(gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE), null);
            Log.d(TAG, "Leaving: onCharacteristicWrite");

        }


        //This method is called everytime a characteristic that was registered for notification changed
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Entering: onCharactersitcChanged");
            //Data was successfully read from GATT server
            stateMachine.advanceStateMachine(gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE), characteristic);
            Log.d(TAG, "Leaving: onCharactersitcChanged");
        }
    };


    public void connectToDevice(BluetoothDevice device){
        Log.d(TAG, "Entering: connectToDevice");
        gatt = device.connectGatt(this, true, gattCallback);

        //Prepare action string for intent/broadcast receivers
        String action = ACTION_GATT_CONNECTING;

        //Notify all listeners
        broadcastUpdate(action);
        Log.d(TAG, "Leaving: connectToDevice");
    }
    public void disconnect(){
        Log.d(TAG, "Entering: disconnect");
        if (gatt != null){
            gatt.disconnect();
            gatt.close();
            gatt = null;

            //Prepare action string for intent/broadcast receivers
            String action = ACTION_GATT_DISCONNECTED;

            //Notify all listeners
            broadcastUpdate(action);
        }
        Log.d(TAG, "Leaving: disconnect");
    }

    public void broadcastUpdate(String action){
        Log.d(TAG, "Entering: broadcastUpdate(String)");
        Intent intent = new Intent(action);
        sendBroadcast(intent);
        Log.d(TAG, "Leaving: broadcastUpdate(String)");
    }
    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        Log.d(TAG, "Entering: broadcastUpdate(String, BluetoothGattCharacteristic)");
        //final byte[] data = characteristic.getValue();
        final Intent intent = new Intent(action);

        //Is data availabe that should be sent to all receivers?
        if (characteristic.getUuid().equals(SensorTag.IR_TEMPERATURE_DATA)){
            double ambientTemperature = SensorTag.extractAmbientTemperature(characteristic);
            Log.d(TAG, "Ambient Temperature: " + String.valueOf(ambientTemperature));
            //Add UUID to Intent (EXTRA_UUID = key, actual UUID of characterisitic = value)
            intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
            intent.putExtra(EXTRA_DATA, String.valueOf(ambientTemperature));
            sendBroadcast(intent);

        }else{
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0){
                //A perfomant StringBuilder was used to collect all data
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                Log.d(TAG, "Number of raw data bytes: " + String.valueOf(data.length));

                //Fill StringBuilder
                for (byte byteChar: data){

                    Log.d(TAG, "Raw data byte: " + (char)byteChar);
                    Log.d(TAG, "Equivalent substring for byte value: " + (char)byteChar);
                    stringBuilder.append(String.format("%2X", byteChar));
                }

                //Add UUID to Intent (EXTRA_UUID = key, actual UUID of characterisitic = value)
                intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());

                //Add data to Intent (EXTRA_DATA = key, StringBuilder-data = value)
                Log.d(TAG, "Datastring with temperature value after StringBuilder operation: " + stringBuilder.toString());
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
                sendBroadcast(intent);
            }
        }
        Log.d(TAG, "Leaving: broadcastUpdate(String, BluetoothGattCharacteristic)");
    }


    public void writeCharacterisitc(BluetoothGattCharacteristic characteristic, byte value[]){
        Log.d(TAG, "Entering: writeCharacterisitc");
        characteristic.setValue(value);
        gatt.writeCharacteristic(characteristic);
        Log.d(TAG, "Leaving: writeCharacterisitc");
    }

    public void readTemperatureData(){
        Log.d(TAG, "Entering: readTemperatureData");
        BluetoothGattCharacteristic characteristic;

        Log.d(TAG, "Reading temperature data");
        characteristic = gatt.getService(SensorTag.IR_TEMPERATURE_SERVICE).getCharacteristic(SensorTag.IR_TEMPERATURE_DATA);
        gatt.readCharacteristic(characteristic);
        Log.d(TAG, "Leaving: readTemperatureData");
    }
    public void disableBluetooth(){
        if(btAdapter.isEnabled()){
            btAdapter.disable();
        }
    }

}
