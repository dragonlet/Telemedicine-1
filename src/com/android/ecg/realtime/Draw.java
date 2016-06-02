package com.android.ecg.realtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;

public class Draw {
	static QRSDet m_qrsDet;
	static int SFV_WIDTH;
	static int VALUE_MAX = 0;
	static int VALUE_MIN = 0;

	private static float oldX = 0;
	static float oldY = 0;
	private static int X_index = 0;
	public static int baseLine = 1;
	private static int rateX = 1;
	private static int rateY = 1;
	// float[] minDatas;
	static int delay = 0;
	

	static float rMax = 240;// real time data
	static float rMin = 40;
	static float rMid = (rMax + rMin) / 2;
	static float dMax = 0; // display
	static float dMin = 0;
	static float dMid = 0;
	static float rate = 0;
	// 计数

//	private static long timecount = 0;

//	private static Vector<Long> herttime = new Vector<Long>();



	 static int  QRSmarker(byte[] markArray, int[] datas) {

		m_qrsDet = new QRSDet();
		for (int i = 0; i < markArray.length; i++) {
			markArray[i] = 0;
		}
		int t_delay = 0;

		for (int j = 0; j < markArray.length; j++) {
			int smp = (datas[j] * 12);
			Log.i("smp", String.valueOf(smp));
			t_delay = m_qrsDet.addSample(smp);
			if (t_delay != 0) {
				delay = t_delay;
				markArray[j] = 1;
			}
		}
		return delay;
	}
	 static void drawGrid(Canvas gridCanvas) {
		Paint gridPaint = new Paint();
		gridPaint.setColor(Color.argb(100, 102, 204, 255));
		for (int k = 0; k < gridCanvas.getHeight(); k = k + 40) {
			gridCanvas.drawLine(0, k, gridCanvas.getWidth(), k, gridPaint);
		}
		for (int k = 0; k < gridCanvas.getWidth(); k = k + 40) {
			gridCanvas.drawLine(k, 0, k, gridCanvas.getHeight(), gridPaint);
		}
	}
	 static Vector<Long> realTimeChart(int[] datas, byte[] markArray,SurfaceView sfv,SurfaceView sfv2,Vector<Long> herttime,long timecount)
			throws IOException {
		 Vector<Long> t = null;
		if (X_index > sfv.getWidth()) {
			X_index = 0;
		}
		if (datas.length != 0) {
			t=SimpleDraw(X_index, datas, rateX, rateY, baseLine, markArray,sfv,sfv2,herttime,timecount);
			X_index = X_index + (datas.length / rateX);//
		}
		return t;
	}
	static Vector<Long> SimpleDraw(int start, int[] datas, int rateX, int rateY, int baseLine,
			byte[] markArray,SurfaceView sfv,SurfaceView sfv2,Vector<Long> herttime,long timecount) {
		if (start == 0)
			oldX = 0;
		int[] sortedArray = new int[datas.length / rateX];
		byte[] markBuffer = new byte[datas.length / rateX];
		for (int i = 0, ii = 0; i < sortedArray.length; i++, ii = i * rateX) {
			sortedArray[i] = datas[ii];
			if (markArray[i] == 1) {
				markBuffer[i / rateX] = 1;
			}
		}
		Canvas canvas = sfv.getHolder().lockCanvas(
				new Rect(start, 0, start + datas.length, sfv.getHeight()));
		Arrays.sort(sortedArray);
		if (sortedArray[0] < VALUE_MIN) {
			VALUE_MIN = sortedArray[0];
		} else 
			if (sortedArray[sortedArray.length - 1] > VALUE_MAX) {
			VALUE_MAX = sortedArray[sortedArray.length - 1];
		}
		if ((VALUE_MAX + VALUE_MIN) / 2 > (sfv.getHeight() / 10 + sfv
				.getHeight() * 9 / 10) / 2) {
			baseLine = (VALUE_MAX + VALUE_MIN) / 2
					- (sfv.getHeight() / 10 + sfv.getHeight() * 9 / 10) / 2;
		} else {
			baseLine = (sfv.getHeight() / 10 + sfv.getHeight() * 9 / 10) / 2
					- (VALUE_MAX + VALUE_MIN) / 2;
		}
		dMax = sfv.getHeight();
		dMin = sfv.getHeight() / 2;
		dMid = (dMax + dMin) / 2;
		rate = dMid / rMid;
		if (canvas != null) {
			canvas.drawColor(Color.BLACK);// 锟斤拷锟�
			drawGrid(canvas);
		}
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3f);
		for (int i = 0; i < datas.length; i++) {
			float y = dMin - datas[i];
			int x = i + start;
			if (oldX != 0 && canvas != null) {
				canvas.drawLine(oldX, (float) oldY, x, y, paint);
			}
			oldX = x;
			oldY = y;
		}
		if (canvas != null)
			sfv.getHolder().unlockCanvasAndPost(canvas);
		Canvas markCanvas = null;
		Paint markPaint = new Paint();
		markPaint.setColor(Color.YELLOW);
		markCanvas = sfv2.getHolder().lockCanvas(
				new Rect(start, 0, start + datas.length * 2, sfv.getHeight()));
		if (markCanvas != null) {
			markCanvas.drawColor(Color.BLACK);
			sfv2.getHolder().unlockCanvasAndPost(markCanvas);
		}
		Log.e("dates.length","-> datas.length"+ datas.length);
		for (int i = 0; i < datas.length; i++) {
			
			if (markArray[i] == 1) {
				int x = 0;
				if (start + i - delay < 0) {
					if (start == 0)
						x = SFV_WIDTH + i - delay;
					else {
						x = SFV_WIDTH + start + i - delay - 6;
					}
				} else {

					x = start + i - delay - 6;
				}

				markCanvas = sfv2.getHolder().lockCanvas(
						new Rect(x, 0, x + datas.length, sfv2.getHeight()));
				Log.e("markCanvas:","markCanvas:"+markCanvas);
				if (markCanvas != null) {

					Log.e("markCanvas:","markCanvas ok");
					markCanvas.drawColor(Color.BLACK);
					markCanvas.drawLine(x + 4, sfv2.getHeight() - 20, x + 4,
							sfv2.getHeight(), markPaint);
					Log.e("ss", "N");
					markCanvas.drawText("N", x, sfv2.getHeight() - 20,
							markPaint);
					sfv2.getHolder().unlockCanvasAndPost(markCanvas);
					herttime.add(timecount);
				}
			} else if (markArray[i] == 2) {
				Log.v("ss", "v");
				markCanvas.drawText("V", start + i - 10, sfv2.getHeight() - 40,
						markPaint);
				markCanvas.drawLine(start + i - 26, sfv2.getHeight() - 40,
						start + i - 26, sfv2.getHeight() - 30, markPaint);
			}
		}
return herttime;
	}
	
}
