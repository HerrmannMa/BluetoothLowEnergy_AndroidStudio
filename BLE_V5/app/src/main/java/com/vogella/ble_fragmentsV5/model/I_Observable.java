package com.vogella.ble_fragmentsV5.model;

import java.util.ArrayList;

/**
 * Created by Matthias on 24.12.2015.
 */
public interface I_Observable {
    public double getAvgTemperature();
    public MeasurementValue getLatestTemperatureValue();
    public ArrayList<MeasurementValue> getAllData(DATATYPE type);
    public double getAverage(DATATYPE datatype);
    public MeasurementValue getLatestValue(DATATYPE type);
}
