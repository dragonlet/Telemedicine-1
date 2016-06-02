package com.android.ecg.realtime;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import com.android.ecg.GPSToLoaction;
import com.android.ecg.Mylocation;
import com.android.ecg.R;
import com.android.upload.DFileItem;
import com.android.upload.DUploadUntil;
import com.mt.helpclass.EXHepler;
import com.mt.helpclass.Ipport;
import com.view.expand.DeviceListActivitydemo;
import com.zone.set.setActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ECGRealTime extends Activity implements
		android.view.View.OnClickListener {

	BluetoothAdapter btAdapter;
	boolean recording = true;
	TextView textView;

	@Override
	protected void onStart() {
		hadSendSMS = false;
		super.onStart();
	}

	DataOutputStream dos;
	static int SFV_WIDTH;
	Context context;
	int VALUE_MAX = 0, VALUE_MIN = 0;

	BluetoothSocket btSocket = null;
	Vector<Float> vector;
	Handler handler, handler2, handler3;

	private Button upload, notupload, search_device,save;

	Thread thread, drawThread;
	Vector<Float> testVector = new Vector<Float>();
	Vector<Float> sestVector = new Vector<Float>();
	Vector<Float> destVector = new Vector<Float>();

	private float oldX = 0;
	float oldY = 0;
	private SurfaceView sfv, sfv2;
	private int X_index = 0;
	public int baseLine = 1;
	private int rateX = 1;
	private int rateY = 1;
	QRSDet m_qrsDet;
	static int ECG_SAMPLE_NUM = 72;
	static int nEcg_Sample = 4;
	// float[] minDatas;
	int delay = 0;
	boolean isSaveHrv = false;
	int lastIndex = -1;
	final static float HRVDATA_CAL_LENTH = 216f;
	Vector<byte[]> Queue1;
	Vector<Vector<Float>> Queue2;
	InputStream SettingInputStream;

	TextView signal;
	String signalStr;
	Detector detector = new Detector();
	AlertDialog dialog;

	private final static float ADJUST_VALUE = 1010;
	private final static int ZOOM_LEVEL = 60;

	float rMax = 240, rMin = 40, rMid = (rMax + rMin) / 2;// real time data
	float dMax = 0, dMin = 0, dMid = 0; // display
	float rate = 0;
	// 璁℃暟
	private TextView hertcount;

	private static long timecount = 0;
	private static Vector<Long> herttime = new Vector<Long>();
	private static Vector<Integer> herttime1 = new Vector<Integer>();
	private static Vector<Float> timeVector = new Vector<Float>();
	private static Vector<Byte> sendBytes1 = new Vector<Byte>();
	private static Vector<Byte> sendBytes2 = new Vector<Byte>();
	private Boolean _dataFlag = true;

	SharedPreferences Inform = null;
	SharedPreferences info;

	String userid = null;

	private static boolean bool_up_StartFlag = false;

	private static int SameFlag = 1;
	AlertDialog smsDialog;
	int count = 0;// 5 secs
	String SMSContent;
	View smsLayout;
	ProgressBar pBar;
	TextView smsContent;
	TextView smsTitle;
	TextView countDownTxt;
	private TextView isnormal;
	final String initialSmsTitle = "After 5s,Send a following sms to hospital(you can cancel)";
	final String afterSmsSentTitle = "The Message has been sent to hospital!";
	boolean isSendSMS = false;

	private static InputStream inputStream = null;
	private static DataInputStream dis = null;
	private String phone;
	private String name;
	private int second1 = 0, second2 = 0;

	private boolean hadSendSMS;
	GPSToLoaction gps;
	private String sms;
	private long swapTime = 0;
	long time[] = { 0 };
	int swap = 0;
	boolean flag = false;
	Map<Integer, Long> map = new HashMap<Integer, Long>();
	private int nowHertLv = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.xindianyi_layout);
		// setContentView(R.layout.xindianyi_layout);

		gps = new GPSToLoaction(ECGRealTime.this);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		context = ECGRealTime.this;
		SFV_WIDTH = getWindowManager().getDefaultDisplay().getWidth();
		ECG_SAMPLE_NUM = SFV_WIDTH / 4;
		nEcg_Sample = SFV_WIDTH / ECG_SAMPLE_NUM;

		sfv = (SurfaceView) findViewById(R.id.sfv);
		sfv.setOnTouchListener(touchListener);
		sfv2 = (SurfaceView) findViewById(R.id.sfv2);
		// sfv.setZOrderOnTop(true);

		// sfv.setZOrderOnTop(true);
		sfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		Queue1 = new Vector<byte[]>(10);
		Queue2 = new Vector<Vector<Float>>(60);

		m_qrsDet = new QRSDet();
		upload = (Button) findViewById(R.id.upload);
		notupload = (Button) findViewById(R.id.notupload);
		search_device = (Button) findViewById(R.id.serch_device);
		save=(Button)findViewById(R.id.save_device);
		upload.setOnClickListener(this);
		notupload.setOnClickListener(this);
		search_device.setOnClickListener(this);

		hertcount = (TextView) findViewById(R.id.heartRate2);
		isnormal = (TextView) findViewById(R.id.isnormal);
		setHeartAnimation((ImageView) findViewById(R.id.heartImg));

		vector = new Vector<Float>();
		signal = (TextView) findViewById(R.id.textview);
		textView = (TextView) findViewById(R.id.textview);
		textView.setVisibility(TextView.VISIBLE);

		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		info = getSharedPreferences("INFO", 0);

		userid = Inform.getString("USERID", "");

		// 鎻愮ず妗?

		smsLayout = View.inflate(context, R.layout.sms_dialog, null);
		pBar = (ProgressBar) smsLayout.findViewById(R.id.SmsprogressBar);
		smsContent = (TextView) smsLayout.findViewById(R.id.smsContent);
		smsTitle = (TextView) smsLayout.findViewById(R.id.smsTitle);
		countDownTxt = (TextView) smsLayout.findViewById(R.id.coutDowntxt);
		smsTitle.setText(initialSmsTitle);
		pBar.setMax(5);
		pBar.incrementProgressBy(1);

		smsLayout = View.inflate(context, R.layout.sms_dialog, null);
		smsDialog = new AlertDialog.Builder(context)
				.setIcon(R.drawable.sms1)
				.setTitle("Send SMS to Hospital")
				.setView(smsLayout)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								isSendSMS = false;
							}
						}).setCancelable(true).create();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1: // VF
					break;
				case 2: // NSR detect
					String nsrSignal = (String) msg.obj;
					signal.setText("Signal:   " + nsrSignal);
					Log.e("nsrSignal", "<<nsrSignal>>");
					if (nsrSignal.equals("NSR")) {
						isnormal.setText("正常");
						info.edit().putString("xindian", "正常").commit();
					} else {
						isnormal.setText("危险");
						info.edit().putString("xindian", "危险").commit();
						if (hadSendSMS) {
							myDialog();
						} else
						// if(!phone.equals("")&&phone!=null){
						if (!"".equals(phone) && null != phone) {
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
						} else {
							Toast.makeText(context, "没有保存紧急联系电话",
									Toast.LENGTH_SHORT).show();
						}

					}
					break;
				case 3:
					break;
				case 4:// waiting for bluetooth connection
					break;
				}
			}
		};
		thread = new Thread(getECGThread);
		thread.start();
		HandlerThread handlerThread = new HandlerThread("HandlerThread");
		handlerThread.start();

		handler2 = new Handler(handlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0: // getECGVector.size==10
					Queue1.add((byte[]) msg.obj);
					if (Queue1.size() >= Queue1.capacity() / 2) {
						byte[] tmp = Queue1.get(0);
						for (int i = 0; i < tmp.length; i = i + 10) {
							byte[] array;
							if (tmp.length - i < 10) {
								array = new byte[tmp.length - i];
								System.arraycopy(tmp, i, array, 0, tmp.length
										- i);
							} else {
								array = new byte[8];
								System.arraycopy(tmp, i, array, 0, 10);
							}
							handleECG(array);
						}
						Queue1.remove(0);
					}
					break;
				case 10:
					break;
				}
			}
		};
		// 蹇冪巼
		handler3 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case 9:
					int i = (Integer) msg.obj;
					hertcount.setText(String.valueOf(i));
					break;
				case 11:
					final int base = (Integer) msg.obj;
					new Thread() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							if (base == 1) {
								if (!sendBytes1.isEmpty()) {
									if (Inform.getBoolean("UploadFlag", false)) {
										if (bool_up_StartFlag) {
											DFileItem file = new DFileItem(
													sendBytes1,
													sendBytes1.size(), SameFlag);
											DFileItem[] uploadfile = new DFileItem[] { file };
											DUploadUntil.Post("1", uploadfile,
													"http://"
															+ Ipport.urlIpTest
															+ Ipport.urlPort
															+ "/RMT"
															+ "/GetDFile");
											SameFlag++;
										}
										sendBytes1.clear();
									} else {
										sendBytes1.clear();
									}
								}
							} else {
								if (!sendBytes2.isEmpty()) {
									if (Inform.getBoolean("UploadFlag", false)) {
										if (bool_up_StartFlag) {
											DFileItem file = new DFileItem(
													sendBytes2,
													sendBytes2.size(), SameFlag);
											DFileItem[] uploadfile = new DFileItem[] { file };
											DUploadUntil.Post("1", uploadfile,
													"http://" + Ipport.urlIp
															+ Ipport.urlPort
															+ "/GetDFile");
											SameFlag++;
										}
										sendBytes2.clear();
									} else {
										sendBytes2.clear();
									}
								}
							}
						}
					}.start();
					break;
				}
			}

		};
		phone = new EXHepler(ECGRealTime.this, "phoneconn").getValue("phone");
		name = new EXHepler(ECGRealTime.this, "perference")
				.getValue("UserName");
	}

	private void setHeartAnimation(ImageView iv) {
		// shan shuo
		AlphaAnimation alphaAnimation1 = new AlphaAnimation(0f, 1.0f);
		alphaAnimation1.setDuration(500);
		alphaAnimation1.setRepeatCount(Animation.INFINITE);
		alphaAnimation1.setRepeatMode(Animation.REVERSE);
		iv.setAnimation(alphaAnimation1);
		alphaAnimation1.start();
	}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "开始上传数据");
		menu.add(0, 2, 0, "停止上传数据");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			bool_up_StartFlag = true;
			SameFlag = 1;
			break;
		case 2:
			bool_up_StartFlag = false;
			SameFlag = 1;
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			baseLine = (int) event.getX();
			return true;
		}
	};

	/**
	 * ---------------循环发送消息向服务器
	 */
	private Runnable getECGThread = new Runnable() {
		BluetoothDevice connectedDev = null;

		@Override
		public void run() {
			String _MAC = Inform.getString("Mac", "");
			Log.v("sdad", _MAC);
			connectedDev = btAdapter.getRemoteDevice(_MAC);
			Log.v("sdsda", _MAC);
			String uuidString = "00001101-0000-1000-8000-00805F9B34FB";
			UUID uuid = UUID.fromString(uuidString);
			btAdapter.cancelDiscovery();
			try {
				InputStream xmlFile = getResources().openRawResource(
						R.raw.detector);
				detector.Init("4,2000,11,40,256,240", xmlFile);
				Canvas gridCanvas = sfv.getHolder().lockCanvas();
				while (gridCanvas == null) {
					Thread.sleep(100);
					gridCanvas = sfv.getHolder().lockCanvas();
				}
				drawGrid(gridCanvas);
				sfv.getHolder().unlockCanvasAndPost(gridCanvas);

				if (btSocket != null) {
					btSocket.close();
					thread.sleep(1000);
				}
				try {
					btSocket = connectedDev
							.createRfcommSocketToServiceRecord(uuid);
					btSocket.connect();
					inputStream = btSocket.getInputStream();
				} catch (IOException e) {
					// TODO: handle exception
					Looper.prepare();
					System.out.println(e.toString());
					Toast.makeText(getApplicationContext(), "蓝牙连接超时，请重新配对",
							Toast.LENGTH_LONG).show();
					Looper.loop();
				}
				dis = new DataInputStream(new BufferedInputStream(inputStream));
				VALUE_MAX = sfv.getHeight() * 9 / 10;
				VALUE_MIN = sfv.getHeight() / 10;
				while (recording) {
					byte[] buffer = new byte[8];
					byte tmp;
					tmp = dis.readByte();
					if (tmp == (byte) 0x55) {
						tmp = dis.readByte();
						if (tmp == (byte) 0xAA) {
							dis.readByte();
							tmp = dis.readByte();
							if (tmp == (byte) 0x01) {
								for (int i = 0; i < 8; i++) {
									buffer[i] = dis.readByte();
									if (_dataFlag) {
										if (sendBytes1.size() >= 2000) {
											_dataFlag = false;
											sendBytes2.add(buffer[i]);
											Message message = new Message();
											message.what = 11;
											message.obj = 1;
											handler3.sendMessage(message);// 涓婁紶璇ユ暟鎹?
										} else {
											sendBytes1.add(buffer[i]);
										}
									} else {
										if (sendBytes2.size() >= 2000) {
											_dataFlag = true;
											sendBytes1.add(buffer[i]);
											Message message = new Message();
											message.what = 11;
											message.obj = 2;
											handler3.sendMessage(message);
										} else {
											sendBytes2.add(buffer[i]);
										}
									}
								}

								Message message = new Message();
								message.what = 0;
								message.obj = buffer;
								handler2.sendMessage(message);
							}
						}
					}
				}
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Looper.prepare();
				Toast.makeText(getApplicationContext(), "结束了1",
						Toast.LENGTH_LONG).show();
				Looper.loop();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private void handleECG(byte[] buffer) {
		try {
			int[] datas = new int[4];
			float[] fdatas = new float[4];
			for (int i = 0, j = 0; i < 8; i += 2, j++) {
				short y = (short) (((buffer[i + 1] & 0xff) << 8) + (buffer[i] & 0xff));
				datas[j] = (int) (y / ADJUST_VALUE * ZOOM_LEVEL);
				fdatas[j] = y / (ADJUST_VALUE * ZOOM_LEVEL);
				testVector.add(fdatas[j]);
				if (testVector.size() >= 2000) {
					detector.Add(testVector);
					Message message = new Message();
					message.what = 2;
					message.obj = detector.Process();
					handler.sendMessage(message);
					testVector.clear();
				}
			}
			byte[] markByte = new byte[datas.length];
			QRSmarker(markByte, datas);
			// QRSdetectResulttoDisplay(markByte);
			realTimeChart(datas, markByte);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("Err", "Error");
			e.printStackTrace();

		}
	}

	boolean isHaveMarked = false;
	int TWO_PEAK_DISTANCE = 0;

	private void QRSmarker(byte[] markArray, int[] datas) {
		for (int i = 0; i < markArray.length; i++) {
			markArray[i] = 0;
		}
		int delay = 0;
		for (int j = 0; j < markArray.length; j++) {
			int smp = (datas[j] * 12);
			Log.i("smp", String.valueOf(smp));
			delay = m_qrsDet.addSample(smp);
			if (delay != 0) {
				this.delay = delay;
				markArray[j] = 1;
			}
		}
	}

	private void QRSmarker1(byte[] markArray, double[] datas) {
		for (int i = 0; i < markArray.length; i++) {
			markArray[i] = 0;
		}
		int delay = 0;
		for (int j = 0; j < markArray.length; j++) {
			int smp = (int) (datas[j] * 12);
			Log.i("smp", String.valueOf(smp));
			delay = m_qrsDet.addSample(smp);
			if (delay != 0) {
				this.delay = delay;
				markArray[j] = 1;
			}
		}
	}

	private void realTimeChart(int[] datas, byte[] markArray)
			throws IOException {
		if (X_index > sfv.getWidth()) {
			X_index = 0;
		}
		if (datas.length != 0) {
			SimpleDraw(X_index, datas, rateX, rateY, baseLine, markArray);
			X_index = X_index + (datas.length / rateX);//
		}
	}

	void SimpleDraw(int start, int[] datas, int rateX, int rateY, int baseLine,
			byte[] markArray) {
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
		} else if (sortedArray[sortedArray.length - 1] > VALUE_MAX) {
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
			canvas.drawColor(Color.BLACK);// 閿熸枻鎷烽敓锟?
			drawGrid(canvas);
		}
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3f);
		for (int i = 0; i < datas.length; i++) {
			float y = dMin - datas[i];
			draw(y);
			timeVector.add(y);

			int yellow_end = 0, xinlv = 0;
			if (timeVector.size() > 800) {
				yellow_end = herttime.size();
				xinlv = 60 * 125 * yellow_end / 800;
				nowHertLv = xinlv;
				timeVector.clear();
				herttime.clear();
				Message message = new Message();
				message.what = 9;
				message.obj = xinlv;
				handler3.sendMessage(message);
			}

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
				if (markCanvas != null) {
					markCanvas.drawColor(Color.BLACK);
					markCanvas.drawLine(x + 4, sfv2.getHeight() - 20, x + 4,
							sfv2.getHeight(), markPaint);
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

	}

	public void draw(float y) {
		if (flag == true) {

			String ret = "";
			ret += -26 - (y / 50 * -1) + 20;
			ret += "\r\n";
			// 获取SD卡目录
			File sdDir = Environment.getExternalStorageDirectory();
			// 在SD卡目录下创建文件name.txt文件，true表示当文件存在时，信息追加在文件尾
			FileWriter fw = null;
			try {
				fw = new FileWriter(sdDir.toString() + "/nv.txt", true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fw.write(ret);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new AlertDialog.Builder(context)
					.setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								if (btSocket != null)
									btSocket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							recording = false;
							bool_up_StartFlag = false;
							SameFlag = 1;
							finish();
						}
					}).setNegativeButton("No", null).setTitle("Really Exit?")
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try {
			if (btSocket != null)
				btSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recording = false;
		bool_up_StartFlag = false;
		SameFlag = 1;
		finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			if (btSocket != null)
				btSocket.close();
			if (inputStream != null)
				if (inputStream != null)
					;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recording = false;
		bool_up_StartFlag = false;
		SameFlag = 1;
		finish();
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.upload) {
			// flag=true;
			bool_up_StartFlag = true;
			SameFlag = 1;
		}
		if (v.getId() == R.id.notupload) {
			// flag=false;
			bool_up_StartFlag = false;
			SameFlag = 1;
		}
		if (v.getId() == R.id.serch_device) {
			Intent intent = new Intent(ECGRealTime.this, SelectLoadMethod.class);
			intent.putExtra("item1", "ECGRealTime");
			intent.putExtra("info1",
					"Get ECG datas and draw graph in real time.");
			intent.putExtra("item2", "ECGDemo");
			intent.putExtra("info2", "Run a ECG demo.");
			startActivity(intent);
		}
		if(v.getId()==R.id.save_device){
			
		}
	}

	protected void myDialog() {
		Builder b = new AlertDialog.Builder(ECGRealTime.this)
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
