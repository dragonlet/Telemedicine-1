package com.android.network;

import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class RegisterAsyncTask extends
		AsyncTask<Map<String, String>, Void, String> {

	private ProgressDialog mProgressDialog;
	private Context mContext;
	private String mNetAddress;

	public RegisterAsyncTask(Context context, String netAddress) {
		super();
		mContext = context;
		mNetAddress = netAddress;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		if (result.equals("200")) {
			Toast.makeText(mContext, "上传成功", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "上传失败", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage("正在上传...");
		mProgressDialog.show();
	}

	@Override
	protected String doInBackground(Map<String, String>... param) {
		return UpLoadWordUtil.sendWord(param[0], mNetAddress);
	}

}
