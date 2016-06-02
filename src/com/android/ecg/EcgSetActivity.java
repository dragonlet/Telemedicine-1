package com.android.ecg;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class EcgSetActivity extends TabActivity{

	private SharedPreferences Inform ;
	private Editor editor ;
	private EditText MAC;
	
	EditText username ;
	EditText familypnone;
	EditText userphone ;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		MAC =(EditText)findViewById(R.id.macaddress);
		if(Inform.getString("Mac","")!=null){
			MAC.setText(Inform.getString("Mac",""));
		}
		if(Inform.getString("UserID","")!=null){
			username.setText(Inform.getString("UserID",""));
		}
		if(Inform.getString("FamilyPhone","")!=null){
			familypnone.setText(Inform.getString("FamilyPhone",""));
		}
		if(Inform.getString("UserPhone","")!=null){
			userphone.setText(Inform.getString("UserPhone",""));
		}
		
		super.onResume();
	}

	public static String Mac;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ecgset);
		
		TabHost tabHost = this.getTabHost(); 
		
		tabHost.addTab(tabHost.newTabSpec("OneTab")   
               .setIndicator("系统设置", getResources().getDrawable(android.R.drawable.star_on))   
               .setContent(R.id.linearLayout1)); 
		
		tabHost.addTab(tabHost.newTabSpec("TwoTab") 
               .setIndicator("用户设置", getResources().getDrawable(android.R.drawable.star_off))   
               .setContent(R.id.linearLayout2));
		
		Button selectecg = (Button)findViewById(R.id.select);
		
		Button saveInfo =(Button)findViewById(R.id.SaveInfo);
		
		username = (EditText)findViewById(R.id.username);
		familypnone = (EditText)findViewById(R.id.familyphone);
		userphone =(EditText)findViewById(R.id.userphone);
		
		
		selectecg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent select =new Intent(EcgSetActivity.this,SerchECG.class);
				startActivity(select);
			}
		});
		
		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		editor=Inform.edit();
		
		saveInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String  name, fphone,uphone;
				name = username.getText().toString();
				fphone = familypnone.getText().toString();
				uphone = userphone.getText().toString();
				name.trim();fphone.trim();uphone.trim();
				if(name!=null&&!name.equals("")){
					editor.putString("UserID", name);
					editor.commit();
				}
				if(fphone!=null&&!fphone.equals("")){
					editor.putString("FamilyPhone", fphone);
					editor.commit();
				}
				if(uphone!=null&&!uphone.equals("")){
					editor.putString("UserPhone", uphone);
					editor.commit();
				}
			}
		});
	}
	
	private boolean IsAble(String mac){
		char[] input=mac.toCharArray();
		if(mac!=null){
			if(mac.length()==17){
				for(int i=0;i<17;i++){
					if(i==2||i==5||i==8||i==11||i==14){
						if (input[i]!=':') {
							return false;
						}
						else continue;
					}
					if(!(input[i]>='0'&&input[i]<='9')){
						if(!(input[i]<='F'&&input[i]>='A'||input[i]<='f'&&input[i]>='a')){
							return false;
						}
					}
					
				}
				return true;
			}
			else	return false;

		}
		return false;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(IsAble(MAC.getText().toString())) { 
				editor.putString("Mac", MAC.getText().toString());
				editor.commit();
				finish();
				return true;
			}
			else {
				Toast.makeText(getApplicationContext(),"请设置正确的MAC地址！",Toast.LENGTH_SHORT).show();
			}
		}

		return super.onKeyDown(keyCode, event);
	}
	
}
	