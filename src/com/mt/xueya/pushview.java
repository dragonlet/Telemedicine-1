package com.mt.xueya;


import java.io.FileInputStream;
import java.io.IOException;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.util.EncodingUtils;

import com.android.ecg.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class pushview extends Activity{
	private final String SD_PATH=android.os.
			Environment.
			getExternalStorageDirectory().
			getAbsolutePath();
//返回扩展存储区(SDCard)目录
public final String File_Path=SD_PATH+"/SaveTData";
private  String file_txt_name;
public SharedPreferences Inform = null;
public String userid;
public String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lineview);
		 Inform = getSharedPreferences("perference", MODE_PRIVATE);

			userid = Inform.getString("USERID", "");
			username = Inform.getString("UserName", "");
			file_txt_name = username+"pulse.txt";
			lineView();
	}
	public void lineView(){  
        //同样是需要数据dataset和视图渲染器renderer  
        XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();  
        XYSeries  series = new XYSeries("高压"); 
        XYSeries  seriesTwo = new XYSeries("低压"); 
        XYSeries  seriesThress = new XYSeries("脉搏"); 
        try {
         if( !readFileSdcardFile().equals("")){
			String[] str = readFileSdcardFile().split(",");
			Log.v("sda", str.toString());
			for(int i=0;i<str.length;i++){
				if(i%3==0){
					series.add(i, Double.parseDouble(str[i]));
				}else if((i+2)%3==0){
					seriesTwo.add(i, Double.parseDouble(str[i]));
				}else if((i+1)%3==0){
					seriesThress.add(i, Double.parseDouble(str[i]));
				}
				
				
			}
         }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        series.add(1, 6);  
//        series.add(2, 5);  
//        series.add(3, 7);  
//        series.add(4, 4);  
        mDataset.addSeries(series);  
     //   XYSeries  seriesTwo = new XYSeries("第二条线");  
//        seriesTwo.add(1, 4);  
//        seriesTwo.add(2, 6);  
//        seriesTwo.add(3, 3);  
//        seriesTwo.add(4, 7);  
        mDataset.addSeries(seriesTwo);  
        mDataset.addSeries(seriesThress);  
          
          
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();  
        //设置图表的X轴的当前方向  
        mRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);  
        mRenderer.setXTitle("");//设置为X轴的标题  
        mRenderer.setYTitle("");//设置y轴的标题  
        mRenderer.setAxisTitleTextSize(20);//设置轴标题文本大小  
        mRenderer.setChartTitle("报表");//设置图表标题  
        mRenderer.setChartTitleTextSize(30);//设置图表标题文字的大小  
        mRenderer.setLabelsTextSize(18);//设置标签的文字大小  
        mRenderer.setLegendTextSize(20);//设置图例文本大小  
        mRenderer.setPointSize(10f);//设置点的大小  
        mRenderer.setYAxisMin(0);//设置y轴最小值是0  
        mRenderer.setYAxisMax(180);  
        mRenderer.setYLabels(10);//设置Y轴刻度个数（貌似不太准确）  
        //mRenderer.setXAxisMax();  
        //mRenderer.setShowGrid(true);//显示网格  
        //将x标签栏目显示如：1,2,3,4替换为显示1月，2月，3月，4月  
//        mRenderer.addXTextLabel(1, "1月");  
//        mRenderer.addXTextLabel(2, "2月");  
//        mRenderer.addXTextLabel(3, "3月");  
//        mRenderer.addXTextLabel(4, "4月");  
  //      mRenderer.setXLabels(0);//设置只显示如1月，2月等替换后的东西，不显示1,2,3等  
        mRenderer.setMargins(new int[] { 20, 30, 15, 20 });//设置视图位置  
        
        XYSeriesRenderer r = new XYSeriesRenderer();//(类似于一条线对象)  
        r.setColor(Color.BLUE);//设置颜色  
        r.setPointStyle(PointStyle.CIRCLE);//设置点的样式  
        r.setFillPoints(true);//填充点（显示的点是空心还是实心）  
        r.setDisplayChartValues(true);//将点的值显示出来  
       r.setChartValuesSpacing(10);//显示的点的值与图的距离  
       r.setChartValuesTextSize(25);//点的值的文字大小  
          
       r.setFillBelowLine(true);//是否填充折线图的下方  
      r.setFillBelowLineColor(Color.GREEN);//填充的颜色，如果不设置就默认与线的颜色一致  
        r.setLineWidth(2);//设置线宽  
        mRenderer.addSeriesRenderer(r);  
          
          
        XYSeriesRenderer rTwo = new XYSeriesRenderer();//(类似于一条线对象)  
        rTwo.setColor(Color.GRAY);//设置颜色  
       rTwo.setPointStyle(PointStyle.CIRCLE);//设置点的样式  
       rTwo.setFillPoints(true);//填充点（显示的点是空心还是实心）  
      rTwo.setDisplayChartValues(true);//将点的值显示出来  
       rTwo.setChartValuesSpacing(10);//显示的点的值与图的距离  
      rTwo.setChartValuesTextSize(25);//点的值的文字大小  
          
        rTwo.setFillBelowLine(true);//是否填充折线图的下方  
        rTwo.setFillBelowLineColor(Color.GREEN);//填充的颜色，如果不设置就默认与线的颜色一致  
        rTwo.setLineWidth(2);//设置线宽  
         
        mRenderer.addSeriesRenderer(rTwo);  
        XYSeriesRenderer rThress = new XYSeriesRenderer();//(类似于一条线对象)  
        rThress.setColor(Color.RED);//设置颜色  
        rTwo.setPointStyle(PointStyle.CIRCLE);//设置点的样式  
        rTwo.setFillPoints(true);//填充点（显示的点是空心还是实心）  
       rTwo.setDisplayChartValues(true);//将点的值显示出来  
       rTwo.setChartValuesSpacing(10);//显示的点的值与图的距离  
       rTwo.setChartValuesTextSize(25);//点的值的文字大小  
          
        rTwo.setFillBelowLine(true);//是否填充折线图的下方  
        rTwo.setFillBelowLineColor(Color.RED);//填充的颜色，如果不设置就默认与线的颜色一致  
        rTwo.setLineWidth(2);//设置线宽  
         
        mRenderer.addSeriesRenderer(rThress);  
          
          
          
        GraphicalView  view = ChartFactory.getLineChartView(this, mDataset, mRenderer);  
        view.setBackgroundColor(Color.BLACK);  
        setContentView(view);  

}
	public String readFileSdcardFile() throws IOException{   
		  String res="";   
		  try{   
		         FileInputStream fin = new FileInputStream(File_Path+"/"+file_txt_name);   
		  
		         int length = fin.available();   
		  
		         byte [] buffer = new byte[length];   
		         fin.read(buffer);       
		  
		         res = EncodingUtils.getString(buffer, "UTF-8");   
		  
		         fin.close();       
		        }   
		  
		        catch(Exception e){   
		         e.printStackTrace();   
		        }   
		        return res;   
		}   

}
