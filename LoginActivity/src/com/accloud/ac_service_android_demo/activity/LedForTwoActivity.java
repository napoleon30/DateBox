package com.accloud.ac_service_android_demo.activity;

import java.util.ArrayList;
import java.util.List;


import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.SendDataBle;
import com.accloud.cloudservice.AC;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.chat.EMClient;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

//句柄0016
public class LedForTwoActivity extends Activity implements OnClickListener {
	public static final String TAG = "LedForTwoActivity";
	
	private ReceiveBroadCast receiverBroadCast;
	SendDataBle sendDataBle;
	
	private GridView gridView;
	private Button back, stop;
	// 图片的文字标题
	private String[] titles = new String[] {getResources().getString(R.string.effect1), getResources().getString(R.string.effect2), getResources().getString(R.string.effect3), getResources().getString(R.string.effect4), getResources().getString(R.string.effect5),
			getResources().getString(R.string.effect6), getResources().getString(R.string.effect7), getResources().getString(R.string.effect8), getResources().getString(R.string.effect9), getResources().getString(R.string.effect10), getResources().getString(R.string.effect11), getResources().getString(R.string.effect12) };
	// 图片ID数组
	private int[] images = new int[] { R.drawable.circle_button,
			R.drawable.circle_button, R.drawable.circle_button,
			R.drawable.circle_button, R.drawable.circle_button,
			R.drawable.circle_button, R.drawable.circle_button,
			R.drawable.circle_button, R.drawable.circle_button,
			R.drawable.circle_button, R.drawable.circle_button,
			R.drawable.circle_button };

	String macAddress;
	String physicalDeviceId, deviceId;
	String message1, message3, message5, message;
	private String subDomain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_led_for_two);
		PushAgent.getInstance(this).onAppStart();

		Intent intent = getIntent();
		macAddress = intent.getStringExtra("macAddress");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		deviceId = intent.getStringExtra("deviceId");
		
		
		gridView = (GridView) findViewById(R.id.gridView);
		back = (Button) findViewById(R.id.two_return);
		back.setOnClickListener(this);
		stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener(this);
		PictureAdapter adapter = new PictureAdapter(titles, images, this);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new MyItemClickListener());
		
		//注册广播
		receiverBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("102");
		registerReceiver(receiverBroadCast, filter);
		
	}

	class MyItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position + 1) {
			case 1:
				sendDataBle.sendData("004a", macAddress + "001601" + "01");
				break;
			case 2:
				sendDataBle.sendData("004a", macAddress + "001601" + "02");
				break;
			case 3:
				sendDataBle.sendData("004a", macAddress + "001601" + "03");
				break;
			case 4:
				sendDataBle.sendData("004a", macAddress + "001601" + "04");
				break;
			case 5:
				sendDataBle.sendData("004a", macAddress + "001601" + "05");
				break;
			case 6:
				sendDataBle.sendData("004a", macAddress + "001601" + "06");
				break;
			case 7:
				sendDataBle.sendData("004a", macAddress + "001601" + "07");
				break;
			case 8:
				sendDataBle.sendData("004a", macAddress + "001601" + "08");
				break;
			case 9:
				sendDataBle.sendData("004a", macAddress + "001601" + "09");
				break;
			case 10:
				sendDataBle.sendData("004a", macAddress + "001601" + "10");
				break;
			case 11:
				sendDataBle.sendData("004a", macAddress + "001601" + "11");
				break;
			case 12:
				sendDataBle.sendData("004a", macAddress + "001601" + "12");
				break;
			}
		}

	}
	
	public class ReceiveBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 得到广播中得到的数据，并显示出来
			String broad_message = intent.getStringExtra("message_login_another_device");
			showDialog(broad_message);
			
		}

	}
	
	private void showDialog(String broad_message) {
		// TODO Auto-generated method stub
		if (!LedForTwoActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(LedForTwoActivity.this);
			builder.setTitle(getResources().getString(R.string.offline_notification)).setMessage(broad_message);
			builder.setPositiveButton(getResources().getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							sendDataBle.sendData("004a", macAddress + "001601" + "00");//停止
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
							
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Log.e(TAG, "退出登录");
							Long userid = PreferencesUtils.getLong(LedForTwoActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							
							dialog.dismiss();
							LedForTwoActivity.this.finish();
							Intent intent = new Intent(LedForTwoActivity.this,
									LoginActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
									| Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
			AlertDialog alert = builder.create();
			alert.setCanceledOnTouchOutside(false);//dialog点击空白不消失
			alert.setCancelable(false);//dialog点击返回键不消失
			alert.show();
		}
	}

	class PictureAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<Picture> pictures;

		public PictureAdapter(String[] titles, int[] images, Context context) {
			super();
			pictures = new ArrayList<Picture>();
			inflater = LayoutInflater.from(context);
			for (int i = 0; i < images.length; i++) {
				Picture picture = new Picture(titles[i], images[i]);
				pictures.add(picture);
			}
		}

		@Override
		public int getCount() {
			if (null != pictures) {
				return pictures.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return pictures.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gridview_item, null);
				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.textview_item);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.title.setText(pictures.get(position).getTitle());
			viewHolder.title.setBackgroundResource(pictures.get(position)
					.getImageId());
			return convertView;
		}

	}

	class ViewHolder {
		public TextView title;
	}

	class Picture {
		private String title;
		private int imageId;

		public Picture() {
			super();
		}

		public Picture(String title, int imageId) {
			super();
			this.title = title;
			this.imageId = imageId;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getImageId() {
			return imageId;
		}

		public void setImageId(int imageId) {
			this.imageId = imageId;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.two_return:
			sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
			finish();
			break;

		case R.id.stop:
			sendDataBle.sendData("004a", macAddress + "001601" + "00");
			break;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		sendDataBle = new SendDataBle(subDomain, physicalDeviceId);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiverBroadCast);
	}

}
