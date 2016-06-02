package com.view.expand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.http.util.EncodingUtils;

import android.util.Log;

public class SaveTData{
	private String Tag="SaveTDate";
	private final String SD_PATH=android.os.
									Environment.
									getExternalStorageDirectory().
									getAbsolutePath();
						//返回扩展存储区(SDCard)目录
	public final String File_Path=SD_PATH+"/SaveTData";
	//为防止和别人的文件混淆，命名我们自己的文件夹。
	public File file_dir=null; 
	//private final String file_txt_name=null;
	private String fileName;
	
	private Date date=null;
	private String dateString=null;
	private StringBuffer outSring=null;
	private FileOutputStream fileops;
	public FileOutputStream fout;
	public SaveTData(String fileName){
		this.fileName = fileName;
		file_dir=new File(File_Path+"//"+fileName);
		if(!file_dir.exists()){
			try {
				file_dir.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//咱的文件夹不存在，创建一个
		 try {
			fout = new FileOutputStream(File_Path+"/"+fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//File file_txt=new File(File_Path+"/"+file_txt_name);
//		if(file_txt.exists()){
//			file_txt.delete();
//		}
		
	}
	public void makdir(){
		
		
	}
	
	
	public void save (String getS) {

			try {
			   
				
				fout.write(getS.getBytes());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(Tag,"写入异常");
				e.printStackTrace();
			}

	}
	public void close(){
		try {
			if(null!=fout)
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void format(String formatS){
		int length=formatS.length();
		outSring=new StringBuffer();
		for(int i=0;i<length;i++){
			if(i%4==0){
				outSring.append("\r\n");
			}
			outSring.append(formatS.charAt(i));
		}
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
