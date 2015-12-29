package com.vogella.ble_fragmentsV5.fragments.PlotFragmentClasses;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vogella.ble_fragmentsV5.MainActivity;
import com.vogella.ble_fragmentsV5.R;
import com.vogella.ble_fragmentsV5.model.DATATYPE;
import com.vogella.ble_fragmentsV5.model.I_Observable;
import com.vogella.ble_fragmentsV5.model.MeasurementValue;

import org.achartengine.GraphicalView;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Matthias on 23.12.2015.
 */
public class Fragment_Plots extends Fragment implements Observer, View.OnTouchListener {
    private final String TAG = Fragment_Plots.class.getSimpleName();
    private I_Observable model;
    private LineChart tempChart;
    private LineChart pressChart;
    private LineChart rpmChart;
    private LineChart selectedChart;
    private GraphicalView view;
    private TextView tvAverageValue;
    private boolean doubleTapDetected;
    private boolean zoomDetected;
    private DATATYPE selectedType = DATATYPE.TEMPERATURE;
    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            doubleTapDetected = true;
            Log.d(TAG, "Entering: onDoubleTap");
            zoomDetected = false;
            redrawChart();
            Log.d(TAG, "Leaving: onDoubleTap");

            return super.onDoubleTap(e);
        }



    });



    private LinearLayout chart;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Entering: onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_plot, container, false);
        chart = (LinearLayout)rootView.findViewById(R.id.chart);
        tvAverageValue = (TextView)rootView.findViewById(R.id.averageValue);
        setHasOptionsMenu(true);

        //drawChart();

        Log.d(TAG, "Leaving: onCreateView");
        return rootView;
    }

    private void drawChart(){
        view = selectedChart.getView(getActivity());
        chart.removeAllViews();
        chart.addView(view);
        tvAverageValue.setText(getCurrentAverageAsString(selectedType));
        view.addZoomListener(zoomListener, true, true);
        view.setOnTouchListener(this);
    }

    private String getCurrentAverageAsString(DATATYPE selType){
        String retValue;
        switch (selType){
            case TEMPERATURE:
                retValue = String.format(Locale.GERMAN, " %.2f °C", model.getAverage(selectedType));
                break;
            case PRESSURE:
                retValue = String.format(Locale.GERMAN, " %.2f bar", model.getAverage(selectedType));
                break;
            case RPM:
                retValue = String.format(Locale.GERMAN, " %.0f 1/min", model.getAverage(selectedType));
                break;
            default:
                retValue = null;
                break;

        }
        return retValue;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "Entering: onCreate");
        super.onCreate(savedInstanceState);
        model = ((MainActivity)getActivity()).getModel();
        tempChart = new LineChart("Temperatur in °C", "Temperatur", Color.BLUE);
        pressChart = new LineChart("Öldruck in bar", "Öldruck", Color.GREEN);
        rpmChart = new LineChart("Umdrehungen pro Minute", "Umdrehungen", Color.RED);
        selectedChart = tempChart;
        Log.d(TAG, "Leaving: onCreate");
    }
    private void initLineChart(){
        Log.d(TAG, "Entering: initLineChart");
        ArrayList<MeasurementValue> allData = model.getAllData(selectedType);
        if (allData != null){
            Log.d(TAG, "Valid data available in model");
            selectedChart.initializeChart(allData);
        }
        //

        Log.d(TAG, "Leaving: initLineChart");
    }


    @Override
    public void onResume() {
        Log.d(TAG, "Entering: onResume");
        super.onResume();

        initChartSize();
        drawChart();

        initLineChart();
        tvAverageValue.setText("");
        ((MainActivity)getActivity()).getModel().addObserver(this);
        Log.d(TAG, "Leaving: onResume");
    }
    private void initAvg(){

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.options_menu, menu);
    }

    public void onPause() {
        Log.d(TAG, "Entering: onPause");
        super.onPause();
        ((MainActivity)getActivity()).getModel().deleteObserver(this);

        Log.d(TAG, "Entering: onPause");
    }

    @Override
    public void onStart(){
        Log.d(TAG, "Entering: onStart");
        super.onStart();
        Log.d(TAG, "Leaving: onStart");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retValue;

        switch (item.getItemId()) {
            case (R.id.menu_temperature):
                Toast.makeText(getContext(), "Temperature selected", Toast.LENGTH_SHORT).show();
                selectedChart = tempChart;
                selectedType = DATATYPE.TEMPERATURE;
                retValue = true;
                break;
            case (R.id.menu_pressure):
                Toast.makeText(getContext(), "Pressure selected", Toast.LENGTH_SHORT).show();
                selectedChart = pressChart;
                selectedType = DATATYPE.PRESSURE;
                retValue = true;
                break;
            case (R.id.menu_rpm):
                Toast.makeText(getContext(), "RPM selected", Toast.LENGTH_SHORT).show();
                selectedChart = rpmChart;
                selectedType = DATATYPE.RPM;
                retValue = true;
                break;
            default:
                retValue =  super.onOptionsItemSelected(item);
                break;
        }
        initChartSize();
        initLineChart();
        drawChart();
        return retValue;
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d(TAG, "Entering: update");

        if (selectedChart != null && view != null) {
            Log.d(TAG, "Init was successful. New data can be added");
            if (data instanceof DATATYPE){
                DATATYPE datatype = (DATATYPE)data;
                switch (datatype){
                    case TEMPERATURE:
                        tempChart.addDataPoint((model.getLatestValue(datatype)));
                        break;
                    case PRESSURE:
                        pressChart.addDataPoint(model.getLatestValue(datatype));
                        break;
                    case RPM:
                        rpmChart.addDataPoint(model.getLatestValue(datatype));
                        break;
                }
                if (datatype == selectedType)
                    tvAverageValue.setText(getCurrentAverageAsString(selectedType));

                if (zoomDetected == false) {
                    redrawChart();
                }
            }




        }
        Log.d(TAG, "Leaving: update");

    }

    private void redrawChart() {
        selectedChart.adjustAxis();
        view.repaint();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Entering: onTouch");
        boolean retValue = false;
        if(tempChart.isInitialized()){
            gestureDetector.onTouchEvent(event);
            //Check if the simple gesture detector detected a double tap
            if(doubleTapDetected == true){
                //If so, the underlying gesture detector of the chart does not need to get the touch event
                retValue = true;
            }
            doubleTapDetected = false;


        }
        Log.d(TAG, "Leaving: onTouch");
        //By returning true, the event is not propagated to the chart
        //By returning false, it's propagated
        return retValue;
    }
    private void initChartSize(){
        /*Configuration configuration = getActivity().getResources().getConfiguration();
        int screenHeightDp = configuration.screenHeightDp; //The current width of the available screen space, in dp units, corresponding to screen height resource qualifier.*/
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chart.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        params.height = (int)Conversion.convertPixelsToDp(metrics.heightPixels - tvAverageValue.getHeight());
    }



    private ZoomListener zoomListener = new ZoomListener() {
        @Override
        public void zoomApplied(ZoomEvent zoomEvent) {
            Log.d(TAG, "Entering: zoomApplied");
            zoomDetected = true;
            Log.d(TAG, "Leaving: zoomApplied");

        }

        @Override
        public void zoomReset() {
            Log.d(TAG, "Entering: zoomReset");
            //view.zoomReset();
            Log.d(TAG, "Leaving: zoomReset");

        }
    };




}
