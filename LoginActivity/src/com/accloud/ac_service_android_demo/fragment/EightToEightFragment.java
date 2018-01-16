package com.accloud.ac_service_android_demo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.ColorActivity;
import com.accloud.ac_service_android_demo.activity.PagerActivity;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.GridItem;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
import com.accloud.service.Receiver;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AbsListView.MultiChoiceModeListener;

public class EightToEightFragment extends Fragment implements OnClickListener,
		MultiChoiceModeListener {
	public static final String TAG = "EightToEightFragment";

	private LocalBroadcastManager mLBM;
	private DetailBroadCastReceiver detailBroadCastReceiver;

	public PictureAdapter adapter;
	private Button color, send;
	private Button clear;
	private String deviceName, deviceID, physicalDeviceId, subDomain;

	private GridView gridView;
	SendData senddata;

	int textColor = -256;
	int Red, Green, Blue;

	private Map<Integer, Boolean> mSelectMapRed = new HashMap<Integer, Boolean>(); // 存放选择红球的数字的集合
	Map<Integer, Integer> mImgIds_red = new HashMap<Integer, Integer>(); // 存放红球的集合
	String select_red_ball = "";// 红球的数字
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

	public EightToEightFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fg_eight_to_eight, container,
				false);
		Log.e(TAG, ((PagerActivity) getActivity()).getDeviceId() + "*"
				+ ((PagerActivity) getActivity()).getPhysicalDeviceId() + "*"
				+ ((PagerActivity) getActivity()).getDeviceName());
		deviceName = ((PagerActivity) getActivity()).getDeviceName();
		deviceID = ((PagerActivity) getActivity()).getDeviceId();
		physicalDeviceId = ((PagerActivity) getActivity())
				.getPhysicalDeviceId();
		
		senddata = new SendData(subDomain,physicalDeviceId);
		
		// 显示63个红色区域的图片
		for (int i = 0; i < 64; i++) {
			mImgIds_red.put(i, R.drawable.imageview_bg_red);
		}
		textColor = PreferencesUtils.getInt(MainApplication.getInstance(),
				"color");

		color = (Button) view.findViewById(R.id.btn_color);
		send = (Button) view.findViewById(R.id.btn_send);
		clear = (Button) view.findViewById(R.id.btn_clear);
		color.setOnClickListener(this);
		send.setOnClickListener(this);
		clear.setOnClickListener(this);

		gridView = (GridView) view.findViewById(R.id.gridView);
		adapter = new PictureAdapter(images, MainApplication.getInstance());

		gridView.setAdapter(adapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 除去默认的黄色背景

		gridView.setMultiChoiceModeListener(this);// 设置多选模式监听器
		adapter.notifyDataSetChanged();

		selectednum_red = new ArrayList<Integer>(); // 存放选中红色数字的list

		selectedposition_red = gridView.getCheckedItemPositions();

		return view;
	}

	/**
	 * 广播接收处理函数
	 * 
	 * @author admin
	 * 
	 */
	public class DetailBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String str = (String) intent.getExtras().get("payload");
			Log.e("8*8", str);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		subDomain = PreferencesUtils.getString(MainApplication.getInstance(),
				"subDomain", Config.SUBDOMAIN);
		InitDetailBroadCastReceiver();
	}

	/**
	 * 初始化Detail广播LocalBroadcastReceiver
	 */
	private void InitDetailBroadCastReceiver() {
		// TODO Auto-generated method stub
		mLBM = LocalBroadcastManager.getInstance(getActivity());
		detailBroadCastReceiver = new DetailBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("PagerPayload");
		mLBM.registerReceiver(detailBroadCastReceiver, intentFilter);
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLBM.unregisterReceiver(detailBroadCastReceiver);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		adapter.notifyDataSetChanged();
		Log.e("onDestroyActionMode", "onDestroyActionMode");
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		Log.e("position==", position + "");
		mSelectMapRed.put(position, checked);/* 放入选中的集合中 */

		mode.invalidate();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_color:
			Intent intent = new Intent(MainApplication.getInstance(),
					ColorActivity.class);
			intent.putExtra("textColor", textColor);
			startActivityForResult(intent, 100);
			break;

		case R.id.btn_send:

			select_red_ball = "";

			Red = (textColor >> 16) & 0xFF;
			Green = (textColor >> 8) & 0xFF;
			Blue = textColor & 0xFF;

			StringBuilder str = new StringBuilder(
					"0000000000000000000000000000000000000000000000000000000000000000");

			selectednum_red = new ArrayList<Integer>(); // 存放选中红色数字的list

				for (int i = 0; i < mImgIds_red.size(); i++) {
					if (selectedposition_red.get(i)) {
						selectednum_red.add(i);
						Log.e("send_i", i + "");
						str.replace(i, i + 1, "1");
						message1 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(0, 8))));
						message2 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(8, 16))));
						message3 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(16, 24))));
						message4 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(24, 32))));
						message5 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(32, 40))));
						message6 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(40, 48))));
						message7 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(48, 56))));
						message8 = ItonAdecimalConver
								.algorismToHEXString(ItonAdecimalConver
										.binaryToAlgorism(reverse(str
												.substring(56, 64))));
					}
				}

				message = 66 + "C50C" + message1 + message2 + message3
						+ message4 + message5 + message6 + message7 + message8
						+ toHex(Red, Green, Blue) + "00" + 99;
			senddata.sendData(message);

			break;

		case R.id.btn_clear:
			selectedposition_red.clear();// 清除选中的红色区域
			adapter.notifyDataSetChanged();
			break;
		}

	}

	/*
	 * 字符串反转
	 */
	public static String reverse(String str) {
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * RGB转十六进制
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static String toHex(int r, int g, int b) {
		return toBrowserHexValue(r) + toBrowserHexValue(g)
				+ toBrowserHexValue(b);
	}

	private static String toBrowserHexValue(int number) {
		StringBuilder builder = new StringBuilder(
				Integer.toHexString(number & 0xff));
		while (builder.length() < 2) {
			builder.append("0");
		}
		return builder.toString().toUpperCase();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 200:
			textColor = data.getIntExtra("data_return", -256);
			// Log.e("EightToEightActivity", textColor + "");
			PreferencesUtils.putInt(MainApplication.getInstance(), "color",
					textColor);
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
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
				item = new GridItem(MainApplication.getInstance());

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
}
