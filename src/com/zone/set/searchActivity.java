package com.zone.set;

import com.android.ecg.FileInput;
import com.android.ecg.R;
import com.mt.xueya.pushview;
import com.view.expand.lineview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class searchActivity extends Activity{
    private Button xueya_search,xuezhi_search,xueyang_search,xindian_search;
    
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.searchlayout);
		xueya_search=(Button)findViewById(R.id.xueya_serch);
		xuezhi_search = (Button)findViewById(R.id.xuezhi_serch);
		xueyang_search = (Button)findViewById(R.id.xueyang_serch);
		xindian_search = (Button)findViewById(R.id.xindian_serch);
		xueya_search.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(searchActivity.this,pushview.class);
				startActivity(intent);
				
			}});
		xuezhi_search.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}});
		xueyang_search.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(searchActivity.this,lineview.class);
				startActivity(intent);
			}});
		xindian_search.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(searchActivity.this,FileInput.class);
				startActivity(intent);
			}});
	   
	}

}
