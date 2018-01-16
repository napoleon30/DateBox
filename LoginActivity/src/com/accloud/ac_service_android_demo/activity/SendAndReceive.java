package com.accloud.ac_service_android_demo.activity;

import java.math.BigDecimal;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SendAndReceive extends Activity implements OnClickListener{
	public static final String TAG="SendAndReceive";
	private String deviceName, deviceID, physicalDeviceId;
	String subDomain;
	private Button back,send,clear;
	private TextView title;
	private EditText et_send;
	private EditText et_rece;
	
	String send_message = null;
	
	SendData senddata;
	Receiver<TopicData> receiver;// 订阅

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_and_receive);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceID = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");

		Log.e(TAG, deviceID + "*" + physicalDeviceId + "*"
				+ deviceName);
		
		back = (Button) findViewById(R.id.btn_back);
		title = (TextView) findViewById(R.id.txt_topbar);
		back.setOnClickListener(this);
		title.setText(deviceName);
		
		et_send = (EditText) findViewById(R.id.et_send_content);
		et_rece = (EditText) findViewById(R.id.et_rece_content);
		send = (Button) findViewById(R.id.btn_send);
		clear = (Button) findViewById(R.id.btn_clear);
		send.setOnClickListener(this);
		clear.setOnClickListener(this);
		
	}
	
	@Override
	protected void onStart() {
		subDomain = PreferencesUtils.getString(MainApplication.getInstance(),
				"subDomain", Config.SUBDOMAIN);
		senddata = new SendData(subDomain, physicalDeviceId);
		subscribe("xinlian01", "topic_type", deviceID);// 订阅，可获取到返回值
		super.onStart();
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
				Log.e("订阅onReceive_PowerActivity", arg0.getValue());
				String jsonData = arg0.getValue();
				OnReceive onRece = GsonUtil.parseJsonWithGson(jsonData,
						OnReceive.class);
				String[] pay = onRece.getPayload();
				byte[] arraysPay = Base64.decode(pay[0], 0);
				String payload = ItonAdecimalConver.byte2hex(arraysPay)
						.replace(" ", "");
				Log.e("接收到的值_SendAndReceive", payload);
				et_rece.setText(payload);

			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
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
			finish();
			break;

		case R.id.btn_send:
			send_message = et_send.getText().toString().trim();
			senddata.sendData(send_message);
			break;
		case R.id.btn_clear:
			et_rece.setText("");
			break;
		}
		
	}
}
