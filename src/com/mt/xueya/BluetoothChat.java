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

package com.mt.xueya;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ecg.R;
import com.android.ecg.UploadUntil;
import com.android.network.UpLoadWordAsyncTask;
import com.mt.helpclass.Ipport;
import com.mt.tools.Constants;
import com.mt.tools.HttpUtils;
import com.mt.xueya.ExtendSQLiteOpenHelper.body;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {

	Context mContext;
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

	// Layout Views
	private TextView mTitle;
	// private ListView mConversationView;
	private TextView highT;
	private TextView lowT;
	private TextView pulseT;
	private Button mOutEditText;
	private Button mSendButton;

	/**
	 * 涓婁紶鏁版嵁缁欐湇鍔″櫒鐨勬寜閽�	 */
	private Button mSendWordButton;

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
	private BluetoothChatService mChatService = null;
	private ProgressDialog pdialog;

	ArrayList list;
	private SimpleAdapter adapter;
	private String cursor = "cursor";
	private String daname = "Health";
	private ExtendSQLiteOpenHelper sqLiteOpenHelper;
	private String table_name = "bodyindex";
	private String[] dbfeild = { body.id, body.time, body.name, body.sex,
			body.age, body.highb, body.lowb, body.pulse, body.date, body.week };
	// IDemoChart[] mCharts = new IDemoChart[] { new SalesStackedBarChart() };
	IDemoChart[] mCharts = new IDemoChart[] { new CombinedTemperatureChart() };
	public String username;
	public SharedPreferences Inform = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		creatrDb(daname);
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mainpage);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		
		//发送按钮
		mSendWordButton=(Button) findViewById(R.id.button_send_word);
		
		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		username = Inform.getString("UserName", "");
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// IDemoChart[] mCharts =new IDemoChart[]( new SalesStackedBarChart()) ;
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

		//	mSendWordButton = (Button) findViewById(R.id.button_send_word);
		mSendWordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				highT = (TextView) findViewById(R.id.highT);
				lowT = (TextView) findViewById(R.id.lowL);
				pulseT = (TextView) findViewById(R.id.pulseT);
				if (highT.getText().toString().trim() != null
						&& !highT.getText().toString().trim().equals("")
						&& lowT.getText().toString().trim() != null
						&& !lowT.getText().toString().trim().equals("")
						&& pulseT.getText().toString().trim() != null
						&& !pulseT.getText().toString().trim().equals("")) {

					Map<String, String> mMap = new HashMap<String, String>();
					mMap.put("user", "张三");
					mMap.put("highValue", highT.getText().toString().trim());
					mMap.put("lowValue", lowT.getText().toString().trim());
					mMap.put("pluseValue", pulseT.getText().toString().trim());
					new UpLoadWordAsyncTask(mContext, "http://" + Ipport.urlIpTest
							+ Ipport.urlPort + "/RMT/Xueya_phoneAdd.action").execute(mMap);
				} else {
					Toast.makeText(mContext, "未获得完整数据", Toast.LENGTH_LONG).show();
				}
			}
		});

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
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
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

		if (values == null) {
			HashMap<String, String> hashMapNu = new HashMap<String, String>();
			hashMapNu.put("highValue", "c");
			Log.e("threeIndex[0]", "c");
			hashMapNu.put("lowValue", "c");
			Log.e("threeIndex[1]", "c");
			hashMapNu.put("pulseValue", "c");
			Log.e("threeIndex[2]", "c");
			values.add(hashMapNu);
		}
		Log.e("values=", String.valueOf(values.size()));

		// adapter = new SimpleAdapter(this,
		// values,
		// R.layout.listitem,
		// new String[]{"highValue","lowValue","pulseValue"},
		// new int[]{R.id.highValue,R.id.lowValue,R.id.pulseValue});
		// // mConversationView.setAdapter(mConversationArrayAdapter);
		// mConversationView.setAdapter(adapter);
		// Initialize the compose field with a listener for the return key
		mOutEditText = (Button) findViewById(R.id.text_this);
		// mOutEditText.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// insertDb("1,2,3");
		// double[] h=new double[] { 129, 110, 113, 112,130, 121, 123, 119, 120,
		// 123, 122,
		// 119, 117,116, 129, 117, 126, 129, 122,116, 119, 112, 117,
		// 125,117,128,119,129,112 ,117};
		// double[] l=new double[] { 73, 77, 76, 77, 85,86, 78,79,88, 73, 84,
		// 83, 79, 70, 84,73,82,75,71, 75,
		// 86, 71, 89, 72, 63,75 ,73,81,84,90};
		// for (int i = 0; i < l.length; i++) {
		// insertDb(h[i]+","+l[i]+","+"3");
		// }
		//
		// Cursor c=queryDb();
		// Log.e(cursor,"cursor getColumnCount"+c.getColumnCount());
		// while (c.moveToNext()) {
		// //
		// Log.e(cursor,String.valueOf(c.getInt(c.getColumnIndex(body.lowb))));
		// Log.e(cursor,String.valueOf(c.getInt(c.getColumnIndex(body.lowb))));
		//
		// }
		// }
		// });

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		// mSendButton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// // Send a message using content of the edit text widget
		// TextView view = (TextView) findViewById(R.id.edit_text_out);
		// String message = view.getText().toString();
		// sendMessage(message);
		// }
		// });

		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar calendarEnd = Calendar.getInstance();
				Calendar calendarStart = Calendar.getInstance();
				// System.out.println(calendarStart.get(Calendar.DAY_OF_WEEK));
				Log.e("calendarStart.get(Calendar.DAY_OF_WEEK)",
						String.valueOf(calendarStart.get(Calendar.DAY_OF_WEEK)));
				calendarStart.add(Calendar.DAY_OF_MONTH,
						-calendarStart.get(Calendar.DAY_OF_WEEK) + 1);
				int startMonth = calendarStart.get(Calendar.MONTH) + 1;
				String start = "" + calendarStart.get(Calendar.YEAR) + "0"
						+ startMonth + calendarStart.get(Calendar.DATE);
				String[] ad = new String[] { start };
				checkHistory(ad);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	private void checkHistory(String[] adtition) {
		Cursor c = queryDb(adtition);
		ArrayList<Integer> high = new ArrayList<Integer>();
		ArrayList<Integer> low = new ArrayList<Integer>();
		ArrayList<Integer> pulse = new ArrayList<Integer>();
		Log.e("c", String.valueOf(c.getCount()));
		while (c.moveToNext()) {
			high.add(c.getInt(c.getColumnIndex(body.highb)));
			low.add(c.getInt(c.getColumnIndex(body.lowb)));
			pulse.add(c.getInt(c.getColumnIndex(body.pulse)));
			Log.e("body.pulse)=", body.pulse);
		}
		c.close();
		ArrayList<ArrayList<Integer>> hl = new ArrayList<ArrayList<Integer>>();
		hl.add(high);
		hl.add(low);
		hl.add(pulse);
		// achar.putExtra("data", hl);

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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
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

			// putExtra(锟斤拷锟斤拷值) 锟斤拷一锟斤拷锟斤拷值锟皆达拷诺锟絠ntent锟斤拷锟斤拷锟叫ｏ拷锟斤拷锟斤拷指锟斤拷锟缴硷拷状态锟斤拷锟斤拷时锟斤拷
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
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
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

	// The action listener for the EditText widget, to listen for the return key
	// private TextView.OnEditorActionListener mWriteListener =
	// new TextView.OnEditorActionListener() {
	// public boolean onEditorAction(TextView view, int actionId, KeyEvent
	// event) {
	// // If the action is a key-up event on the return key, send the message
	// if (actionId == EditorInfo.IME_NULL && event.getAction() ==
	// KeyEvent.ACTION_UP) {
	// String message = view.getText().toString();
	// sendMessage(message);
	// }
	// if(D) Log.i(TAG, "END onEditorAction");
	// return true;
	// }
	// };

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
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
				String readMessage = new String(readBuf, 0, msg.arg1);
				// String[] readMessage = (String[]) msg.obj;
				// String readMessage = new String(readBuf, 0, msg.arg1);
				insertDb(readMessage);
				Log.e("get data", readMessage);
				adapter = formatIndex(readMessage);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// formatIndex(readMessage));
				// mConversationView.setAdapter(adapter);

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
		sqLiteOpenHelper = new ExtendSQLiteOpenHelper(BluetoothChat.this, name);
		// SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
	}

	private SimpleAdapter formatIndex(String threeindexsString) {
		StringTokenizer st = new StringTokenizer(threeindexsString, ",");
		Log.e("锟斤拷锟斤拷",
				threeindexsString + "---" + String.valueOf(st.countTokens()));
		String[] threeIndex = new String[3];
		int i = 0;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			// Log.e("st", st.nextToken());
			threeIndex[i] = st.nextToken();
			i++;
		}
		hashMap.put("highValue", threeIndex[0]);
		Log.e("threeIndex[0]", String.valueOf(threeIndex[0]));
		hashMap.put("lowValue", threeIndex[1]);
		Log.e("threeIndex[1]", String.valueOf(threeIndex[1]));
		hashMap.put("pulseValue", threeIndex[2]);
		Log.e("threeIndex[2]", String.valueOf(threeIndex[2]));
		values.add(hashMap);
		Log.e("values.size()", String.valueOf(values.size()));
		adapter = new SimpleAdapter(this, values, R.layout.listitem,
				new String[] { "highValue", "lowValue", "pulseValue" },
				new int[] { R.id.highValue, R.id.lowValue, R.id.pulseValue });
		Log.e("adapter.getCount()", String.valueOf(adapter.getCount()));
		// return
		// "锟斤拷压:"+threeIndex[0]+"  锟斤拷压:"+threeIndex[1]+"  锟斤拷锟斤拷"+threeIndex[2];
		highT.setText(threeIndex[0]);
		lowT.setText(threeIndex[1]);
		pulseT.setText(threeIndex[2]);
		uploadFat(username, threeIndex[0], threeIndex[1], threeIndex[2]);
		return adapter;
	}

	private void uploadFat(String user, String highValue, String lowValue,
			String pulseValue) {
		// TODO Auto-generated method stub
		if (!user.equals("")) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("user", user);
			params.put("highValue", highValue);
			params.put("lowValue", lowValue);
			params.put("pulseValue", pulseValue);

			new EditAsyncTask().execute(params);
		} else {
			Toast.makeText(BluetoothChat.this, "锟矫伙拷未锟斤拷录锟斤拷", 1).show();
		}
	}

	class EditAsyncTask extends AsyncTask<Map<String, String>, Void, String> {
		public void onPreExecute() {
			pdialog = new ProgressDialog(BluetoothChat.this);
			pdialog.setMessage("锟斤拷锟斤拷锟较达拷");
			pdialog.setCanceledOnTouchOutside(false);
			pdialog.show();
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(Map<String, String>... params) {
			// TODO Auto-generated method stub
			Map<String, String> map = params[0];
			Map<String, Object> params2 = new HashMap<String, Object>();
			params2.put("user", map.get("user"));
			params2.put("highValue", map.get("highValue"));
			params2.put("lowValue", map.get("lowValue"));
			params2.put("pulseValue", map.get("pulseValue"));

			String result = null;
			if (!username.equals("")) {
				result = HttpUtils.sendPostMethod(Constants.sendxueyaInfo,
						params2, "utf-8");
			}
			return result;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("200")) {
				Toast.makeText(getApplicationContext(), "锟较达拷锟缴癸拷",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "锟较达拷失锟斤拷",
						Toast.LENGTH_LONG).show();
			}
			pdialog.dismiss();
		}

	}

	private void insertDb(String threeindexsString) {
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();
		Date date = new Date();
		System.out.println(date.toString());
		Calendar calendarEnd = Calendar.getInstance();
		Calendar calendarStart = Calendar.getInstance();
		// System.out.println(calendarStart.get(Calendar.DAY_OF_WEEK));
		calendarStart.add(Calendar.DAY_OF_MONTH,
				-calendarEnd.get(Calendar.DAY_OF_WEEK));

		StringTokenizer st = new StringTokenizer(threeindexsString, ",");
		StringTokenizer stTime = new StringTokenizer(date.toString());
		Log.e("锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷",
				threeindexsString + "---" + String.valueOf(st.countTokens()));
		Log.e("锟斤拷锟斤拷时锟斤拷", stTime + "---" + String.valueOf(stTime.countTokens()));
		String[] threeIndex = new String[3];
		String[] time = new String[6];
		ContentValues values = new ContentValues();
		int i = 0;
		while (st.hasMoreTokens()) {
			// Log.e("st", st.nextToken());
			threeIndex[i] = st.nextToken();
			i++;
		}
		i = 0;
		while (stTime.hasMoreTokens()) {
			time[i] = stTime.nextToken();
			i++;
		}

		values.put(body.highb, threeIndex[0]);
		values.put(body.lowb, threeIndex[1]);
		values.put(body.pulse, threeIndex[2]);
		int dataToday = calendarEnd.get(Calendar.MONTH) + 1;
		Log.e("dataToday", String.valueOf(dataToday));
		Log.e("date.getYear()", String.valueOf(date.getYear()));
		Log.e("date.getMonth()", String.valueOf(date.getMonth()));
		Log.e("date.getDay()", String.valueOf(date.getDay()));
		if (dataToday < 10) {
			values.put(
					body.date,
					Integer.parseInt("" + time[5] + "0" + dataToday
							+ date.getDate()));
			Log.e("+time[5]+0+dataToday+date.getDate())", "" + time[5] + "0"
					+ dataToday + date.getDate());
		} else {
			values.put(
					body.date,
					Integer.parseInt("" + time[5] + dataToday + "0"
							+ date.getDate()));
			Log.e("time[5]+dataToday+date.getDate())", "" + time[5] + "0"
					+ dataToday + date.getDate());
		}

		values.put(body.week, date.getDay());
		values.put(body.time, time[3]);

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
}