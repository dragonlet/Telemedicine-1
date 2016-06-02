package com.mt.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 上传文字
 * @author Administrator
 *
 */
public class HttpUtils {

	public HttpUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param path
	 * @param params
	 *            模拟http表单数据
	 * @param encode
	 * @return
	 */
	public static String sendPostMethod(String path,
			Map<String, Object> params, String encoding) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(path);

		String result = "";
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		try {
			if (params != null && !params.isEmpty()) {
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					String name = entry.getKey();
					String value = entry.getValue().toString();
					BasicNameValuePair valuePair = new BasicNameValuePair(name,
							value);
					parameters.add(valuePair);
				}
			}
			// 纯文本表单，不包含文�?
			UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(
					parameters, encoding);
			httpPost.setEntity(encodedFormEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResponse.getEntity(),
						encoding);
				result="200";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}
}

