package com.mt.helpclass;

import android.content.Context;
import android.content.SharedPreferences;

public class EXHepler {
   SharedPreferences sp;
   SharedPreferences.Editor editor;
   Context context;
   public EXHepler(Context c,String name){
	   context=c;
	   sp = context.getSharedPreferences(name, 0);
	   editor = sp.edit();
   }
   
public void putValue(String key,String value){
	   editor = sp.edit();
	   editor.putString(key, value);
	   editor.commit();
   }
   public String getValue(String key){
	return sp.getString(key, null);
	   
   }
}
