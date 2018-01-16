package com.accloud.ac_service_android_demo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.GridItem;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class EightToEightActivity extends Activity implements OnClickListener,
		MultiChoiceModeListener {
	public static final String TAG = "EightToEightActivity";
	public PictureAdapter adapter;
	private Button back, color, send;
	private ImageView clear;
	private String deviceName, deviceID, physicalDeviceId, subDomain;

	private GridView gridView;

	int textColor = -256;
	int Red, Green, Blue;

	private Map<Integer, Boolean> mSelectMapRed = new HashMap<Integer, Boolean>(); // 存放选择红色的数字的集合
	Map<Integer, Integer> mImgIds_red = new HashMap<Integer, Integer>(); // 存放红色的集合
	String select_red_ball = "";// 红色的数字
	SparseBooleanArray selectedposition_red;
	List<Integer> selectednum_red;

	String message, message1, message2, message3, message4, message5, message6,
			message7, message8;

	Receiver<TopicData> receiver;// 订阅

	// 图片ID数组
	private int[] images = new int[] { R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg, R.drawable.imageview_bg,
			R.drawable.imageview_bg };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eight_to_eight);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceID = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");

		Log.e(TAG, deviceID + "*" + physicalDeviceId + "*" + deviceName);

		// 显示33个红色区域的图片
		for (int i = 0; i < 64; i++) {
			mImgIds_red.put(i, R.drawable.imageview_bg_red);
		}

		textColor = PreferencesUtils.getInt(EightToEightActivity.this, "color");
		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(this);
		color = (Button) findViewById(R.id.btn_color);
		send = (Button) findViewById(R.id.btn_send);
		color.setOnClickListener(this);
		send.setOnClickListener(this);

		gridView = (GridView) findViewById(R.id.gridView);

		adapter = new PictureAdapter(images, this);

		gridView.setAdapter(adapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 除去默认的黄色背景

		gridView.setMultiChoiceModeListener(this);// 设置多选模式监听器
		adapter.notifyDataSetChanged();

		selectednum_red = new ArrayList<Integer>(); // 存放选中红色数字的list

		selectedposition_red = gridView.getCheckedItemPositions();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
		// AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		Log.e("onResume", textColor + "");

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
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_color:
			Intent intent = new Intent(EightToEightActivity.this,
					ColorActivity.class);
			intent.putExtra("textColor", textColor);
			startActivityForResult(intent, 100);
			break;
		case R.id.btn_send:

			Log.e("send", "send");
			select_red_ball = "";
			Red = (textColor & 0xff0000) >> 16;
			Green = (textColor & 0x00ff00) >> 8;
			Blue = (textColor & 0x0000ff);

			StringBuilder str = new StringBuilder(
					"0000000000000000000000000000000000000000000000000000000000000000");

			List<Integer> selectednum_red = new ArrayList<Integer>(); // 存放选中红色数字的list
			SparseBooleanArray selectedposition_red;
			selectedposition_red = gridView.getCheckedItemPositions();
			for (int i = 0; i < mImgIds_red.size(); i++) {
				if (selectedposition_red.get(i)) {
					selectednum_red.add(i);
					Log.e("send_i", i + "");
					str.replace(i, i + 1, "1");
					Log.e("str", str.substring(0, 8));
					message1 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(0, 8)));
					message2 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(8, 16)));
					message3 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(16, 24)));
					message4 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(24, 32)));
					message5 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(32, 40)));
					message6 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(40, 48)));
					message7 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(48, 56)));
					message8 = ItonAdecimalConver
							.algorismToHEXString(ItonAdecimalConver
									.binaryToAlgorism(str.substring(56, 64)));
				}
			}

			message = 66 + message1 + message2 + message3 + message4 + message5
					+ message6 + message7 + message8 + Red + Green + Blue + 99;
			Log.e("message", message);
			sendData(message);
			// for (int i = 0; i < selectednum_red.size(); i++) {
			// select_red_ball = select_red_ball + selectednum_red.get(i)
			// + " ";
			// Log.e("select_red_ball", select_red_ball);
			// }

			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 200:
			textColor = data.getIntExtra("data_return", -256);
			Log.e("EightToEightActivity", textColor + "");
			PreferencesUtils.putInt(EightToEightActivity.this, "color",
					textColor);
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

	/**
	 * 发送命令
	 */
	public void sendData(String message) {
		Log.e("message", message);
		byte[] midbytes = message.getBytes();
		// Log.e("发送的数据byte[]（十进制）", "*********" + Arrays.toString(midbytes));
		byte[] b = new byte[midbytes.length / 2];
		for (int i = 0; i < midbytes.length / 2; i++) {
			b[i] = uniteBytes(midbytes[i * 2], midbytes[i * 2 + 1]);
		}

		AC.bindMgr().sendToDeviceWithOption(subDomain, physicalDeviceId,
				getDeviceMsg(b), AC.ONLY_CLOUD,// ///////////////////////////////////////////////////
				new PayloadCallback<ACDeviceMsg>() {
					@Override
					public void success(ACDeviceMsg msg) {
						if (parseDeviceMsg(msg)) {
							String returnedValue = ItonAdecimalConver
									.byte2hex(msg.getContent());
							Log.e("callBack返回的消息十六进制转换", returnedValue);
						}
					}

					@Override
					public void error(ACException arg0) {
						// TODO Auto-generated method stub
					}

				});
		// Log.e("发送的消息", getDeviceMsg(b) + "");
	}

	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	private ACDeviceMsg getDeviceMsg(byte[] b) {
		// 注意：实际开发的时候请选择其中的一种消息格式即可
		switch (getFormatType()) {
		case ConfigurationActivity.BINARY:

			return new ACDeviceMsg(Config.LIGHT_MSGCODE, b);
		case ConfigurationActivity.JSON:
			JSONObject object = new JSONObject();
			try {
				object.put("switch", b);
			} catch (JSONException e) {
			}
			return new ACDeviceMsg(70, object.toString().getBytes());
		}
		return null;
	}

	private int getFormatType() {
		return PreferencesUtils.getInt(EightToEightActivity.this, "formatType",
				ConfigurationActivity.BINARY);
	}

	protected boolean parseDeviceMsg(ACDeviceMsg msg) {
		// 注意：实际开发的时候请选择其中的一种消息格式即可
		switch (getFormatType()) {
		case ConfigurationActivity.BINARY:
			byte[] bytes = msg.getContent();
			if (bytes != null)
				// return bytes[0] == 0x69 ? true : false;//
				return true;
		case ConfigurationActivity.JSON:
			try {
				JSONObject object = new JSONObject(new String(msg.getContent()));
				return object.optBoolean("result");
			} catch (Exception e) {
			}
		}
		return false;
	}

	class PictureAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<Picture> pictures;

		public PictureAdapter(int[] images, Context context) {
			super();
			pictures = new ArrayList<Picture>();
			inflater = LayoutInflater.from(context);
			for (int i = 0; i < images.length; i++) {
				Picture picture = new Picture(images[i]);
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
			GridItem item;
			if (convertView == null) {
				item = new GridItem(EightToEightActivity.this);

				// Log.e("222", textColor + "");
				item.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			} else {
				item = (GridItem) convertView;
			}
			item.setChecked(mSelectMapRed.get(position) == null ? false
					: mSelectMapRed.get(position));
			item.setBackgroudColor(textColor);

			return item;
		}

	}

	// class ViewHolder {
	// public ImageView title;
	// }
	class Picture {
		private int imageId;

		public Picture() {
			super();
		}

		public Picture(int imageId) {
			super();
			this.imageId = imageId;
		}

		public int getImageId() {
			return imageId;
		}

		public void setImageId(int imageId) {
			this.imageId = imageId;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub

		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		adapter.notifyDataSetChanged();
		Log.e("onDestroyActionMode", "onDestroyActionMode");
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		Log.e("position==", position + "");
		mSelectMapRed.put(position, checked);/* 放入选中的集合中 */

		mode.invalidate();
	}
}
