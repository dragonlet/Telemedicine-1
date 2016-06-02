package com.android.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class UpLoadWordUtil {

	public static String sendWord(Map<String, String> mValues, String url) {
		String result = "";

		try {
			HttpClient mHttpClient = new DefaultHttpClient();
			HttpPost mHttpPost = new HttpPost(url);

			List<BasicNameValuePair> mBasicNameValuePairs = new ArrayList<BasicNameValuePair>();

			if (mValues != null && !mValues.isEmpty()) {

				for (Map.Entry<String, String> temp : mValues.entrySet()) {
					mBasicNameValuePairs.add(new BasicNameValuePair(temp
							.getKey(), temp.getValue()));

				}
			}
			UrlEncodedFormEntity mEntity;

			mEntity = new UrlEncodedFormEntity(mBasicNameValuePairs, "UTF-8");
			mHttpPost.setEntity(mEntity);
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);

			System.out.println("UpLoadWordUtil--------"
					+ mHttpResponse.getStatusLine().getStatusCode());

			if (mHttpResponse.getStatusLine().getStatusCode() == 200) {
				result = "200";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ÉÏ´«Ê§°Ü£¡");
		}

		return result;

	}
}
