package com.vogella.ble_fragmentsV5.general;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import static java.lang.Math.pow;


/**
 * Created by Matthias on 09.12.2015.
 */
/*SensorTag holds all the necessary bluetooth information and data conversion methods
  If RN4020 will be used this code needs to be changed*/
public class SensorTag {

    public static final UUID IR_TEMPERATURE_SERVICE = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    public static final UUID IR_TEMPERATURE_DATA = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    public static final UUID IR_TEMPERATURE_CONFIG = UUID.fromString("f000aa02-0451-4000-b000-000000000000");

    public static final UUID BAROMETRIC_PRESSURE_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    public static final UUID BAROMETRIC_PRESSURE_DATA = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    public static final UUID BAROMETRIC_PRESSURE_CONFIG = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
    public static final UUID BAROMETRIC_PRESSURE_CALIB = UUID.fromString("f000aa43-0451-4000-b000-000000000000");

    /*public static final UUID RPM_VALUE = UUID.fromString("abcdefg-0001-0002-0003-000000000000");
    public static final UUID RPM_SERVICE = UUID.fromString("abcdefg-0001-0002-0003-000000000000");
    public static final UUID OIL_PRESSURE_VALUE = UUID.fromString("abcdefg-0001-0002-0003-000000000000");
    public static final UUID OIL_PRESSURE_SERVICE = UUID.fromString("abcdefg-0001-0002-0003-000000000000");*/


    private static int[] c;

    public static double extractAmbientTemperature(BluetoothGattCharacteristic c) {
        int offset = 2;
        return shortUnsignedAtOffset(c, offset) / 128.0;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

        return (upperByte << 8) + lowerByte;
    }
    private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); // Note: interpret MSB as signed.

        return (upperByte << 8) + lowerByte;
    }

    public static double extractBarometricPressure(BluetoothGattCharacteristic characteristic){
        // c holds the calibration coefficients

        final Integer t_r;	// Temperature raw value from sensor
        final Integer p_r;	// Pressure raw value from sensor
        final Double t_a;    // Temperature actual value in unit centi degrees celsius
        final Double S;	// Interim value in calculation
        final Double O;	// Interim value in calculation
        final Double p_a; 	// Pressure actual value in unit Pascal.

        t_r = shortSignedAtOffset(characteristic, 0);
        p_r = shortUnsignedAtOffset(characteristic, 2);

        t_a = (100 * (c[0] * t_r / pow(2,8) + c[1] * pow(2,6))) / pow(2,16);
        S = c[2] + c[3] * t_r / pow(2,17) + ((c[4] * t_r / pow(2,15)) * t_r) / pow(2,19);
        O = c[5] * pow(2,14) + c[6] * t_r / pow(2,3) + ((c[7] * t_r / pow(2,15)) * t_r) / pow(2,4);
        p_a = (S * p_r + O) / pow(2,14);

        return (p_a/100);
    }

    public static void extractBarometricPressureCalibration(BluetoothGattCharacteristic characteristic){

        int NUMBER_CALIBRATION_VALUES = 8;
        int[] calibrationValue = new int[NUMBER_CALIBRATION_VALUES];


        calibrationValue[0] = shortUnsignedAtOffset(characteristic,0);
        calibrationValue[1] = shortUnsignedAtOffset(characteristic,2);
        calibrationValue[2] = shortUnsignedAtOffset(characteristic,4);
        calibrationValue[3] = shortUnsignedAtOffset(characteristic,6);
        calibrationValue[4] = shortSignedAtOffset(characteristic, 8);
        calibrationValue[5] = shortSignedAtOffset(characteristic,10);
        calibrationValue[6] = shortSignedAtOffset(characteristic,12);
        calibrationValue[7] = shortSignedAtOffset(characteristic,14);

        c = calibrationValue;
    }
}
