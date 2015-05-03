package org.vudroid.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;

import org.vudroid.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LinkService extends Activity implements PeerListListener,
		ConnectionInfoListener, OnItemSelectedListener {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private BroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;
	private Button mScanButton;
	private Button mConnectButton;
	private WifiP2pConfig mConfig;
	private TextView deviceNameTextView;
	private TextView deviceAddressTextView;
	private TextView connectionAttemptTextView;
	private DiscoveryActionListenter discoveryListener;
	private ConnectionActionListener connectionListener;
	private String sAddr;
	public String hostIP;
	private Boolean sORc;
	private Spinner spinner;
	ArrayList<String> addrList = new ArrayList<String>();	
	ArrayList<String> nameList = new ArrayList<String>();	
	String[] nameStr;
	String[] addrStr;
	int position = -1;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		
		spinner = (Spinner) findViewById(R.id.spinner1);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mScanButton = (Button) findViewById(R.id.scan_devices);
		deviceNameTextView = (TextView) findViewById(R.id.device_name);
		deviceAddressTextView = (TextView) findViewById(R.id.device_address);
		connectionAttemptTextView = (TextView) findViewById(R.id.connection_attempt);
		discoveryListener = new DiscoveryActionListenter();
		connectionListener = new ConnectionActionListener();
		final String PATH = Environment.getExternalStorageDirectory() + "/zzz/";
		if(!(new File(PATH)).exists()) 
			new File(PATH).mkdirs();
		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deviceNameTextView.setVisibility(View.VISIBLE);
				deviceNameTextView.setText("Loading phones...");
				mManager.discoverPeers(mChannel, discoveryListener);
			}
		});
		mConnectButton = (Button) findViewById(R.id.connect_devices);
		mConfig = new WifiP2pConfig();
		mConnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mConfig.deviceAddress != null){
				connectionAttemptTextView.setVisibility(View.VISIBLE);
				connectionAttemptTextView.setText("Trying to connect...");
				mManager.connect(mChannel, mConfig, connectionListener);
				}
				else{
					connectionAttemptTextView.setVisibility(View.VISIBLE);
					connectionAttemptTextView.setText("No device to connect...");
				}
			}
		});
		createIntentFilter();
		createBroadcastReceiver();
	}
	
	private void showBrowser(Context context){
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(context, BaseBrowserActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("host", hostIP);
		bundle.putBoolean("sORc", sORc);
		intent.putExtras(bundle);
		//intent.putExtra("info", "WHAT");
		startActivity(intent);
	}

	void buttonHandler(View v) {
		switch (v.getId()) {
		case R.id.scan_devices:

			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
   @Override
    protected void onDestroy()
    {
    	 if (mManager != null && mChannel != null) {
    		 mManager.removeGroup(mChannel, new ActionListener() {

                 @Override
                 public void onFailure(int reasonCode) {
                     Log.d("", "Disconnect failed. Reason :" + reasonCode);
                 }

                 @Override
                 public void onSuccess() {
                 }

             });
         }
         super.onDestroy();
    }
	/**
	 * 
	 */
	private void createBroadcastReceiver() {
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
					Log.d("STATE", "P2P State Changed");
					int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
					if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
						Toast.makeText(getApplicationContext(),"Wifi Direct On", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(),"Wifi Direct Off", Toast.LENGTH_SHORT).show();
					}
				} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
					Log.d("STATE", "P2P Peers Changed");
					mManager.requestPeers(mChannel, LinkService.this);

				} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
					Log.d("STATE", "P2P Connection Changed");
					NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
					if (networkInfo != null) {
						if (networkInfo.isConnected()) {
							mManager.requestConnectionInfo(mChannel,LinkService.this);
						} else {
							// We're disconnected!
							connectionAttemptTextView.setText("Disconnected :(");
						}
					}
				} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
						.equals(action)) {
					Log.d("STATE", "P2P Device Changed");
					WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
					Log.d("STATE", "Device name " + device.deviceName);
				}
			}
		};
	}

	/**
	 * 
	 */
	private void createIntentFilter() {
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Log.d("STATE", "P2P Peers Available");
		int i =0;
		for (WifiP2pDevice device : peers.getDeviceList()) {
			Toast.makeText(getApplicationContext(),
					"Device " + device.deviceName + " discovered",
					Toast.LENGTH_SHORT).show();
			Log.d("STATE", "Device " + device.deviceName + " discovered");
			
			addrList.add(device.deviceAddress);
			nameList.add(device.deviceName);
			
			
			/*
			mConfig.deviceAddress = device.deviceAddress;
			deviceNameTextView.setVisibility(View.VISIBLE);
			deviceNameTextView.setText(device.deviceName);
			deviceAddressTextView.setVisibility(View.VISIBLE);
			deviceAddressTextView.setText(device.deviceAddress);
			*/
		}
		nameStr = nameList.toArray( new String[ nameList.size() ] );
		addrStr = addrList.toArray( new String[ addrList.size() ] );
		nameStr = new HashSet<String>(Arrays.asList(nameStr)).toArray(new String[0]);
		addrStr = new HashSet<String>(Arrays.asList(addrStr)).toArray(new String[0]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nameStr);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
	}


	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Log.d("STATE", "ConnectionInfo " + info.describeContents());
		//mIP = getDottedDecimalIP(getLocalIPAddress());
		if (info.isGroupOwner) {
			Log.d("STATE", "I am the MASTER! :D");
			//createClientSocket(info, mIP);
			sORc = true;
			//createServerSocket();
			Toast.makeText(getApplicationContext(),"Master device", Toast.LENGTH_SHORT).show();
		} else {
			//createServerSocket();
			//createClientSocket(info);
			sORc = false;
			Log.d("STATE", "I am the SLAVE ! :(");
			Toast.makeText(getApplicationContext(), "Slave device", Toast.LENGTH_SHORT).show();
		}
		hostIP = info.groupOwnerAddress.getHostAddress();
		showBrowser(LinkService.this);
	}
	
	/*private byte[] getLocalIPAddress() {
	    try { 
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
	            NetworkInterface intf = en.nextElement(); 
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
	                InetAddress inetAddress = enumIpAddr.nextElement(); 
	                if (!inetAddress.isLoopbackAddress()) { 
	                    if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-) 
	                        return inetAddress.getAddress(); 
	                    } 
	                    //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6 
	                } 
	            } 
	        } 
	    } catch (SocketException ex) { 
	        //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
	    } catch (NullPointerException ex) { 
	        //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
	    } 
	    return null; 
	}
	
	private String getDottedDecimalIP(byte[] ipAddr) {
	    //convert to dotted decimal notation:
	    String ipAddrStr = "";
	    for (int i=0; i<ipAddr.length; i++) {
	        if (i > 0) {
	            ipAddrStr += ".";
	        }
	        ipAddrStr += ipAddr[i]&0xFF;
	    }
	    return ipAddrStr;
	}*/

	

	class DiscoveryActionListenter implements ActionListener {

		@Override
		public void onFailure(int reason) {
			Toast.makeText(getApplicationContext(),
					"Discovery Failed - " + reason, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess() {
			// Will trigger the action
			// WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
			Toast.makeText(getApplicationContext(), "Discovery Success",
					Toast.LENGTH_SHORT).show();
		}

	}

	class ConnectionActionListener implements ActionListener {

		@Override
		public void onSuccess() {
			Toast.makeText(getApplicationContext(),
					"Device " + mConfig.deviceAddress + " connected",
					Toast.LENGTH_SHORT).show();
			
			connectionAttemptTextView.setText(" Connected");
		}

		@Override
		public void onFailure(int reason) {
			// TODO Auto-generated method stub
			connectionAttemptTextView.setText("Failed :( ");
		}

	}
	
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		position = spinner.getSelectedItemPosition();
		
		mConfig.deviceAddress = addrStr[position];
		deviceNameTextView.setVisibility(View.VISIBLE);
		deviceNameTextView.setText(nameStr[position]);
		deviceAddressTextView.setVisibility(View.VISIBLE);
		deviceAddressTextView.setText(addrStr[position]);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	

}
