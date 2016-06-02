package com.zone.set;


import com.android.ecg.R;
import com.android.ecg.realtime.ECGRealTime;
import com.mt.helpclass.EXHepler;
import com.view.expand.DeviceListActivitydemo;
import com.view.expand.WifiAdamin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class setActivity extends Activity{
	private ToggleButton bluetooth;
	private ToggleButton wifi_btn;
	private Button serch_bluetooth;
	//private Button serch_wifi;
	private static Boolean isOpen = false;
	private static Boolean wifiOpen = false;

	private Button phone_btn;
	private WifiManager mWifiManager;
	private WifiAdamin wifiadmin;
	 //����һ��WifiInfo����  
    private WifiInfo mWifiInfo; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setlayout);
		wifiadmin = new WifiAdamin(this);
		//ȡ��WifiManager����  
        mWifiManager=(WifiManager) this.getSystemService(Context.WIFI_SERVICE);  
        //ȡ��WifiInfo����  
        mWifiInfo=mWifiManager.getConnectionInfo();  
		final BluetoothAdapter mybAdapter =BluetoothAdapter.getDefaultAdapter();
//		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
//		registerReceiver(mReceiver, filter);
		initView();
		if(mybAdapter.isEnabled()){
			bluetooth.setChecked(true);
			isOpen=true;
		}
		
		//��������ť
		bluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isOpen){
					new Thread(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							while(!isOpen){
								mybAdapter.enable();
								if(mybAdapter.isEnabled()){
									isOpen=true;
								}
								else {
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}.start();
				}
				else {
					mybAdapter.disable();
					isOpen=false;
				}
			}
		});
		if(mWifiManager.isWifiEnabled()){
			 wifi_btn.setChecked(true);
			 wifiOpen = true;
			
		}
		wifi_btn.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(!wifiOpen){
					wifiadmin.openWifi();
					wifiOpen=true;
				 }else{
					 wifiadmin.closeWifi();
					 wifiOpen=false;
				 }
				}
			});
		
		
			serch_bluetooth.setOnClickListener(new Button.OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(isOpen){
					Intent intent = new Intent();
					intent.setClass(setActivity.this, DeviceListActivitydemo.class);
					startActivity(intent);
					}else{
						Toast.makeText(getApplicationContext(), "���ȴ�����",Toast.LENGTH_SHORT).show();
						
					}
				}
			});
			
		phone_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				myDialog();				
			}
		});
	}
	public void initView(){
		bluetooth = (ToggleButton)findViewById(R.id.bluetooth);
		serch_bluetooth = (Button)findViewById(R.id.serch_btn);
		wifi_btn = (ToggleButton)findViewById(R.id.wifi_btn);
		phone_btn = (Button)findViewById(R.id.phone_btn);
	}
	protected void myDialog(){
		LayoutInflater inflater = LayoutInflater.from(setActivity.this);
		View layout = inflater.inflate(R.layout.dialogview, null);
		Builder b = new AlertDialog.Builder(setActivity.this).setTitle("�����������ϵ�绰").setView(layout);
		final EditText myview = (EditText) layout.findViewById(R.id.phone_edit);
		final String phonenum=myview.getText().toString();

		myview.setText(new EXHepler(setActivity.this, "phoneconn").getValue("phone"));
		b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Log.e("myview", myview.toString());
				EXHepler  e=new EXHepler(setActivity.this, "phoneconn");
				e.putValue("phone",myview.getText().toString());
				
				SharedPreferences mySharedPreferences = getSharedPreferences("test", 
				Activity.MODE_PRIVATE); 
				SharedPreferences.Editor editor = mySharedPreferences.edit(); 
				editor.putString("phone", phonenum); 
				editor.commit(); 
			}
		});
		b.setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}});
		b.create().show();
		
	}
}
