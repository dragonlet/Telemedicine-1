package com.android.ecg.realtime;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.android.ecg.Mylocation;
import com.android.ecg.R;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.ecg.GPSToLoaction;
import com.mt.helpclass.EXHepler;

public class ECGDemo1 extends Activity {
	private static Vector<Float> timeVector = new Vector<Float>();
	private TextView hertcount;
	private static long timecount = 0;
	boolean recording = true;
	private Vector<Float> testVector;
	private float oldX = 0, oldY = 0;
	private SurfaceView sfv, sfv2;// sfv is to show ECG chart,sfv2 is to mark

	protected void onStart() {
		hadSendSMS = false;
		super.onStart();
	}

	private int X_index = 0;
	public float baseLine = 0;// ajust the center of ECG chart
	Thread thread;
	private int rateX = 1;
	private int rateY = 2;
	boolean drawing = true, flag = false, initial = false, flag_send = true;
	private String path = null;
	Detector detector = new Detector();// NSR&VT/VF Detector
	TextView signal;// textview to show NSR or VF
	String signalStr;// signal string
	Handler handler;// change the signal string
	QRSDet m_qrsDet; // QRS detector
	static int ECG_SAMPLE_NUM = 72;
	static int SFV_WIDTH, SFV_HEIGHT;
	int delay, index = 0;
	List<Float> tmpList;
	float[] minDatas;
	boolean saveHrv = false;
	BufferedReader savedReader = null;
	DataOutputStream savedDos = null;
	File savedHRVFile = null;
	String savedHRVfName = null;
	final static float HRVDATA_CAL_LENTH = 300;
	String hrvFileName;
	Context context;
	AlertDialog dialog;
	int TWO_PEAK_DISTANCE = 0;

	float dMax = 0, dMin = 0, dMid = 0;
	float rMax = 240, rMin = 40, rMid = (rMax + rMin) / 2;
	float VALUE_MAX = 0, VALUE_MIN = 0;
	float rate = 1.5f;
	final static float TWO_PEAK_LENTH = 180f;
	int lastPeakIndex = 0;
	float SURFACEVIEW_HEIGHT = 0;
	boolean onTouch = false, Switch = true;
	private static Vector<Long> herttime = new Vector<Long>();
	private String phone;
	private int nowHertLv = 0;
	private String sms;
	private String name;
	GPSToLoaction gps;
	private boolean hadSendSMS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bluetooth_char);
		phone = new EXHepler(ECGDemo1.this, "phoneconn").getValue("phone");
		name = new EXHepler(ECGDemo1.this, "perference").getValue("UserName");
		context = ECGDemo1.this;
		gps = new GPSToLoaction(ECGDemo1.this);
		// Button recStart=(Button)findViewById(R.id.recStart);
		// Button recReset=(Button)findViewById(R.id.recReset);
		// Button recEnd=(Button)findViewById(R.id.recEnd);
		hertcount = (TextView) findViewById(R.id.heartRate11);
		((TextView) findViewById(R.id.xlabel)).setVisibility(TextView.GONE);
		((TextView) findViewById(R.id.ylabel)).setVisibility(TextView.GONE);
		((TextView) findViewById(R.id.zlabel)).setVisibility(TextView.GONE);
		signal = (TextView) findViewById(R.id.textview);
		TextView fNTextView = (TextView) findViewById(R.id.fileName);

		findViewById(R.id.xlegend).setVisibility(View.GONE);
		findViewById(R.id.ylegend).setVisibility(View.GONE);
		findViewById(R.id.zlegend).setVisibility(View.GONE);

		setHeartAnimation((ImageView) findViewById(R.id.heartImg));

		sfv = (SurfaceView) findViewById(R.id.sfv);

		// sfv.setOnTouchListener(listener);
		sfv2 = (SurfaceView) findViewById(R.id.sfv2);

		SFV_WIDTH = getWindowManager().getDefaultDisplay().getWidth();

		ECG_SAMPLE_NUM = SFV_WIDTH / 4;

		Intent intent = getIntent();
		path = intent.getStringExtra("file");
		/*
		 * if(path==null) path="/sdcard/HRV/nv-2.txt";
		 */
		String[] strs = path.split("/");
		fNTextView.setText(strs[strs.length - 1]);

		testVector = new Vector<Float>();
		m_qrsDet = new QRSDet();

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0: // NSR detector
					String nsrSignal = (String) msg.obj;
					signal.setText("Signal:   " + nsrSignal);
					Log.e("nsrSignal", "<<nsrSignal>>");
					if (nsrSignal.equals("NSR")) {

					} else {
						if (hadSendSMS) {
							myDialog();
						} else if (!"".equals(phone) && null != phone) {
							if (gps.isEnbled() == true) {
								gps.exec();
								Mylocation location = gps.getMyLocation();
								sms = name + "出现健康危险前兆" + "\n" + "当前心率:"
										+ nowHertLv + "\n" + "当前位置:"
										+ location.getLatitude() + ","
										+ location.getLongitude();
								sendSMS(sms, phone);
								hadSendSMS = true;
							}
						}

						else {
							Toast.makeText(context, "没有保存紧急联系电话",
									Toast.LENGTH_SHORT).show();
						}

					}
					break;

				case 1: // QRS detector
					Toast.makeText(context, "HRV Finished", Toast.LENGTH_LONG)
							.show();
					break;
				case 9:
					nowHertLv = (Integer) msg.obj;
					hertcount.setText(String.valueOf(nowHertLv));

					break;
				}

			}

		};

		thread = new Thread(runnable1);

		thread.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Get HRV");
		return super.onCreateOptionsMenu(menu);
	}

	// set a menu option
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (item.getTitle().equals("Get HRV")) {
				// structure an alert dialog
				View fnInputLayout = View.inflate(context,
						R.layout.hrv_fninput_layout, null);
				dialog = new AlertDialog.Builder(context)
						.setTitle("Please input the HRV File name: ")
						.setView(fnInputLayout)
						.setPositiveButton("Yes", new OnClickListener() {

							@Override
							public void onClick(DialogInterface di, int id) {
								EditText fnInput = (EditText) dialog
										.findViewById(R.id.fnInput);
								final String fileName = fnInput.getText()
										.toString() + ".txt";
								final File HRVfolder = new File(
										"/sdcard/HRV/HRV/");
								if (!HRVfolder.exists()) {
									HRVfolder.mkdir();
								}
								File[] fileList = HRVfolder.listFiles();

								for (int i = 0; i < fileList.length; i++) { // when
																			// same
																			// file
																			// name
																			// exits
									if (fileName.equals(fileList[i].getName())) {
										new AlertDialog.Builder(context)
												.setTitle(
														"Same File Name Exists! Do you want to cover it?")
												.setPositiveButton("Yes",
														new OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface arg0,
																	int arg1) {
																String fn = HRVfolder
																		.getAbsolutePath()
																		+ "/"
																		+ fileName;

																try {
																	File file = new File(
																			fn);
																	file.delete();
																	if (!file
																			.exists()) {
																		file.createNewFile();
																	}

																} catch (FileNotFoundException e) {
																	// TODO
																	// Auto-generated
																	// catch
																	// block
																	e.printStackTrace();
																} catch (IOException e) {
																	// TODO
																	// Auto-generated
																	// catch
																	// block
																	e.printStackTrace();
																}

															}
														})
												.setNegativeButton("No",
														new OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface arg0,
																	int arg1) {
																dialog.show();

															}
														}).show();

										break;
									}
								}
								createHRVFile(HRVfolder.getAbsolutePath() + "/"
										+ fileName);
								// item.setTitle("HRV End");
								new Thread(new Runnable() {
									public void run() {
										drawing = false;
										saveHRVData(HRVfolder.getAbsolutePath()
												+ "/" + fileName);
									}
								}).start();
							}
						}).setNegativeButton("Cancel", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								saveHrv = false;
								item.setTitle("HRV Start");

							}
						}).show();

				// save the hrv data

				// ///////////////////////////////////////////////

			} else if (item.getTitle().equals("HRV End")) {
				item.setTitle("HRV Start");
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendSMS(String content, String phonenum) {

		SmsManager manager = SmsManager.getDefault();
		if (manager == null) {
			Toast.makeText(context, "SMS Module is not Available!",
					Toast.LENGTH_SHORT);
			return;
		}
		if (content.length() > 70) {
			List<String> all = manager.divideMessage(content);
			for (String each : all) {
				manager.sendTextMessage(phonenum, null, each, null, null);
			}
		} else {
			manager.sendTextMessage(phonenum, null, content, null, null);
		}

		Toast.makeText(context, R.string.sms_send_successfully,
				Toast.LENGTH_LONG).show();
	}

	// Heart animation at top right corner
	private void setHeartAnimation(ImageView iv) {
		// shan shuo
		AlphaAnimation alphaAnimation1 = new AlphaAnimation(0f, 1.0f);
		alphaAnimation1.setDuration(500);
		alphaAnimation1.setRepeatCount(Animation.INFINITE);
		alphaAnimation1.setRepeatMode(Animation.REVERSE);
		iv.setAnimation(alphaAnimation1);
		alphaAnimation1.start();
	}

	// create a new HRV file
	private void createHRVFile(String fileName) {
		File HRVfile = new File(fileName);
		saveHrv = true;
		this.hrvFileName = fileName;
		try {
			if (!HRVfile.exists())
				HRVfile.createNewFile();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * input:markArray ,vectoroutput: markArraymark the markArray so we can use
	 * markArray to display
	 */
	private void QRSmarker(byte[] markArray, Vector<Float> vector) {

		for (int i = 0; i < markArray.length; i++) {
			markArray[i] = 0;
		}
		int delay = 0;
		for (int j = 0; j < markArray.length; j++) {
			float tmp = vector.get(j);
			if (tmp < 5) {
				tmp *= 200f;
			} else {
				tmp *= 2f;
			}
			int smp = (int) (tmp);
			// add each ecg sample to the QRS Dectector by addSample method,then
			// get a number
			// when the number !=0,mark the markArray
			delay = m_qrsDet.addSample(smp);
			if (delay != 0) {
				this.delay = delay;
				markArray[j] = 1;
				flag = true;

			}

		}

	}

	// 画格子 //////
	private void drawGrid(Canvas gridCanvas) {

		Paint gridPaint = new Paint();
		gridPaint.setColor(Color.argb(100, 102, 204, 255));

		for (int k = 0; k < gridCanvas.getHeight(); k = k + 40) {
			gridCanvas.drawLine(0, k, gridCanvas.getWidth(), k, gridPaint);

		}
		for (int k = 0; k < gridCanvas.getWidth(); k = k + 40) {
			gridCanvas.drawLine(k, 0, k, gridCanvas.getHeight(), gridPaint);

		}

	}

	Runnable runnable1 = new Runnable() {

		@Override
		public void run() {
			InputStream xmlFile = getResources()
					.openRawResource(R.raw.detector);// open
			detector.Init("4,2000,11,40,256,240", xmlFile);
			readDemoFile(path);

		}
	};

	private OnTouchListener listener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			onTouch = !onTouch;
			if (!onTouch) {
				thread = new Thread(runnable1);
				saveHrv = false;
				thread.start();
			}

			return false;
		}
	};

	private void readDemoFile(String path) {
		try {

			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			if (!onTouch && savedReader != null) {
				bufferedReader = savedReader;
				// dos=savedDos;

			} else {
				bufferedReader = new BufferedReader(fileReader);
			}

			float next = 0;
			String string;

			Vector<Float> drawVector = new Vector<Float>();
			Vector<Integer> vector2 = new Vector<Integer>();
			Date date = new Date(System.currentTimeMillis());
			String dateString = (String) DateFormat.format(
					"yyyy.MM.dd  hh.mm.ss", date);
			String fileName = "sdcard/HRV/HRV/HRV-" + dateString + ".txt";

			Canvas gridCanvas = sfv.getHolder().lockCanvas();

			drawGrid(gridCanvas);
			sfv.getHolder().unlockCanvasAndPost(gridCanvas);
			VALUE_MAX = sfv.getHeight() * 9 / 10;
			VALUE_MIN = sfv.getHeight() / 10;

			while ((string = bufferedReader.readLine()) != null && drawing) {
				String str[] = string.split(" ");

				if (str[0].equals("")) {
					System.out.println("------------------------>" + string);

					try {
						next = Float.parseFloat(string);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				} else {
					System.out.println("------------------------>" + str[0]);
					try {
						next = Float.parseFloat(str[0]);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

				}

				testVector.add(next); // NSR/VT&VF test vector
				drawVector.add(next);// draw vector

				if (drawVector.size() == 10) {
					byte[] markByte = new byte[10];
					QRSmarker(markByte, drawVector);
					vector2.clear();
					realTimeChart(drawVector, markByte);
					drawVector.clear();
				}
				if (testVector.size() == 2000) { // NSR/VF test per 6 secs
					detector.Add(testVector);
					signalStr = detector.Process();

					Message message = new Message();
					message.obj = signalStr;
					message.what = 0;
					handler.sendMessage(message);
					testVector.clear();

				}
				//
			}

			// bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveHRVData(String hrvFN) {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(
					hrvFN));

			BufferedReader bReader = new BufferedReader(new FileReader(
					new File(path)));
			int count = 0, lastIndex = -1, TWO_PEAK_DIFF;
			int delay = 0;
			String string;
			while ((string = bReader.readLine()) != null) {

				float tmp = Float.parseFloat(string);
				if (tmp < 5) {
					tmp *= 200f;
				} else {
					tmp *= 2f;
				}
				int smp = (int) (tmp);
				delay = m_qrsDet.addSample(smp);
				count++;
				if (delay != 0) {
					if (lastIndex < 0) {
						lastIndex = count - delay;
					} else {
						TWO_PEAK_DIFF = count - lastIndex - delay;
						lastIndex = count - delay;
						dos.writeBytes(TWO_PEAK_DIFF / HRVDATA_CAL_LENTH + "\n");
					}
				}
			}
			handler.sendEmptyMessage(1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// draw real time chart
	private void realTimeChart(Vector<Float> vector, byte[] markArray) {
		if (drawing) {
			if (X_index >= sfv.getWidth()) {
				X_index = 0;
			}
			SampleDraw(X_index, vector, rateX, rateY, baseLine, markArray);
			X_index = X_index + (vector.size() / rateX);
		}

	}

	// draw every ECG sample on the screen
	void SampleDraw(int start, Vector<Float> vector, int rateX, int rateY,
			float baseLine, byte[] markArray) {

		if (start == 0)
			oldX = 0;

		Float array[] = new Float[vector.size()];
		vector.toArray(array);
		Arrays.sort(array);
		dMax = sfv.getHeight();
		dMin = sfv.getHeight() / 2;
		dMid = (dMax + dMin) / 2;
		rate = dMid / rMid;
		if (array[array.length - 1] < 5) {
			rate = 40;

		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] < 5) {
				array[i] *= 100;
			}
		}

		if (array[0] < VALUE_MIN) {
			VALUE_MIN = array[0];
		} else if (array[array.length - 1] > VALUE_MAX) {
			VALUE_MAX = array[array.length - 1];
		}
		if ((VALUE_MAX + VALUE_MIN) / 2 > (sfv.getHeight() / 10 + sfv
				.getHeight() * 9 / 10) / 2) {
			baseLine = (VALUE_MAX + VALUE_MIN) / 2
					- (sfv.getHeight() / 10 + sfv.getHeight() * 9 / 10) / 2;
		} else {
			baseLine = (sfv.getHeight() / 10 + sfv.getHeight() * 9 / 10) / 2
					- (VALUE_MAX + VALUE_MIN) / 2;
		}

		// float[] buffer = new float[vector.size() / rateX];
		byte[] markBuffer = markArray;

		// for (int i = 0, ii = 0; i < buffer.length; i++, ii = i * rateX){
		// buffer[i] = vector.get(ii);
		// if (markArray[i]==1) {
		// markBuffer[i/rateX]=1;
		// }
		// }

		Canvas canvas = sfv.getHolder().lockCanvas(
				new Rect(start, 0, start + vector.size(), sfv.getHeight()) // draw
				);

		if (canvas != null) {
			canvas.drawColor(Color.BLACK);// clear previous line
			drawGrid(canvas);
		}
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		// paint.setStrokeWidth(3f);

		for (int i = 0; i < vector.size(); i++) {
			float y = vector.get(i);
			timeVector.add(y);
			int yellow_end = 0, xinlv = 0;
			if (timeVector.size() > 800) {
				yellow_end = herttime.size();
				/*
				 * yellow_end/(800/256)=x/60 x=3/40*256*yellow_end
				 */
				xinlv = 60 * 300 * yellow_end / 800;
				// xinlv=(red_end-red_begin)*60/(264*4);
				timeVector.clear();
				herttime.clear();
				Message message = new Message();
				message.what = 9;
				message.obj = xinlv;
				handler.sendMessage(message);
			}

			y = y * rate + baseLine - 0.2f;
			int x = i + start;

			if (oldX != 0 && canvas != null) {
				// try {
				// Thread.sleep(4);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				canvas.drawLine(oldX, (float) oldY, x, y, paint);
				canvas.drawLine(oldX + 1, (float) oldY, x, y, paint);
				canvas.drawLine(oldX + 2, (float) oldY, x, y, paint);

			}
			oldX = x;
			oldY = y;

		}
		if (canvas != null)
			sfv.getHolder().unlockCanvasAndPost(canvas);// upload drawn chart
		Canvas markCanvas = null;
		Paint markPaint = new Paint();
		markPaint.setColor(Color.YELLOW);
		// if (start+buffer.length>SFV_WIDTH) {
		// markCanvas=sfv2.getHolder().lockCanvas(
		// new Rect(0, 0, start + buffer.length*2, sfv.getHeight())
		// );
		// }
		// else {
		markCanvas = sfv2.getHolder().lockCanvas( // clear mark
				new Rect(start, 0, start + vector.size(), sfv2.getHeight()));
		// }

		if (markCanvas != null) {
			markCanvas.drawColor(Color.BLACK);
			sfv2.getHolder().unlockCanvasAndPost(markCanvas); // upload cleared
																// canvas
		}

		for (int i = 0; i < markBuffer.length; i++) {
			if (markBuffer[i] == 1) {

				int x = 0;
				if (start + i - delay < 0) {
					if (start == 0)
						x = SFV_WIDTH + i - delay;
					else {
						x = SFV_WIDTH + start + i - delay;
					}
				} else {

					x = start + i - delay - 8;
				}
				markCanvas = sfv2.getHolder().lockCanvas(
						new Rect(x, 0, x + vector.size(), sfv2.getHeight()));
				if (markCanvas != null) {
					markCanvas.drawColor(Color.BLACK);
					markCanvas.drawText("N", x, sfv2.getHeight() - 20,
							markPaint);
					markCanvas.drawLine(x + 4, sfv2.getHeight() - 20, x + 4,
							sfv2.getHeight() - 10, markPaint);
					sfv2.getHolder().unlockCanvasAndPost(markCanvas);
					Calendar calendar = Calendar.getInstance();
					int seconds = calendar.get(Calendar.SECOND);
					herttime.add(timecount);
					/*
					 * int red_begin=0,red_end=0,xinlv=0;
					 * if(herttime.size()==1){ red_begin=timeVector.size(); }
					 * if(herttime.size()>4){ red_end=timeVector.size();
					 * xinlv=(red_end-red_begin)*60/(264*4); timeVector.clear();
					 * herttime.clear(); Message message = new Message();
					 * message.what = 9; message.obj = xinlv;
					 * handler3.sendMessage(message); }
					 */

				}
			} else if (markBuffer[i] == 2) {
				markCanvas.drawText("V", start + i - 10, sfv2.getHeight() - 40,
						markPaint);
				markCanvas.drawLine(start + i - 6, sfv2.getHeight() - 40, start
						+ i - 6, sfv2.getHeight() - 30, markPaint);

			}

		}

	}

	// when the return key is pressed,handle the keyevent
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new AlertDialog.Builder(context)
					.setPositiveButton("Yes", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();

						}
					}).setNegativeButton("No", null).setTitle("Really Exit?")
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// @Override
	// protected void onStop() {
	// // TODO Auto-generated method stub
	// super.onStop();
	// drawing=false;
	// X_index=0;
	// }
	/**
	 * 获取现在时间
	 * 
	 * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	protected void myDialog() {
		Builder b = new AlertDialog.Builder(ECGDemo1.this)
				.setTitle("已经发送过!是否继续发送?");
		b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				sendSMS(sms, phone);
			}
		});
		b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		b.create().show();

	}
}