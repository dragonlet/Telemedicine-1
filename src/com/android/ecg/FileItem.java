package com.android.ecg;

import java.io.Serializable;

/**
 * 文件项类，表示每一个接收到的文件项
 *
 * @author sjh
 *
 */
public class FileItem implements Serializable {
	/**
	 * 序列化版本标识符定义
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 文件项原始文件名
	 */
	public final String fileName;

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
	public FileItem(byte[] data, int length) {
		fileData = data;
		fileLength = length;

		fileName = String.format("20%1$02d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
				fileData[0], fileData[1], fileData[2], fileData[3],
				fileData[4], fileData[5]);
	}

	public String toString() {
		return fileName;
	}
}
