package com.android.ecg;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

import com.android.ecg.Exception.*;


/**
 * 心电仪封装库类
 * 
 * @author sjh
 */
public class libecg {
	public static BluetoothSocket _socket = null;
	private FileTransfer _transfer = null;
	private boolean _isOpen = false;

    /**
     * 获取设备是否处于打开状态
     *
     * @return 若设备已打开则返回 true，否则返回 false
     */
	public boolean isOpen() {
		return _isOpen;
	}

	/**
	 * 异常信息消息常量定义
	 */
	public final static int MESSAGE_EXCEPTION = -1;

	/**
	 * 开始文件传输消息常量定义
	 */
	public final static int MESSAGE_START = 1;
	
	/**
	 * 文件传输进度消息常量定义
	 */
	public final static int MESSAGE_PROGRESS = 2;

	/**
	 * 单个文件已接收消息常量定义
	 */
	public final static int MESSAGE_ITEMRECEIVED = 3;
	
	/**
	 * 文件传输完成消息常量定义
	 */
	public final static int MESSAGE_COMPLETE = 4;

	/**
	 * 使用蓝牙心电仪物理地址打开设备
	 * 
	 * @param address 心电仪的 MAC 地址
	 * @return 目标设备打开成功则返回 true，否则返回 false
	 */
	public boolean openDevice(String address) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// 检查蓝牙适配器是否已存在
		if (adapter == null) {
			onException(new BluetoothAdapterNotFoundException());

			return false;
		}

		// 检查蓝牙适配器是否已启用
		if (!adapter.isEnabled()) {
			onException(new UnableToStartServiceDiscoveryException());

			return false;
		}

		BluetoothDevice device;

        try {
            device = adapter.getRemoteDevice(address.toUpperCase());
        } catch (Exception ex) {
            // 蓝牙设备物理地址无效异常
            onException(new BluetoothMacAddressIsNotValidException());

            return false;
        }

		try {
			_socket = device.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();

			// 创建 SOCKET 失败异常
			onException(new CreateSocketFailedException());

			return false;
		}

		adapter.cancelDiscovery();

		try {
			_socket.connect();
		} catch (IOException e) {
			// 连接设备失败异常
			onException(new ServiceDiscoveryFailedException());

			return false;
		}

		if (_transfer == null) {
			_transfer = new FileTransfer(handler, _socket);
		}

		_transfer.start();

		_isOpen = true;

		return true;
	}

	/**
	 * 关闭蓝牙心电仪
	 * 
	 * @return 关闭设备成功则返回 true，否则返回 false
	 */
	public boolean closeDevice() {
		if (!_isOpen)
			return false;

		if (_transfer != null) {
			if (_transfer.isRunning())
				_transfer.end();
		}

		_transfer = null;

		if (_socket != null) {
			try {
				_socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		_isOpen = false;

		return true;
	}

	/**
	 * 获取心率
	 * 
	 * @param file 心电数据文件项
	 * @return 心率
	 */
	public float getHeartRate(FileItem file) {
        if (file == null) return 0;
        if (file.fileData == null) return 0;

        return (((file.fileData[15] & 0xff) << 8) + (file.fileData[14] & 0xff)) / 10.0f;
	}

	private OnFileTransferListener _listener = null;

	/**
	 * 设置文件传输侦听器
	 * 
	 * @param listener 文件传输侦听器，用来接收心电文件及接收进度等通知
	 */
	public void setOnFileTransfer(OnFileTransferListener listener) {
		_listener = listener;
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_EXCEPTION) {
				onException((Exception) msg.obj);
			} else if (msg.what == MESSAGE_START) {
				onStart(msg.arg1);
			} else if (msg.what == MESSAGE_PROGRESS) {
				onProgress(msg.arg1, msg.arg2);
			} else if (msg.what == MESSAGE_ITEMRECEIVED) {
				onItemReceived((FileItem) msg.obj);
			} else if (msg.what == MESSAGE_COMPLETE) {
				onComplete(msg.arg1);
			}
		}
	};

	private void onException(Exception e) {
        if (e instanceof DeviceDisconnectedException) {
            closeDevice();
        }

		if (_listener != null) {
			_listener.onException(e);
		}
	}

	private void onStart(int fileCount) {
		if (_listener != null) {
			_listener.onStart(fileCount);
		}
	}

	private void onProgress(int current, int total) {
		if (_listener != null) {
			_listener.onProgress(current, total);
		}
	}

	private void onItemReceived(FileItem file) {
		if (_listener != null) {
			_listener.onItemReceived(file);
		}
	}

	private void onComplete(int fileCount) {
		if (_listener != null) {
			_listener.onComplete(fileCount);
		}
	}
}
