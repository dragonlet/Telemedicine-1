package com.view.expand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import android.os.Environment;

public class Savefile {
	final static String FOLDER = "/ecg/";   
	//final static String FILENAME = "sample";    
	final static String SUFFIX = ".txt"; // suffix could be replaced on demand 
	//写数据到SD中的文件  
	public void writeFileSdcardFile(String fileName,String write_str) throws IOException{   
	 try{   
		 String foldername = Environment.getExternalStorageDirectory().getPath()   
                 + FOLDER; 
		    File folder = new File(foldername); 
		    if (folder != null && !folder.exists()) {   
		        if (!folder.mkdir() && !folder.isDirectory())   
		        {   
		         //   Log.d(TAG, "Error: make dir failed!");   
		            return;   
		        }   
		    }   
		    String targetPath = foldername + fileName + SUFFIX;   
		    File targetFile = new File(targetPath);   
		    if (targetFile != null) {   
		        if (targetFile.exists()) {   
		            targetFile.delete();   
		        }   
	       FileOutputStream fout = new FileOutputStream(targetFile);   
	       byte [] bytes = write_str.getBytes();   
	  
	       fout.write(bytes);  
	       fout.flush();
	       fout.close();   
	     }  
	   }
	      catch(Exception e){   
	        e.printStackTrace();   
	       }   
	   }   
	 
	  
	    
	//读SD中的文件  
	public String readFileSdcardFile(String fileName) throws IOException{   
	  String res="";   
	  if (null == fileName) {   
	        //Log.d(TAG, "Error: Invalid file name!");   
	        return null;   
	    }   
	  try{   
	         FileInputStream fin = new FileInputStream(fileName);   
	  
	         int length = fin.available();   
	  
	         byte [] buffer = new byte[length];   
	         fin.read(buffer);       
	  
	         res = EncodingUtils.getString(buffer, "UTF-8");   
	  
	         fin.close();       
	        }   
	  
	        catch(Exception e){   
	         e.printStackTrace();   
	        }   
	        return res;   
	}   
	
}
