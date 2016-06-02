package com.android.ecg;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ToggleButton;

public class SerchECG extends Activity{
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}

	private static Boolean isOpen = false;
	private ArrayAdapter<String> results=null;
	private ListView myView=null;
	private List<String> adapterdata=new ArrayList<String>();
	private int BLUETOOTHNUM=0;
	private List<String> _MACADDRESS =new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.macselect);
		
		final ToggleButton open = (ToggleButton)findViewById(R.id.bluetooth);
		final BluetoothAdapter mybAdapter =BluetoothAdapter.getDefaultAdapter();
		Button serch = (Button)findViewById(R.id.serch);
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(mReceiver, filter);
		
		myView=(ListView)findViewById(R.id.result);
		results=new ArrayAdapter<String>(SerchECG.this, android.R.layout.simple_list_item_1,adapterdata);
		myView.setAdapter(results);
		
		if(mybAdapter.isEnabled()){
			open.setChecked(true);
			isOpen=true;
		}
		//开蓝牙按钮
		open.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
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
		//搜索设备按钮
		serch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isOpen){
					mybAdapter.startDiscovery();
				}
				
			}
		});
		
		//ListView单击事件
		myView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				SharedPreferences Inform=getSharedPreferences("perference", MODE_PRIVATE);
				Editor macEditor = Inform.edit();
				macEditor.putString("Mac", _MACADDRESS.get(arg2));
				macEditor.commit();
				finish();
			}
			
		});
	}
	
	private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action =intent.getAction();
			//发现设备
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				// 从Intent中获取设备对象 
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 将设备名称和地址放入array adapter，以便在ListView中显示 
				String res = device.getName() + "\n" + device.getAddress();
				if(adapterdata.indexOf(res)==-1){
					adapterdata.add(res);
					_MACADDRESS.add(device.getAddress());
				}
				results.notifyDataSetChanged();
			}
		}
	};
	
	
}
