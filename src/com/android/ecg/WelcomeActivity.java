package com.android.ecg;

import com.android.ecg.realtime.ECGRealTime;
import com.mt.xueyang.XueYang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class WelcomeActivity extends Activity{
	
	private SharedPreferences Inform ;
	private String MAC;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcom);
		Button wenjian = (Button)findViewById(R.id.wenjian);
		Button dongtai = (Button) findViewById( R.id.dongtai);
		Button setting = (Button) findViewById( R.id.setting);
		
		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		MAC=Inform.getString("Mac", "");
		
		wenjian.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(MAC!=null && !MAC.equals("")){
					//Intent wenjian=new Intent(WelcomeActivity.this,FileInput.class);
					Intent wenjian=new Intent(WelcomeActivity.this,XueYang.class);
					startActivity(wenjian);
				}
				else{
					Toast.makeText(getApplicationContext(),"请设置好Mac地址后使用此功能！",Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		dongtai.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(MAC!=null && !MAC.equals("")){
					Intent wenjian=new Intent(WelcomeActivity.this,ECGRealTime.class);
					startActivity(wenjian);
				}
				else{
					Toast.makeText(getApplicationContext(),"请设置好Mac地址后使用此功能！",Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent wenjian=new Intent(WelcomeActivity.this,EcgSetActivity.class);
				startActivity(wenjian);
			}
		});
		
		
	}

}
