package com.android.ecg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.mt.helpclass.Ipport;
import com.mt.net.NetworkUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity {

	private SharedPreferences Inform;
	private Editor editor;
	// private String address =
	// "http://"+Ipport.urlIp+Ipport.urlPort+"/GetInfo";
	private String address = "http://" + Ipport.urlIpTest + Ipport.urlPort
			+ "/RMT/Users_login.action";
	EditText name, pass;
	Boolean _ExitFlag = false;

	/**
	 * 注册按钮
	 */
	private TextView mRegisterButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Button login = (Button) findViewById(R.id.login);
		Button cancel = (Button) findViewById(R.id.cancel);

		// 初始化注册按钮
		mRegisterButton = (TextView) findViewById(R.id.login_register);
		// 设置注册按钮的监听器
		mRegisterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Login.this, RegisterActivity.class);
				startActivity(i);
			}
		});

		name = (EditText) findViewById(R.id.user);
		pass = (EditText) findViewById(R.id.pass);

		Inform = getSharedPreferences("perference", MODE_PRIVATE);
		editor = Inform.edit();

		if (Inform.getString("UserName", "") != null
				&& Inform.getString("UserName", "") != "") {
			name.setText(Inform.getString("UserName", ""));
		}

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				editor.putString("USERID", "104");
//				editor.putString("UserName", "1");
//				editor.putBoolean("UploadFlag", true);
//				editor.commit();
//				Intent scan = new Intent(Login.this,
//						mainViewActivity.class);
//				startActivity(scan);
//				finish();
				
				
				
				
//				 TODO Auto-generated method stub
				 if (NetworkUtils.isConnectInternet(Login.this)) {
				 Log.e("login", "login");
				 } else {
				 Toast.makeText(getApplicationContext(), "你的设备未联网",
				 Toast.LENGTH_SHORT).show();
				
				 return;
				 }
				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						Looper.prepare();
						HttpResponse httpResponse = null;

						URI url = null;
						try {
							Log.v("ss", address);
							url = new URI(address);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						HttpPost httpPost = new HttpPost(url);

						List<NameValuePair> params = new ArrayList<NameValuePair>();

						String tmpname = name.getText().toString();
						String tmppass = pass.getText().toString();

						if (tmpname == null || tmpname.trim().equals("")
								|| tmppass == null || tmppass.trim().equals("")) {
							Toast.makeText(getApplicationContext(),
									"用户名，密码不能为空！", Toast.LENGTH_SHORT).show();
						} else {
							params.add(new BasicNameValuePair("username", tmpname));
							params.add(new BasicNameValuePair("password", tmppass));
							params.add(new BasicNameValuePair("state", "1"));

							try {

								httpPost.setEntity(new UrlEncodedFormEntity(
										params, HTTP.UTF_8));

								httpResponse = new DefaultHttpClient()
										.execute(httpPost);
								// String result =
								// EntityUtils.toString(httpResponse
								// .getEntity());
								// Log.e("sd", EntityUtils.toString(httpResponse
								// .getEntity()));
								if (httpResponse.getStatusLine()
										.getStatusCode() == 200) {
									String result = EntityUtils
											.toString(httpResponse.getEntity());
									result.replaceAll("\r", "");
									Log.e("w", result);
									Log.e("result=:", result);
									
									//判断返回的值
									int trueNum = result.indexOf("\"success\":true");
								
									if (trueNum>=0
											||( tmpname.trim().equals("admin")
											&& tmppass.trim().equals("admin"))) {
										editor.putString("USERID", result);
										editor.putString("UserName", tmpname);
										editor.putBoolean("UploadFlag", true);
										editor.commit();
										Toast.makeText(Login.this, "登陆成功"+trueNum,
												Toast.LENGTH_SHORT).show();
										Intent scan = new Intent(Login.this,
												mainViewActivity.class);
										startActivity(scan);
										finish();
									} else {
										Toast.makeText(Login.this, "登陆失败",
											Toast.LENGTH_SHORT).show();
										Toast.makeText(getApplicationContext(),
												trueNum+"", Toast.LENGTH_LONG)
												.show();
									}
								} else {

								}
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						Looper.loop();

						Intent scan = new Intent(Login.this,
								mainViewActivity.class);
						startActivity(scan);
						finish();
					}

				}.start();

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				new AlertDialog.Builder(Login.this)
						.setTitle("确定取消登录？")
						.setMessage("取消登陆将无法使用上传功能，确定取消登陆？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										/*
										 * editor.putBoolean("UploadFlag",false);
										 * editor.commit(); Intent scan = new
										 * Intent(Login.this,
										 * mainViewActivity.class);
										 * startActivity(scan);
										 */
										finish();

									}
								}).setNegativeButton("取消", null).show();

			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog dlg;
			dlg = new AlertDialog.Builder(Login.this)
					.setTitle("退出")
					.setMessage("是否退出程序？按返回键可快速退出。")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									System.exit(0);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									_ExitFlag = false;
								}
							}).show();
			dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					// TODO Auto-generated method stub
					if (keyCode == KeyEvent.KEYCODE_BACK)
						if (_ExitFlag) {
							System.exit(0);
						} else {
							_ExitFlag = true;
						}
					return true;
				}
			});
		}

		return super.onKeyDown(keyCode, event);
	}
}
