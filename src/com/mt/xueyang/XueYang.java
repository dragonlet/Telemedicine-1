/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mt.xueyang;

import java.io.DataInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import mobileSqlite.ExtendSQLiteOpenHelper;
import mobileSqlite.ExtendSQLiteOpenHelper.body;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.chart.CombinedTemperatureChart;
import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ecg.R;
import com.android.ecg.realtime.Detector;
import com.android.upload.DFileItem;
import com.android.upload.DUploadUntil;
import com.mt.helpclass.Ipport;
import com.view.expand.lineview;

/**
 * This is the main Activity that displays the current chat session.
 */
public class XueYang extends Activity implements OnClickListener {

	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final int MESSAGE_READINT = 6;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// public SaveTData saveTData;
	public String username = null;
	public boolean savedata = false;
	public boolean upload = false;

	public Thread mThread = null;
	// Layout Views
	private TextView mTitle;
	// private ListView mConversationView;
	private TextView highT;
	private TextView lowT;
	private TextView pulseT;
	private Button mOutEditText;
	private Button mSendButton;
	private Button search_btn;
	// List<String> list1, list2, list3;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	// private ArrayAdapter<String> mConversationArrayAdapter;
	private ArrayList<HashMap<String, String>> values = new ArrayList<HashMap<String, String>>();
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private XueYangBluetoothService mChatService = null;
	String userid = null;
	String readMessage;
	SharedPreferences Inform = null;

	private SimpleAdapter adapter;
	private String cursor = "cursor";
	private String daname = "Health";
	private ExtendSQLiteOpenHelper sqLiteOpenHelper;
	private String table_name = "bodyindex";
	private String[] dbfeild = { body.id, body.time, body.name, body.sex,
			body.age, body.highb, body.lowb, body.pulse, body.date, body.week };
	/*************************************************************************/
	private Timer timer = new Timer();
	private TimerTask task;
	private Handler handler;
	private String title = "Signal Strength";
	private XYSeries series;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private Context context;
	private int addX = -1, addY;
	int[] xv = new int[1];
	int[] yv = new int[1];
	private int barData;
	/*************************************************************************/
	// IDemoChart[] mCharts = new IDemoChart[] { new SalesStackedBarChart() };
	IDemoChart[] mCharts = new IDemoChart[] { new CombinedTemperatureChart() };

	Calendar today;
	Calendar calendarStart;// 锟斤拷锟斤拷时锟斤拷
	private ProgressDialog pdialog;

	// 上传和停止上传的按钮
	Button upload_bt, stopUploadButton;

	// 上传的Handler
	Handler mUpLoadHandler;
	private Thread thread;

	// 发送的字节流
	private static Vector<Byte> sendBytes1 = new Vector<Byte>();
	// 上传标识
	private static boolean bool_up_StartFlag = false;
	//
	private static int SameFlag = 1;
	// 异步锁标记
	private Object syncFlag = new Object();

	// 统计棒图的记录及数量
	Vector<Byte> spo2List = new Vector<Byte>();
	Vector<Byte> bangList = new Vector<Byte>();
	DFileItem file,file2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		creatrDb(daname);
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.pluselayout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// 上传按钮设置
		upload_bt = (Button) findViewById(R.id.upload);
		upload_bt.setOnClickListener(this);

		// 停止上传按钮设置
		stopUploadButton = (Button) findViewById(R.id.stop_upload);
		stopUploadButton.setOnClickListener(this);
		// IDemoChart[] mCharts =new IDemoChart[]( new SalesStackedBarChart()) ;
		// Get local Bluetooth adapter
		Inform = getSharedPreferences("perference", MODE_PRIVATE);

		userid = Inform.getString("USERID", "");
		username = Inform.getString("UserName", "");
		// savedata = Inform.getBoolean("Upload", defValue);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// list1 = new ArrayList<String>();
		// list2 = new ArrayList<String>();
		// list3 = new ArrayList<String>();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			Toast.makeText(this, "Bluetooth is  available", Toast.LENGTH_LONG)
					.show();
		}

		/***************************************************************************/
		context = getApplicationContext();
		// if(!username.equals("")){
		// saveTData = new SaveTData(username+"pulse.txt");
		// }else{
		// saveTData = new SaveTData("pulse.txt");
		// }

		// 锟斤拷锟斤拷锟斤拷main锟斤拷锟斤拷锟较的诧拷锟街ｏ拷锟斤拷锟斤拷锟斤拷图锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutReal);
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷系锟斤拷锟斤拷械悖拷锟揭伙拷锟斤拷锟侥硷拷锟较ｏ拷锟斤拷锟斤拷锟斤拷些锟姐画锟斤拷锟斤拷锟斤拷
		series = new XYSeries(title);
		// 锟斤拷锟斤拷一锟斤拷锟斤拷锟捷硷拷锟斤拷实锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷菁锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵硷拷锟�
		mDataset = new XYMultipleSeriesDataset();
		// 锟斤拷锟姐集锟斤拷拥锟斤拷锟斤拷锟斤拷锟捷硷拷锟斤拷
		mDataset.addSeries(series);

		// 锟斤拷锟铰讹拷锟斤拷锟斤拷锟竭碉拷锟斤拷式锟斤拷锟斤拷锟皆等等碉拷锟斤拷锟矫ｏ拷renderer锟洁当锟斤拷一锟斤拷锟斤拷锟斤拷锟斤拷图锟斤拷锟斤拷锟斤拷染锟侥撅拷锟�
		int color = Color.GREEN;
		PointStyle style = PointStyle.SQUARE;
		renderer = buildRenderer(color, style, true);

		// 锟斤拷锟矫猴拷图锟斤拷锟斤拷锟绞�
		setChartSettings(renderer, "X", "Y", -2, 2, 0, 65, Color.WHITE,
				Color.WHITE);
		// 锟斤拷锟斤拷图锟斤拷
		String[] types = new String[] { BarChart.TYPE };
		chart = ChartFactory.getCombinedXYChartView(context, mDataset,
				renderer, types);
		// chart = ChartFactory.getBubbleChartView(context, mDataset,
		// renderer);
		// 锟斤拷图锟斤拷锟斤拷拥锟斤拷锟斤拷锟斤拷锟饺�
		layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		// 锟斤拷锟斤拷锟紿andler实锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷Timer实锟斤拷锟斤拷锟斤拷啥锟绞憋拷锟斤拷锟酵硷拷锟侥癸拷锟斤拷
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 刷锟斤拷图锟斤拷
				updateChart();
				super.handleMessage(msg);
			}
		};

		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};

		timer.schedule(task, 1, 1);

		// 上传Handler设置
		mUpLoadHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					new Thread() {
						@Override
						public void run() {
							if (spo2List.isEmpty() || bangList.isEmpty()) {

							} else {
								if (Inform.getBoolean("UploadFlag", false)) {
									
									DFileItem[] uploadfile = new DFileItem[] { file };
									DFileItem[] uploadfile2 = new DFileItem[] { file2 };
									DUploadUntil.Post("1", uploadfile,
											"http://" + Ipport.urlIpTest
													+ Ipport.urlPort + "/RMT"
													+ "/GetSFile");
									DUploadUntil.Post("1", uploadfile2,
											"http://" + Ipport.urlIpTest
													+ Ipport.urlPort + "/RMT"
													+ "/GetBFile");
									/*
									 * spo2List.clear(); bangList.clear();
									 */

									SameFlag++;

								}
							}
							/*
							 * if (!sendBytes1.isEmpty()) { if
							 * (Inform.getBoolean("UploadFlag", false)) { if
							 * (bool_up_StartFlag) { DFileItem file = new
							 * DFileItem( sendBytes1, sendBytes1.size(),
							 * SameFlag); DFileItem[] uploadfile = new
							 * DFileItem[] { file }; DUploadUntil.Post("1",
							 * uploadfile, "http://" + Ipport.urlIpTest +
							 * Ipport.urlPort + "/RMT" + "/GetBFile");
							 * SameFlag++; } sendBytes1.clear(); } else {
							 * sendBytes1.clear(); } }
							 */
						}
					}.start();
					break;

				default:
					break;
				}
			}

		};

		/*
		 * thread = new Thread(getECGThread); thread.start();
		 */

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			Toast.makeText(this, "权锟斤拷锟斤拷锟斤拷:", Toast.LENGTH_LONG).show();

			// 锟斤拷锟斤拷Activity锟斤拷锟斤拷示锟角凤拷锟斤拷锟斤拷锟斤拷锟借备
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			Toast.makeText(this, "权锟斤拷锟斤拷锟斤拷...", Toast.LENGTH_LONG).show();
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == XueYangBluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		// mConversationArrayAdapter = new ArrayAdapter<String>(this,
		// R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		highT = (TextView) findViewById(R.id.highT);
		lowT = (TextView) findViewById(R.id.lowL);
		pulseT = (TextView) findViewById(R.id.pulseT);
		search_btn = (Button) findViewById(R.id.search_btn);

		if (values == null) {
			HashMap<String, String> hashMapNu = new HashMap<String, String>();
			hashMapNu.put("highValue", "c");
			// Log.e("threeIndex[0]", "c");
			hashMapNu.put("lowValue", "c");
			// Log.e("threeIndex[1]", "c");
			hashMapNu.put("pulseValue", "c");
			// Log.e("threeIndex[2]", "c");
			values.add(hashMapNu);
		}
		// Log.e("values=", String.valueOf(values.size()));

		search_btn.setOnClickListener(this);
		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);

		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(XueYang.this, lineview.class);
				startActivity(intent);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new XueYangBluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	private void checkHistory(String[] adtition) {
		Cursor c = queryDb(adtition);
		ArrayList<Integer> high = new ArrayList<Integer>();
		ArrayList<Integer> low = new ArrayList<Integer>();
		ArrayList<Integer> pulse = new ArrayList<Integer>();
		// Log.e("c", String.valueOf(c.getCount()));
		while (c.moveToNext()) {
			high.add(c.getInt(c.getColumnIndex(body.highb)));
			low.add(c.getInt(c.getColumnIndex(body.lowb)));
			pulse.add(c.getInt(c.getColumnIndex(body.pulse)));
			// Log.e("body.pulse)=", body.pulse);
		}
		c.close();
		ArrayList<ArrayList<Integer>> hl = new ArrayList<ArrayList<Integer>>();
		hl.add(high);
		hl.add(low);
		hl.add(pulse);

		Intent intent = mCharts[0].execute(getApplicationContext(),
				(ArrayList<? extends Parcelable>) hl);
		startActivity(intent);

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
		// saveTData.close();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		// saveTData.close();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

			// 锟斤拷锟斤拷Intent锟斤拷锟襟，诧拷锟斤拷锟斤拷action值锟斤拷锟斤拷为BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

			// putExtra(锟斤拷锟斤拷值)
			// 锟斤拷一锟斤拷锟斤拷值锟皆达拷诺锟絠ntent锟斤拷锟斤拷锟叫ｏ拷锟斤拷锟斤拷指锟斤拷锟缴硷拷状态锟斤拷锟斤拷时锟斤拷
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != XueYangBluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case XueYangBluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case XueYangBluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case XueYangBluetoothService.STATE_LISTEN:
				case XueYangBluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// // construct a string from the valid bytes in the buffer
				readMessage = new String(readBuf, 0, msg.arg1);
				// String[] readMessage = (String[]) msg.obj;
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// insertDb(readMessage);
				Log.e("get data", readMessage);
				adapter = formatIndex(readMessage);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// formatIndex(readMessage));
				// mConversationView.setAdapter(adapter);
				// saveTData.save(readMessage);
				// if(upload){
				//
				// new Thread(){
				//
				// @Override
				// public void run() {
				// // TODO Auto-generated method stub
				// super.run();
				// PFileItem file = new PFileItem(
				// readMessage,1);
				// PFileItem[] uploadfile = new PFileItem[] { file };
				// pulseUpload.Post(userid,
				// uploadfile,"http://"+Ipport.urlIp+Ipport.urlPort+"/REMSWeb/GetPulseFile");
				// }}.start();
				// }

				break;
			case MESSAGE_READINT:
				int[] readBufI = (int[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessageI = new String(readBufI, 0, msg.arg1);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessageI);
				break;

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	private void creatrDb(String name) {
		sqLiteOpenHelper = new ExtendSQLiteOpenHelper(XueYang.this, name);
		// SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
	}

	private SimpleAdapter formatIndex(String threeindexsString) {
		StringTokenizer st = new StringTokenizer(threeindexsString, ",");
		// Log.e("锟斤拷锟斤拷",
		// threeindexsString + "---" + String.valueOf(st.countTokens()));
		String[] threeIndex = new String[3];
		int i = 0;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			// Log.e("st", st.nextToken());
			threeIndex[i] = st.nextToken();
			i++;
		}
		hashMap.put("highValue", threeIndex[0]);
		// Log.e("threeIndex[0]", String.valueOf(threeIndex[0]));
		hashMap.put("lowValue", threeIndex[1]);
		// Log.e("threeIndex[1]", String.valueOf(threeIndex[1]));
		hashMap.put("pulseValue", threeIndex[2]);
		// Log.e("threeIndex[2]", String.valueOf(threeIndex[2]));
		values.add(hashMap);
		// Log.e("values.size()", String.valueOf(values.size()));
		adapter = new SimpleAdapter(this, values, R.layout.listitem,
				new String[] { "highValue", "lowValue", "pulseValue" },
				new int[] { R.id.highValue, R.id.lowValue, R.id.pulseValue });
		
		highT.setText(threeIndex[0]);
		lowT.setText(threeIndex[1]);
		if (bool_up_StartFlag) {
			spo2List.add(Byte.parseByte(threeIndex[1]));
			bangList.add(Byte.parseByte(threeIndex[2]));
			if (spo2List.size() >= 100) {
				
				file = new DFileItem(spo2List,
				spo2List.size(), SameFlag);	
				spo2List.clear();
				file2 = new DFileItem(bangList,
						bangList.size(), SameFlag);
			
				bangList.clear();
				mUpLoadHandler.sendEmptyMessage(1);

			}
		}
		pulseT.setText(threeIndex[2]);
		barData = Integer.parseInt(threeIndex[2]);
		
		return adapter;
	}

	private void insertDb(String threeindexsString) {
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();
		// Date date=new Date();
		today = Calendar.getInstance();
		

		StringTokenizer st = new StringTokenizer(threeindexsString, ",");
		
		String[] threeIndex = new String[3];
		// String[] time=new String[6];
		ContentValues values = new ContentValues();
		int i = 0;
		while (st.hasMoreTokens()) {
			// Log.e("st", st.nextToken());
			threeIndex[i] = st.nextToken();
			i++;
		}
		i = 0;
		

		values.put(body.highb, threeIndex[0]);
		values.put(body.lowb, threeIndex[1]);
		values.put(body.pulse, threeIndex[2]);
		int monthOfToday = today.get(Calendar.MONTH) + 1;
		

		String month;
		String dateString;
		if (monthOfToday < 10) {
			month = "0" + monthOfToday;
		} else {
			month = "" + monthOfToday;
		}

		if (today.get(Calendar.DATE) < 10) {
			dateString = "0" + today.get(Calendar.DATE);
		} else {
			dateString = "" + today.get(Calendar.DATE);
		}

		values.put(
				body.date,
				Integer.parseInt("" + today.get(Calendar.YEAR) + month
						+ dateString));
		values.put(body.week, today.get(Calendar.DAY_OF_WEEK));
		values.put(
				body.time,
				"" + today.get(Calendar.HOUR_OF_DAY)
						+ today.get(Calendar.MINUTE)
						+ today.get(Calendar.SECOND));

		db.insert(table_name, null, values);
		db.close();

	}

	private Cursor queryDb(String[] start) {

		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();
		// int da=Integer.parseInt(start[0]);
		// Cursor c=db.query(table_name, dbfeild,"date > da", start, null, null,
		// body.id);
		Cursor c = db.query(table_name, dbfeild, "date >= ?", start, null,
				null, body.date);
		// db.close();
		return c;
	}

	public String fileName() {
		String fileName = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fileName = df.format(new Date());
		return fileName + ".txt";

	}

	/*******************************************************************************/
	protected XYMultipleSeriesRenderer buildRenderer(int color,
			PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 锟斤拷锟斤拷图锟斤拷锟斤拷锟斤拷锟竭憋拷锟斤拷锟斤拷锟绞斤拷锟斤拷锟斤拷锟斤拷锟缴拷锟斤拷锟侥达拷小锟皆硷拷锟竭的达拷细锟斤拷
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.RED);
		// r.setPointStyle(style);
		// r.setFillPoints(fill);
		r.setLineWidth(200);
		renderer.addSeriesRenderer(r);
		return renderer;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, int axesColor, int labelsColor) {
		// 锟叫关讹拷图锟斤拷锟斤拷锟饺撅拷刹慰锟絘pi锟侥碉拷
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.GREEN);
		renderer.setXLabels(1);
		renderer.setYLabels(10);
		renderer.setXTitle("Time");
		renderer.setYTitle("dBm");
		renderer.setYLabelsAlign(Align.RIGHT);
		// renderer.setPointSize((float) 2);
		renderer.setShowLegend(false);
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setPanEnabled(false, false);
		renderer.setMarginsColor(Color.WHITE);
	}

	private void updateChart() {
		// 锟斤拷锟矫猴拷锟斤拷一锟斤拷锟斤拷要锟斤拷锟接的节碉拷
		addX = 0;
		int num = (int) (barData);
		addY = num;
		// Log.e("num=", String.valueOf(num));
		// 锟狡筹拷锟斤拷锟捷硷拷锟叫旧的点集
		mDataset.removeSeries(series);
		// 锟叫断碉拷前锟姐集锟叫碉拷锟斤拷锟叫讹拷锟劫点，锟斤拷为锟斤拷幕锟杰癸拷只锟斤拷锟斤拷锟斤拷100锟斤拷锟斤拷锟斤拷锟皆碉拷锟斤拷锟斤拷锟斤拷锟斤拷100时锟斤拷锟斤拷锟斤拷锟斤拷远锟斤拷100
		int length = series.getItemCount();
		if (length > 0) {
			length = 0;
		}
		// 锟斤拷锟缴的点集锟斤拷x锟斤拷y锟斤拷锟斤拷值取锟斤拷锟斤拷锟斤拷锟斤拷backup锟叫ｏ拷锟斤拷锟揭斤拷x锟斤拷值锟斤拷1锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟狡斤拷频锟叫э拷锟�
		for (int i = 0; i < length; i++) {
			xv[i] = (int) series.getX(i) + 1;
			yv[i] = (int) series.getY(i);
		}
		// 锟姐集锟斤拷锟斤拷眨锟轿拷锟斤拷锟斤拷锟斤拷碌牡慵拷锟阶硷拷锟�
		series.clear();

		// 锟斤拷锟铰诧拷锟斤拷锟侥碉拷锟斤拷锟饺硷拷锟诫到锟姐集锟叫ｏ拷然锟斤拷锟斤拷循锟斤拷锟斤拷锟叫斤拷锟斤拷锟斤拷浠伙拷锟斤拷一系锟叫点都锟斤拷锟铰硷拷锟诫到锟姐集锟斤拷
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟揭伙拷掳锟剿筹拷锟竭碉拷锟斤拷锟斤拷锟斤拷什么效锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷循锟斤拷锟藉，锟斤拷锟斤拷锟斤拷虏锟斤拷锟斤拷牡锟�
		series.add(addX, addY);
		for (int k = 0; k < length; k++) {
			series.add(xv[k], yv[k]);
		}
		// 锟斤拷锟斤拷锟捷硷拷锟斤拷锟斤拷锟斤拷碌牡慵�
		mDataset.addSeries(series);

		// 锟斤拷图锟斤拷锟铰ｏ拷没锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷锟竭诧拷锟斤拷锟斤拷侄锟教�
		// 锟斤拷锟斤拷诜锟経I锟斤拷锟竭筹拷锟叫ｏ拷锟斤拷要锟斤拷锟斤拷postInvalidate()锟斤拷锟斤拷锟斤拷慰锟絘pi
		chart.invalidate();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.search_btn) {

			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		}

		// 点击上传按钮
		if (v.getId() == R.id.upload) {
			bool_up_StartFlag = true;
			SameFlag = 1;
			spo2List = new Vector<Byte>();
			bangList = new Vector<Byte>();
		}

		// 点击停止上传按钮
		if (v.getId() == R.id.stop_upload) {
			Log.i("XueYang", "-->停止上传");

			bool_up_StartFlag = false;

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// System.exit(0);
			bool_up_StartFlag = false;
			SameFlag = 1;
			finish();

		}
		return super.onKeyDown(keyCode, event);

	}

	public void handComm(String user) {

		if (!user.equals("")) { // 锟较达拷锟斤拷片锟斤拷锟斤拷

			// 锟斤拷锟斤拷锟斤拷
			Map<String, String> params = new HashMap<String, String>();

			params.put("user", user);// 锟矫伙拷锟斤拷

			// new EditAsyncTask().execute(params);

		} else {
			Toast.makeText(XueYang.this, "锟矫伙拷未锟斤拷录锟斤拷", 1).show();
		}
	}



	private static InputStream inputStream = null;

	private static Vector<Byte> sendBytes2 = new Vector<Byte>();

	private Boolean _dataFlag = true;

	boolean recording = true;

	Detector detector = new Detector();

	BluetoothSocket btSocket = null;

	private static DataInputStream dis = null;


}
