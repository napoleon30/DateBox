package com.accloud.ac_service_android_demo.activity;

import java.util.ArrayList;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.pickerview.OptionsPickerView;
import com.accloud.ac_service_android_demo.pickerview.Util;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DeviceNameActivity extends Activity implements OnClickListener {
	private static final String TAG = "DeviceNameActivity";
	
	private EditText mac_address;
	private EditText alias;
	private EditText deviceChoicedType;
	private Button Ok, Cancel;
	String deviceSelected;
	private ArrayList<String> mList = new ArrayList<String>();
	OptionsPickerView pvOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_name);
		PushAgent.getInstance(this).onAppStart();
		
		mac_address = (EditText) findViewById(R.id.et_mac);
		alias = (EditText) findViewById(R.id.et_alias);
		deviceChoicedType = (EditText) findViewById(R.id.et_device);
		Ok = (Button) findViewById(R.id.btn_ok);
		Cancel = (Button) findViewById(R.id.btn_cancel);
		Ok.setOnClickListener(this);
		Cancel.setOnClickListener(this);
		deviceChoicedType.setOnClickListener(this);

		mList.add(getResources().getString(R.string.thermometer));
		mList.add(getResources().getString(R.string.led_light_band_for_two));
		mList.add(getResources().getString(R.string.led_light_band_for_four));
		mList.add(getResources().getString(R.string.emergency_light));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ok:
			if (mac_address.getText().toString() != null
					&& mac_address.getText().toString().length() == 12 && !TextUtils.isEmpty(deviceSelected)) {

				Intent mIntent = new Intent();
				mIntent.putExtra("deviceSelected", deviceSelected);
				mIntent.putExtra("mac", mac_address.getText().toString());
				mIntent.putExtra("aliasName", alias.getText().toString());
				this.setResult(10, mIntent);
				this.finish();
			} else {
				Toast.makeText(DeviceNameActivity.this,
						getString(R.string.check_device_number),
						Toast.LENGTH_SHORT).show();
			}

			break;

		case R.id.btn_cancel:
			this.finish();
			break;
		case R.id.et_device:
			Util.alertBottomWheelOption(DeviceNameActivity.this, mList,
					new Util.OnWheelViewClick() {
						@Override
						public void onClick(View view, int postion) {
							deviceSelected = mList.get(postion).toString();
							Log.e(TAG, deviceSelected);
							deviceChoicedType.setText(deviceSelected);
							
							
						}
					});
			break;
		}

	}
}
