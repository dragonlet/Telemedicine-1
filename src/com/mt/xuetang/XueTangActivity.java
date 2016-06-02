package com.mt.xuetang;

import java.util.HashMap;
import java.util.Map;

import com.android.ecg.R;
import com.android.ecg.R.layout;
import com.android.ecg.R.menu;
import com.android.network.UpLoadWordAsyncTask;
import com.mt.helpclass.Ipport;
import com.mt.tools.Constants;
import com.mt.tools.HttpUtils;
import com.mt.xueyang.DeviceListActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class XueTangActivity extends Activity implements OnClickListener {
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
	// 用户，脂肪含量，bmi，基础代谢值，体质指数，体重指数
	private TextView xuetangText, dailyText, timeText;

	private Button conn_device;
	private Button readData;

	/**
	 * 上傳按鈕
	 */
	private Button mUploadButton;

	public String userid;
	public String username;

	private String mConnectedDeviceName = null;

	private StringBuffer mOutStringBuffer;
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private XueTangBluetoothService mChatService = null;
	public SharedPreferences Inform = null;
	SharedPreferences info;
	private TextView mTitle;
	private ProgressDialog pdialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_xue_tang);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// 初始化视图
		initView();
		conn_device.setOnClickListener(this);
		readData.setOnClickListener(this);

		// Get local Bluetooth adapter
		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		info = getSharedPreferences("INFO", 0);

		userid = Inform.getString("USERID", "");
		username = Inform.getString("UserName", "");
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
	}

	public void initView() {

		xuetangText = (TextView) findViewById(R.id.xuetang);
		dailyText = (TextView) findViewById(R.id.daily);
		timeText = (TextView) findViewById(R.id.time);
		conn_device = (Button) findViewById(R.id.conn_device);
		readData = (Button) findViewById(R.id.readData);

		mUploadButton = (Button) findViewById(R.id.activity_xue_tang_upload);
		mUploadButton.setOnClickListener(this);

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
			Toast.makeText(this, "权限申请:", Toast.LENGTH_LONG).show();

			// 启动Activity，提示是否开启蓝牙设备
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			Toast.makeText(this, "权限申请...", Toast.LENGTH_LONG).show();
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		mChatService = new XueTangBluetoothService(this, mHandler);

		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if (mChatService != null) {

			if (mChatService.getState() == XueTangBluetoothService.STATE_NONE) {

				mChatService.start();
			}
		}
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

		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

			// 创建Intent对象，并将其action值设置为BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

			// putExtra(键，值) 将一个键值对存放到intent对象中，用于指定可见状态持续时间
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case XueTangBluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// // mConversationArrayAdapter.clear();
					break;
				case XueTangBluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case XueTangBluetoothService.STATE_LISTEN:
				case XueTangBluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;

				String writeMessage = new String(writeBuf);

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;

				String readMessage = new String(readBuf, 0, msg.arg1);
				String[] str = readMessage.split(",");

				int year = Integer.parseInt(str[0]);
				int month = Integer.parseInt(str[1]);
				int day = Integer.parseInt(str[2]);
				int hour = Integer.parseInt(str[3]);
				int minute = Integer.parseInt(str[4]);
				int am_pm = Integer.parseInt(str[7]);
				String daily = "";
				if (year < 10)
					daily = "200" + year + "年";
				else
					daily = "20" + year + "年";
				if (month < 10)
					daily = daily + "0" + month + "月";
				else
					daily = daily + month + "月";
				if (day < 10)
					daily = daily + "0" + day + "日";
				else
					daily = daily + day + "日";
				String time = "";
				time = hour + ":";
				if (minute < 10)
					time = time + "0" + minute + " ";
				else
					time = time + minute + " ";

				if (am_pm == -86) {
					time = time + "上午";
				}
				if (am_pm == -69) {
					time = time + "下午";
				}
				double xuetang = (Double.parseDouble(str[6]) + (Double
						.parseDouble(str[5])) * 256) / 18;
				java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
				xuetangText.setText(df.format(xuetang) + "mmol/L");
				dailyText.setText(daily);
				timeText.setText(time);
				handComm(username, xuetang + "");
				break;
			case MESSAGE_READINT:
				int[] readBufI = (int[]) msg.obj;

				String readMessageI = new String(readBufI, 0, msg.arg1);

				break;

			case MESSAGE_DEVICE_NAME:

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
			if (resultCode == Activity.RESULT_OK) {

				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);

				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);

				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:

			if (resultCode == Activity.RESULT_OK) {

				setupChat();
			} else {

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
			// Intent serverIntent = new Intent(this, DeviceListActivity.class);
			// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.conn_device:
			Intent serverIntent = new Intent(XueTangActivity.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		// case R.id.readData:
		// Intent intent = new Intent(Etcomm.this,pushview.class);
		// startActivity(intent);
		// break;
		case R.id.activity_xue_tang_upload:
			// xuetangText,dailyText,timeText
			if (xuetangText.getText().toString().trim() != ""
					&& xuetangText.getText().toString().trim() != null
					&& dailyText.getText().toString().trim() != ""
					&& dailyText.getText().toString().trim() != null
					&& timeText.getText().toString().trim() != null
					&& timeText.getText().toString().trim() != "") {
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("user", "张三");
				mMap.put("nongdu", xuetangText.getText().toString().trim());
				// mMap.put("lowT", dailyText.getText().toString().trim());
				// mMap.put("pulseT", timeText.getText().toString().trim());
				new UpLoadWordAsyncTask(this, "http://" + Ipport.urlIpTest
						+ Ipport.urlPort + "/RMT/Xt_phoneAdd.action")
						.execute(mMap);
			} else {
				Toast.makeText(this, "未获得完整数据", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

	}

	// 字节到浮点转换
	public static double byteToDouble(byte[] b) {
		long l;

		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffl;

		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[6] << 48);

		l |= ((long) b[7] << 56);
		return Double.longBitsToDouble(l);
	}

	public void handComm(String user, String xuetang) {

		if (!user.equals("")) { // 上传照片评论

			// 表单数据
			Map<String, String> params = new HashMap<String, String>();

			params.put("user", user);// 用户名
			params.put("xuetang", xuetang);// 血糖

			new EditAsyncTask().execute(params);

		} else {
			Toast.makeText(XueTangActivity.this, "用户未登录！", 1).show();
		}
	}

	// 上传图片评论
	class EditAsyncTask extends AsyncTask<Map<String, String>, Void, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			pdialog = new ProgressDialog(XueTangActivity.this);
			pdialog.setMessage("正在上传...");
			pdialog.setCanceledOnTouchOutside(false);
			pdialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Map<String, String>... params) {
			// TODO Auto-generated method stub
			Map<String, String> map = params[0];
			// 表单数据
			Map<String, Object> params2 = new HashMap<String, Object>();

			params2.put("user", map.get("user"));
			params2.put("xuetang", map.get("xuetang"));

			String result = null;
			if (!username.equals("")) {
				result = HttpUtils.sendPostMethod(Constants.sendXueTangInfo,
						params2, "utf-8");
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// Toast.makeText(ContactsHandTalk.this, "发表成功！", 0).show();
			// finish();
			System.out.println(result);

			if (result.equals("200")) {
				Toast.makeText(XueTangActivity.this, "上传成功!", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(XueTangActivity.this, "上传失败!", Toast.LENGTH_LONG)
						.show();
			}
			pdialog.dismiss();
		}
	}

}
