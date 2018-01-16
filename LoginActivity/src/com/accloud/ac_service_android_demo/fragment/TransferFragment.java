package com.accloud.ac_service_android_demo.fragment;


import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.PagerActivity;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
import com.accloud.utils.PreferencesUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TransferFragment extends Fragment implements OnClickListener {
	public static final String TAG = "TransferFragment";

	private Button send, clear;
	private EditText send_content, receive_content;
	private LocalBroadcastManager mLBM;
	private DetailBroadCastReceiver detailBroadCastReceiver;

	SendData senddata;
	String subDomain, physicalDeviceId;

	public TransferFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("TransferFragment", "onCreate");
		InitDetailBroadCastReceiver();
		subDomain = PreferencesUtils.getString(MainApplication.getInstance(),
				"subDomain", Config.SUBDOMAIN);
		physicalDeviceId = ((PagerActivity) getActivity())
				.getPhysicalDeviceId();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fg_trans, container, false);
		Log.e("TransferFragment", "onCreateView");
		send = (Button) view.findViewById(R.id.btn_send);
		clear = (Button) view.findViewById(R.id.btn_clear);
		send_content = (EditText) view.findViewById(R.id.et_send_content);
		receive_content = (EditText) view.findViewById(R.id.et_receive_content);
		send.setOnClickListener(this);
		clear.setOnClickListener(this);
		
		senddata = new SendData(subDomain,physicalDeviceId);

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			String send_message = send_content.getText().toString().trim();
			String message = ItonAdecimalConver.stringToAscii(send_message);
			String[] mes=message.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mes.length; i++) {
				sb.append(ItonAdecimalConver.algorismToHEXString(Integer.parseInt(mes[i])));
				message = sb.toString();
			}
			Log.e(TAG+"1", message);
			
			senddata.sendData("66C1"+ItonAdecimalConver.algorismToHEXString(message.length()/2)+message+"99");

			break;

		case R.id.btn_clear:
			receive_content.setText("");
			break;
		}
	}

	/**
	 * 初始化Detail广播LocalBroadcastReceiver
	 */
	private void InitDetailBroadCastReceiver() {
		mLBM = LocalBroadcastManager.getInstance(getActivity());
		detailBroadCastReceiver = new DetailBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("PagerPayload");
		mLBM.registerReceiver(detailBroadCastReceiver, intentFilter);
	}

	/**
	 * 广播接收处理函数
	 */
	public class DetailBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String str = (String) intent.getExtras().get("payload");
			Log.e("透传", str);
			if (str.substring(2, 4).equals("C1")) {
				String message11 = str.substring(6, str.length() - 2);
				if (message11.contains("0A")|| message11.contains("0D")) {
					message11=message11.replace("0A", "");
					message11=message11.replace("0D", "");
				}
				receive_content.setText("");
				receive_content.setText(message11);
				Log.e(TAG, message11);
			}
		}

	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.e("TransferFragment", "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLBM.unregisterReceiver(detailBroadCastReceiver);
	}

}
