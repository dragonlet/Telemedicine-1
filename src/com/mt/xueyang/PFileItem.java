package com.mt.xueyang;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class PFileItem{

	/**
	 * ���л��汾��ʶ������
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * �ļ���ԭʼ�ļ���
	 */
	public static String fileName = "";

	/**
	 * �ļ������洢����������
	 */
	public final byte[] fileData;

	/**
	 * �ļ������洢�����ݳ���
	 */
	public final int fileLength;

	/**
	 * �����ļ������һ����ʵ��
	 *
	 * @param data �ļ������洢����������
	 * @param length �ļ������洢�����ݳ���
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
