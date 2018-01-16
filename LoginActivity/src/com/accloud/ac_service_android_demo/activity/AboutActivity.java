package com.accloud.ac_service_android_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.accloud.ac_service_android_demo.R;
import com.umeng.message.PushAgent;

/**
 * 关于页面
 */
public class AboutActivity extends Activity {
	TextView back;
	TextView logo;
	TextView company;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		PushAgent.getInstance(this).onAppStart();
		
		back = (TextView) findViewById(R.id.about_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AboutActivity.this.finish();
			}
		});
		logo = (TextView) findViewById(R.id.about_logo);
		Animation animation = AnimationUtils.loadAnimation(
		AboutActivity.this, R.anim.logo_anim_scale);
		logo.startAnimation(animation);
		
		company = (TextView) findViewById(R.id.company_id);
		Animation anim = AnimationUtils.loadAnimation(AboutActivity.this,
				R.anim.company_anim_alpha);
		company.startAnimation(anim);

	}
}
