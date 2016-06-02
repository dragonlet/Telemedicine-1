package com.mt.fat;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ecg.R;
import com.android.network.UpLoadWordAsyncTask;
import com.mt.helpclass.Ipport;
import com.mt.tools.Constants;
import com.mt.tools.HttpUtils;
import com.mt.xueyang.DeviceListActivity;

/**
 * 
 * 脂锟斤拷锟斤拷
 */

public class FatActivity extends Activity implements OnClickListener {
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
	// 锟矫伙拷锟斤拷脂锟斤拷锟斤拷锟斤拷锟斤拷bmi锟斤拷锟斤拷锟斤拷锟斤拷谢值锟斤拷锟斤拷锟斤拷指锟斤拷锟斤拷锟斤拷锟斤拷指锟斤拷
	private TextView user, zfhl, bmi, jcdxz, tzzs, txzs;

	private Button conn_device;
	private Button readData;

	/**
	 * 涓婁紶鏁版嵁鎸夐挳
	 */
	private Button mSendWordButton;

	public String userid;
	public String username;

	private String mConnectedDeviceName = null;

	private StringBuffer mOutStringBuffer;
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private FatBluetoothService mChatService = null;
	public SharedPreferences Inform = null;
	SharedPreferences info;
	private TextView mTitle;
	private ProgressDialog pdialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_fat);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// 锟斤拷始锟斤拷锟斤拷图
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

		mSendWordButton = (Button) findViewById(R.id.activity_fat_send_word);
		mSendWordButton.setOnClickListener(this);
	}

	public void initView() {

		user = (TextView) findViewById(R.id.user);
		zfhl = (TextView) findViewById(R.id.zfhl);
		bmi = (TextView) findViewById(R.id.bmi);
		jcdxz = (TextView) findViewById(R.id.jcdxz);
		tzzs = (TextView) findViewById(R.id.tzzs);
		txzs = (TextView) findViewById(R.id.txzs);
		conn_device = (Button) findViewById(R.id.conn_device);
		readData = (Button) findViewById(R.id.readData);

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

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		mChatService = new FatBluetoothService(this, mHandler);

		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if (mChatService != null) {

			if (mChatService.getState() == FatBluetoothService.STATE_NONE) {

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

			// 锟斤拷锟斤拷Intent锟斤拷锟襟，诧拷锟斤拷锟斤拷action值锟斤拷锟斤拷为BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

			// putExtra(锟斤拷锟斤拷值) 锟斤拷一锟斤拷锟斤拷值锟皆达拷诺锟絠ntent锟斤拷锟斤拷锟叫ｏ拷锟斤拷锟斤拷指锟斤拷锟缴硷拷状态锟斤拷锟斤拷时锟斤拷
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
				case FatBluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// // mConversationArrayAdapter.clear();
					break;
				case FatBluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case FatBluetoothService.STATE_LISTEN:
				case FatBluetoothService.STATE_NONE:
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
				user.setText(str[0]);
                switch(Integer.parseInt(str[17])){
                case 1:
                	tzzs.setText("偏低");
                	break;
                case 2:
                	tzzs.setText("标准");
                	break;
                case 3:
                	tzzs.setText("偏高");
                	break;
                case 4:
                	tzzs.setText("高");
                	break;
                default:
                	break;
                }
                switch(Integer.parseInt(str[18])){
                case 1:
                	txzs.setText("消瘦");
                	break;
                case 2:
                	txzs.setText("标准");
                	break;
                case 3:
                	txzs.setText("隐藏性肥胖");
                	break;
                case 4:
                	txzs.setText("肌肉性肥胖/健壮");
                	break;
                case 5:
                	txzs.setText("肥胖");
                	break;
               default:
                	break;
                }
                System.out.println();
				String zfhlStr = (Double.parseDouble(str[11]) + Double
						.parseDouble(str[12]) * 256) / 10 + "%";
				String bmiStr = (Double.parseDouble(str[2]) + Double
						.parseDouble(str[3]) * 256) / 10 + "";
				String jcdxzStr = Double.parseDouble(str[15])
						+ Double.parseDouble(str[16]) * 256 + "Kcal";
				zfhl.setText(zfhlStr);
				bmi.setText(bmiStr);
				jcdxz.setText(jcdxzStr);
				handComm(username, zfhlStr, bmiStr, jcdxzStr,
						Integer.parseInt(str[17]) + "",
						Integer.parseInt(str[18]) + "");
				/*
				 * DFileItem file = new DFileItem( sendBytes2,
				 * sendBytes2.size(), SameFlag); DFileItem[] uploadfile = new
				 * DFileItem[] { file }; DUploadUntil.Post("1", uploadfile,
				 * "http://" + Ipport.urlIp + Ipport.urlPort + "/GetDFile");
				 */
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
			Intent serverIntent = new Intent(FatActivity.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		case R.id.activity_fat_send_word:
//			user, zfhl, bmi, jcdxz, tzzs, txzs
			Map<String, String> params = new HashMap<String, String>();
			if (user.getText().toString().trim()!=null&&zfhl.getText().toString().trim()!=null
			&&bmi.getText().toString().trim()!=null&&jcdxz.getText().toString().trim()!=null&&
			tzzs.getText().toString().trim()!=null&&txzs.getText().toString().trim()!=null&&user.getText().toString().trim()!=""&&zfhl.getText().toString().trim()!=""
			&&bmi.getText().toString().trim()!=""&&jcdxz.getText().toString().trim()!=""&&
			tzzs.getText().toString().trim()!=""&&txzs.getText().toString().trim()!="") {
				

//				params.put("user", user.getText().toString().trim());//用户名
				params.put("user","张三");//用户名
				params.put("fat", zfhl.getText().toString().trim());//脂肪含量
				params.put("bmi", bmi.getText().toString().trim());//BMI
				params.put("jcdxz", jcdxz.getText().toString().trim());//基础代谢值
				params.put("tzzs", tzzs.getText().toString().trim());//体质指数
				params.put("txzs", txzs.getText().toString().trim());//体型
				
				new UpLoadWordAsyncTask(this, "http://" + Ipport.urlIpTest
						+ Ipport.urlPort + "/RMT/Zf_phoneAdd.action").execute(params);
			}else{
				Toast.makeText(this, "未获得完整数据", Toast.LENGTH_SHORT).show();
			}
			
			break;
		// case R.id.readData:
		// Intent intent = new Intent(Etcomm.this,pushview.class);
		// startActivity(intent);
		// break;
		}

	}

	// 锟街节碉拷锟斤拷锟斤拷转锟斤拷
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

	public void handComm(String user, String fat, String bmi, String jcdxz,
			String tzzs, String txzs) {if (!user.equals("")) {

				// 表单数据
				Map<String, String> params = new HashMap<String, String>();

				params.put("user", user);//用户名
				params.put("fat", fat);//脂肪含量
				params.put("bmi", bmi);//BMI
				params.put("jcdxz", jcdxz);//基础代谢值
				params.put("tzzs", tzzs);//体质指数
				params.put("txzs", txzs);//体型
//				new EditAsyncTask().execute(params);

			} else {
				Toast.makeText(FatActivity.this, "用户未登录！", 1).show();
			}}

	
			class EditAsyncTask extends AsyncTask<Map<String, String>, Void, String> {

				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					pdialog = new ProgressDialog(FatActivity.this);
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
					params2.put("fat", map.get("fat"));
					params2.put("bmi", map.get("bmi"));
					params2.put("jcdxz", map.get("jcdxz"));
					params2.put("tzzs", map.get("tzzs"));
					params2.put("txzs", map.get("tzzs"));

					String result = null;
					if (!username.equals("")) {
						result = HttpUtils.sendPostMethod(
								"http://" + Ipport.urlIpTest
								+ Ipport.urlPort + "/RMT/Zf_phoneAdd.action", params2, "utf-8");
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
						Toast.makeText(FatActivity.this, "上传成功!",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(FatActivity.this, "上传失败!",
								Toast.LENGTH_LONG).show();
					}
					pdialog.dismiss();
				}
			}

}