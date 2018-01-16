package com.accloud.ac_service_android_demo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.accloud.ac_service_android_demo.R;

public class SettingFragment extends Fragment{

	public SettingFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fg_content, container, false);
		TextView tv = (TextView) view.findViewById(R.id.txt_content);
		tv.setText("ÆäËû");
		return view;
	}
}
