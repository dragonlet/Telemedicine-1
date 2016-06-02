package com.android.ecg;

/**
 * 文件传输事件侦听器接口
 *
 * @author sjh
 */
public interface OnFileTransferListener {
	/**
	 * 文件传输发生异常事件
	 *
	 * @param exception
	 *            异常信息
	 */
	void onException(Exception exception);

	/**
	 * 文件传输开始事件
	 *
	 * @param fileCount
	 *            待传输的文件总数目
	 */
	void onStart(int fileCount);

	/**
	 * 文件传输进度事件
	 *
	 * @param current
	 *            当前已传输数据包数目
	 * @param total
	 *            待传输的总数据包数目
	 */
	void onProgress(int current, int total);

	/**
	 * 单个文件已接收事件
	 *
	 * @param file
	 *            已接收到的文件
	 */
	void onItemReceived(FileItem file);

	/**
	 * 文件传输完成事件
	 *
	 * @param fileCount
	 *            已接收的文件数目
	 */
	void onComplete(int fileCount);
}
