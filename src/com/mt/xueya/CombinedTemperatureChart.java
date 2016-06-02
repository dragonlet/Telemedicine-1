/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mt.xueya;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;

import org.achartengine.chart.BubbleChart;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Parcelable;
import android.util.Log;

/**
 * Combined temperature demo chart.
 */
public class CombinedTemperatureChart extends AbstractDemoChart {
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Combined temperature";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "The average temperature in 2 Greek islands, water temperature and sun shine hours (combined chart)";
  }

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context,ArrayList<? extends Parcelable> a) {
    String[] titles = new String[] {  "收缩压", "舒张压"};
    
    ArrayList<integer> high=(ArrayList<integer>) a.get(0);
    ArrayList<integer> low=(ArrayList<integer>) a.get(1);
    ArrayList<integer> pulse=(ArrayList<integer>) a.get(2);
//    double[] hd=new double[high.size()];
    double[] hd=new double[pulse.size()];
    double[] ld=new double[low.size()];
    for (int i = 0; i < high.size(); i++) {
//    	hd[i]=Double.parseDouble(String.valueOf(high.get(i)));
    	hd[i]=Double.parseDouble(String.valueOf(high.get(i)));
    	ld[i]=Double.parseDouble(String.valueOf(low.get(i)));
	}
    
    List<double[]> x = new ArrayList<double[]>();
    for (int i = 0; i < titles.length; i++) {
    	
    	
    	double[] arr=new double[ld.length];
    	for (int j = 0; j < arr.length; j++) {
			arr[j]=j+1;
		}
    	x.add(arr);
    	
    	
//      x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
    }
//    for (int i = 0; i < titles.length; i++) {
//      x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
//    }
    List<double[]> values = new ArrayList<double[]>();
//    values.add(new double[] { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4, 26.1, 23.6, 20.3, 17.2});
//    values.add(new double[] { 9, 10, 11, 15, 19, 23, 26, 25, 22, 18, 13,});
    values.add(hd);
    values.add(ld);
    int[] colors = new int[] { Color.YELLOW, Color.BLUE};
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    renderer.setPointSize(5.5f);
    int length = renderer.getSeriesRendererCount();
    for (int i = 0; i < length; i++) {
      XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
      r.setLineWidth(5);
      r.setChartValuesTextSize(20);
      r.setDisplayChartValues(true);
      r.setDisplayChartValuesDistance(30);
      r.setFillPoints(true);
    }
    setChartSettings(renderer, "本周记录", "星期", "值", 0, 8,0, 180,
        Color.LTGRAY, Color.LTGRAY);

    renderer.setXLabels(12);
    renderer.setYLabels(10);
    renderer.setShowGrid(true);
    renderer.setXLabelsAlign(Align.RIGHT);
    renderer.setYLabelsAlign(Align.RIGHT);
    renderer.setZoomButtonsVisible(true);
    renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
    renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
//    renderer.setPanEnabled(false, false);
//    renderer.setDisplayChartValues(true);
    
    renderer.setLegendTextSize(20);
    XYValueSeries sunSeries = new XYValueSeries("Sunshine hours");
    sunSeries.add(1f, 35, 4.3);
    sunSeries.add(2f, 35, 4.9);
    sunSeries.add(3f, 35, 5.9);
    sunSeries.add(4f, 35, 8.8);
    sunSeries.add(5f, 35, 10.8);
    sunSeries.add(6f, 35, 11.9);
    sunSeries.add(7f, 35, 13.6);
    sunSeries.add(8f, 35, 12.8);
    sunSeries.add(9f, 35, 11.4);
    sunSeries.add(10f, 35, 9.5);
    sunSeries.add(11f, 35, 7.5);
    sunSeries.add(12f, 35, 5.5);
    XYSeriesRenderer lightRenderer = new XYSeriesRenderer();
    lightRenderer.setColor(Color.YELLOW);

    XYSeries waterSeries = new XYSeries("脉搏");
    for (int i = 0; i < high.size(); i++) {
    	Log.e("String.valueOf(pulse.get(i))=", String.valueOf(pulse.get(i)));
		waterSeries.add(i+1, Double.parseDouble(String.valueOf(pulse.get(i))));
		
	}
//    waterSeries.add(1, 16);
//    waterSeries.add(2, 15);
//    waterSeries.add(3, 16);
//    waterSeries.add(4, 17);
//    waterSeries.add(5, 20);
//    waterSeries.add(6, 23);
//    waterSeries.add(7, 25);
//    waterSeries.add(8, 25.5);
//    waterSeries.add(9, 26.5);
//    waterSeries.add(10, 24);
//    waterSeries.add(11, 22);
//    waterSeries.add(12, 18);
    renderer.setBarSpacing(0.1);
    
    XYSeriesRenderer waterRenderer = new XYSeriesRenderer();
    waterRenderer.setColor(Color.RED);
    waterRenderer.setLineWidth(5);
    waterRenderer.setDisplayChartValuesDistance(30);

    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
//    dataset.addSeries(0, sunSeries);
    Log.e("dataset.getSeriesCount()", String.valueOf(dataset.getSeriesCount()));
//    dataset.addSeries(0, waterSeries);
    dataset.addSeries(waterSeries);
    Log.e("增加后的dataset.getSeriesCount()", String.valueOf(dataset.getSeriesCount()));
//    renderer.addSeriesRenderer(0, lightRenderer);
//    renderer.addSeriesRenderer(0, waterRenderer);
    waterRenderer.setPointStyle(PointStyle.CIRCLE);
    renderer.addSeriesRenderer(waterRenderer);    
    waterRenderer.setDisplayChartValues(true);
    waterRenderer.setChartValuesTextSize(20);
//    String[] types = new String[] { CubicLineChart.TYPE, CubicLineChart.TYPE ,CubicLineChart.TYPE
//    };
    String[] types = new String[] { BarChart.TYPE, BarChart.TYPE ,CubicLineChart.TYPE
        };
//  String[] types = new String[] { CubicLineChart.TYPE, CubicLineChart.TYPE, CubicLineChart.TYPE };
    Intent intent = ChartFactory.getCombinedXYChartIntent(context, dataset, renderer, types,
        "本周记录");
    return intent;
  }

}
