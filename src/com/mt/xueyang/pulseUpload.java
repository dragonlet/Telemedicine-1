package com.mt.xueyang;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.android.upload.DFileItem;

public class pulseUpload{
	public static String Post(String userid,PFileItem[] files,String _URL){
		String BOUNDARY = UUID.randomUUID().toString();
		String MULTIPART_FORM_DATA = "multipart/form-data";
		String result=null;

		
		try {
			HttpPost httpPost = new HttpPost(new URI(_URL));
	        HttpResponse httpResponse;
	        List<NameValuePair> params = new ArrayList<NameValuePair>();

	        params.add(new BasicNameValuePair("userid", userid));
	        httpPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			
			httpResponse = new DefaultHttpClient().execute(httpPost);
			httpResponse.getStatusLine().getStatusCode();
			
			
			URL url = new URL(_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(6000);
	        conn.setDoInput(true);//允许输入  
	        conn.setDoOutput(true);//允许输出  
	        conn.setUseCaches(false);//不使用Cache 
	        conn.setRequestMethod("POST");       
	        conn.setRequestProperty("Connection", "Keep-Alive"); 
	        conn.setRequestProperty("Charset", "UTF-8");
	        conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY); 
	        StringBuilder sb = new StringBuilder(); 
	        
	        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
	        
	        
	        for(PFileItem file : files){
	        	StringBuilder split = new StringBuilder();
	            split.append("--");  
	            split.append(BOUNDARY);  
	            split.append("\r\n");  
	            split.append("Content-Disposition: form-data;name=\"ecgfile\";filename=\""+userid+"|"+ file.fileName + "\"\r\n");
	            split.append("Content-Type: application/octet-stream\r\n\r\n");  
	            outStream.write(split.toString().getBytes());  
	            outStream.write(file.fileData, 0, file.fileData.length);
	            outStream.write("\r\n".getBytes());
	        }
	        byte[] end_data = ("--" + BOUNDARY + "--\r\n").getBytes();//数据结束标志
	        outStream.write(end_data);
	        outStream.flush(); 
	        
	        int cah = conn.getResponseCode();
	        
	        if (cah != 200) throw new RuntimeException("请求url失败");
	        
	        InputStream is = conn.getInputStream(); 
	        InputStreamReader isr = new InputStreamReader(is, "utf-8");
	        BufferedReader br = new BufferedReader(isr);
	        result = br.readLine();
	        outStream.close();
	        conn.disconnect();
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return result;
	}

}
