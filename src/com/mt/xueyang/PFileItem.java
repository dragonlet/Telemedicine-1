package com.mt.xueyang;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class PFileItem{

	/**
	 * 序列化版本标识符定义
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 文件项原始文件名
	 */
	public static String fileName = "";

	/**
	 * 文件项所存储的数据内容
	 */
	public final byte[] fileData;

	/**
	 * 文件项所存储的数据长度
	 */
	public final int fileLength;

	/**
	 * 构造文件项类的一个新实例
	 *
	 * @param data 文件项所存储的数据内容
	 * @param length 文件项所存储的数据长度
	 */
	public PFileItem(Vector<Byte> data, int length,int sameFlag) {
		int in = data.size();
		byte bdata[]=new byte[data.size()];
		for(int i=0;i<data.size();i++){
			bdata[i]=data.get(i);
		}
		fileData=bdata;
		fileLength = length;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(sameFlag==1){
			fileName = df.format(new Date());
		}
	}
	public PFileItem(String str,int sameFlag) {
		//int in = data.size();
		
		byte bdata[]=str.getBytes();
		
		fileData=bdata;
		fileLength = bdata.length;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(sameFlag==1){
			fileName = df.format(new Date());
		}
	}

}
