package com.vogella.ble_fragmentsV5;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.vogella.ble_fragmentsV5.general.SensorTag;
import com.vogella.ble_fragmentsV5.services.BluetoothLeService;

/**
 * Created by Matthias on 14.12.2015.
 */
public class StateMachine {
    private States currentState;
    private final String TAG = StateMachine.class.getSimpleName();

    private BluetoothLeService btleService;
    private BluetoothGattCharacteristic currentCharacteristic;

    public void setBtleService(BluetoothLeService btleService){
        this.btleService = btleService;
    }

    public StateMachine(){
        resetStateMachine();
    }

    private void resetStateMachine(){
        Log.d(TAG, "Entering: resetStateMachine");
        currentState = States.NOT_ENABLED;
        Log.d(TAG, "Leaving: resetStateMachine");
    }

    /*public void startInitializationSequence(BluetoothGatt gatt, BluetoothGattService service){
        Log.d(TAG, "Entering: startInitializationSequence");
        currentState = States.ENABLING;
        enableSensor(gatt, service);
        Log.d(TAG, "Leaving: startInitializationSequence");
    }

    private void enableSensor(BluetoothGatt gatt, BluetoothGattService service){
        Log.d(TAG, "Entering: enableSensor");

        byte enableVal[];
        if(service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
            Log.d(TAG, "Enable temperature sensor");
            enableVal = new byte[]{0x01};
            btleService.writeCharacterisitc(service.getCharacteristic(SensorTag.IR_TEMPERATURE_CONFIG), enableVal);

        *//*}else if (service.getUuid().equals(SensorTag.RPM_SERVICE)){
            Log.d(TAG, "Enable RPM sensor");
            enableVal = new byte[]{0x02};
            //btleService.writeCharacterisitc(service.getCharacteristic(SensorTag.BAROMETRIC_PRESSURE_CONFIG), enableVal);

        }else if (service.getUuid().equals(SensorTag.OIL_PRESSURE_SERVICE)){
            Log.d(TAG, "Enable oil pressure sensor");
            enableVal = new byte[]{0x01};*//*
        }else{
            Log.d(TAG, "Error: Invalid BluetoothGattService");
        }
        Log.d(TAG, "Leaving: enableSensor");
    }*/

    public void advanceStateMachine(BluetoothGattService service, BluetoothGattCharacteristic optionalCharacterisitc){
        Log.d(TAG, "Entering: advanceStateMachine");
        Log.d(TAG, "Current state: " + currentState.toString());
        switch (currentState){
            case NOT_ENABLED:
                BluetoothGattCharacteristic characteristic = null;
                byte enableVal[] = null;
                if(service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
                    Log.d(TAG, "Enable temperature sensor");
                    enableVal = new byte[]{0x01};
                    characteristic = service.getCharacteristic(SensorTag.IR_TEMPERATURE_CONFIG);

                }else if (service.getUuid().equals(SensorTag.BAROMETRIC_PRESSURE_SERVICE)){
                    Log.d(TAG, "Enable barometric pressure sensor");
                    enableVal = new byte[]{0x02};
                    characteristic = service.getCharacteristic(SensorTag.BAROMETRIC_PRESSURE_CONFIG);

                }else {}
                currentState = States.ENABLING;
                btleService.writeCharacterisitc(characteristic, enableVal);
                break;

            case ENABLING:
                currentState = States.ENABLED;
                if (service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
                    Log.d(TAG, "Temperature sensor enabled");
                    Log.d(TAG, "Try to enable notification for temperature value");
                    currentState = States.NOTIFICATION_DISABLED;
                    btleService.enableNotification(SensorTag.IR_TEMPERATURE_DATA, service.getUuid());
                }else if(service.getUuid().equals(SensorTag.BAROMETRIC_PRESSURE_SERVICE)){
                    Log.d(TAG, "Barometric pressure sensor enabled");
                    Log.d(TAG, "Try to read calibration values");
                    currentState = States.NOT_CALIBRATED;
                    btleService.writeCharacterisitc(service.getCharacteristic(SensorTag.BAROMETRIC_PRESSURE_CONFIG), new byte[]{0x02});

                }
                break;
            case NOT_CALIBRATED:

                if (service.getUuid().equals(SensorTag.BAROMETRIC_PRESSURE_SERVICE)){
                    Log.d(TAG, "Barometric pressure sensor calibrated");
                    Log.d(TAG, "Try to enable notifications for pressure value");
                    if (optionalCharacterisitc != null) {
                        SensorTag.extractBarometricPressureCalibration(optionalCharacterisitc);
                        Log.d(TAG, "Barometric pressure sensor calibrated");
                        currentState = States.NOTIFICATION_DISABLED;
                        btleService.enableNotification(SensorTag.BAROMETRIC_PRESSURE_DATA, service.getUuid());
                    } else{
                        Log.d(TAG, "Cannot extract calibration values because of null pointer");
                    }
                }
                break;
            case NOTIFICATION_DISABLED:
                if (service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
                    Log.d(TAG, "Temperature sensor sucessfully initialized");
                    currentState = States.NOTIFICATION_ENABLED;
                }else if (service.getUuid().equals(SensorTag.BAROMETRIC_PRESSURE_SERVICE)){
                    Log.d(TAG, "Barometric pressure sensor successfully initialzied");
                    currentState = States.NOTIFICATION_ENABLED;
                }
                break;
            case NOTIFICATION_ENABLED:
                if (service.getUuid().equals(SensorTag.IR_TEMPERATURE_SERVICE)){
                    Log.d(TAG, "Temperature value changed");
                    btleService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, optionalCharacterisitc);
                }else if (service.getUuid().equals(SensorTag.BAROMETRIC_PRESSURE_SERVICE)){
                    Log.d(TAG, "Barometric pressure value changed");
                    btleService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, optionalCharacterisitc);
                }
                break;
            default:
                Log.d(TAG, "Something went terribly wrong");

        }
        Log.d(TAG, "Leaving: advanceStateMachine");
    }









    private enum States {IDLE, NOT_ENABLED, ENABLING ,ENABLED, NOT_CALIBRATED, CALIBRATED, NOTIFICATION_ENABLED, NOTIFICATION_DISABLED};
}
