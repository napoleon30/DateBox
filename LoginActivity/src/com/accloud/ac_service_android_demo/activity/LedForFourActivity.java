package com.accloud.ac_service_android_demo.activity;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendDataBle;
import com.accloud.ac_service_android_demo.utils.XColorPicker;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LedForFourActivity extends Activity implements OnClickListener {
	Receiver<TopicData> receiver;// 订阅
	SendDataBle sendDataBle;

	private Button back;
	String macAddress;
	String physicalDeviceId, deviceId;

	private ToggleButton power;
	private Button play;
	private Button pause;
	private Button red;
	private Button green;
	private Button blue;
	private Button white;
	XColorPicker color_picker;
	private SeekBar seekBar_light;
	private SeekBar seekBar_speed;

	private int lastProgress = 0;
	private int newProgress = 0;
	private int lastProgress_speed = 0;
	private int newProgress_speed = 0;

	String message1, message3, message5, message;
	private String subDomain;

	private Button scanDescribe;
	String handleForFour = "0016";// //////////////////四线灯带句柄?????????????

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_led_for_four);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		macAddress = intent.getStringExtra("macAddress");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		deviceId = intent.getStringExtra("deviceId");
		// sendData("0048", macAddress);//查找描述，为获取句柄

		receiver = new Receiver<TopicData>() {

			@Override
			public void onReceive(TopicData arg0) {
				Log.e("订阅onReceive_LED", arg0.getValue());
				String jsonData = arg0.getValue();
				OnReceive onRece = GsonUtil.parseJsonWithGson(jsonData,
						OnReceive.class);
				String[] pay = onRece.getPayload();
				byte[] arraysPay = Base64.decode(pay[0], 0);
				String payload = ItonAdecimalConver.byte2hex(arraysPay)
						.replace(" ", "");
				Log.e("tag", payload);

			}
		};
		AC.customDataMgr().registerDataReceiver(receiver);

		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		power = (ToggleButton) findViewById(R.id.cb_power);
		power.setOnCheckedChangeListener(new MyCheckedButton());

		play = (Button) findViewById(R.id.btn_play);
		play.setOnClickListener(this);
		pause = (Button) findViewById(R.id.btn_pause);
		pause.setOnClickListener(this);
		red = (Button) findViewById(R.id.btn_red);
		red.setOnClickListener(this);
		green = (Button) findViewById(R.id.btn_green);
		green.setOnClickListener(this);
		blue = (Button) findViewById(R.id.btn_blue);
		blue.setOnClickListener(this);
		white = (Button) findViewById(R.id.btn_white);
		white.setOnClickListener(this);

		color_picker = (XColorPicker) findViewById(R.id.color_picker);
		color_picker.setOnColorChangedListener(new MyColorListener());

		seekBar_light = (SeekBar) findViewById(R.id.seekbar);
		seekBar_light.setOnSeekBarChangeListener(new MySeekBarLight());
		seekBar_speed = (SeekBar) findViewById(R.id.seekbar_speed);
		seekBar_speed.setOnSeekBarChangeListener(new MySeekBarSpeed());

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		sendDataBle = new SendDataBle(subDomain, physicalDeviceId);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
			finish();
			break;
		case R.id.btn_play:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6603ff0099");
			break;
		case R.id.btn_pause:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6603000099");
			break;
		case R.id.btn_red:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6606000099");
			break;
		case R.id.btn_green:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6606010099");
			break;
		case R.id.btn_blue:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6606020099");
			break;
		case R.id.btn_white:
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6606030099");
			break;

		}
	}

	public class MyCheckedButton implements
			ToggleButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {// isChecked
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6601ff0099");
			} else {// unChecked
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6601000099");
			}
		}
	}

	public class MySeekBarLight implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.e("seekBar当前值", progress + "");
			Log.i("onProgressChanged=", "cc progress=" + progress
					+ " lastProgress=" + lastProgress + " newProgress="
					+ newProgress);
			if (progress > newProgress + 10 || progress < newProgress - 10) {
				newProgress = lastProgress;
				seekBar.setProgress(lastProgress);
				return;
			}
			newProgress = progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.e("seekBar", "正在滑动");
			Log.i("onStartTrackingTouch=", " lastProgress=" + lastProgress
					+ " newProgress=" + newProgress);

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.e("seekBar", "停止滑动");

			Log.i("onStopTrackingTouch=", " lastProgress=" + lastProgress
					+ " newProgress=" + newProgress);

			if (newProgress <= 5) {
				lastProgress = 0;
				newProgress = 0;
				seekBar.setProgress(0);
			} else if (newProgress > 5 && newProgress <= 15) {
				// 设置lastProgress 要放在setProgress之前，否则可能导致执行多次onProgressChanged
				// 改变了原值
				lastProgress = 10;
				newProgress = 10;
				seekBar.setProgress(10);
				// sendData("004a", "80eacaa010010016016604010099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604010099");
			} else if (newProgress > 15 && newProgress <= 25) {
				lastProgress = 20;
				newProgress = 20;
				seekBar.setProgress(20);
				// sendData("004a", "80eacaa010010016016604020099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604020099");
			} else if (newProgress > 25 && newProgress <= 35) {
				lastProgress = 30;
				newProgress = 30;
				seekBar.setProgress(30);
				// sendData("004a", "80eacaa010010016016604030099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604030099");
			} else if (newProgress > 35 && newProgress <= 45) {
				lastProgress = 40;
				newProgress = 40;
				seekBar.setProgress(40);
				// sendData("004a", "80eacaa010010016016604040099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604040099");
			} else if (newProgress > 45 && newProgress <= 55) {
				lastProgress = 50;
				newProgress = 50;
				seekBar.setProgress(50);
				// sendData("004a", "80eacaa010010016016604050099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604050099");
			} else if (newProgress > 55 && newProgress <= 65) {
				lastProgress = 60;
				newProgress = 60;
				seekBar.setProgress(60);
				// sendData("004a", "80eacaa010010016016604060099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604060099");
			} else if (newProgress > 65 && newProgress <= 75) {
				lastProgress = 70;
				newProgress = 70;
				seekBar.setProgress(70);
				// sendData("004a", "80eacaa010010016016604070099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604070099");
			} else if (newProgress > 75 && newProgress <= 85) {
				lastProgress = 80;
				newProgress = 80;
				seekBar.setProgress(80);
				// sendData("004a", "80eacaa010010016016604080099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604080099");
			} else if (newProgress > 85 && newProgress <= 95) {
				lastProgress = 90;
				newProgress = 90;
				seekBar.setProgress(90);
				// sendData("004a", "80eacaa010010016016604090099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604090099");
			} else if (newProgress > 95 && newProgress <= 100) {
				lastProgress = 100;
				newProgress = 100;
				seekBar.setProgress(100);
				// sendData("004a", "80eacaa010010016016604100099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6604100099");
			}
			Log.v("onStopTrackingTouch2=", " lastProgress=" + lastProgress
					+ " newProgress=" + newProgress);
		}
	}

	public class MySeekBarSpeed implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.e("seekBar当前值", progress + "");
			if (progress > newProgress_speed + 10
					|| progress < newProgress_speed - 10) {
				newProgress_speed = lastProgress_speed;
				seekBar.setProgress(lastProgress_speed);
				return;
			}
			newProgress_speed = progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.e("seekBar", "正在滑动");
			Log.i("onStartTrackingTouch=", " lastProgress=" + lastProgress
					+ " newProgress=" + newProgress);

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.e("seekBar", "停止滑动");
			if (newProgress_speed <= 5) {
				lastProgress_speed = 0;
				newProgress_speed = 0;
				seekBar.setProgress(0);
			} else if (newProgress_speed > 5 && newProgress_speed <= 15) {
				// 设置lastProgress 要放在setProgress之前，否则可能导致执行多次onProgressChanged
				// 改变了原值
				lastProgress_speed = 10;
				newProgress_speed = 10;
				seekBar.setProgress(10);
				// sendData("004a", "80eacaa010010016016605010099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605010099");
			} else if (newProgress_speed > 15 && newProgress_speed <= 25) {
				lastProgress_speed = 20;
				newProgress_speed = 20;
				seekBar.setProgress(20);
				// sendData("004a", "80eacaa010010016016605020099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605020099");
			} else if (newProgress_speed > 25 && newProgress_speed <= 35) {
				lastProgress_speed = 30;
				newProgress_speed = 30;
				seekBar.setProgress(30);
				// sendData("004a", "80eacaa010010016016605030099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605030099");
			} else if (newProgress_speed > 35 && newProgress_speed <= 45) {
				lastProgress_speed = 40;
				newProgress_speed = 40;
				seekBar.setProgress(40);
				// sendData("004a", "80eacaa010010016016605040099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605040099");
			} else if (newProgress_speed > 45 && newProgress_speed <= 55) {
				lastProgress_speed = 50;
				newProgress_speed = 50;
				seekBar.setProgress(50);
				// sendData("004a", "80eacaa010010016016605050099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605050099");
			} else if (newProgress_speed > 55 && newProgress_speed <= 65) {
				lastProgress_speed = 60;
				newProgress_speed = 60;
				seekBar.setProgress(60);
				// sendData("004a", "80eacaa010010016016605060099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605060099");
			} else if (newProgress_speed > 65 && newProgress_speed <= 75) {
				lastProgress_speed = 70;
				newProgress_speed = 70;
				seekBar.setProgress(70);
				// sendData("004a", "80eacaa010010016016605070099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605070099");
			} else if (newProgress_speed > 75 && newProgress_speed <= 80) {
				lastProgress_speed = 80;
				newProgress_speed = 80;
				seekBar.setProgress(80);
				// sendData("004a", "80eacaa010010016016605080099");
				sendDataBle.sendData("004a", macAddress + handleForFour + "01"
						+ "6605080099");
			}
		}
	}

	public class MyColorListener implements XColorPicker.OnColorChangedListener {
		@Override
		public void OnColorChanged(int color) {
			// 可拿到滑块选中的颜色
		}

		@Override
		public String setByteTwo(String byte2) {
			// Log.e("byte2----------", byte2);
			return byte2;
		}

		@Override
		public String setByteThree(String byte3) {
			// Log.e("byte3-------十进制", byte3);
			byte3 = Integer.toHexString(Integer.valueOf(byte3).intValue());
			// Log.e("byte3-------十六进制", byte3);
			return byte3;
		}

		@Override
		public void SendData(String byte2, String byte3) {
			Log.e("byte2,byte3", byte2 + "---" + byte3);
			// sendData("004a", "80eacaa010010016016602" + byte2 + byte3 +
			// "99");
			sendDataBle.sendData("004a", macAddress + handleForFour + "01" + "6602" + byte2
					+ byte3 + "99");
		}
	}



}
