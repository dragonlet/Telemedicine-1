/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mt.xueya;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ChartDemo extends ListActivity {
  private IDemoChart[] mCharts = new IDemoChart[] {  new SalesStackedBarChart()  };

  private String[] mMenuText;

  private String[] mMenuSummary;

  ArrayList<Parcelable> thdb=new ArrayList<Parcelable>();
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int length = mCharts.length;
    mMenuText = new String[length];
    mMenuSummary = new String[length];
  
    Intent achartdb=getIntent();
     thdb=achartdb.getParcelableArrayListExtra("data");
    
    for (int i = 0; i < length; i++) {
      mMenuText[i] = mCharts[i].getName();
      mMenuSummary[i ] = mCharts[i].getDesc();
    }
    Intent intent = null;
    intent = mCharts[0].execute(this,(ArrayList<? extends Parcelable>) thdb);
    startActivity(intent);
//    setListAdapter(new SimpleAdapter(this, getListValues(), android.R.layout.simple_list_item_2,
//        new String[] { IDemoChart.NAME, IDemoChart.DESC }, new int[] { android.R.id.text1,
//            android.R.id.text2 }));
    
  }

  private List<Map<String, String>> getListValues() {
    List<Map<String, String>> values = new ArrayList<Map<String, String>>();
    int length = mMenuText.length;
    for (int i = 0; i < length; i++) {
      Map<String, String> v = new HashMap<String, String>();
      v.put(IDemoChart.NAME, mMenuText[i]);
      v.put(IDemoChart.DESC, mMenuSummary[i]);
      values.add(v);
    }
    return values;
  }


  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Intent intent = null;
    if (position == 0) {
//      intent = new Intent(this, XYChartBuilder.class);
//    	intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) thdb);
    	intent = mCharts[position].execute(this,(ArrayList<? extends Parcelable>) thdb);
    	Log.e("--------", "aaaaaaaa");
    }
//    	else if (position <= mCharts.length + 1) {
//      intent = mCharts[position - 2].execute(this,(ArrayList<? extends Parcelable>) thdb);
//      intent = mCharts[position].execute(this,(ArrayList<? extends Parcelable>) thdb);
//      Log.e("--------", "aaaaaaaa");
//  	intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) thdb);
//
//    } else {
//      intent = new Intent(this, GeneratedChartDemo.class);
//      intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) thdb);
//    }
    startActivity(intent);
    	
    }
}