package com.accloud.ac_service_android_demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.cloudservice.AC;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.chat.EMClient;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

/**
 * 个人中心页面
 * <p/>
 */
public class MenuActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "MenuActivity";

	private ReceiveBroadCast receiveBroadCast;

	private RelativeLayout about;
	private RelativeLayout logout;
	private RelativeLayout config;
	private TextView back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		PushAgent.getInstance(this).onAppStart();
		
		about = (RelativeLayout) findViewById(R.id.about);
		logout = (RelativeLayout) findViewById(R.id.logout);
		config = (RelativeLayout) findViewById(R.id.config_detail);
		back = (TextView) findViewById(R.id.menu_back);
		config.setOnClickListener(this);
		back.setOnClickListener(this);
		about.setOnClickListener(this);
		logout.setOnClickListener(this);

		receiveBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("102");
		registerReceiver(receiveBroadCast, filter);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.config_detail:
			startActivity(new Intent(this, ConfigDetailActivity.class));
			break;
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.menu_back:
			finish();
			break;
		case R.id.logout:
			// 退出登录
			EMClient.getInstance().logout(true);
			Log.e(TAG, "环信退出登录");
			AC.accountMgr().logout();
			Log.e(TAG, "ablecloud退出登录");
			Long userid = PreferencesUtils.getLong(MenuActivity.this, "userId");
			PushAgent mPushAgent = MainApplication.push();
			//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
			mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
			    @Override
			    public void onMessage(boolean isSuccess, String message) {

			    }
			});
			finish();
			break;
		}
	}

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
		// TODO Auto-generated method stub
		if (!MenuActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(MenuActivity.this);
			builder.setTitle(getResources().getString(R.string.offline_notification)).setMessage(broad_message);
			builder.setPositiveButton(getResources().getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Long userid = PreferencesUtils.getLong(MenuActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							
							Log.e(TAG, "退出登录");
							dialog.dismiss();
							MenuActivity.this.finish();
							Intent intent = new Intent(MenuActivity.this,
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiveBroadCast);
	}
}
