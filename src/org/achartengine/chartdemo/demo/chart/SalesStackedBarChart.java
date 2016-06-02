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
 **/
package org.achartengine.chartdemo.demo.chart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.BubbleChart;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.chart.LineChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Parcelable;




/**
 * Sales demo bar chart.
 */
public class SalesStackedBarChart extends AbstractDemoChart {
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
	ArrayList<Parcelable> thdb=new ArrayList<Parcelable>();
  public String getName() {
    return "����ѹ������ѹ�ı仯";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "һ��������Ѫѹֵ��ƽ���仯���";
  }

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  @SuppressWarnings("deprecation")
public Intent execute(Context context ,ArrayList<? extends Parcelable> a) {
    String[] titles = new String[] { "����ѹ", "����ѹ" };
   
//    Intent getdata=getIntent();
//    thdb=getdata.getParcelableArrayListExtra("data");
    ArrayList<integer> h=(ArrayList<integer>) a.get(0);
    ArrayList<integer> l=(ArrayList<integer>) a.get(1);

    double[] hd=new double[h.size()];
    double[] ld=new double[l.size()];
    for (int i = 0; i < h.size(); i++) {
    	hd[i]=Double.parseDouble(String.valueOf(h.get(i)));
    	ld[i]=Double.parseDouble(String.valueOf(l.get(i)));
	}
    
    List<double[]> x = new ArrayList<double[]>();
    for (int i = 0; i < titles.length; i++) {
        x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
      }
    List<double[]> values = new ArrayList<double[]>();
//    values.add(new double[] {  129, 110, 113, 112,130, 121, 123, 119, 120, 123, 122,
//            119, 117,116, 129, 117, 126, 129, 122,116, 119, 112, 117, 125,117,128,119,129,112 ,117});
//    values.add(new double[] {  73, 77, 76, 77, 85,86, 78,79,88, 73,  84, 83, 79, 70, 84,73,82,75,71, 75,
// 	       86, 71, 89, 72, 63,75 ,73,81,84,90});
    values.add(hd);
    values.add(ld);
    int[] colors = new int[] { Color.YELLOW, Color.BLUE };
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);//����AbstractDemoChart�еķ�������renderer.   
    setChartSettings(renderer, " Ѫѹֵ��ƽ���仯���", "days", "Ѫѹֵ��ƽ���仯���", 0,8, 40, 180, Color.LTGRAY, Color.LTGRAY);
  //����AbstractDemoChart�еķ�������renderer��һЩ����.   
//    renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
//    renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
//    renderer.getSeriesRendererAt(0).s
    renderer.setXLabels(8);
    renderer.setYLabels(15);
    renderer.setXLabelsAlign(Align.LEFT);
    renderer.setYLabelsAlign(Align.LEFT);
    renderer.setPanEnabled(false, false);//�ֱ������Ƿ�����x��y�����Ҽ������ƶ�
    // renderer.setZoomEnabled(false);
    renderer.setBarSpacing(0.7f);
    renderer.setFitLegend(true);// �������ʵ�λ��
//    renderer.setXLabels(0);
//    renderer.setXLabels(1);
//    renderer.setXLabels(2);
//    renderer.setXLabels(3);
//    renderer.setXLabels(4);
//    renderer.setXLabels(5);
//    renderer.setXLabels(6);
//    renderer.setXLabels(7);
//    renderer.setXLabels(8);
//    renderer.addTextLabel(1, "��һ");
//    renderer.addTextLabel(2, "�ܶ�");
//    renderer.addTextLabel(3, "����");
//    renderer.addTextLabel(4, "����");
//    renderer.addTextLabel(5, "����");
//    renderer.addTextLabel(6, "����");
//    renderer.addTextLabel(7, "����");
//    renderer.setXAxisMax(8);
    XYValueSeries sunSeries = new XYValueSeries("Sunshine hours");
    sunSeries.add(1f, 35, 4.3);
    sunSeries.add(2f, 35, 4.9);
    sunSeries.add(3f, 35, 5.9);
    sunSeries.add(4f, 35, 8.8);
    sunSeries.add(5f, 35, 10.8);


    XYSeriesRenderer lightRenderer = new XYSeriesRenderer();
    lightRenderer.setColor(Color.YELLOW);
    
    
    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
    dataset.addSeries(0, sunSeries);
    
    String[] types = new String[] { BarChart.TYPE, BubbleChart.TYPE, LineChart.TYPE,
            CubicLineChart.TYPE };
    renderer.addSeriesRenderer(0, lightRenderer);
//    return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, values), renderer,
//        Type.STACKED);//����Intent, buildBarDataset�ǵ���AbstractDemochart�еķ���.
    
    return ChartFactory.getBarChartIntent(context, dataset, renderer,
            Type.STACKED);//����Intent, buildBarDataset�ǵ���AbstractDemochart�еķ���.
  }

private Intent getIntent() {
	// TODO Auto-generated method stub
	return null;
}



}
