package com.vogella.ble_fragmentsV5.fragments.GeneralDisplayFragmentClasses;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

/**
 * Created by Matthias on 29.12.2015.
 */
public class DialChart {
    private GraphicalView view;

    CategorySeries category = new CategorySeries("RPM");
    DialRenderer renderer = new DialRenderer();
    SimpleSeriesRenderer r = new SimpleSeriesRenderer();

    public DialChart()
    {
        category.add("Geschwindigkeit", 0);
        //renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        //renderer.setLegendTextSize(15);
        renderer.setMargins(new int[] {0, 0, 0, 0});
        r.setColor(Color.RED);
        renderer.addSeriesRenderer(r);
        renderer.setLabelsTextSize(20);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setShowLabels(true);
        renderer.setVisualTypes(new DialRenderer.Type[] {DialRenderer.Type.NEEDLE});
        renderer.setMinValue(0);
        renderer.setMaxValue(8000);
        renderer.setMajorTicksSpacing(1000);
        renderer.setShowLegend(false);
        renderer.setMinorTicksSpacing(250);


        renderer.setAngleMax(45);
        renderer.setAngleMin(315);

    }

    public GraphicalView getView(Context context)
    {
        view =  ChartFactory.getDialChartView(context, category, renderer);
        return view;
    }

   public void addNewSpeeds(double v)
    {
        category.set(0,"Geschwindigkeit", v);
    }

}
