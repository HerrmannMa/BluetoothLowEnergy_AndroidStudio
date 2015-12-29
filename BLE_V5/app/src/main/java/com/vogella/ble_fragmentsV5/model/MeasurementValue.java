package com.vogella.ble_fragmentsV5.model;

/**
 * Created by Matthias on 23.12.2015.
 */
//MeasurementValue is a holder class for every measurement. It just stores the data and the corresponding time
public class MeasurementValue {
    private double value;
    private int seconds;


    public MeasurementValue(double value, int seconds){
        this.value = value;
        this.seconds = seconds;
    }

    public void setTimestamp(int seconds) {
        this.seconds = seconds;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public int getSeconds() {
        return seconds;
    }
}
