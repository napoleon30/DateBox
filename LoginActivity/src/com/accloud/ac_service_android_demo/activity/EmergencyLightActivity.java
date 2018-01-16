package com.accloud.ac_service_android_demo.activity;

import com.accloud.ac_service_android_demo.R;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EmergencyLightActivity extends Activity implements OnClickListener{
	private Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency_light);
		PushAgent.getInstance(this).onAppStart();
		
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;

		default:
			break;
		}
		
	}
}
