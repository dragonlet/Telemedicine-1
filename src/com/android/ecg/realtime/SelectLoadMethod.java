package com.android.ecg.realtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.ecg.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;



public class SelectLoadMethod extends Activity {
	ListView  listView;
	String item2,info2;//item1 is realtime display for ECG,ACC;item2 is demo version for ECG,ACC;
									//HRV only have item1
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_listview);
		
		Intent intent=getIntent();
		
		item2=intent.getStringExtra("item2");
		info2=intent.getStringExtra("info2");
		
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>make a list of files in sdcard/HRV/>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		listView=(ListView)findViewById(R.id.filelist);
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		Map<String, Object> map2=new HashMap<String, Object>();
		
		if (item2!=null) {
			
			
			
			map2.put("title",item2);
			map2.put("info", info2);
			map2.put("img", R.drawable.directory);
			
			list.add(map2);
			
			
		}
		
		SimpleAdapter adapter=new SimpleAdapter(this, list, R.layout.itemlayout,
				new String[]{"title","info","img"}, 
				new int[]{R.id.txt1,R.id.txt2,R.id.img});
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(itemClickListener);
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		
		
		
		
	}
	//listen touch event , if we wanna select file from sdcard,then make a filelist;if we wanna a real time display
	//we don't need to
	OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent;
			
				intent=new Intent();
				intent.putExtra("item2", item2);
			
					
					intent.setClass(SelectLoadMethod.this, FileListView.class);
					startActivity(intent);
					finish();
				
			
		}
	};
}
