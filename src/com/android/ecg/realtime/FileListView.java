package com.android.ecg.realtime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.ecg.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class FileListView extends Activity {
ListView fileListView;
private File targetDir=null;//record which dir you are now
private SimpleAdapter adapter;//simple adater for listview
Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_listview);
		intent=getIntent();
		fileListView=(ListView)findViewById(R.id.filelist);
		fileListView.setOnItemClickListener(itemClickListener);
		
		makeFileList("sdcard/HRV/");
	}
	
	private void makeFileList(String filePath) {
		
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();//android simple arraylist declaration
		Map<String, Object> map;
		File path=new File(filePath);
		if (!path.exists()) { //if there is no dir named "filepath" then make it
			path.mkdir();
		}
		if (targetDir==null) {
			targetDir=new File(path.getAbsolutePath());
			         
			
		}
		File filelist[]=path.listFiles();
		//give file and dir different pic
		for (int i = 0; i < filelist.length; i++) {
			if (filelist[i].isFile()) {
				map=new HashMap<String, Object>();
				map.put("name",filelist[i].getName() );
				
				map.put("img", R.drawable.file);
				list.add(map);//put the map which includes info of files or dirs into a list
			}
			else if (filelist[i].isDirectory()) {
				map=new HashMap<String, Object>();
				map.put("name", filelist[i].getName());
				
				map.put("img", R.drawable.dir);
				list.add(map);
			}
		}
		
			adapter=new SimpleAdapter(this, list, R.layout.itemlayout,
					new String[]{"name","img"},
					new int[]{R.id.txt1,R.id.img});//structure a android arrayadapter
			fileListView.setAdapter(adapter);
		Message msg=new Message();
		msg.obj=path;
		titleUpdate.sendMessage(msg);//update the title bar for showing present path
		

	}
	
	//arraylist click listener class
	private OnItemClickListener itemClickListener=new OnItemClickListener() {
		
		@Override 
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			File[] HRVFileList=targetDir.listFiles();
			if (HRVFileList[position].isDirectory()) {//if the file pressed is a dir--> 
														
				fileListView.removeAllViewsInLayout();//-->then remove present listview-->
				
				targetDir=new File(HRVFileList[position].getAbsolutePath());
				makeFileList(targetDir.getAbsolutePath());//-->make a new one with the file pressed path
				
			}else if (HRVFileList[position].isFile()) {
				if (HRVFileList[position].getName().endsWith(".txt")) {
					Bundle bundle=new Bundle();
					String path=HRVFileList[position].getAbsolutePath();
					
					if (intent.getStringExtra("item2")!=null) {
						if (intent.getStringExtra("item2").equals("ECGDemo")) {
							Intent intent1=new Intent(FileListView.this, ECGDemo1.class);
							if(path==null)
								intent.putExtra("file", "/sdcard/HRV/nv-2.txt");
							else
							intent1.putExtra("file", path);
							startActivity(intent1);
							finish();
						}
					}				
				}
			}
			
			
			
		}
	};
//private void makeFileList() {
//	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
//	Map<String, Object> map;
//	File path=new File("sdcard/HRV/");
//	if (!path.exists()) {
//		path.mkdir();
//	}
//	if (targetDir==null) {
//		targetDir=new File(path.getAbsolutePath());
//		
//		
//	}
//	path=targetDir;
//	File filelist[]=path.listFiles();
//	//give file and dir different pic
//	for (int i = 0; i < filelist.length; i++) {
//		if (filelist[i].isFile()) {//if the selected item is a file
//			map=new HashMap<String, Object>();
//			map.put("name",filelist[i].getName() );
//			
//			map.put("img", R.drawable.file);
//			list.add(map);
//		}
//		else if (filelist[i].isDirectory()) {//if the selected item is a directory
//			map=new HashMap<String, Object>();
//			map.put("name", filelist[i].getName());
//			
//			map.put("img", R.drawable.dir);
//			list.add(map);
//		}
//	}
//	
//		adapter=new SimpleAdapter(this, list, R.layout.itemlayout,
//				new String[]{"name","img"},
//				new int[]{R.id.txt1,R.id.img});
//		fileListView.setAdapter(adapter);
//	Message msg=new Message();
//	msg.obj=path;
//	titleUpdate.sendMessage(msg);
//	
//}

private Handler titleUpdate=new Handler(){

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		String title=((File)msg.obj).getAbsolutePath();
		setTitle(title);
	}
	
};

}
