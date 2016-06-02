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
 * �ĵ��Ƿ�װ����
 * 
 * @author sjh
 */
public class libecg {
	public static BluetoothSocket _socket = null;
	private FileTransfer _transfer = null;
	private boolean _isOpen = false;

    /**
     * ��ȡ�豸�Ƿ��ڴ�״̬
     *
     * @return ���豸�Ѵ��򷵻� true�����򷵻� false
     */
	public boolean isOpen() {
		return _isOpen;
	}

	/**
	 * �쳣��Ϣ��Ϣ��������
	 */
	public final static int MESSAGE_EXCEPTION = -1;

	/**
	 * ��ʼ�ļ�������Ϣ��������
	 */
	public final static int MESSAGE_START = 1;
	
	/**
	 * �ļ����������Ϣ��������
	 */
	public final static int MESSAGE_PROGRESS = 2;

	/**
	 * �����ļ��ѽ�����Ϣ��������
	 */
	public final static int MESSAGE_ITEMRECEIVED = 3;
	
	/**
	 * �ļ����������Ϣ��������
	 */
	public final static int MESSAGE_COMPLETE = 4;

	/**
	 * ʹ�������ĵ��������ַ���豸
	 * 
	 * @param address �ĵ��ǵ� MAC ��ַ
	 * @return Ŀ���豸�򿪳ɹ��򷵻� true�����򷵻� false
	 */
	public boolean openDevice(String address) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// ��������������Ƿ��Ѵ���
		if (adapter == null) {
			onException(new BluetoothAdapterNotFoundException());

			return false;
		}

		// ��������������Ƿ�������
		if (!adapter.isEnabled()) {
			onException(new UnableToStartServiceDiscoveryException());

			return false;
		}

		BluetoothDevice device;

        try {
            device = adapter.getRemoteDevice(address.toUpperCase());
        } catch (Exception ex) {
            // �����豸�����ַ��Ч�쳣
            onException(new BluetoothMacAddressIsNotValidException());

            return false;
        }

		try {
			_socket = device.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();

			// ���� SOCKET ʧ���쳣
			onException(new CreateSocketFailedException());

			return false;
		}

		adapter.cancelDiscovery();

		try {
			_socket.connect();
		} catch (IOException e) {
			// �����豸ʧ���쳣
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
	 * �ر������ĵ���
	 * 
	 * @return �ر��豸�ɹ��򷵻� true�����򷵻� false
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
	 * ��ȡ����
	 * 
	 * @param file �ĵ������ļ���
	 * @return ����
	 */
	public float getHeartRate(FileItem file) {
        if (file == null) return 0;
        if (file.fileData == null) return 0;

        return (((file.fileData[15] & 0xff) << 8) + (file.fileData[14] & 0xff)) / 10.0f;
	}

	private OnFileTransferListener _listener = null;

	/**
	 * �����ļ�����������
	 * 
	 * @param listener �ļ����������������������ĵ��ļ������ս��ȵ�֪ͨ
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
