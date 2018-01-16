package com.accloud.ac_service_android_demo.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import java.util.Set;


import android.os.Handler;
import android.os.Message;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.utils.CircleImageView;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.Pop;
import com.accloud.ac_service_android_demo.utils.SendDataBle;
import com.accloud.ac_service_android_demo.utils.ViewHolder;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AI6060HActivity extends Activity implements OnClickListener {
	public static final String TAG = "AI6060HActivity";
	private ReceiveBroadCast receiveBroadCast;
	
	SendDataBle sendDataBle;

	Receiver<TopicData> receiver;// 订阅
	private String subDomain;
	TextView scanBle_tv;
	String message1, message3, message5, message;
	String MAC_for_4C;// 用于表示20707当前连接设备的MAC
	List<String> MAC_for_4C_list;// 用于存放已连接的设备的集合

	private List<String> scanedBleAddressList;// 用以存放扫描到的蓝牙地址
	private List<String> addrTypeList;// 用以存放相应蓝牙的addr_type
	int position_i;

	private ListView showScanBleAddressListView;// 用于展示扫描到的蓝牙地址
	private List<String> scanBleList;// 扫描到的蓝牙设备mac地址集合
	ArrayAdapter scanBleListAdapter;

	String deviceId, physicalDeviceId;
	String deviceName;
	String type, mac, aliasName;
	String deviceType;
	String addedMac;
	String txtString;// 手动添加的listview项中，设备的type+mac_aliasName

	String bleScanedAddress;// 字符串，扫描到的蓝牙地址+“*”组成的字符串，用于快速辨别设备是否在线

	private ListView addedDevice;// 手动添加蓝牙设备的listview

	private List<String> addDevice_list;// 手动添加的蓝牙设备数据
	private List<String> addDevice_list_read;

	private TextView headTitle;
	private Button back, add, scan;

	AddDeviceAdapter addDeviceAdapter;//添加蓝牙设备listview的适配器
	ProgressDialog dialog_scan;
	ProgressDialog dialog_connect;

	MyItemListener myItemListener;

	Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				dialog_scan.dismiss();
				break;
			case 2:
				dialog_connect.dismiss();
				break;
			}
		};
	};

	public class ReceiveBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 得到广播中得到的数据，并显示出来
			String broad_message = intent
					.getStringExtra("message_login_another_device");
			showDialog(broad_message);

		}

	}

	private void showDialog(String broad_message) {
		if (!AI6060HActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(AI6060HActivity.this);
			builder.setTitle(getResources().getString(R.string.offline_notification)).setMessage(broad_message);
			builder.setPositiveButton(getResources().getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Log.e(TAG, "退出登录");
							Long userid = PreferencesUtils.getLong(AI6060HActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							dialog.dismiss();
							AI6060HActivity.this.finish();
							Intent intent = new Intent(AI6060HActivity.this,
									LoginActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
									| Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
			AlertDialog alert = builder.create();
			alert.setCanceledOnTouchOutside(false);// dialog点击空白不消失
			alert.setCancelable(false);// dialog点击返回键不消失
			alert.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ai6060_h);
		Log.e("AI6060HActiviyt", "onCreate");
		PushAgent.getInstance(this).onAppStart();

		Intent intent = getIntent();
		deviceId = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		deviceName = intent.getStringExtra("deviceName");
		Log.e("AI6060HActivity", deviceId + "*" + physicalDeviceId + "*"
				+ deviceName);
		
		

		MAC_for_4C_list = new ArrayList<String>();

		scanedBleAddressList = new ArrayList<String>();
		addrTypeList = new ArrayList<String>();

		dialog_scan = new ProgressDialog(this);
		dialog_connect = new ProgressDialog(this);

		addDevice_list = new ArrayList<String>();
		addDevice_list_read = new ArrayList<String>();

		addDevice_list_read = (List<String>) readObject(AI6060HActivity.this,
				physicalDeviceId);
		if (addDevice_list_read != null) {
			addDevice_list.addAll(addDevice_list_read);
		}

		headTitle = (TextView) findViewById(R.id.header_title);
		headTitle.setText(deviceName);
		back = (Button) findViewById(R.id.btn_back);
		add = (Button) findViewById(R.id.btn_add);
		scan = (Button) findViewById(R.id.btn_scan);

		back.setOnClickListener(this);
		add.setOnClickListener(this);
		scan.setOnClickListener(this);

		addedDevice = (ListView) findViewById(R.id.lv_added_device);

		myItemListener = new MyItemListener();

		addDeviceAdapter = new AddDeviceAdapter(AI6060HActivity.this);
		addedDevice.setAdapter(addDeviceAdapter);
		addDeviceAdapter.notifyDataSetChanged();
		addedDevice.setOnItemClickListener(myItemListener);

		showScanBleAddressListView = (ListView) findViewById(R.id.lv_scan_device);
		scanBleList = new ArrayList<String>();
		scanBleListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, scanBleList);
		showScanBleAddressListView.setAdapter(scanBleListAdapter);

		scanBle_tv = (TextView) findViewById(R.id.tv_ble_scan);

		// 注册广播接收
		receiveBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("102"); // 只有持有相同的action的接受者才能接收此广播
		registerReceiver(receiveBroadCast, filter);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		subscribe("xinlian01", "topic_type", deviceId);// 订阅，可获取到返回值

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		sendDataBle = new SendDataBle(subDomain, physicalDeviceId);
		sendDataBle.sendData("004C", "");// 判断当前20707是否有未断开的蓝牙设备。004C，查询当前连接的设备
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AC.customDataMgr().unregisterDataReceiver(receiver);
		unregisterReceiver(receiveBroadCast);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;

		case R.id.btn_add:
			Intent intent = new Intent(AI6060HActivity.this,
					DeviceNameActivity.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.btn_scan:
			scanBle_tv.setText("");
			bleScanedAddress = null;
			scanedBleAddressList.clear();
			addrTypeList.clear();
			addDeviceAdapter.notifyDataSetChanged();
			scanBleList.clear();
			scanBleListAdapter.notifyDataSetChanged();
			dialog_scan.setTitle(getString(R.string.scan));
			dialog_scan.setMessage(getString(R.string.scanning_ble_device));
			dialog_scan.setCancelable(true);
			dialog_scan.setCanceledOnTouchOutside(false);
			dialog_scan.show();
			// 扫描蓝牙地址设备
			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						sendDataBle.sendData("0042", "01");// 开始扫描
						Thread.sleep(5000);
						Log.e("停止扫描", "停止扫描");
						sendDataBle.sendData("0042", "00");// 停止扫描
						dialog_scan.dismiss();
						// getDeviceList();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			break;
		}

	}

	/**
	 * 订阅方法和获取返回值
	 * 
	 * @param submain
	 * @param topic_type
	 * @param deviceId
	 */
	private void subscribe(String submain, String topic_type,
			final String deviceId) {
		// TODO Auto-generated method stub
		AC.customDataMgr().subscribe(
				Topic.customTopic(submain, topic_type, deviceId),
				new VoidCallback() {

					@Override
					public void error(ACException arg0) {
						Log.e("/*/*/*", "订阅失败");
					}

					@Override
					public void success() {
						Log.e("/*/*/*", "订阅成功");
					}
				});
		receiver = new Receiver<TopicData>() {

			@Override
			public void onReceive(TopicData arg0) {
				Log.e("订阅onReceive", arg0.getValue());
				String jsonData = arg0.getValue();
				OnReceive onRece = GsonUtil.parseJsonWithGson(jsonData,
						OnReceive.class);
				String[] pay = onRece.getPayload();
				byte[] arraysPay = Base64.decode(pay[0], 0);
				String payload = ItonAdecimalConver.byte2hex(arraysPay)
						.replace(" ", "");
				Log.e("接收到的值", payload);
				String Command_t = null;

				if (payload.startsWith("6974617A")) {
					Command_t = payload.substring(8, 12);// 根据返回值的Command_t来判断执行的是哪一条20707协议

					if (Command_t.equals("0042")) {// LE使能扫描蓝牙设备
						Message msgs = new Message();
						msgs.what = 1;
						mhandler.sendMessage(msgs);

						String[] messages = payload.split("6974617A");
						for (int i = 0; i < messages.length; i++) {
							String messagess = "6974617A" + messages[i];
							String BackData = null;// BLE地址
							String Addr_type = null;
							if (messages[i].length() > 22) {
								Addr_type = messagess.substring(20, 22);
								BackData = messagess.substring(22, 34);
								Log.e("Addr_type/BackData", Addr_type + "/"
										+ BackData);
								addrTypeList.add(Addr_type);
								scanedBleAddressList.add(BackData);
								
								scanBle_tv.append(BackData + "*");
								String[] scanBleArray = scanBle_tv.getText()
										.toString().trim().split("\\*");// 字符串拆分为字符串数组，"\\*"正则表达式规定，"*"前得加上"\\"
								Collections.addAll(scanBleList, scanBleArray);// 数组转list
								scanBleList = removeDuplicate(scanBleList);// 集合元素的唯一性判断
								position_i = scanBleList.indexOf(BackData);
								Log.e("position_i", position_i + "");
								scanBleListAdapter.notifyDataSetChanged();
								addDeviceAdapter.notifyDataSetChanged();
							}
						}

					} else if (Command_t.equals("0043")) {// 连接特定地址设备
						Message msgs = new Message();
						msgs.what = 2;
						mhandler.sendMessage(msgs);
						Toast.makeText(AI6060HActivity.this,
								getString(R.string.connection_successful),
								Toast.LENGTH_SHORT).show();

						if (deviceType.startsWith(getResources().getString(R.string.led_light_band_for_four))) {
							Intent intent = new Intent(AI6060HActivity.this,
									LedForFourActivity.class);
							intent.putExtra("macAddress", addedMac);
							intent.putExtra("physicalDeviceId",
									physicalDeviceId);
							intent.putExtra("deviceId", deviceId);
							startActivity(intent);
						} else if (deviceType.startsWith(getResources().getString(R.string.emergency_light))) {
							// 跳转到应急灯界面
							Intent intent = new Intent(AI6060HActivity.this,
									EmergencyLightActivity.class);
							intent.putExtra("macAddress", addedMac);
							intent.putExtra("physicalDeviceId",
									physicalDeviceId);
							intent.putExtra("deviceId", deviceId);
							startActivity(intent);
						} else if (deviceType.startsWith(getResources().getString(R.string.thermometer))) {
							// 跳转到温度计界面
							Intent intent = new Intent(AI6060HActivity.this,
									ThermActivity.class);
							intent.putExtra("macAddress", addedMac);
							Log.e("macAddress---Therm", addedMac);
							intent.putExtra("physicalDeviceId",
									physicalDeviceId);
							intent.putExtra("deviceId", deviceId);
							startActivity(intent);
						} else if (deviceType.startsWith(getResources().getString(R.string.led_light_band_for_two))) {
							// 跳转到二线灯带界面
							Intent intent = new Intent(AI6060HActivity.this,
									LedForTwoActivity.class);
							intent.putExtra("macAddress", addedMac);
							intent.putExtra("physicalDeviceId",
									physicalDeviceId);
							intent.putExtra("deviceId", deviceId);
							startActivity(intent);

						} else {
							Toast.makeText(AI6060HActivity.this, getResources().getString(R.string.device_not_exist),
									Toast.LENGTH_SHORT).show();
						}

					} else if (Command_t.equals("004A")) {
						MAC_for_4C = payload.substring(18, 30);
						MAC_for_4C_list.add(MAC_for_4C);
						addDeviceAdapter.notifyDataSetChanged();

						String tem1 = payload.substring(36, 38);
						String tem2 = payload.substring(38, 40);
						String tem = tem2 + tem1;
						Log.e("tem1+tem2", tem1 + "*" + tem2);

						Intent intent_broadcast = new Intent(); // Itent就是我们要发送的内容
						intent_broadcast.putExtra("tem", tem);
						intent_broadcast.setAction("101"); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
						sendBroadcast(intent_broadcast); // 发送广播给下面的设备

					} else if (Command_t.equals("004C")) {

						// 69 74 61 7A 00 4C 01 00 01 00 00，没有已连接设备的返回值
						// 6974617A004C01000101FF6974617A004C010009010100F0C77F5F73E610，存在已连接设备的返回值
						if (payload.equals("6974617A004C0100010000")) {
							MAC_for_4C_list.clear();
							addDeviceAdapter.notifyDataSetChanged();
						}
						String[] messages = payload.split("6974617A");
						for (int i = 0; i < messages.length; i++) {
							String messagess_4c = "6974617A" + messages[i];
							if (messagess_4c.length() > 23) {
								MAC_for_4C = messagess_4c.substring(24, 36);
								Log.e("MAC_for_4C", MAC_for_4C);
								MAC_for_4C_list.add(MAC_for_4C);

							}

						}
						addDeviceAdapter.notifyDataSetChanged();
					} else if (Command_t.equals("0045")) {// 6974617A004501000100006974617A004C0100010000，断开连接的返回值和查询当前连接设备的返回值一起返回
						String[] messages = payload.split("6974617A");
						String meg = "6974617A" + messages[1];
						if (meg.length() < 23) {
							Log.e("**--*-**-*-", "9999999");
							MAC_for_4C_list.clear();
							addDeviceAdapter.notifyDataSetChanged();
						}
					}

				} else {
//					Pop.popToast(AI6060HActivity.this,
//							getString(R.string.returns_fault) + payload);
					Log.e("返回值错误", payload);
				}
			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
		// AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	public class MyItemListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			deviceType = parent.getItemAtPosition(position).toString();
			Log.e("deviceType", deviceType);
			addedMac = null;
			if (deviceType.startsWith(getResources().getString(R.string.thermometer)) || deviceType.startsWith(getResources().getString(R.string.emergency_light))) {
				addedMac = deviceType.substring(4, 16);
			} else if (deviceType.startsWith(getResources().getString(R.string.led_light_band_for_two))
					|| deviceType.startsWith(getResources().getString(R.string.led_light_band_for_four))) {
				addedMac = deviceType.substring(5, 17);
			}
			Log.e("addedMac", addedMac);
			int j = MAC_for_4C_list.indexOf(addedMac.toUpperCase());
			Log.e("j", j + "");

			final int i = scanedBleAddressList.indexOf(addedMac.toUpperCase());
			Log.e("i", i + "");

			if (j >= 0) {// 1.先判断设备是否已连接，若设备已被连接，则不用发送连接命令，直接跳转
				Intent intents = null;
				if (deviceType.startsWith(getResources().getString(R.string.thermometer))) {
					intents = new Intent(AI6060HActivity.this,
							ThermActivity.class);
					intents.putExtra("macAddress", addedMac);
					Log.e("macAddress---Therm", addedMac);
					intents.putExtra("physicalDeviceId", physicalDeviceId);
					intents.putExtra("deviceId", deviceId);
					startActivity(intents);
				} else if (deviceType.startsWith(getResources().getString(R.string.emergency_light))) {
					intents = new Intent(AI6060HActivity.this,
							EmergencyLightActivity.class);
					intents.putExtra("macAddress", addedMac);
					intents.putExtra("physicalDeviceId", physicalDeviceId);
					intents.putExtra("deviceId", deviceId);
					startActivity(intents);
				} else if (deviceType.startsWith(getResources().getString(R.string.led_light_band_for_two))) {
					intents = new Intent(AI6060HActivity.this,
							LedForTwoActivity.class);
					intents.putExtra("macAddress", addedMac);
					intents.putExtra("physicalDeviceId", physicalDeviceId);
					intents.putExtra("deviceId", deviceId);
					startActivity(intents);

				} else if (deviceType.startsWith(getResources().getString(R.string.led_light_band_for_four))) {
					intents = new Intent(AI6060HActivity.this,
							LedForFourActivity.class);
					intents.putExtra("macAddress", addedMac);
					intents.putExtra("physicalDeviceId", physicalDeviceId);
					intents.putExtra("deviceId", deviceId);
					startActivity(intents);
				}
			} else {
				if (i >= 0) {// 2.若设备未被连接，再判断设备是否在线
					Log.e("addedMac", addedMac);

					Log.e("lianjie", scanedBleAddressList.get(i));
					new Thread(new Runnable() {

						@Override
						public void run() {
							sendDataBle.sendData("0043", scanedBleAddressList.get(i)
									+ addrTypeList.get(i));// 连接特定地址设备BleAddress+addr_type
							
						}

					}).start();
					dialog_connect.setTitle(getResources().getString(R.string.connect_device));
					dialog_connect.setMessage(getResources().getString(R.string.connecting_wait));
					dialog_connect.setCancelable(true);
					dialog_connect.setCanceledOnTouchOutside(false);
					dialog_connect.show();

				} else {
					Toast.makeText(AI6060HActivity.this,
							getString(R.string.device_offline),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}





	/*
	 * 封装的GSON解析工具类，提供泛型参数
	 */
	public static class GsonUtil {
		// 将Json数据解析成相应的映射对象
		public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
			Gson gson = new Gson();
			T result = gson.fromJson(jsonData, type);
			return result;
		}

	}

	public class OnReceive {
		String deviceId;
		String[] payload;

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public String[] getPayload() {
			return payload;
		}

		public void setPayload(String[] payload) {
			this.payload = payload;
		}

	}

	public class AddDeviceAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;
		Context context;

		public AddDeviceAdapter(Context context) {
			this.context = context;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return addDevice_list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return addDevice_list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.added_device_list_item,
					null);
			TextView deviceTypeName = ViewHolder.get(convertView,
					R.id.tv_device_type);
			CircleImageView civImg = ViewHolder.get(convertView, R.id.civ_pic);
			LinearLayout ll = ViewHolder.get(convertView, R.id.list_item);
			deviceTypeName.setText(addDevice_list.get(position));

			civImg.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(AI6060HActivity.this)
							.setTitle(
									getString(R.string.main_aty_delete_device_title))
							.setMessage(
									getString(R.string.main_aty_delete_device_desc))
							.setNegativeButton(
									getString(R.string.main_aty_delete_device_cancle),
									null)
							.setPositiveButton(
									getString(R.string.main_aty_delete_device_confirm),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialogInterface,
												int m) {
											addDevice_list.remove(position);
											notifyDataSetChanged();
											saveObject(AI6060HActivity.this,
													physicalDeviceId,
													addDevice_list);

										}
									}).show();

					return false;
				}
			});

			bleScanedAddress = scanBle_tv.getText().toString().trim();
			txtString = deviceTypeName.getText().toString().trim();
			Log.e("**-*-*-txtString", txtString);
			if (txtString.startsWith(getResources().getString(R.string.thermometer))) {
				txtString = txtString.substring(4, 16);
				civImg.setImageDrawable(getResources().getDrawable(
						R.drawable.therm));
			} else if (txtString.startsWith(getResources().getString(R.string.emergency_light))) {
				txtString = txtString.substring(4, 16);
				civImg.setImageDrawable(getResources().getDrawable(
						R.drawable.car));
			} else if (txtString.startsWith(getResources().getString(R.string.led_light_band_for_two))
					|| txtString.startsWith(getResources().getString(R.string.led_light_band_for_four))) {
				txtString = txtString.substring(5, 18);
				civImg.setImageDrawable(getResources().getDrawable(
						R.drawable.light));
			}
			txtString = txtString.toUpperCase();
			Log.e("txtString", txtString);
			Log.e("bleScanedAddress", bleScanedAddress);

			if (MAC_for_4C_list.contains(txtString)) {
				civImg.setBorderColor(getResources().getColor(R.color.green));
				deviceTypeName.setTextColor(getResources().getColor(R.color.green));
				// online.setText(getString(R.string.had_connect));
				Log.e("position已连接", position + "");
				
			} else {
				if (bleScanedAddress.indexOf(txtString)!=-1) {
					civImg.setBorderColor(getResources()
							.getColor(R.color.green));
					deviceTypeName.setTextColor(getResources().getColor(R.color.green));
					// online.setText(getString(R.string.online));
					Log.e("position在线", position + "");
					Log.e("bleScanedAddress", bleScanedAddress);
				} else {
					civImg.setBorderColor(getResources()
							.getColor(R.color.grey));
					deviceTypeName.setTextColor(getResources().getColor(R.color.grey));
					// online.setText(getString(R.string.offline));
					Log.e("position不在线", position + "");
				}
			}

			addDeviceAdapter.notifyDataSetChanged();

			return convertView;
		}

	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 10:
			type = data.getStringExtra("deviceSelected");
			mac = data.getStringExtra("mac");
			aliasName = data.getStringExtra("aliasName");
			Log.e("-*-*-**-******", type + "*" + mac + "*" + aliasName);
			if (aliasName == null) {
				aliasName = "";
			}
			addDevice_list.add(type + "\n" + mac + "\n" + aliasName);
			saveObject(AI6060HActivity.this, physicalDeviceId, addDevice_list);
			addDeviceAdapter.notifyDataSetChanged();
			break;
		}
	}

	/* 用SharedPreferences保存/取出arrayList数组 */

	/**
	 * desc:保存对象
	 * 
	 * @param context
	 * @param key
	 * @param obj
	 *            要保存的对象，只能保存实现了serializable的对象 modified:
	 */
	public static void saveObject(Context context, String key, Object obj) {
		try {
			// 保存对象
			SharedPreferences.Editor sharedata = context.getSharedPreferences(
					"FILE_NAME", 0).edit();
			// 先将序列化结果写到byte缓存中，其实就分配一个内存空间
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bos);
			// 将对象序列化写入byte缓存
			os.writeObject(obj);
			// 将序列化的数据转为16进制保存
			String bytesToHexString = bytesToHexString(bos.toByteArray());
			// 保存该16进制数组
			sharedata.putString(key, bytesToHexString);
			sharedata.commit();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("", "保存obj失败");
		}
	}

	/**
	 * desc:将数组转为16进制
	 * 
	 * @param bArray
	 * @return modified:
	 */
	public static String bytesToHexString(byte[] bArray) {
		if (bArray == null) {
			return null;
		}
		if (bArray.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * desc:获取保存的Object对象
	 * 
	 * @param context
	 * @param key
	 * @return modified:
	 */
	public static Object readObject(Context context, String key) {
		try {
			SharedPreferences sharedata = context.getSharedPreferences(
					"FILE_NAME", 0);
			if (sharedata.contains(key)) {
				String string = sharedata.getString(key, "");
				if (TextUtils.isEmpty(string)) {
					return null;
				} else {
					// 将16进制的数据转为数组，准备反序列化
					byte[] stringToBytes = StringToBytes(string);
					ByteArrayInputStream bis = new ByteArrayInputStream(
							stringToBytes);
					ObjectInputStream is = new ObjectInputStream(bis);
					// 返回反序列化得到的对象
					Object readObject = is.readObject();
					return readObject;
				}
			}
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 所有异常返回null
		return null;

	}

	/**
	 * desc:将16进制的数据转为数组
	 * 
	 * @param data
	 * @return modified:
	 */
	public static byte[] StringToBytes(String data) {
		String hexString = data.toUpperCase().trim();
		if (hexString.length() % 2 != 0) {
			return null;
		}
		byte[] retData = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i++) {
			int int_ch; // 两位16进制数转化后的10进制数
			char hex_char1 = hexString.charAt(i); // //两位16进制数中的第一位(高位*16)
			int int_ch1;
			if (hex_char1 >= '0' && hex_char1 <= '9')
				int_ch1 = (hex_char1 - 48) * 16; // // 0 的Ascll - 48
			else if (hex_char1 >= 'A' && hex_char1 <= 'F')
				int_ch1 = (hex_char1 - 55) * 16; // // A 的Ascll - 65
			else
				return null;
			i++;
			char hex_char2 = hexString.charAt(i); // /两位16进制数中的第二位(低位)
			int int_ch2;
			if (hex_char2 >= '0' && hex_char2 <= '9')
				int_ch2 = (hex_char2 - 48); // // 0 的Ascll - 48
			else if (hex_char2 >= 'A' && hex_char2 <= 'F')
				int_ch2 = hex_char2 - 55; // // A 的Ascll - 65
			else
				return null;
			int_ch = int_ch1 + int_ch2;
			retData[i / 2] = (byte) int_ch;// 将转化后的数放入Byte里
		}
		return retData;
	}

	/**
	 * 剔除list中重复数据，并保持顺序
	 * 
	 * @param list
	 * @return
	 */
	public static List removeDuplicate(List list) {
		// HashSet h = new HashSet(list);
		// list.clear();
		// list.addAll(h);
		Set set = new HashSet();
		List newList = new ArrayList();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		list.clear();
		list.addAll(newList);
		return list;
	}

}
