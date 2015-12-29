package com.vogella.ble_fragmentsV5.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Matthias on 23.12.2015.
 */
public class Model extends Observable implements I_Observable{
    //For debug purposes only
    private final String TAG = Model.class.getSimpleName();

    //Maximum data stored in the internal database
    private final int MAX_DATA = 500;

    //Actual dataset for temperature value
    private ArrayList<MeasurementValue> temperatureData;
    private ArrayList<MeasurementValue> pressureData;
    private ArrayList<MeasurementValue> rpmData;
    private double avgPressure;
    private double avgRPM;
    private double avgTemperature;
    private int second;

    public Model(){
        temperatureData = new ArrayList<>();
        pressureData = new ArrayList<>();
        rpmData = new ArrayList<>();
        for(int i = 0; i < 10; ++i){
            pressureData.add(new MeasurementValue(i*10, i));
            rpmData.add(new MeasurementValue(i*5, i));
        }
        avgPressure = calculateAverage(pressureData);
        avgRPM = calculateAverage(rpmData);
        avgTemperature = calculateAverage(temperatureData);
        second = 0;
    }

    public void addTemperatureData(double tempData, int seconds){
        Log.d(TAG, "Entering: addTemperatureData");
        if (temperatureData.size() == MAX_DATA){
            Log.d(TAG, "Reached maximum data");
            temperatureData.remove(0);
        }
        temperatureData.add(new MeasurementValue(tempData, seconds));
        Log.d(TAG, "Leaving: addTemperatureData");
    }

    public void addTemperatureData(double tempData){
        Log.d(TAG, "Entering: addTemperatureData (double)");

        //Check if database is bigger than the maximum
        if (temperatureData.size() == MAX_DATA){
            Log.d(TAG, "Reached maximum data");
            //Remove the first dataset and create a dynamic behaviour
            temperatureData.remove(0);

        }
        temperatureData.add(new MeasurementValue(tempData, second));
        second++;
        //Update the average temperature
        calculateAverageTemperature();

        //Notify observer that new data is available
        this.setChanged();
        this.notifyObservers(DATATYPE.TEMPERATURE);
        Log.d(TAG, "Leaving: addTemperatureData (double)");
    }
    //TODO Hochzählen der Sekunden muss noch geändert werden und direkt vom Timer des Boards übernommen werden
    public void addPressureData(double pressData){
        Log.d(TAG, "Entering: addPressureData (double");
        if (pressureData.size() == MAX_DATA){
            Log.d(TAG, "Reached maximum data");
            pressureData.remove(0);
        }
        pressureData.add(new MeasurementValue(pressData, second));
        second++;
        avgPressure = calculateAverageAtUpdate(pressureData, avgPressure);
        this.setChanged();
        this.notifyObservers(DATATYPE.PRESSURE);
        Log.d(TAG, "Leaving: addPressureData (double");
    }
    //TODO Hochzählen der Sekunden muss noch geändert werden und direkt vom Timer des Boards übernommen werden
    public void addRPMData(double rpm){
        Log.d(TAG, "Entering: addRPMData (double");
        if (rpmData.size() == MAX_DATA){
            Log.d(TAG, "Reached maximum data");
            rpmData.remove(0);
        }
        rpmData.add(new MeasurementValue(rpm, second));
        second++;
        avgRPM = calculateAverageAtUpdate(rpmData, avgRPM);
        this.setChanged();
        this.notifyObservers(DATATYPE.RPM);
        Log.d(TAG, "Leaving: addRPMData (double");
    }

    public double getAvgTemperature() {
        return avgTemperature;
    }

    @Override
    public MeasurementValue getLatestTemperatureValue() {
        return temperatureData.get(temperatureData.size()-1);
    }

    @Override
    public ArrayList<MeasurementValue> getAllData(DATATYPE type) {
        switch (type){
            case TEMPERATURE:
                return temperatureData;
            case PRESSURE:
                return pressureData;
            case RPM:
                return rpmData;
            default:
                return null;
        }

    }

    public void clear(){
        Log.d(TAG, "Entering: clear");
        temperatureData.clear();
        pressureData.clear();
        rpmData.clear();
        Log.d(TAG, "Leaving: clear");
    }

    public ArrayList<MeasurementValue> getAllDatasets(){
        return temperatureData;
    }

    private void calculateAverageTemperature(){
        if(temperatureData.size() == 1){
            avgTemperature = temperatureData.get(0).getValue();
        }else{
            //new value    =  (old value * (current number values - 1) + latest value)/current number values
            avgTemperature = (avgTemperature * (temperatureData.size()-1)+temperatureData.get(temperatureData.size()-1).getValue())/temperatureData.size();
        }
    }
    private double calculateAverageAtUpdate(ArrayList<MeasurementValue> list, double currentAverage){
        double retValue = 0;
        if (list.size() == 1){
            retValue = list.get(0).getValue();
        }else{
            retValue = (currentAverage * (list.size()-1)+list.get(list.size()-1).getValue()/list.size());
        }
        return retValue;
    }
    public double getAverage(DATATYPE type){
        switch (type){
            case TEMPERATURE:
                return avgTemperature;
            case PRESSURE:
                return avgPressure;
            case RPM:
                return avgRPM;
            default:
                return 0;
        }
    }
    public MeasurementValue getLatestValue (DATATYPE type){
        switch (type){
            case TEMPERATURE:
                return temperatureData.get(temperatureData.size() - 1);
            case PRESSURE:
                return pressureData.get(pressureData.size() - 1);
            case RPM:
                return rpmData.get(rpmData.size() - 1);
            default:
                return null;

        }
    }
    private double calculateAverage(ArrayList<MeasurementValue> list){
        double retValue = 0;
        if (list.isEmpty() == false){

            for (MeasurementValue m : list){
                retValue += m.getValue();
            }
            retValue /= list.size();
        }
        return retValue;
    }

}
