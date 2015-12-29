package com.vogella.ble_fragmentsV5.fragments.GeneralDisplayFragmentClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vogella.ble_fragmentsV5.R;
import com.vogella.ble_fragmentsV5.general.BroadcastFilters;
import com.vogella.ble_fragmentsV5.general.SensorTag;
import com.vogella.ble_fragmentsV5.services.BluetoothLeService;

import org.achartengine.GraphicalView;

import java.util.Locale;

/**
 * Created by Matthias on 12.12.2015.
 */
public class Fragment_GeneralDisplay extends android.support.v4.app.Fragment {


    //references for view elements
    private TextView tvTemperature;
    private TextView tvPressure;
    private TextView tvRPM;
    private LinearLayout chart;
    private DialChart dial;
    private GraphicalView view;

    //individual tag for fragment
    private static final String TAG = Fragment_GeneralDisplay.class.getSimpleName();

    //minimum and maximum values for individual measurements
    private final double MIN_TEMP = -10;
    private final double MAX_TEMP = 100;
    private final double MIN_PRESSURE = 1;
    private final double MAX_PRESSURE = 80;
    private final int MIN_RPM = 1000;
    private final int MAX_RPM = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "Entering: OnCreate");
        super.onCreate(savedInstanceState);
        dial = new DialChart();
        Log.d(TAG, "Leaving: OnCreate");
    }

    @Override
    public void onResume(){
        Log.d(TAG, "Entering: onResume");
        //internal broadcast receiver has to be registered so that it can listen to dedicated broadcast messages
        super.onResume();
        clearDisplay();
        getActivity().registerReceiver(mGattUpdateReceiver, BroadcastFilters.getGeneralDisplayFragmentFilter());
        Log.d(TAG, "Leaving: onResume");
    }

    public void onPause(){
        Log.d(TAG, "Entering: onPause");
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        Log.d(TAG, "Leaving: onPause");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.d(TAG, "Entering: onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        //Clear all view elements as onCreateView() was called before this method ==> All internal view references are initialized
        clearDisplay();
        view = dial.getView(getContext());
        chart.removeAllViews();
        chart.addView(view);
        Log.d(TAG, "Leaving: onActivityCreated");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Entering: onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_general_display, container, false);

        tvTemperature = (TextView)rootView.findViewById(R.id.valueTemperature);
        tvPressure = (TextView)rootView.findViewById(R.id.valuePressure);
        tvRPM = (TextView)rootView.findViewById(R.id.valueRPM);
        chart = (LinearLayout)rootView.findViewById(R.id.rpmChart);

        Log.d(TAG, "Leaving: onCreateView");
        return rootView;
    }

    private void clearDisplay(){
        Log.d(TAG, "Entering: clearDisplay");
        int black = getResources().getColor(R.color.black);

        tvTemperature.setTextColor(black);
        tvTemperature.setText(getResources().getString(R.string.noValues));
        tvPressure.setTextColor(black);
        tvPressure.setText(getResources().getString(R.string.noValues));
        tvRPM.setTextColor(black);
        tvRPM.setText(getResources().getString(R.string.noValues));
        Log.d(TAG, "Leaving: clearDisplay");
    }

    public void updateTemperature(double temperature){
        Log.d(TAG, "Entering: updateTemperature");
        //Check if value is either near minimum or maximum. Change text color accordingly
        tvTemperature.setTextColor(checkMinMax(temperature, Measurement.TEMPERATURE));
        //Only show two decimal places and user german separator (',')
        tvTemperature.setText(String.format(Locale.GERMAN, "%.2f", temperature));
        dial.addNewSpeeds(temperature* 100);
        view.repaint();
        Log.d(TAG, "Leaving: updateTemperature");
    }

    public void updateRPM(int rpm){
        Log.d(TAG, "Entering: updateRPM");
        tvRPM.setTextColor(checkMinMax(rpm, Measurement.RPM));
        tvRPM.setText(String.format("%d 1/min", rpm));
        Log.d(TAG, "Leaving: updateRPM");
    }
    public void updatePressure(double pressure){
        Log.d(TAG, "Entering: updatePressure");
        tvPressure.setTextColor(checkMinMax(pressure, Measurement.PRESSURE));
        tvPressure.setText(String.format("%.2f bar", pressure));
        Log.d(TAG, "Leaving: updatePressure");
    }

    private int checkMinMax(double value, final Measurement selector){
        double localMin, localMax, offset;

        //Offset specifies the borders for min and max
        //0.10 means that a different color is used if the value is < 10% or > 90% of its range
        offset = 0.10;
        //Get corresponding min and max values according to the current update method
        switch (selector){
            case TEMPERATURE:
                localMin = MIN_TEMP;
                localMax = MAX_TEMP;
                break;
            case PRESSURE:
                localMin = MIN_PRESSURE;
                localMax = MAX_PRESSURE;
                break;
            case RPM:
                localMin = MIN_RPM;
                localMax = MAX_RPM;
                break;
            default:
                localMax = Double.MIN_VALUE;
                localMin = Double.MIN_VALUE;
                Log.d(TAG, "Severe error during checking min and max values");
                getActivity().finish();
                break;

        }
        if (value > localMax*(1-offset) || value < localMin*offset){
            return getResources().getColor(R.color.signalBad);
        }
        else{
            return getResources().getColor(R.color.black);
        }

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Entering: onReceive() in " + Fragment_GeneralDisplay.class.getSimpleName());
            final String action = intent.getAction();
            switch (action){
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    clearDisplay();
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    //get data from intent
                    //display it
                    String UUID = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    if (UUID.equals(SensorTag.IR_TEMPERATURE_DATA.toString())){
                        //Data from temperature sensor was received
                        updateTemperature(Double.parseDouble(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)));

                    /*}else if(UUID.equals(SensorTag.RPM_VALUE.toString())){
                        //Data from RPM sensor was received
                        updateRPM(Integer.parseInt(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)));
                    }else if (UUID.equals(SensorTag.OIL_PRESSURE_VALUE.toString())){
                        //Data from oil pressure sensor was received
                        updatePressure(Double.parseDouble(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)));*/
                    }else{
                        Log.d(TAG, "Error: Unknown data was received");
                        getActivity().finish();
                    }
                    //TODO Methodenaufrufe für Drehzahl und Öldruck noch hinzufügen
                    break;
            }
            Log.d(TAG, "Leaving: onReceive() in " + Fragment_GeneralDisplay.class.getSimpleName());

        }
    };
    private enum Measurement {TEMPERATURE, PRESSURE, RPM};

}


