package com.android.ecg;

import java.util.List;

import com.android.ecg.realtime.ECGRealTime;
import com.mt.fat.FatActivity;
import com.mt.xuetang.XueTangActivity;
import com.mt.xueya.BluetoothChat;
import com.mt.xueyang.XueYang;
import com.zone.set.searchActivity;
import com.zone.set.setActivity;

import comandroid.screen.TestVedioActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class mainViewActivity extends Activity implements OnClickListener {

	
	
	LinearLayout blood_Ele,blood_push,blood_ong,blood_zhi,set_btn,finish,sendsms,talk_look,xuetang;
	Intent intent = null;
	View smsLayout;
	ProgressBar pBar;
	TextView smsContent;
	TextView smsTitle;
	TextView countDownTxt;
	AlertDialog smsDialog;
	int count = 0;// 5 secs
	boolean isSendSMS = false;
	SharedPreferences info;
	private Handler mHandler;
	private String phonenum1;

	final String initialSmsTitle = "After 5s,Send a following sms to hospital(you can cancel)";
	final String afterSmsSentTitle = "The Message has been sent to hospital!";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewlayout);
		SharedPreferences sharedPreferences = getSharedPreferences("test",
				Activity.MODE_PRIVATE);
		phonenum1 = sharedPreferences.getString("phone", "");
		initView();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.xindian_mbt:
			intent = new Intent(mainViewActivity.this, ECGRealTime.class);
			startActivity(intent);
			break;
		case R.id.xueya_mbt:
			intent = new Intent(mainViewActivity.this, BluetoothChat.class);
			startActivity(intent);
			break;
		case R.id.xueyang_mbt:
			intent = new Intent(mainViewActivity.this, XueYang.class);
			startActivity(intent);
			break;
		case R.id.xuezhi_mbt:
			intent = new Intent(mainViewActivity.this, FatActivity.class);
			startActivity(intent);
			break;
		case R.id.xuetang:
			intent = new Intent(mainViewActivity.this, XueTangActivity.class);
			startActivity(intent);
			break;
		case R.id.set_mbt:
			intent = new Intent(mainViewActivity.this, setActivity.class);
			startActivity(intent);
			break;
		/*
		 * case R.id.serch_mbt: intent = new
		 * Intent(mainViewActivity.this,searchActivity.class);
		 * startActivity(intent); break;
		 */
		case R.id.talk_look:
			intent = new Intent(mainViewActivity.this, TestVedioActivity.class);
			startActivity(intent);
			break;
		case R.id.sendsms:
			Message message = new Message();

			message.what = 0;
			mHandler.sendMessage(message);
			// sendSMS(temp,"");

			break;
		case R.id.finish:
			System.exit(0);
			finish();
			break;
		default:
			break;

		}

	}

	class MyHandler extends Handler {
		public boolean isAccAbnormal = false;
		public String SMSContent;
		// SettingBluethooth sb;
		AlertDialog alertDialog;
		View smsLayout;
		int count = 0;// 5 secs
		boolean isshow = true;
		ProgressBar pBar;
		TextView smsContent;
		TextView smsTitle;
		TextView countDownTxt;
		boolean isSendSMS = true;
		final String initialSmsTitle = "After 5s,Send a following sms to hospital(you can cancel)";
		final String afterSmsSentTitle = "The Message has been sent to hospital!";

		public MyHandler() {
			smsLayout = View.inflate(mainViewActivity.this,
					R.layout.sms_dialog, null);
			// sb=new SettingBluethooth();
			pBar = (ProgressBar) smsLayout.findViewById(R.id.SmsprogressBar);
			smsContent = (TextView) smsLayout.findViewById(R.id.smsContent);
			smsTitle = (TextView) smsLayout.findViewById(R.id.smsTitle);
			countDownTxt = (TextView) smsLayout.findViewById(R.id.coutDowntxt);
			smsTitle.setText(initialSmsTitle);
			pBar.setMax(5);
			pBar.incrementProgressBy(1);
			alertDialog = new AlertDialog.Builder(mainViewActivity.this)
					.setIcon(R.drawable.sms1)
					.setTitle("Send SMS to Hospital")
					.setView(smsLayout)
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									initialSms();
								}
							}).setCancelable(true).create();

		}

		// private void setAccAbnormal() {
		// isAccAbnormal=true;
		// }
		// private void setAccNormal() {
		// isAccAbnormal=false;
		// }
		private void initialSms() {
			isSendSMS = false;
			alertDialog.cancel();
			count = 0;
			smsTitle.setTextColor(Color.RED);
			smsTitle.setText(initialSmsTitle);
			pBar.setProgress(0);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				// String strs[]=sb.getPatientInfos();
				// String
				// labels[]={"ID: dell ","Telephone:5553333 ","Cellphone:5678322 ",
				// "Familyphone: 3578883","EmergencyEvent: ","Position: "};
				// String content ="";
				// for (int i = 0; i < 4; i++) {
				// content+=labels[i]+"\n";
				// }

				// Mylocation location = gps.getMyLocation();
				// content+=labels[labels.length-2]+"Fall Down\t/VF\n"+labels[labels.length-1]+"("+
				// location.getLatitude()+","+location.getLongitude()+")";
				// Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
				String temp = "蹇冪數" + info.getString("xindian", "") + "\n"
						+ "楂樺帇" + info.getString("height", "") + "\n" + "浣庡帇"
						+ info.getString("low", "") + "\n" + "鑴夋悘"
						+ info.getString("pulse", "");
				SMSContent = temp;
				// ((TextView)smsContentLayout.findViewById(R.id.smsContent)).setText(SMSContent+"\n 5s");
				// count=0;
				smsContent.setText(SMSContent);
				if ((!alertDialog.isShowing())) {
					alertDialog.show();
					isSendSMS = true;
					sendEmptyMessage(2);
				}

				break;

			case 2: // QRS detector

				if (isSendSMS) {
					if (count <= 5) {

						pBar.setProgress(count);

						countDownTxt.setText(Integer.toString(5 - count) + "s");
						count++;

						sendEmptyMessageDelayed(2, 1000);
					} else if (count > 5) {
						count = 0;
						alertDialog.cancel();
						sendSMS(SMSContent, phonenum1);
						if (smsTitle != null) {
							smsTitle.setTextColor(Color.YELLOW);
							smsTitle.setText(afterSmsSentTitle);
						}
						Toast.makeText(mainViewActivity.this,
								"SMS is sent successfully!", Toast.LENGTH_SHORT)
								.show();
					}
					//
				}

				break;
			case 1: // QRS detector
				Toast.makeText(mainViewActivity.this, "HRV Finished",
						Toast.LENGTH_LONG).show();

			}

		}

		private void sendSMS(String content, String phonenum) {

			SmsManager manager = SmsManager.getDefault(); // 鍙栧緱榛樿鐨凷msManager鐢ㄤ簬鐭俊鐨勫彂閫�
			if (manager == null) {
				Toast.makeText(mainViewActivity.this,
						"SMS Module is not Available!", Toast.LENGTH_SHORT);
				return;
			}
			if (content.length() > 70) {
				List<String> all = manager.divideMessage(content); // 鐭俊鐨勫唴瀹规槸鏈夐檺鐨勶紝瑕佹牴鎹煭淇￠暱搴︽埅鍙栥�傞�愭潯鍙戦��
				for (String each : all) {
					manager.sendTextMessage(phonenum, null, each, null, null); // 閫愭潯鍙戦�佺煭鎭�
				}
			} else {
				manager.sendTextMessage(phonenum, null, content, null, null); // 閫愭潯鍙戦�佺煭鎭�

			}

			Toast.makeText(mainViewActivity.this,
					R.string.sms_send_successfully, Toast.LENGTH_LONG).show();

		}

	}

	public void initView() {
		blood_Ele = (LinearLayout) findViewById(R.id.xindian_mbt);
		blood_push = (LinearLayout) findViewById(R.id.xueya_mbt);
		blood_ong = (LinearLayout) findViewById(R.id.xueyang_mbt);
		blood_zhi = (LinearLayout) findViewById(R.id.xuezhi_mbt);
		set_btn = (LinearLayout) findViewById(R.id.set_mbt);
		// serch_btn = (Button)findViewById(R.id.serch_mbt);
		finish = (LinearLayout) findViewById(R.id.finish);
		sendsms = (LinearLayout) findViewById(R.id.sendsms);
		talk_look = (LinearLayout) findViewById(R.id.talk_look);
		xuetang = (LinearLayout) findViewById(R.id.xuetang);

		smsLayout = View.inflate(mainViewActivity.this, R.layout.sms_dialog,
				null);
		// pBar = (ProgressBar) smsLayout.findViewById(R.id.SmsprogressBar);
		smsContent = (TextView) smsLayout.findViewById(R.id.smsContent);
		smsTitle = (TextView) smsLayout.findViewById(R.id.smsTitle);
		countDownTxt = (TextView) smsLayout.findViewById(R.id.coutDowntxt);
		smsTitle.setText(initialSmsTitle);
		// pBar.setMax(5);
		// pBar.incrementProgressBy(1);

		smsLayout = View.inflate(mainViewActivity.this, R.layout.sms_dialog,
				null);
		smsDialog = new AlertDialog.Builder(mainViewActivity.this)
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
		info = getSharedPreferences("INFO", 0);
		mHandler = new MyHandler();
		blood_Ele.setOnClickListener(this);
		blood_push.setOnClickListener(this);
		blood_ong.setOnClickListener(this);
		blood_zhi.setOnClickListener(this);
		set_btn.setOnClickListener(this);
		// serch_btn.setOnClickListener(this);
		finish.setOnClickListener(this);
		talk_look.setOnClickListener(this);
		xuetang.setOnClickListener(this);
		sendsms.setOnClickListener(this);
	}

}
