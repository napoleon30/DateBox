package com.accloud.ac_service_android_demo.activity;

import java.text.DecimalFormat;

import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.AmountView;
import com.accloud.ac_service_android_demo.utils.AmountView.OnAmountChangeListener;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
import com.accloud.ac_service_android_demo.utils.SlideSwitch;
import com.accloud.ac_service_android_demo.utils.SlideSwitch.SlideListener;

import com.accloud.ac_service_android_demo.R;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class CarActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {
	public static final String TAG = "AI6060HActivity";
	private TextView title;
	private Button back;
	private ImageButton up, down, left, right, pause;

	private RadioGroup rg_speed;
	private RadioButton rbtn_speed1, rbtn_speed2, rbtn_speed3;

	SlideSwitch swit_tem;
	TextView tv_tem;
	int num_tem;
	EditText et_tem_num;
	AmountView amount_view_tem;
	boolean isOpen_tem = false;

	SlideSwitch swit_distance;
	TextView tv_distance;
	EditText et_distance_num;
	AmountView amount_view_distance;
	boolean isOpen_distance = false;

	SlideSwitch swit_led0;
	SlideSwitch swit_led1;
	SlideSwitch swit_led2;
	SlideSwitch swit_led3;

	private String deviceName, deviceID, physicalDeviceId;
	private String subDomain;

	Receiver<TopicData> receiver;// 订阅
	SendData senddata;

	String message1 = "6602";
	String message2 = "80";// 马达0,0x00为马达反转，0xff为马达正转，0x80为停止
	String message3 = "80";// 马达1,0x00为马达反转，0xff为马达正转，0x80为停止
	String message4 = "8080";
	String message5 = "02";// 速度档位调节,01/02/03
	String message6 = "00";// 温度、超声波、led启闭，默认00；温度上传f0，温度关闭0f；超声波距离打开80，超声波关闭08；Led0打开0a，关闭a0；Led1打开1a，关闭a1;Led2打开2a,关闭a2;Led3打开3a,关闭a3
	String message7 = "01";
	String message7_tem = "01";// 温度、超声波上传周期(温度、距离)，默认01
	String message7_distance = "01";// 温度、超声波上传周期(温度、距离)，默认01
	String message8 = "99";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_car);
		PushAgent.getInstance(this).onAppStart();

		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceID = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");

		Log.e("CarActivity", deviceID + "*" + physicalDeviceId + "*"
				+ deviceName);

		title = (TextView) findViewById(R.id.header_title);
		title.setText(deviceName);

		back = (Button) findViewById(R.id.btn_back);
		up = (ImageButton) findViewById(R.id.ibtn_up);
		down = (ImageButton) findViewById(R.id.ibtn_down);
		left = (ImageButton) findViewById(R.id.ibtn_left);
		right = (ImageButton) findViewById(R.id.ibtn_right);
		pause = (ImageButton) findViewById(R.id.ibtn_pause);

		rg_speed = (RadioGroup) findViewById(R.id.rg_speed);
		rbtn_speed1 = (RadioButton) findViewById(R.id.rbtn_speed1);
		rbtn_speed2 = (RadioButton) findViewById(R.id.rbtn_speed2);
		rbtn_speed3 = (RadioButton) findViewById(R.id.rbtn_speed3);

		initTem();
		initDistance();
		initLed();

		rg_speed.setOnCheckedChangeListener(this);

		back.setOnClickListener(this);
		up.setOnClickListener(this);
		down.setOnClickListener(this);
		left.setOnClickListener(this);
		right.setOnClickListener(this);
		down.setOnClickListener(this);
		pause.setOnClickListener(this);

	}

	private void initLed() {
		swit_led0 = (SlideSwitch) findViewById(R.id.swit_led0);
		swit_led1 = (SlideSwitch) findViewById(R.id.swit_led1);
		swit_led2 = (SlideSwitch) findViewById(R.id.swit_led2);
		swit_led3 = (SlideSwitch) findViewById(R.id.swit_led3);
		swit_led0.setState(false);
		swit_led1.setState(false);
		swit_led2.setState(false);
		swit_led3.setState(false);
		swit_led0.setSlideListener(new SlideListener() {

			@Override
			public void open() {
				message6 = "0a";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
			}

			@Override
			public void close() {
				message6 = "a0";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
			}
		});
		swit_led1.setSlideListener(new SlideListener() {

			@Override
			public void open() {
				message6 = "1a";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);

			}

			@Override
			public void close() {
				message6 = "a1";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);

			}
		});
		swit_led2.setSlideListener(new SlideListener() {
			
			@Override
			public void open() {
				message6 = "2a";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
				
			}
			
			@Override
			public void close() {
				message6 = "a2";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
				
			}
		});
		swit_led3.setSlideListener(new SlideListener() {
			
			@Override
			public void open() {
				message6 = "3a";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
				
			}
			
			@Override
			public void close() {
				message6 = "a3";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7 + message8);
				
			}
		});

	}

	private void initDistance() {
		swit_distance = (SlideSwitch) findViewById(R.id.swit_distance);
		tv_distance = (TextView) findViewById(R.id.tv_show_distance);
		tv_distance.setVisibility(View.GONE);
		amount_view_distance = (AmountView) findViewById(R.id.amount_view_distance);
		amount_view_distance.setGoods_storage(10);
		swit_distance.setState(false);
		swit_distance.setSlideListener(new SlideListener() {

			@Override
			public void open() {
				isOpen_distance = true;
				message6 = "80";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7_distance + message8);
				tv_distance.setVisibility(View.VISIBLE);
			}

			@Override
			public void close() {
				isOpen_distance = false;
				message6 = "08";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7_distance + message8);
				tv_distance.setVisibility(View.GONE);
			}
		});

		amount_view_distance
				.setOnAmountChangeListener(new OnAmountChangeListener() {

					@Override
					public void onAmountChange(View view, int amount) {
						message7_distance = ItonAdecimalConver.algorismToHEXString(
								amount, 2);
						Log.e("message7_distance", message7_distance);
						if (isOpen_distance == true) {
							senddata.sendData(message1 + message2 + message3
									+ message4 + message5 + message6 + message7_distance
									+ message8);
						}
					}
				});

	}

	private void initTem() {
		swit_tem = (SlideSwitch) findViewById(R.id.swit_tem);
		tv_tem = (TextView) findViewById(R.id.tv_show_tem);
		tv_tem.setVisibility(View.GONE);
		amount_view_tem = (AmountView) findViewById(R.id.amount_view_tem);
		amount_view_tem.setGoods_storage(10);
		swit_tem.setState(false);
		swit_tem.setSlideListener(new SlideListener() {

			@Override
			public void open() {
				isOpen_tem = true;
				message6 = "f0";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7_tem + message8);
				tv_tem.setVisibility(View.VISIBLE);
			}

			@Override
			public void close() {
				isOpen_tem = false;
				message6 = "0f";
				senddata.sendData(message1 + message2 + message3 + message4
						+ message5 + message6 + message7_tem + message8);
				tv_tem.setVisibility(View.GONE);
			}
		});
		amount_view_tem.setOnAmountChangeListener(new OnAmountChangeListener() {

			@Override
			public void onAmountChange(View view, int amount) {
				message7_tem = ItonAdecimalConver.algorismToHEXString(amount, 2);
				Log.e("message7_tem", message7_tem);
				if (isOpen_tem == true) {
					senddata.sendData(message1 + message2 + message3 + message4
							+ message5 + message6 + message7_tem + message8);
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		subscribe("xinlian01", "topic_type", deviceID);// 订阅，可获取到返回值

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
				byte[] arraysPay = Base64.decode(pay[0], 0);
				String payload = ItonAdecimalConver.byte2hex(arraysPay)
						.replace(" ", "");
				Log.e("接收到的值", payload);
				if (!TextUtils.isEmpty(payload)&& payload.length()>=10) {
					
					String[] pays = payload.split("9966");
					for (int i = 0; i < pays.length; i++) {
						if (!pays[i].startsWith("66")) {
							pays[i] = "66"+pays[i];
						}
						String type = pays[i].substring(2, 4);
						if ("c2".equals(type.toLowerCase())) {
							String c2_rece = pays[i].substring(4,8);
							Log.e("c2_rece", c2_rece);
							float f_tem = ItonAdecimalConver
									.hexStringToAlgorism(c2_rece)/ 10;
							DecimalFormat df = new DecimalFormat("0.0");// 格式化小数，不足的补0
							String filesize = df.format(f_tem);// 返回的是String类型的
							tv_tem.setText(filesize + "℃");
						}
						if ("c3".equals(type.toLowerCase())) {
							String c3_rece = pays[i].substring(4,8);
							Log.e("c3_rece", c3_rece);
							float f_distance = ItonAdecimalConver
									.hexStringToAlgorism(c3_rece)/10;
							Log.e("float_f", f_distance+"");
							DecimalFormat df = new DecimalFormat("0.0");// 格式化小数，不足的补0
							String filesize = df.format(f_distance);// 返回的是String类型的
							Log.e("filesize", filesize);
							tv_distance.setText(filesize + "cm");
						}
					}
				}
			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		senddata = new SendData(subDomain, physicalDeviceId);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_back:
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  message6 + message7 + message8);//小车停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "0f" + message7 + message8);//温度停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "08" + message7 + message8);//超声波停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a0" + message7 + message8);//led0停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a1" + message7 + message8);//led1停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a2" + message7 + message8);//led2停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a3" + message7 + message8);//led3停止
			
			finish();
			break;
		case R.id.ibtn_up:
			message2 = "ff";
			message3 = "ff";
			senddata.sendData(message1 + message2 + message3 + message4
					+ message5 + "00" + message7 + message8);
			break;
		case R.id.ibtn_down:
			message2 = "00";
			message3 = "00";
			senddata.sendData(message1 + message2 + message3 + message4
					+ message5 + "00" + message7 + message8);
			break;
		case R.id.ibtn_left:
			message2 = "00";
			message3 = "ff";
			senddata.sendData(message1 + message2 + message3 + message4
					+ message5 +  "00"  + message7 + message8);
			break;
		case R.id.ibtn_right:
			message2 = "ff";
			message3 = "00";
			senddata.sendData(message1 + message2 + message3 + message4
					+ message5 +  "00"  + message7 + message8);
			break;
		case R.id.ibtn_pause:
			message2 = "80";
			message3 = "80";
			senddata.sendData(message1 + message2 + message3 + message4
					+ message5 +  "00"  + message7 + message8);
			break;

		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rbtn_speed1:
			message5 = "01";
			break;
		case R.id.rbtn_speed2:
			message5 = "02";
			break;
		case R.id.rbtn_speed3:
			message5 = "03";
			break;

		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  message6 + message7 + message8);//小车停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "0f" + message7 + message8);//温度停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "08" + message7 + message8);//超声波停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a0" + message7 + message8);//led0停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a1" + message7 + message8);//led1停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a2" + message7 + message8);//led2停止
			senddata.sendData(message1 + "80" + "80" + message4
					+ message5 +  "a3" + message7 + message8);//led3停止
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
