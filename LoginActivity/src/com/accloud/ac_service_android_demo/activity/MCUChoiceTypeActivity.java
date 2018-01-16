package com.accloud.ac_service_android_demo.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.ReceiveBroadCast;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.utils.GalleryViewPager;
import com.accloud.ac_service_android_demo.utils.MyPageradapter;
import com.accloud.ac_service_android_demo.utils.ScalePageTransformer;
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;

public class MCUChoiceTypeActivity extends Activity implements OnClickListener {
	public static final String TAG = MCUChoiceTypeActivity.class
			.getSimpleName();
	private ReceiveBroadCast receiveBroadCast;

	private Button back;
	String deviceName, deviceID, physicalDeviceID;
	private TextView title;

	private GalleryViewPager mViewPager;
	private SimpleAdapter mPagerAdapter;
	List<Integer> list;

	LinearLayout ll_root;
	TextView deviceDescribe;
	ImageView deviceImg;
	int bridge = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mcucchoice_type);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceID = intent.getStringExtra("deviceId");
		physicalDeviceID = intent.getStringExtra("physicalDeviceId");
		Log.e("MCUChoiceTypeActivity", deviceID + "*" + physicalDeviceID + "*"
				+ deviceName);

		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.txt_topbar);
		title.setText(deviceName);

		ll_root = (LinearLayout) findViewById(R.id.ll_foot);
		ll_root.setOnClickListener(this);
		deviceDescribe = (TextView) findViewById(R.id.tv_device_descrile);
		deviceImg = (ImageView) findViewById(R.id.iv_device_img);
		deviceDescribe.setText(getResources().getString(R.string.car_function));
		deviceImg.setBackgroundResource(R.drawable.car_pic2);

		mViewPager = (GalleryViewPager) findViewById(R.id.viewpager);
		mViewPager.setPageTransformer(true, new ScalePageTransformer());
		findViewById(R.id.root).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mViewPager.dispatchTouchEvent(event);
			}
		});

		mPagerAdapter = new SimpleAdapter(this);
		mViewPager.setAdapter(mPagerAdapter);
		initData();

		// 使viewpager能够循环切换
		mViewPager
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					int currentPosition;

					@Override
					public void onPageScrolled(int position,
							float positionOffset, int positionOffsetPixels) {

					}

					@Override
					public void onPageSelected(int position) {
						currentPosition = position;

					}

					@Override
					public void onPageScrollStateChanged(int state) {
						// ViewPager.SCROLL_STATE_IDLE
						// 标识的状态是当前页面完全展现，并且没有动画正在进行中，如果不
						// 是此状态下执行 setCurrentItem 方法回在首位替换的时候会出现跳动！
						if (state != ViewPager.SCROLL_STATE_IDLE)
							return;
						Log.e(TAG, currentPosition + "*");

						// 当视图在第一个时，将页面号设置为图片的最后一张。
						if (currentPosition == 0) {
							mViewPager.setCurrentItem(list.size() - 2, false);

						} else if (currentPosition == list.size() - 1) {
							// 当视图在最后一个时,将页面号设置为图片的第一张。
							mViewPager.setCurrentItem(1, false);
						}

						if (currentPosition == 0 || currentPosition == 4) {
							bridge = 0;
							deviceDescribe.setText(getResources().getString(R.string.car_function));
							deviceImg.setBackgroundResource(R.drawable.car_pic2);
						} else if (currentPosition == 1 || currentPosition==5) {
							bridge = 1;
							deviceDescribe
									.setText(getResources().getString(R.string.pager_function));
							deviceImg
									.setBackgroundResource(R.drawable.trans_pic2);
						} else if (currentPosition == 2 ) {
							bridge = 2;
							deviceDescribe.setText(getResources().getString(R.string.passthrough_function));
							deviceImg
									.setBackgroundResource(R.drawable.a6060_pic2);
						}else if (currentPosition == 3) {
							bridge = 3;
							deviceDescribe.setText("iPower\n电流功率");
							deviceImg
							.setBackgroundResource(R.drawable.elec);
						}
					}
				});

		// 注册广播接收
		receiveBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("102"); // 只有持有相同的action的接受者才能接收此广播
		registerReceiver(receiveBroadCast, filter);

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
		if (!MCUChoiceTypeActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(
					MCUChoiceTypeActivity.this);
			builder.setTitle(getResources().getString(R.string.offline_notification)).setMessage(broad_message);
			builder.setPositiveButton(getResources().getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Log.e(TAG, "退出登录");
							Long userid = PreferencesUtils.getLong(MCUChoiceTypeActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							dialog.dismiss();
							MCUChoiceTypeActivity.this.finish();
							Intent intent = new Intent(
									MCUChoiceTypeActivity.this,
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

	private void initData() {
		list = new ArrayList<Integer>();
		list.add(R.drawable.car_pic2);
		list.add(R.drawable.trans_pic2);
		list.add(R.drawable.a6060_pic2);
		list.add(R.drawable.elec);
		list.add(R.drawable.car_pic2);
		list.add(R.drawable.trans_pic2);

		// 设置OffscreenPageLimit
		mViewPager.setOffscreenPageLimit(Math.min(list.size(), 5));
		mPagerAdapter.addAll(list);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.ll_foot:
			Intent intent;
			switch (bridge) {
			case 0:
				intent = new Intent(MCUChoiceTypeActivity.this,
						CarActivity.class);
				intent.putExtra("deviceId", deviceID);
				intent.putExtra("deviceName", deviceName);
				intent.putExtra("physicalDeviceId", physicalDeviceID);
				startActivity(intent);
				break;

			case 1:
				intent = new Intent(MCUChoiceTypeActivity.this,
						PagerActivity.class);
				intent.putExtra("deviceId", deviceID);
				intent.putExtra("deviceName", deviceName);
				intent.putExtra("physicalDeviceId", physicalDeviceID);
				startActivity(intent);
				break;
			case 2:
				intent = new Intent(MCUChoiceTypeActivity.this,
						SendAndReceive.class);
				intent.putExtra("deviceId", deviceID);
				intent.putExtra("deviceName", deviceName);
				intent.putExtra("physicalDeviceId", physicalDeviceID);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(MCUChoiceTypeActivity.this,PowerActivity.class);
				intent.putExtra("deviceId", deviceID);
				intent.putExtra("deviceName", deviceName);
				intent.putExtra("physicalDeviceId", physicalDeviceID);
				startActivity(intent);
			}
			break;
		}
	}

	public class SimpleAdapter extends MyPageradapter {

		private final List<Integer> mList;
		private final Context mContext;

		public SimpleAdapter(Context context) {
			mList = new ArrayList<Integer>();
			mContext = context;
		}

		public void addAll(List<Integer> list) {
			mList.addAll(list);
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup container) {
			ImageView imageView = null;
			if (convertView == null) {
				imageView = new ImageView(mContext);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setTag(position);
			imageView.setImageResource(mList.get(position));

			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i("wwwwwwwwss", mViewPager.getCurrentItem() + "------"
							+ position);
					if ((mViewPager.getCurrentItem()) == position) {
						// Toast.makeText(mContext, "点击的位置是:::"+position,
						// Toast.LENGTH_SHORT).show();
					}

				}
			});
			// }

			return imageView;
		}

		@Override
		public int getCount() {
			return mList.size();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiveBroadCast);
	}

}
