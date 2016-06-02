package com.android.ecg;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.android.ecg.Exception.*;

/**
 * 文件传输器线程类
 *
 * @author sjh
 */
class FileTransfer extends Thread {
	private boolean _isRunning = false;

	/**
	 * 获取线程是否正在运行
     *
     * @return 若返回 true 则表示线程正在运行，否则为 false 则线程已停止 
	 */
	public boolean isRunning() {
		return _isRunning;
	}

	private Handler _handler = null;
    private BluetoothSocket _socket = null;

	/**
	 * 构造文件传输器线程类的一个新实例
	 *
	 * @param handler 事件通知器对象
	 * @param socket 数据通讯对象
	 */
	public FileTransfer(Handler handler, BluetoothSocket socket) {
		_handler = handler;
		_socket = socket;
	}

	public void run() {
		_isRunning = true;

		try {
            List<Byte> tempBuffer = new ArrayList<Byte>();
            List<Byte> dataBuffer = new ArrayList<Byte>();

            int prevPackIndex = 0;
            int prevPackLength = 0;
            int fileCount = 0;

            InputStream inStream = _socket.getInputStream();
            OutputStream outStream = _socket.getOutputStream();

			while (_isRunning) {
                sleep(1);

                byte[] tmp = new byte[1024];
                int length = inStream.read(tmp);

                for (int i=0; i<length; i++) {
                    tempBuffer.add(tmp[i]);
                }

                while (_isRunning && tempBuffer.size() >= 4) {
                    // 检查数据包头是否正确
                    while (tempBuffer.size() > 0 && tempBuffer.get(0) != (byte) 0xaa) {
                        tempBuffer.remove(0);
                    }

                    // 检查缓冲区内的数据是否足够解析
                    if (tempBuffer.size() < 4) break;

                    if (tempBuffer.get(0) == (byte) 0xaa &&
                        tempBuffer.get(1) == 0x01 &&
                        tempBuffer.get(2) == 0x01 &&
                        tempBuffer.get(3) == 0x55) {

                        // 接收到 Hello 包，表示心电仪正在等待连接
                        outStream.write(new byte[] { 0x55, 0x01, 0x01, (byte) 0xaa, 0x0a });

                        for (int i=0; i<4; i++) {
                            tempBuffer.remove(0);
                        }
                    } else if (tempBuffer.get(0) == (byte) 0xaa && tempBuffer.get(1) == 0x02) {
                        // 接收到文件个数包，表示准备传输数据
                        fileCount = ((tempBuffer.get(3) & 0xff) << 8) + (tempBuffer.get(2) & 0xff);
                        prevPackIndex = 0;

                        outStream.write(new byte[] { 0x55, 0x02, 0x0a });

                        for (int i=0; i<4; i++) {
                            tempBuffer.remove(0);
                        }

                        // 开始传输心电文件
                        _handler.obtainMessage(libecg.MESSAGE_START, fileCount, 0).sendToTarget();

                    } else if (tempBuffer.get(0) == (byte) 0xaa && (tempBuffer.get(1) == 0x03 || tempBuffer.get(1) == 0x04)) {
                        // 接收到心电数据
                        if (tempBuffer.size() < 10) break;

                        int fileIndex = ((tempBuffer.get(3) & 0xff) << 8) + (tempBuffer.get(2) & 0xff);
                        int packTotal = ((tempBuffer.get(5) & 0xff) << 8) + (tempBuffer.get(4) & 0xff);
                        int packIndex = ((tempBuffer.get(7) & 0xff) << 8) + (tempBuffer.get(6) & 0xff);
                        int dataLength = ((tempBuffer.get(9) & 0xff) << 8) + (tempBuffer.get(8) & 0xff);

                        // 检查数据包是否重复
                        if (packIndex == prevPackIndex) {
                            for (int i=0; i<10+prevPackLength; i++) {
                                if (tempBuffer.size() > 0) {
                                    tempBuffer.remove(0);
                                }
                            }
                            
                            break;
                        }

                        int packLength = 10 + dataLength;

                        // 检查数据包是否完整
                        if (tempBuffer.size() < packLength) break;

                        outStream.write(new byte[] { 0x55, tempBuffer.get(1), tempBuffer.get(6), tempBuffer.get(7), 0x0a });

                        // 传输进度通知消息
                        _handler.obtainMessage(libecg.MESSAGE_PROGRESS, packIndex, packTotal).sendToTarget();

                        List<Byte> packBuffer = tempBuffer.subList(10, packLength);

                        dataBuffer.addAll(packBuffer);

                        if (packIndex == packTotal) {
                            byte[] pack = new byte[dataBuffer.size()];

                            for (int i=0; i<dataBuffer.size(); i++) {
                                pack[i] = dataBuffer.get(i);
                            }

                            FileItem file = new FileItem(pack, pack.length);

                            // 一条文件已传输
                            _handler.obtainMessage(libecg.MESSAGE_ITEMRECEIVED, file).sendToTarget();

                            if (fileIndex == fileCount) {
                                // 所有文件已传输完成
                                _handler.obtainMessage(libecg.MESSAGE_COMPLETE, fileCount, 0).sendToTarget();

                                fileCount = 0;
                            }
                        }

                        for (int i=0; i<packLength; i++) {
                            tempBuffer.remove(0);
                        }
                        
                        prevPackIndex = packIndex;
                        prevPackLength = dataLength;
                    } else {
                        tempBuffer.remove(0);
                    }
                }
			}
		} catch (Exception e) {
			e.printStackTrace();

            if (_isRunning) {
                // 设备已断开
                _handler.obtainMessage(libecg.MESSAGE_EXCEPTION, new DeviceDisconnectedException()).sendToTarget();
            }
            
            _isRunning = false;
		}
	}

	/**
	 * 停止文件传输线程
	 */
	public void end() {
		if (_isRunning) {
			_isRunning = false;

			try {
                _socket.getInputStream().close();

				join();
            } catch (IOException e) {
                e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
            }
        }
	}
}
