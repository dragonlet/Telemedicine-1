package com.mt.tools;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * 存储app常量的工具类
 * 
 * @author Administrator
 *
 */
public class Constants {
	public static final String XueYangAction="http://192.168.49.181:8080/REMSWeb/XueYangAction";
	public static final String sendFatInfo = "http://192.168.49.181:8080/REMSWeb/GetFatServlet";		
	public static final String sendXueTangInfo = "http://192.168.49.181:8080/REMSWeb/GetXueTangServlet";
	public static String sendxueyaInfo="http://192.168.49.181:8080/REMSWeb/XueYaAction";
	public static float SCREEN_DENSITY = 1.0f;
	private static int SCREEN_WIDTH = 0;
	private static int SCREEN_HEIGHT = 0;
	
	public static void loadConfigs(Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics dm = resources.getDisplayMetrics();
		SCREEN_DENSITY = dm.density;
		SCREEN_WIDTH = dm.widthPixels;
		SCREEN_HEIGHT = dm.heightPixels;
	}
	
}
