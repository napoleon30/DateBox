package com.accloud.ac_service_android_demo.activity;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.utils.ColorPickView;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ColorActivity extends Activity implements OnClickListener {
	private ImageView txtColor;
	private ColorPickView myView;
	private Button sure;
	int textColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_color);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		textColor = intent.getIntExtra("textColor", -256);

		sure = (Button) findViewById(R.id.btn_sure);
		sure.setOnClickListener(this);
		myView = (ColorPickView) findViewById(R.id.color_picker_view);
		txtColor = (ImageView) findViewById(R.id.txt_color);
		txtColor.setBackgroundColor(textColor);
		myView.setOnColorChangedListener(new ColorPickView.OnColorChangedListener() {
			@Override
			public void onColorChange(int color) {
				txtColor.setBackgroundColor(color);
				textColor = color;
				Log.e("ColorActivity", textColor + "");
			}

		});
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_sure:
			Intent intent = new Intent();
			intent.putExtra("data_return", textColor);
			setResult(200, intent);
			finish();
			break;
		}
	}
	
	
}
