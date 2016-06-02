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
						//������չ�洢��(SDCard)Ŀ¼
	public final String File_Path=SD_PATH+"/SaveTData";
	//Ϊ��ֹ�ͱ��˵��ļ����������������Լ����ļ��С�
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
		}//�۵��ļ��в����ڣ�����һ��
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
				Log.e(Tag,"д���쳣");
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
