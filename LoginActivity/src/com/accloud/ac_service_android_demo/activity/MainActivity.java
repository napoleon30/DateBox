package com.accloud.ac_service_android_demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.controller.Light;
import com.accloud.ac_service_android_demo.utils.ActionSheetDialog;
import com.accloud.ac_service_android_demo.utils.ActionSheetDialog.OnSheetItemClickListener;
import com.accloud.ac_service_android_demo.utils.ActionSheetDialog.SheetItemColor;
import com.accloud.ac_service_android_demo.utils.DBOXException;
import com.accloud.ac_service_android_demo.utils.DensityUtils;
import com.accloud.ac_service_android_demo.utils.Pop;
import com.accloud.ac_service_android_demo.utils.ViewHolder;
import com.accloud.ac_service_android_demo.utils.XListView;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.*;
import com.accloud.utils.PreferencesUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.zbar.lib.CaptureActivity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.ManagedClientConnection;

/**
 * 主页面
 */
public class MainActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "MainActivity";
	private PopupWindow mPopupWindow;

	String deviceId, physicalDeviceId;
	String deviceNamString;

	private TextView menu;
	private TextView noDevice;
	private TextView addDevice;
	private XListView listView;
	private MyAdapter adapter;
	// 设备管理器
	ACBindMgr bindMgr;

	private String subDomain;

	Timer timer;
	boolean isRunning = false;

	private Button uds_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PushAgent.getInstance(this).onAppStart();

		menu = (TextView) findViewById(R.id.left_menu);
		noDevice = (TextView) findViewById(R.id.no_device);
		addDevice = (TextView) findViewById(R.id.right_add_device);
		listView = (XListView) findViewById(R.id.device_list);

		noDevice.setVisibility(View.GONE);
		menu.setOnClickListener(this);
		addDevice.setOnClickListener(this);

		// 获取设备管理器
		bindMgr = AC.bindMgr();

		adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
		listView.setXListViewListener(new XListView.IXListViewListener() {
			@Override
			public void onRefresh() {
				getDeviceList();
			}
		});

		// 检查设备是否有OTA升级
		checkOTAUpdates();

		// 注册一个监听连接状态的listener
		EMClient.getInstance()
				.addConnectionListener(new MyConnectionListener());

		uds_btn = (Button) findViewById(R.id.btn_uds);
		uds_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ACMsg req = new ACMsg();
				req.setName("text");
				// req.put("deviceId", deviceId);
				// req.put("startTime", 0);
				// req.put("endTime", System.currentTimeMillis());
				AC.sendToService(subDomain, "cloudService", 1, req,
						new PayloadCallback<ACMsg>() {
							@Override
							public void success(ACMsg resp) {
								// 发送成功并接收服务的响应消息
								Toast.makeText(MainActivity.this, "推送UDS成功",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void error(ACException e) {
								// 网络错误或其他，根据e.getErrorCode()做不同的提示或处理，此处一般为传递的参数或UDS云端问题，可到AbleCloud平台查看log日志
								Log.e("----------", e.toString());
							}
						});
			}
		});

	}

	// 实现ConnectionListener接口
	private class MyConnectionListener implements EMConnectionListener {
		@Override
		public void onConnected() {
		}

		@Override
		public void onDisconnected(final int error) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (error == EMError.USER_REMOVED) {
						// 显示帐号已经被移除
						Log.e(TAG, "显示帐号已经被移除");
					} else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
						// 显示帐号在其他设备登录
						Log.e(TAG, "显示帐号在其他设备登录");
						showDialog(getResources()
								.getString(
										R.string.the_account_is_logged_in_on_other_devices));

						Intent intent_broadcast = new Intent(); // Itent就是我们要发送的内容
						intent_broadcast
								.putExtra(
										"message_login_another_device",
										getResources()
												.getString(
														R.string.the_account_is_logged_in_on_other_devices));
						intent_broadcast.setAction("102"); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
						sendBroadcast(intent_broadcast); // 发送广播

					} else {
						if (NetUtils.hasNetwork(MainActivity.this)) {
							// 连接不到聊天服务器
							Log.e(TAG, "连接不到聊天服务器");
						} else {
							// 当前网络不可用，请检查网络设置
							Log.e(TAG, "当前网络不可用，请检查网络设置");
						}
					}
				}
			});
		}
	}

	public void showDialog(String string) {

		if (!MainActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setTitle(
					getResources().getString(R.string.offline_notification))
					.setMessage(string);
			builder.setPositiveButton(getResources()
					.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Log.e(TAG, "退出登录");
							Long userid = PreferencesUtils.getLong(
									MainActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							// userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid),
									"ablecloud", new UTrack.ICallBack() {
										@Override
										public void onMessage(
												boolean isSuccess,
												String message) {

										}
									});

							dialog.dismiss();
							MainActivity.this.finish();
							Intent intent = new Intent(MainActivity.this,
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
	protected void onResume() {
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		noDevice.setVisibility(View.GONE);
		if (!AC.accountMgr().isLogin()) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		} else {
			getDeviceList();
		}
	}

	@Override
	public void onClick(View view) {
		Intent intent = null;
		switch (view.getId()) {
		case R.id.left_menu:
			intent = new Intent(MainActivity.this, MenuActivity.class);
			startActivity(intent);
			break;
		case R.id.right_add_device:
			// intent = new Intent(MainActivity.this, AddDeviceActivity.class);
			// startActivity(intent);
			View popupView = getLayoutInflater().inflate(
					R.layout.mian_popupwindow, null);
			mPopupWindow = new PopupWindow(popupView, DensityUtils.dipTopx(MainActivity.this,
					113), DensityUtils.dipTopx(MainActivity.this, 100), true);
			TextView wifi_add = (TextView) popupView
					.findViewById(R.id.add_for_wifi);
			TextView scan_add = (TextView) popupView
					.findViewById(R.id.add_for_scan);
			wifi_add.setOnClickListener(this);
			scan_add.setOnClickListener(this);
			setBackgroundAlpha(0.5f);// 设置屏幕透明度
			mPopupWindow.setTouchable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable(
					getResources(), (Bitmap) null));

			mPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					// popupWindow隐藏时恢复屏幕正常透明度
					setBackgroundAlpha(1.0f);
				}
			});
			mPopupWindow.showAsDropDown(view, DensityUtils.dipTopx(MainActivity.this, -80),
					DensityUtils.dipTopx(MainActivity.this, 5));
			break;
		case R.id.add_for_wifi:
			intent = new Intent(MainActivity.this, AddDeviceActivity.class);
			startActivity(intent);
			mPopupWindow.dismiss();
			break;
		case R.id.add_for_scan:
			intent = new Intent(MainActivity.this, CaptureActivity.class);
//			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			startActivityForResult(intent, 1234);
			// startActivity(intent);
			mPopupWindow.dismiss();
			break;
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1234)
		switch (resultCode) {
		case 555:
			String shareCode = data.getStringExtra("scan_result");
			Log.e(TAG, "result :"+ shareCode);
			
			//普通用户通过扫分享码绑定设备
			bindMgr.bindDeviceWithShareCode(shareCode, new PayloadCallback<ACUserDevice>() {
			    @Override
			    public void success(ACUserDevice userDevice) {
			         //成功绑定管理员分享的设备
			    	Log.e(TAG,"SUCCESS ");
//			    	Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
			    	adapter.deviceList.add(userDevice);
			    	adapter.notifyDataSetChanged();
			    	getDeviceList();
			    }

			    @Override
			    public void error(ACException e) {
			         //网络错误或其他，根据e.getErrorCode()做不同的提示或处理
			    }
			});
			break;

		case 554:
			break;
		}
	}

	/**
	 * 设置添加屏幕的背景透明度
	 * 
	 * @param bgAlpha
	 *            屏幕透明度0.0-1.0 1表示完全不透明
	 */
	public void setBackgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.alpha = bgAlpha;
		this.getWindow().setAttributes(lp);
	}

	class MyAdapter extends BaseAdapter {
		public List<ACUserDevice> deviceList;
		private Context context;
		private Light light;

		public MyAdapter(Context context) {
			this.context = context;
			light = new Light(context);
			deviceList = new ArrayList<ACUserDevice>();
		}

		@Override
		public int getCount() {
			return deviceList.size();
		}

		@Override
		public Object getItem(int i) {
			return deviceList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(final int i, View view, ViewGroup viewGroup) {
			view = LayoutInflater.from(context).inflate(
					R.layout.adapter_list_device, null);

			LinearLayout rl = ViewHolder.get(view, R.id.list_item);
			ImageView pic = ViewHolder.get(view, R.id.iv_pic);
			final TextView deviceName = ViewHolder
					.get(view, R.id.tv_deviceName);
			TextView physical = ViewHolder.get(view, R.id.tv_physicalDeviceId);

			final ACUserDevice device = deviceList.get(i);

			physicalDeviceId = device.getPhysicalDeviceId();

			deviceId = device.getDeviceId() + "";

			physical.setText(physicalDeviceId);
			deviceName.setText(PreferencesUtils.getString(MainActivity.this,
					physicalDeviceId + "deviceName", device.getName()));
			switch (device.getStatus()) {

			case ACUserDevice.OFFLINE:
				deviceName.setTextColor(Color.GRAY);
				physical.setTextColor(Color.GRAY);

				// deviceName.setText(getString(R.string.main_aty_device_offline,
				// device.getName()));
				pic.setBackgroundResource(R.drawable.cloud_off);
				Log.e("LINE", "OFFLINE");

				break;
			case ACUserDevice.NETWORK_ONLINE:
				deviceName.setTextColor(Color.GREEN);
				physical.setTextColor(Color.GREEN);
				pic.setBackgroundResource(R.drawable.cloud_on);
				Log.e("LINE", "NETWORK_ONLINE");
				break;
			case ACUserDevice.LOCAL_ONLINE:
				deviceName.setTextColor(Color.GREEN);
				physical.setTextColor(Color.GREEN);
				pic.setBackgroundResource(R.drawable.cloud_on);
				Log.e("LINE", "LOCAL_ONLINE");
				break;
			case ACUserDevice.BOTH_ONLINE:
				deviceName.setTextColor(Color.GREEN);
				physical.setTextColor(Color.GREEN);
				pic.setBackgroundResource(R.drawable.cloud_on);
				Log.e("LINE", "BOTH_ONLINE");
				break;
			}

			rl.setOnClickListener(new View.OnClickListener() {// 短按跳转

				@Override
				public void onClick(View v) {
							Intent intent = new Intent(MainActivity.this,
									MCUChoiceTypeActivity.class);
							intent.putExtra("deviceId", device.getDeviceId()
									+ "");
							intent.putExtra("physicalDeviceId",
									device.getPhysicalDeviceId());
							intent.putExtra("deviceName", deviceName.getText()
									.toString().trim());
							Log.e("MainActivity", device.getDeviceId() + "/"
									+ device.getPhysicalDeviceId() + "/"
									+ device.getName());
							startActivity(intent);

				}
			});
			rl.setOnLongClickListener(new View.OnLongClickListener() {// 长按

				@Override
				public boolean onLongClick(View v) {
					showConfigurationDialog(device);
					return false;
				}
			});
			return view;
		}
	}

	public void showConfigurationDialog(final ACUserDevice device) {
		new ActionSheetDialog(MainActivity.this)
				.builder()
				.setCancelable(false)
				.setCanceledOnTouchOutside(false)
				.addSheetItem(getResources().getString(R.string.delete),
						SheetItemColor.Blue, new OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								unbindDevice(device);
							}
						})
				.addSheetItem(getResources().getString(R.string.set),
						SheetItemColor.Blue, new OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								showSetDialog(device.getDeviceId(),
										device.getPhysicalDeviceId());
							}
						})
				.addSheetItem(getResources().getString(R.string.share),
						SheetItemColor.Blue, new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {

								// 管理员获取分享码
								AC.bindMgr().fetchShareCode(
										device.getDeviceId(), 3600,
										new PayloadCallback<String>() {

											@Override
											public void success(String shareCode) {
												Log.e(TAG, "分享码 ：" + shareCode);
												createQRImage(shareCode);

											}

											@Override
											public void error(ACException e) {
												DBOXException.errorCode(
														MainActivity.this,
														e.getErrorCode());

											}
										});
							}
						}).show();
	}

	private void showShareCodeDialog(Bitmap bitmap) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		View view = LayoutInflater.from(MainActivity.this).inflate(
				R.layout.share_code_dialog, null);
		builder.setView(view);
		ImageView iv = (ImageView) view.findViewById(R.id.iv_share_code);
		iv.setImageBitmap(bitmap);
		builder.create().show();
	}

	/**
	 * 将字符串生成二维码，并用dialog展示
	 * 
	 * @param url
	 */
	private void createQRImage(String url) {
		int QR_WIDTH = 300;
		int QR_HEIGHT = 300;
		// 判断URL合法性
		if (url == null || "".equals(url) || url.length() < 1) {
			return;
		}
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		// 图像数据转换，使用了矩阵转换
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE,
					QR_WIDTH, QR_HEIGHT, hints);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
		// 下面这里按照二维码的算法，逐个生成二维码的图片，
		// 两个for循环是图片横列扫描的结果
		for (int y = 0; y < QR_HEIGHT; y++) {
			for (int x = 0; x < QR_WIDTH; x++) {
				if (bitMatrix.get(x, y)) {
					pixels[y * QR_WIDTH + x] = 0xff000000;
				} else {
					pixels[y * QR_WIDTH + x] = 0xffffffff;
				}
			}
		}
		// 生成二维码图片的格式，使用ARGB_8888
		Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
		showShareCodeDialog(bitmap);
	}

	protected void showSetDialog(final long deviceId,
			final String physicalDeviceId) {
		AlertDialog.Builder builder = new Builder(MainActivity.this);

		View view; // 使用view来接入方法写出的dialog，方便相关初始化
		LayoutInflater inflater; // 引用自定义dialog布局
		inflater = LayoutInflater.from(MainActivity.this);
		view = inflater.inflate(R.layout.dialog_set, null);
		builder.setView(view);

		final AlertDialog alert = builder.create();

		Button cancel = (Button) view.findViewById(R.id.cancel_btn);
		Button confirm = (Button) view.findViewById(R.id.confirm_btn);
		final EditText edit_name = (EditText) view.findViewById(R.id.edit_name);

		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e(TAG, edit_name.getText().toString().trim());
				Log.e(TAG, "点击确定");

				if (!edit_name.getText().toString().trim().equals("")) {
					bindMgr.changeName(subDomain, deviceId, edit_name.getText()
							.toString().trim(), new VoidCallback() {

						@Override
						public void error(ACException arg0) {
							Log.e(TAG, "名称修改失败---" + arg0.toString());
							Toast.makeText(
									MainActivity.this,
									getResources()
											.getString(
													R.string.the_device_name_change_fails_please_try_again),
									Toast.LENGTH_SHORT).show();
						}

						@Override
						public void success() {
							Log.e(TAG, "名称修改成功");
							PreferencesUtils.putString(MainActivity.this,
									physicalDeviceId + "deviceName", edit_name
											.getText().toString().trim());

							adapter.notifyDataSetChanged();
						}
					});
				}

				adapter.notifyDataSetChanged();

				alert.dismiss();

			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				alert.dismiss();
			}
		});
		alert.show();
	}

	// 获取设备列表
	public void getDeviceList() {
		bindMgr.listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
			@Override
			public void success(List<ACUserDevice> deviceList) {
				for (ACUserDevice device : deviceList) {
					device.getStatus();
				}
				if (deviceList.size() == 0) {
					noDevice.setVisibility(View.VISIBLE);
					listView.setPullRefreshEnable(false);
				} else {
					listView.setPullRefreshEnable(true);
				}
				adapter.deviceList = deviceList;
				adapter.notifyDataSetChanged();
				listView.stopRefresh();
				// 启动定时器,定时更新局域网状态
				startTimer();
			}

			@Override
			public void error(ACException e) {
				DBOXException.errorCode(MainActivity.this, e.getErrorCode());
			}
		});
	}

	// 删除设备
	public void unbindDevice(ACUserDevice device) {
		bindMgr.unbindDevice(subDomain, device.getDeviceId(),
				new VoidCallback() {
					@Override
					public void success() {
						Pop.popToast(
								MainActivity.this,
								getString(R.string.main_aty_delete_device_success));
						getDeviceList();
					}

					@Override
					public void error(ACException e) {
						// Pop.popToast(MainActivity.this, e.getErrorCode()
						// + "-->" + e.getMessage());
						DBOXException.errorCode(MainActivity.this,
								e.getErrorCode());
					}
				});
	}

	// 定时更新设备当前的局域网状态
	public void refreshDeviceStatus() {
		// 当设备掉线或网络环境不稳定导致获取局域网显示状态不准确时，需要手动刷新设备列表与局域网状态
		AC.findLocalDevice(AC.FIND_DEVICE_DEFAULT_TIMEOUT,
				new PayloadCallback<List<ACDeviceFind>>() {
					@Override
					public void success(List<ACDeviceFind> deviceFinds) {
						// 局域网状态是否发生改变,是否需要更新界面
						boolean isRefresh = false;
						// 遍历当前用户绑定的所有设备列表
						for (ACUserDevice device : adapter.deviceList) {
							// 判断当前设备是否局域网本地在线
							boolean isLocalOnline = false;
							// 遍历当前发现的局域网在线列表
							for (ACDeviceFind deviceFind : deviceFinds) {
								// 通过设备的物理Id进行匹配,若当前设备在发现的局域网列表中,则置为局域网在线
								if (device.getPhysicalDeviceId().equals(
										deviceFind.getPhysicalDeviceId())) {
									isLocalOnline = true;
								}
							}
							if (isLocalOnline) {
								// 当前设备由不在线更新为局域网在线
								if (device.getStatus() == ACUserDevice.OFFLINE) {
									device.setStatus(ACUserDevice.LOCAL_ONLINE);
									isRefresh = true;
									// 当前设备由云端在线更新为云端局域网同时在线
								} else if (device.getStatus() == ACUserDevice.NETWORK_ONLINE) {
									device.setStatus(ACUserDevice.BOTH_ONLINE);
									isRefresh = true;
								}
							} else {
								// 当前设备由局域网在线更新为不在线
								if (device.getStatus() == ACUserDevice.LOCAL_ONLINE) {
									device.setStatus(ACUserDevice.OFFLINE);
									AC.SEND_TO_LOCAL_DEVICE_DEFAULT_TIMEOUT = 6000;
									isRefresh = true;
									// 当前设备由云端局域网同时在线更新为云端在线
								} else if (device.getStatus() == ACUserDevice.BOTH_ONLINE) {
									device.setStatus(ACUserDevice.NETWORK_ONLINE);
									isRefresh = true;
								}
							}
						}
						// 局域网状态需要发生改变,更新列表界面
						if (isRefresh)
							adapter.notifyDataSetChanged();
					}

					@Override
					public void error(ACException e) {
						// 局域网状态是否发生改变,是否需要更新列表界面
						boolean isRefresh = false;
						for (ACUserDevice device : adapter.deviceList) {
							// 没有设备当前局域网在线,所以把所有当前显示局域网在线的设备状态重置
							if (device.getStatus() == ACUserDevice.LOCAL_ONLINE) {
								device.setStatus(ACUserDevice.OFFLINE);
								isRefresh = true;
							} else if (device.getStatus() == ACUserDevice.BOTH_ONLINE) {
								device.setStatus(ACUserDevice.NETWORK_ONLINE);
								isRefresh = true;
							}
						}
						// 局域网状态需要发生改变,更新列表界面
						if (isRefresh)
							adapter.notifyDataSetChanged();
					}
				});
	}

	// 启动定时器,定时更新局域网状态
	private void startTimer() {
		if (!isRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					refreshDeviceStatus();
				}
			}, 0, 3000);
			isRunning = true;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopTimer();
	}

	// 关闭局域网在线状态定时器
	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			isRunning = false;
		}
	}

	// 检查所有设备是否有OTA升级
	private void checkOTAUpdates() {
		// 获取所有设备列表
		AC.bindMgr().listDevices(new PayloadCallback<List<ACUserDevice>>() {
			@Override
			public void success(List<ACUserDevice> devices) {
				// 遍历所有绑定的设备
				for (final ACUserDevice device : devices) {
					ACOTACheckInfo checkInfo = new ACOTACheckInfo(device
							.getDeviceId(), 1);
					// 检查该设备是否有OTA升级
					AC.otaMgr().checkUpdate(subDomain, checkInfo,
							new PayloadCallback<ACOTAUpgradeInfo>() {
								@Override
								public void success(ACOTAUpgradeInfo upgradeInfo) {
									// 如果有OTA新版本,则弹框显示是否确认升级
									if (upgradeInfo.isUpdate())
										showOTADialog(device, upgradeInfo);
								}

								@Override
								public void error(ACException e) {
								}
							});
				}
			}

			@Override
			public void error(ACException e) {
			}
		});
	}

	// 如果有OTA新版本升级,则弹框显示
	private void showOTADialog(final ACUserDevice device,
			final ACOTAUpgradeInfo info) {
		new AlertDialog.Builder(this)
				.setTitle(R.string.main_aty_ota_upgrade_title)
				.setMessage(
						getString(R.string.main_aty_ota_upgrade_desc,
								device.getPhysicalDeviceId(), device.getName(),
								info.getTargetVersion(), info.getUpgradeLog()))
				.setPositiveButton(
						getString(R.string.main_aty_ota_upgrade_confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 确认升级
								AC.otaMgr().confirmUpdate(subDomain,
										device.getDeviceId(),
										info.getTargetVersion(), 1,
										new VoidCallback() {
											@Override
											public void success() {
												Pop.popToast(
														MainActivity.this,
														getString(R.string.main_aty_ota_upgrade_toast_hint));
											}

											@Override
											public void error(ACException e) {
												Pop.popToast(MainActivity.this,
														e.toString());
											}
										});
							}
						})
				.setNegativeButton(
						getString(R.string.main_aty_ota_upgrade_cancle), null)
				.create().show();
	}

}
