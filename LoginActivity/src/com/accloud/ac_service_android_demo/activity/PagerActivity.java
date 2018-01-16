package com.accloud.ac_service_android_demo.activity;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.fragment.MyFragmentPagerAdapter;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PagerActivity extends FragmentActivity implements
		RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {
	public static final String TAG = "PagerActivity";

	// private MainCallBackLocalBroadCastReceiver
	// mainCallBackLocalBroadCastReceiver;
	// private LocalBroadcastManager lBM;

	Receiver<TopicData> receiver;// 订阅
	Intent mIntent;
	private LocalBroadcastManager mLBM;

	String payload = null;

	// UI Objects
	private TextView txt_topbar;
	private Button back;
	private RadioGroup rg_tab_bar;
	private RadioButton rb_channel;
	private RadioButton rb_message;
	private RadioButton rb_better;
	// private RadioButton rb_setting;
	private ViewPager vpager;

	String deviceName, deviceId, physicalDeviceId, subDomain;

	private MyFragmentPagerAdapter mAdapter;

	// 几个代表页面的常量
	public static final int PAGE_ONE = 0;
	public static final int PAGE_TWO = 1;
	public static final int PAGE_THREE = 2;
	public static final int PAGE_FOUR = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager);
		PushAgent.getInstance(this).onAppStart();

		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceId = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		Log.e(TAG, deviceId + "*" + physicalDeviceId + "*" + deviceName);

		mLBM = LocalBroadcastManager.getInstance(PagerActivity.this);

		mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
		bindViews();
		rb_channel.setChecked(true);

		// 注册一个监听连接状态的listener
		EMClient.getInstance()
				.addConnectionListener(new MyConnectionListener());

	}

	@Override
	protected void onResume() {
		super.onResume();
		subscribe("xinlian01", "topic_type", deviceId);// 订阅，可获取到返回值
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
				String deviceId_from_payload = onRece.getDeviceId();
				if (deviceId_from_payload.equals(deviceId)) {

					byte[] arraysPay = Base64.decode(pay[0], 0);
					String payload = ItonAdecimalConver.byte2hex(arraysPay)
							.replace(" ", "");
					Log.e("PagerActivity", payload);

					if (payload != null) {
						if (mIntent == null) {
							mIntent = new Intent();
						}
						mIntent.setAction("PagerPayload");
						Bundle bundle = new Bundle();
						bundle.putString("payload", payload);
						mIntent.putExtras(bundle);
						mLBM.sendBroadcast(mIntent);
					}


				}
			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
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
						if (NetUtils.hasNetwork(PagerActivity.this)) {
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

		if (!PagerActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(PagerActivity.this);
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
							Long userid = PreferencesUtils.getLong(PagerActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							dialog.dismiss();
							PagerActivity.this.finish();
							Intent intent = new Intent(PagerActivity.this,
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
	protected void onStart() {
		super.onStart();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getPhysicalDeviceId() {
		return physicalDeviceId;
	}

	public void setPhysicalDeviceId(String physicalDeviceId) {
		this.physicalDeviceId = physicalDeviceId;
	}

	private void bindViews() {
		txt_topbar = (TextView) findViewById(R.id.txt_topbar);
		txt_topbar.setText(deviceName);

		rg_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
		rb_channel = (RadioButton) findViewById(R.id.rb_channel);
		rb_message = (RadioButton) findViewById(R.id.rb_message);
		rb_better = (RadioButton) findViewById(R.id.rb_better);
		rg_tab_bar.setOnCheckedChangeListener(this);

		vpager = (ViewPager) findViewById(R.id.vpager);
		vpager.setAdapter(mAdapter);
		vpager.setCurrentItem(0);
		vpager.setOnPageChangeListener(this);

		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PagerActivity.this.finish();
			}
		});
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_channel:
			vpager.setCurrentItem(PAGE_ONE);
			break;
		case R.id.rb_message:
			vpager.setCurrentItem(PAGE_TWO);
			break;
		case R.id.rb_better:
			vpager.setCurrentItem(PAGE_THREE);
			break;
		// case R.id.rb_setting:
		// vpager.setCurrentItem(PAGE_FOUR);
		// break;
		}
	}

	// 重写ViewPager页面切换的处理方法
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
		if (state == 2) {
			switch (vpager.getCurrentItem()) {
			case PAGE_ONE:
				rb_channel.setChecked(true);
				break;
			case PAGE_TWO:
				rb_message.setChecked(true);
				break;
			case PAGE_THREE:
				rb_better.setChecked(true);
				break;
			// case PAGE_FOUR:
			// rb_setting.setChecked(true);
			// break;
			}
		}
	}

}
