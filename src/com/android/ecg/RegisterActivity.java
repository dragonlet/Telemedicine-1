package com.android.ecg;

import java.util.HashMap;
import java.util.Map;

import tool.SMSUtil;
import tool.StringUtil;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.android.network.UpLoadWordAsyncTask;
import com.mt.helpclass.Ipport;

/**
 * 注册页面
 * 
 * @author LiuShuai
 * 
 */
public class RegisterActivity extends Activity implements OnClickListener {
	
	private Context mContext;

	/**
	 * 注册输入框（登录名，真实姓名，性别 ，年龄， 家庭住址，病史 ，身份证号，设备id，手机号，密码 ，确认密码，注册。）
	 */
	private EditText mLoginIdEditText, mRealNameEditText, mSexEditText,
			mAgeEditText, mAddressEditText, mSickHistoryEditText,
			mIDCardEditText, mHardIdEditText, mPasswEditText,
			mAffirmPasswEditText, mPhoneNumEditText, mTestCodeEditText;

	/**
	 * 获得验证码按钮，注册按钮
	 */
	private Button mGetTestCodeButton, mRegisterButton;

	private Handler mSMSHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				int event = msg.arg1;
				int result = msg.arg2;
				Object data = msg.obj;
				if (result == SMSSDK.RESULT_COMPLETE) {
					if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
						//提交驗證碼成功						
						Toast.makeText(mContext, "验证码校验成功", Toast.LENGTH_SHORT)
								.show();
						
					} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
						//获取验证码成功						
						Toast.makeText(mContext, "获取验证码成功，请注意查收",
								Toast.LENGTH_SHORT).show();
					} else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
						//返回支持发送验证码的国家
					}
				} else {
					((Throwable) data).printStackTrace();
					// int resId = SMSSDK.getStringRes();
					Toast.makeText(mContext, "获取验证码失败", Toast.LENGTH_SHORT)
							.show();
					if (event == SMSSDK.RESULT_ERROR) {
						Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT)
								.show();
					}
				}
				break;
			case 2:

				break;
			case 3:

				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext = this;
		initView();
		initMobSDK();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		SMSSDK.unregisterAllEventHandler();
		mContext = null;
	}

	/**
	 * 初始化视图
	 */
	private void initView() {
		mLoginIdEditText = (EditText) findViewById(R.id.activity_register_login_id);
		mRealNameEditText = (EditText) findViewById(R.id.activity_register_rel_name);
		mSexEditText = (EditText) findViewById(R.id.activity_register_sex);
		mAgeEditText = (EditText) findViewById(R.id.activity_register_age);
		mAddressEditText = (EditText) findViewById(R.id.activity_register_address);
		mSickHistoryEditText = (EditText) findViewById(R.id.activity_register_sick_history);
		mIDCardEditText = (EditText) findViewById(R.id.activity_register_idcard);
		mHardIdEditText = (EditText) findViewById(R.id.activity_register_hard_id);
		mPasswEditText = (EditText) findViewById(R.id.activity_register_passwords);
		mAffirmPasswEditText = (EditText) findViewById(R.id.activity_register_affirm_passwords);
		mPhoneNumEditText = (EditText) findViewById(R.id.activity_register_opone_number);
		mTestCodeEditText = (EditText) findViewById(R.id.activity_register_text_code);

		mGetTestCodeButton = (Button) findViewById(R.id.activity_register_btn_send_testCode);
		mGetTestCodeButton.setOnClickListener(this);
		mRegisterButton = (Button) findViewById(R.id.activity_register_btn_register);
		mRegisterButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_register_btn_register:
			if (!StringUtil.isEmpty(mLoginIdEditText.getText().toString()
					.trim(), mRealNameEditText.getText().toString().trim(),
					mSexEditText.getText().toString().trim(), mAgeEditText
							.getText().toString().trim(), mAddressEditText
							.getText().toString().trim(), mSickHistoryEditText
							.getText().toString().trim(), mIDCardEditText
							.getText().toString().trim(), mHardIdEditText
							.getText().toString().trim(), mPasswEditText
							.getText().toString().trim(), mAffirmPasswEditText
							.getText().toString().trim(), mPhoneNumEditText
							.getText().toString().trim(), mTestCodeEditText
							.getText().toString().trim())) {

				if (mPasswEditText
						.getText()
						.toString()
						.trim()
						.equals(mAffirmPasswEditText.getText().toString()
								.trim())) {
//					if (!TextUtils.isEmpty(mTestCodeEditText.getText().toString())) {
//						SMSSDK.submitVerificationCode("86", mPhoneNumEditText.getText()
//								.toString(), mTestCodeEditText.getText().toString());
//						Toast.makeText(this, "正在验证...", Toast.LENGTH_SHORT).show();
//
//					} else {
//						Toast.makeText(this, "请填写验证码", Toast.LENGTH_SHORT).show();
//						
//					}
//					
					Map<String, String> mMap = new HashMap<String, String>();
					// mMap.put("user", "张三");
					mMap.put("username", mLoginIdEditText.getText().toString()
							.trim());
					mMap.put("truename", mRealNameEditText.getText().toString()
							.trim());
					mMap.put("sex", mSexEditText.getText().toString().trim());
					mMap.put("age", mAgeEditText.getText().toString().trim());
					mMap.put("address", mAddressEditText.getText().toString()
							.trim());
					mMap.put("symptom", mSickHistoryEditText.getText()
							.toString().trim());
					mMap.put("idcard", mIDCardEditText.getText().toString()
							.trim());
					mMap.put("equipmentId", mHardIdEditText.getText()
							.toString().trim());
					mMap.put("password", mPasswEditText.getText().toString()
							.trim());
					mMap.put("tel", mPhoneNumEditText.getText().toString()
							.trim());
					mMap.put("state", "1");
					// mMap.put("test_code", mTestCodeEditText.getText()
					// .toString().trim());
					new UpLoadWordAsyncTask(mContext, "http://" + Ipport.urlIpTest
							+ Ipport.urlPort + "/RMT/Users_addUsers.action")
							.execute(mMap);
				} else {
					Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(this, "请填写完整数据", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.activity_register_btn_send_testCode:
			if (!TextUtils.isEmpty(mPhoneNumEditText.getText().toString())) {
				SMSSDK.getVerificationCode("86", mPhoneNumEditText.getText()
						.toString());
				Toast.makeText(this, "正在获取...", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(this, "请填写手机号码", Toast.LENGTH_SHORT).show();
			}
			break;
			
		default:
			break;
		}

	}
	
	/**
	 * SMS短信验证初始化
	 */
	private void initMobSDK() {
		SMSSDK.initSDK(this, SMSUtil.MOBAPPKEY, SMSUtil.MOBAPPSECRET);
		EventHandler eventHandler = new EventHandler() {

			@Override
			public void afterEvent(int event, int result, Object data) {
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				msg.what = 1;
				mSMSHandler.sendMessage(msg);
			}

		};
		SMSSDK.registerEventHandler(eventHandler);
	}

}
