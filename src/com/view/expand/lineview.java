package com.view.expand;

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
import android.widget.LinearLayout;
import android.widget.Toast;

public class lineview extends Activity{
	private XYSeries series;
//	private SaveTData savaData;
	private final String SD_PATH=android.os.
			Environment.
			getExternalStorageDirectory().
			getAbsolutePath();
//������չ�洢��(SDCard)Ŀ¼
public final String File_Path=SD_PATH+"/SaveTData";
private  String file_txt_name;
public SharedPreferences Inform = null;
public String userid;
public String username;
public String[] str;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lineview);
		//LinearLayout linear = (LinearLayout)findViewById(R.id.linear);
		//savaData = new SaveTData();
		 Inform = getSharedPreferences("perference", MODE_PRIVATE);

			userid = Inform.getString("USERID", "");
			username = Inform.getString("UserName", "");
			if(!username.equals("")){
			file_txt_name = username+"pulse.txt";
			}else{
				file_txt_name = "pulse.txt";
				
			}
			Log.v("1",file_txt_name);
					lineView();
				
		
	}
	public void lineView(){  
        //ͬ������Ҫ����dataset����ͼ��Ⱦ��renderer  
        XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();  
        XYSeries  series = new XYSeries("spo2%"); 
        XYSeries  seriesTwo = new XYSeries("PRbpm"); 
        XYSeries  seriesThress = new XYSeries("��ͼ"); 
        try {
        	
         if( !readFileSdcardFile().equals("")){
			String[] str = readFileSdcardFile().split(",");
			Log.v("sda", str.toString());
			for(int i=0;i<str.length;i++){
				if(i%3==0){
					series.add(i, Double.parseDouble(str[i]));
				}else if((i+2)%3==0){
					seriesTwo.add((i+2)%3, Double.parseDouble(str[i]));
				}else if((i+1)%3==0){
					seriesThress.add((i+1)%3, Double.parseDouble(str[i]));
				}					
			}
         }else{
        	 Toast.makeText(lineview.this,"�����޸��û�����", Toast.LENGTH_LONG).show();
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
     //   XYSeries  seriesTwo = new XYSeries("�ڶ�����");  
//        seriesTwo.add(1, 4);  
//        seriesTwo.add(2, 6);  
//        seriesTwo.add(3, 3);  
//        seriesTwo.add(4, 7);  
        mDataset.addSeries(seriesTwo);  
        mDataset.addSeries(seriesThress);  
          
          
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();  
        //����ͼ���X��ĵ�ǰ����  
        mRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);  
        mRenderer.setXTitle("");//����ΪX��ı���  
        mRenderer.setYTitle("");//����y��ı���  
        mRenderer.setAxisTitleTextSize(20);//����������ı���С  
        mRenderer.setChartTitle("��ʷ��¼");//����ͼ�����  
        mRenderer.setChartTitleTextSize(30);//����ͼ��������ֵĴ�С  
        mRenderer.setLabelsTextSize(18);//���ñ�ǩ�����ִ�С  
        mRenderer.setLegendTextSize(20);//����ͼ���ı���С  
        mRenderer.setPointSize(10f);//���õ�Ĵ�С  
        mRenderer.setYAxisMin(0);//����y����Сֵ��0  
        mRenderer.setYAxisMax(120);  
        mRenderer.setYLabels(10);//����Y��̶ȸ�����ò�Ʋ�̫׼ȷ��  
        //mRenderer.setXAxisMax();  
        //mRenderer.setShowGrid(true);//��ʾ����  
        //��x��ǩ��Ŀ��ʾ�磺1,2,3,4�滻Ϊ��ʾ1�£�2�£�3�£�4��  
//        mRenderer.addXTextLabel(1, "1��");  
//        mRenderer.addXTextLabel(2, "2��");  
//        mRenderer.addXTextLabel(3, "3��");  
//        mRenderer.addXTextLabel(4, "4��");  
  //      mRenderer.setXLabels(0);//����ֻ��ʾ��1�£�2�µ��滻��Ķ���������ʾ1,2,3��  
        mRenderer.setMargins(new int[] { 20, 30, 15, 20 });//������ͼλ��  
        
        XYSeriesRenderer r = new XYSeriesRenderer();//(������һ���߶���)  
        r.setColor(Color.BLUE);//������ɫ  
        //r.setPointStyle(PointStyle.CIRCLE);//���õ����ʽ  
       // r.setFillPoints(true);//���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�  
       // r.setDisplayChartValues(true);//�����ֵ��ʾ����  
       // r.setChartValuesSpacing(10);//��ʾ�ĵ��ֵ��ͼ�ľ���  
      //  r.setChartValuesTextSize(25);//���ֵ�����ִ�С  
          
      //  r.setFillBelowLine(true);//�Ƿ��������ͼ���·�  
      //  r.setFillBelowLineColor(Color.GREEN);//������ɫ����������þ�Ĭ�����ߵ���ɫһ��  
        r.setLineWidth(2);//�����߿�  
        mRenderer.addSeriesRenderer(r);  
          
          
        XYSeriesRenderer rTwo = new XYSeriesRenderer();//(������һ���߶���)  
        rTwo.setColor(Color.GRAY);//������ɫ  
//        rTwo.setPointStyle(PointStyle.CIRCLE);//���õ����ʽ  
//        rTwo.setFillPoints(true);//���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�  
//        rTwo.setDisplayChartValues(true);//�����ֵ��ʾ����  
//        rTwo.setChartValuesSpacing(10);//��ʾ�ĵ��ֵ��ͼ�ľ���  
     //   rTwo.setChartValuesTextSize(25);//���ֵ�����ִ�С  
          
      //  rTwo.setFillBelowLine(true);//�Ƿ��������ͼ���·�  
      //  rTwo.setFillBelowLineColor(Color.GREEN);//������ɫ����������þ�Ĭ�����ߵ���ɫһ��  
        rTwo.setLineWidth(2);//�����߿�  
         
        mRenderer.addSeriesRenderer(rTwo);  
        XYSeriesRenderer rThress = new XYSeriesRenderer();//(������һ���߶���)  
        rThress.setColor(Color.RED);//������ɫ  
//        rTwo.setPointStyle(PointStyle.CIRCLE);//���õ����ʽ  
//        rTwo.setFillPoints(true);//���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�  
//        rTwo.setDisplayChartValues(true);//�����ֵ��ʾ����  
//        rTwo.setChartValuesSpacing(10);//��ʾ�ĵ��ֵ��ͼ�ľ���  
     //   rTwo.setChartValuesTextSize(25);//���ֵ�����ִ�С  
          
      //  rTwo.setFillBelowLine(true);//�Ƿ��������ͼ���·�  
      //  rTwo.setFillBelowLineColor(Color.GREEN);//������ɫ����������þ�Ĭ�����ߵ���ɫһ��  
        rThress.setLineWidth(2);//�����߿�  
         
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
