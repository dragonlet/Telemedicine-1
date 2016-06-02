package com.zone.set;

import java.util.ArrayList;
import java.util.List;

import com.android.ecg.R;
import com.view.expand.WifiAdamin;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class serchWifi extends Activity {
    private Button serch_wifi_btn;
    private ListView wifi_lst;
    private List<ScanResult> lstnet;
    private StringBuffer sb= new StringBuffer();
    private ArrayAdapter<String> network;
    private WifiAdamin wifiAdmin;
    private ScanResult mScanResult; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serchwifi);
		wifiAdmin = new WifiAdamin(this);
		serch_wifi_btn = (Button)findViewById(R.id.serch_wifi_btn);
		wifi_lst = (ListView)findViewById(R.id.wifi_lst);
		lstnet = new ArrayList<ScanResult>();

		serch_wifi_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}});
		 
	}
	public void getAllNetWorkList(){
		//每次点击清空上次的扫描结果
		if(sb!=null){
			sb = new StringBuffer();
			
		}
		//开始扫描网络
	   wifiAdmin.startScan();
	   lstnet=wifiAdmin.getWifiList();
	   if(lstnet!=null){
		   for(int i=0;i<lstnet.size();i++){
		   //得到扫描结果  
           mScanResult=lstnet.get(i);  
           sb=sb.append(mScanResult.BSSID+"  ").append(mScanResult.SSID+"   ")  
           .append(mScanResult.capabilities+"   ").append(mScanResult.frequency+"   ")  
           .append(mScanResult.level+"\n\n");   
		   
	   }
	   }
		
	}

}
