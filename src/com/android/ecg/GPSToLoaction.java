package com.android.ecg;

import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class GPSToLoaction {
	private Context mContext;

	private LocationManager locationManager;
	private String provider;
	private String provider_gps_network;
	private Location location = null;
	private Mylocation myLocation = null;

	public GPSToLoaction(Context mContext) {
		super();
		this.mContext = mContext;
		locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		myLocation = new Mylocation();
	}

	public boolean isEnbled() {
		if (hasGPSDevice(mContext)) {
			Toast.makeText(mContext, "has GPS", Toast.LENGTH_SHORT).show();
			// ���GPS�أ����
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
				OnOffGps();
			provider_gps_network = LocationManager.GPS_PROVIDER;
			return true;
		} else {
			Toast.makeText(mContext, "no GPS", Toast.LENGTH_SHORT).show();
			if (isNetworkAvailable(mContext) == false) {
				Toast.makeText(mContext, "no network", Toast.LENGTH_SHORT)
						.show();
				return false;
			} else {
				provider_gps_network = LocationManager.NETWORK_PROVIDER;
				return true;
			}
		}
	}

	public void exec() {
		// ͨ��GPS��ȡ��ǰ����
		// ��ȡ��γ��
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// ���÷����̵���Ϣ
			Criteria criteria = new Criteria();
			// �ṩ����ľ��ȱ�׼
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// ����Ҫ�߶���Ϣ
			criteria.setAltitudeRequired(false);
			// ����Ҫ��λ��Ϣ
			criteria.setBearingRequired(false);
			// �������������
			criteria.setCostAllowed(false);
			// ���ĵ���Ϊ��
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			// ȡ����ƥ���criteria
			provider = locationManager.getBestProvider(criteria, true);

			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				setMyLocation();
			} else {
				if (provider_gps_network == LocationManager.GPS_PROVIDER)
					GpsForArea();
				else if (provider_gps_network == LocationManager.NETWORK_PROVIDER)
					NetWorkForArea();
			}
		} else {// ͨ�������ȡ��ǰ����
			NetWorkForArea();
		}
	}

	// �ж��Ƿ��п�������
	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else { // ��������������ж��������� ������������ //�����ʹ��
					// cm.getActiveNetworkInfo().isAvailable();
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// �ж��Ƿ���GPS�豸
	public boolean hasGPSDevice(Context context) {
		final LocationManager mgr = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (mgr == null)
			return false;
		final List<String> providers = mgr.getAllProviders();
		if (providers == null)
			return false;
		return providers.contains(LocationManager.GPS_PROVIDER);
	}

	// ����GPS
	public void OnOffGps() {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	// ͨ�������ȡ��ǰλ������
	public void NetWorkForArea() {
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
		location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			setMyLocation();
		}
	}

	// ͨ��GPS��ȡ��ǰλ������
	public void GpsForArea() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, locationListener);
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			setMyLocation();
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) { // ������ı�ʱ�����˺��������Provider������ͬ�����꣬���Ͳ��ᱻ����
			if (location != null) {
				setMyLocation();
			}
		}

		public void onProviderDisabled(String provider) {
			// Provider��disableʱ�����˺���������GPS���ر�
		}

		public void onProviderEnabled(String provider) {
			// Provider��enableʱ�����˺���������GPS����
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Provider��ת̬�ڿ��á���ʱ�����ú��޷�������״ֱ̬���л�ʱ�����˺���
		}
	};

	private void setMyLocation() {
		myLocation.setLatitude(location.getLatitude());
		myLocation.setLongitude(location.getLongitude());
	}

	public Mylocation getMyLocation() {
		return myLocation;
	}

}


