package com.vogella.ble_fragmentsV5.fragments.PlotFragmentClasses;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.vogella.ble_fragmentsV5.model.MeasurementValue;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

/**
 * Created by Matthias on 24.12.2015.
 */
public class LineChart {
    private final String TAG = LineChart.class.getSimpleName();
    private GraphicalView view;
    private TimeSeries seriesXY;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private boolean initialized;

    public LineChart(String yTitle, String seriesName, int color){
        seriesXY = new TimeSeries(seriesName);
        mDataset.addSeries(seriesXY);
        //mDataset.addSeries(avgTemperatue);


        //prepare layout for temperature chart
        xyRenderer.setColor(color);
        xyRenderer.setLineWidth(Conversion.convertDpToPixel(5));
        xyRenderer.setStroke(BasicStroke.SOLID);
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        xyRenderer.setFillPoints(true);



        mRenderer.addSeriesRenderer(xyRenderer);
        //mRenderer.addSeriesRenderer(avgTempRenderer);

        mRenderer.setShowLegend(true);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.DKGRAY);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setAxesColor(Color.DKGRAY);

        mRenderer.setYTitle(yTitle);
        mRenderer.setXTitle("Time in s");

        /*double[] panlimits = {0,100,20,40};
        mRenderer.setZoomLimits(panlimits);*/
        //mRenderer.setDisplayChartValues(true);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setXLabelsAlign(Paint.Align.LEFT);
        mRenderer.setShowLegend(true);
        mRenderer.setAxisTitleTextSize(Conversion.convertDpToPixel(15));
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setLabelsTextSize(Conversion.convertDpToPixel(12));
        mRenderer.setZoomButtonsVisible(false);
        //prepare chart layout

        mRenderer.setMargins(new int[]{0, (int)Conversion.convertDpToPixel(12), (int)Conversion.convertDpToPixel(12), 0}); // 5dp on the left to show that little line
        mRenderer.setFitLegend(true);

        //mRenderer.setZoomButtonsVisible(false); // bye bye zoom
        mRenderer.setShowAxes(true); // show both axes

        mRenderer.setShowLabels(true); // See the values
        mRenderer.setYLabelsAlign(Paint.Align.LEFT); // put the Y labels on the left of the axis
        //mRenderer.setZoomButtonsVisible(true);

        mRenderer.setZoomEnabled(true, true);
        mRenderer.setExternalZoomEnabled(true);
        // And from here proceed to add the TimeCharts with using
        //renderer.setDisplayChartValues(false);
        initialized = false;


    }


    public void initializeChart(ArrayList<MeasurementValue> values){
        for (MeasurementValue currentValue : values){
            seriesXY.add(currentValue.getSeconds(), currentValue.getValue());
        }
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void addDataPoint (MeasurementValue value){
        Log.d(TAG, "Time: " + value.getSeconds() + " Value: " + value.getValue());
        seriesXY.add(value.getSeconds(), value.getValue());
    }


    public GraphicalView getView(Context context){
        view = ChartFactory.getLineChartView(context,mDataset, mRenderer);
        return view;
    }


    public void adjustAxis(){
        double minX = 0;
        double maxX = seriesXY.getMaxX();
        double minY = (seriesXY.getMinY()*0.9);
        double maxY = (seriesXY.getMaxY()*1.1);

        mRenderer.setXAxisMin(minX);
        mRenderer.setXAxisMax(maxX);
        mRenderer.setYAxisMin(minY);
        mRenderer.setYAxisMax(maxY);

    }



}
