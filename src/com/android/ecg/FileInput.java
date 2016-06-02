package com.android.ecg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.ecg.Exception.*;
import com.mt.helpclass.Ipport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileInput extends Activity {

	private libecg ecg = new libecg();

	private String _MAC = null,userid = null;

	ProgressBar pbrCurrent = null;
	ProgressBar pbrTotal = null;

	private ArrayAdapter<FileItem> files = null;
	private FileItem file = null;
	
	SharedPreferences Inform=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileinput);

		// ��ȡ�ؼ�����
		pbrCurrent = (ProgressBar) findViewById(R.id.ecg_current);
		pbrTotal = (ProgressBar) findViewById(R.id.ecg_total);

		ListView lstData = (ListView) findViewById(R.id.ecg_data);

		Inform = getSharedPreferences("perference",
				MODE_PRIVATE);
		_MAC = Inform.getString("Mac", "");
		
		userid= Inform.getString("USERID", "");

		files = new ArrayAdapter<FileItem>(FileInput.this, R.layout.viewfile);

		lstData.setAdapter(files);

		for (int i = 0; i < fileList().length; i++) {
			String file = fileList()[i];

			try {
				InputStream stream = openFileInput(file);

				int length = stream.available();
				byte[] data = new byte[length];

				stream.read(data);

				FileItem item = new FileItem(data, length);

				files.add(item);

				stream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		lstData.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu arg0, View arg1,
					ContextMenuInfo arg2) {
				arg0.setHeaderTitle(R.string.ecg_choose);

				MenuItem item;

				item = arg0.add(R.string.ecg_view);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						if (file != null)
							viewFile(file);
						return false;
					}
				});
				
				item = arg0.add(R.string.ecg_upload);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						new Thread(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Looper.prepare();
								if(Inform.getBoolean("UploadFlag", false)){
								if(file!=null){
									FileItem[] uploadfile=new FileItem[]{file};
//									String res=UploadUntil.Post(uploadfile, "http://192.168.123.109:8080/REMSWeb/GetFile");
									String res=UploadUntil.Post(userid,uploadfile, "http://"+Ipport.urlIp+Ipport.urlPort+"/GetFile");
									if(res==null){
										Toast.makeText(FileInput.this, "����ʧ��", Toast.LENGTH_SHORT)
										.show();
									}
									else {
										Toast.makeText(FileInput.this, "����ɹ�", Toast.LENGTH_SHORT)
										.show();
									}
								}
								super.run();
								}
								else {
									Toast.makeText(getApplicationContext(), "���½��ʹ�ô˹���", Toast.LENGTH_SHORT).show();
								}
								Looper.loop();
							}
							
						}.start();

						return false;
					}
				});
				
				item = arg0.add(R.string.ecg_uploadall);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						
						new Thread(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Looper.prepare();
								if(Inform.getBoolean("UploadFlag", false)){
								String res=null;
								FileItem[] uploadall=new FileItem[files.getCount()];
								for (int i = 0; i < files.getCount(); i++) {
									uploadall[i]=files.getItem(i);
								}
//								String res=UploadUntil.Post(userid,uploadfile, "http://"+Ipport.urlIp+"/GetFile");

								res=UploadUntil.Post(userid,uploadall, "http://"+Ipport.urlIp+Ipport.urlPort+"/GetFile");
//								res=UploadUntil.Post(userid,uploadall, "http://192.168.123.108:8080/REMSWeb/GetFile");
//								res=UploadUntil.Post(userid,uploadall, "http://192.168.123.108:8080/REMSWeb/GetFile");
								if(res==null){
									Toast.makeText(FileInput.this, "����ʧ��", Toast.LENGTH_SHORT)
									.show();
								}
								else{
									Toast.makeText(FileInput.this, "����ɹ�", Toast.LENGTH_SHORT)
									.show();
								}
								super.run();
								}
								else {
									Toast.makeText(getApplicationContext(), "���½��ʹ�ô˹���", Toast.LENGTH_SHORT).show();
								}
								Looper.loop();
							}

						}.start();
						return false;
					}
				});

				item = arg0.add(R.string.ecg_delete);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						if (file != null) {
							FileInput.this.deleteFile(file.fileName);
							files.remove(file);
						}

						file = null;
						return false;
					}
				});

				item = arg0.add(R.string.ecg_clear);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						for (int i = 0; i < files.getCount(); i++) {
							FileInput.this.deleteFile(files.getItem(i).fileName);
						}

						files.clear();
						file = null;
						return false;
					}
				});
			}
		});

		lstData.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				file = (FileItem) arg0.getItemAtPosition(arg2);
				if (file != null)
					viewFile(file);
			}
		});

		lstData.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				file = (FileItem) arg0.getItemAtPosition(arg2);
				return false;
			}
		});

		// ����Ѫѹ�����¼�������
		ecg.setOnFileTransfer(transfer);
		
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				super.run();
				if (!ecg.isOpen())
					ecg.openDevice(_MAC);
				Looper.loop();
			}
			
		}.start();
	}
	
	private void viewFile(FileItem file) {
		Intent in = new Intent(this, EcgViewerActivity.class);
		
		in.putExtra("data", file);

		startActivity(in);
	}
	

	/**
	 * �ļ������¼�������
	 */
	private OnFileTransferListener transfer = new OnFileTransferListener() {
		/**
		 * �ļ����䷢���쳣�¼�
		 * 
		 * @param exception
		 *            �쳣��Ϣ
		 */
		public void onException(Exception exception) {
			if (ecg.isOpen())
				ecg.closeDevice();

			if (exception instanceof BluetoothAdapterNotFoundException) {
				// δ��⵽����������
				Toast.makeText(FileInput.this, "�����豸δ�ҵ��������ֻ���֧������<",
						Toast.LENGTH_LONG).show();
			} else if (exception instanceof UnableToStartServiceDiscoveryException) {
				// �ֻ�����δ����
				Toast.makeText(FileInput.this, "�����豸δ���ã����ȿ��������豸",
						Toast.LENGTH_LONG).show();
			} else if (exception instanceof CreateSocketFailedException) {
				// ���ֻ���������ʧ��
				Toast.makeText(FileInput.this, "���� SOCKET ����ʧ��",
						Toast.LENGTH_LONG).show();
			} else if (exception instanceof ServiceDiscoveryFailedException) {
				// �ĵ�������δ����
				Toast.makeText(FileInput.this, "�����豸ʧ�ܣ������ĵ����Ƿ��Ѵ�",
						Toast.LENGTH_LONG).show();
			} else if (exception instanceof BluetoothMacAddressIsNotValidException) {
				// ���������ַ��Ч
				Toast.makeText(FileInput.this, " �����豸�����ַ��Ч", Toast.LENGTH_LONG)
						.show();
			} else if (exception instanceof CreateStreamFailedException) {
				// ���ֻ���������ͨѶʧ��
				Toast.makeText(FileInput.this, "����������ʧ��", Toast.LENGTH_LONG)
						.show();
			} else if (exception instanceof DeviceDisconnectedException) {
				// �豸�����ѶϿ��쳣
				Toast.makeText(FileInput.this, "�豸�ѶϿ�����", Toast.LENGTH_LONG)
						.show();
			}

		}

		private int _fileCount = 0;
		private int _progress = 0;

		/**
		 * �ļ����俪ʼ�¼�
		 * 
		 * @param fileCount
		 *            ��������ļ�����Ŀ
		 */
		public void onStart(int fileCount) {
			_fileCount = fileCount;
			_progress = 1;

			pbrCurrent.setMax(0);
			pbrCurrent.setProgress(0);

			pbrTotal.setMax(_fileCount);
			pbrTotal.setProgress(_progress);
		}

		/**
		 * �ļ���������¼�
		 * 
		 * @param current
		 *            ��ǰ�Ѵ������ݰ���Ŀ
		 * @param total
		 *            ������������ݰ���Ŀ
		 */
		public void onProgress(int current, int total) {
			pbrCurrent.setMax(total);
			pbrCurrent.setProgress(current);
		}

		/**
		 * �����ļ��ѽ����¼�
		 * 
		 * @param file
		 *            �ѽ��յ����ļ�
		 */
		public void onItemReceived(FileItem file) {
			pbrTotal.setProgress(_progress++);

			files.add(file);
		}

		/**
		 * �ļ���������¼�
		 * 
		 * @param fileCount
		 *            �ѽ��յ��ļ���Ŀ
		 */
		public void onComplete(int fileCount) {
			pbrCurrent.setProgress(pbrCurrent.getMax());
			pbrTotal.setProgress(pbrTotal.getMax());
		}

	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		// �ر��豸
		if (ecg.isOpen())
			ecg.closeDevice();

		for (int i = 0; i < files.getCount(); i++) {
			FileItem file = files.getItem(i);
			if (file == null)
				continue;

			try {
				OutputStream stream = openFileOutput(file.fileName, Context.MODE_PRIVATE);

				stream.write(file.fileData);
				stream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// �������˵�
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.open:
			new Thread(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Looper.prepare();
					super.run();
					if (!ecg.isOpen())
						ecg.openDevice(_MAC);
					Looper.loop();
				}
				
			}.start();
			
			break;

		case R.id.close:
			if (ecg.isOpen())
				ecg.closeDevice();
			break;
		}

		return true;
	}

}
