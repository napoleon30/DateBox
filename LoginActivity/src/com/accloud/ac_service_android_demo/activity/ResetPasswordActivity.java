package com.accloud.ac_service_android_demo.activity;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.country.CountryActivity;
import com.accloud.ac_service_android_demo.utils.CountDownTimerUtils;
import com.accloud.ac_service_android_demo.utils.DBOXException;
import com.accloud.ac_service_android_demo.utils.DensityUtils;
import com.accloud.ac_service_android_demo.utils.Pop;
import com.accloud.ac_service_android_demo.utils.ResourceReader;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.accloud.utils.PreferencesUtils;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ResetPasswordActivity extends Activity implements OnClickListener {
	public static final String TAG = ResetPasswordActivity.class
			.getSimpleName();

	private TextView back;
	private TextView areaCode;
	private EditText etPhone;
	private EditText etEmail;
	private EditText etPwd;
	private EditText etVCode;
	private Button vcodeBtn;
	private Button reset;

	private Button resetPhone;
	private Button resetEmail;
	private LinearLayout layout_tab;
	private RelativeLayout rl_phone;
	private RelativeLayout rl_email;

	String phone, pwd, rePwd, vcode, email;
	String account = null;

	String countryNumber = "+86";

	// 账号管理器
	ACAccountMgr accountMgr;
	int num = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		PushAgent.getInstance(this).onAppStart();
		num = PreferencesUtils.getInt(ResetPasswordActivity.this, "num", 0);

		back = (TextView) findViewById(R.id.reset_back);
		etPhone = (EditText) findViewById(R.id.reset_edit_tel);
		etEmail = (EditText) findViewById(R.id.reset_edit_email);
		etPwd = (EditText) findViewById(R.id.reset_edit_pwd);
		vcodeBtn = (Button) findViewById(R.id.reset_vcode);
		etVCode = (EditText) findViewById(R.id.reset_edit_vcode);
		reset = (Button) findViewById(R.id.reset);
		areaCode = (TextView) findViewById(R.id.area_code);
		areaCode.setOnClickListener(this);

		rl_phone = (RelativeLayout) findViewById(R.id.rl_phone);
		rl_email = (RelativeLayout) findViewById(R.id.rl_email);
		resetPhone = (Button) findViewById(R.id.btnTab001);
		resetEmail = (Button) findViewById(R.id.btnTab002);
		layout_tab = (LinearLayout) findViewById(R.id.layout_tab);
		resetPhone.setOnClickListener(this);
		resetEmail.setOnClickListener(this);
		if (num == 0) {
			rl_phone.setVisibility(View.VISIBLE);
			rl_email.setVisibility(View.GONE);
			setTabSelected(resetPhone);
		} else if (num == 1) {
			rl_phone.setVisibility(View.GONE);
			rl_email.setVisibility(View.VISIBLE);
			setTabSelected(resetEmail);
		}

		// 通过AC获取账号管理器
		accountMgr = AC.accountMgr();

		back.setOnClickListener(this);
		vcodeBtn.setOnClickListener(this);
		reset.setOnClickListener(this);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		countryNumber = PreferencesUtils.getString(ResetPasswordActivity.this,
				"areacode", "+86");
		areaCode.setText(countryNumber);
		String r_phone = PreferencesUtils.getString(ResetPasswordActivity.this,
				"phone","");
		String r_email = PreferencesUtils.getString(ResetPasswordActivity.this,
				"email","");
		if (r_phone.length()>0) {
			etPhone.setText(r_phone);
			etPwd.requestFocus();
		}
		if (r_email.length()>0) {
			etEmail.setText(r_email);
			etPwd.requestFocus();
		}
	}

	private void setTabSelected(Button btnSelected) {
		Drawable selectedDrawable = ResourceReader.readDrawable(this,
				R.drawable.shape_nav_indicator);
		int screenWidth = DensityUtils
				.getScreenSize(ResetPasswordActivity.this)[0];
		int right = screenWidth / 2;
		selectedDrawable.setBounds(0, 0, right, DensityUtils.dipTopx(this, 3));
		btnSelected.setSelected(true);
		btnSelected.setCompoundDrawables(null, null, null, selectedDrawable);
		int size = layout_tab.getChildCount();
		for (int i = 0; i < size; i++) {
			if (btnSelected.getId() != layout_tab.getChildAt(i).getId()) {
				layout_tab.getChildAt(i).setSelected(false);
				((Button) layout_tab.getChildAt(i)).setCompoundDrawables(null,
						null, null, null);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reset_vcode:
			PreferencesUtils.putInt(ResetPasswordActivity.this, "num", num);
			phone = etPhone.getText().toString().trim();
			email = etEmail.getText().toString().trim();
			if (num == 0) {

				if (phone.length() > 0) {
					if (countryNumber.equals("+86")) {
						account = phone;
					} else {
						account = countryNumber.replace("+", "00") + phone;
					}
					PreferencesUtils.putString(ResetPasswordActivity.this,"phone", phone);

				} else {
					Toast.makeText(ResetPasswordActivity.this,
							getString(R.string.enter_phone_num),
							Toast.LENGTH_SHORT).show();
					return;
				}
				sendVerifyCode(account);
			}else if (num==1) {
				if (email.length()>0) {
					account = email;
					PreferencesUtils.putString(ResetPasswordActivity.this,"email", email);
				}else{
					Toast.makeText(ResetPasswordActivity.this,
							getString(R.string.enter_email_num),
							Toast.LENGTH_SHORT).show();
					return;
				}
				sendVerifyCode(account);
			}
			break;

		case R.id.reset:
			pwd = etPwd.getText().toString().trim();
			vcode = etVCode.getText().toString().trim();
			if (account == null || account.equals("")) {
				Toast.makeText(ResetPasswordActivity.this,
						getString(R.string.enter_phone_num), Toast.LENGTH_SHORT)
						.show();
			} else if (vcode == null || account.equals("")) {
				Toast.makeText(ResetPasswordActivity.this,
						getString(R.string.enter_vcode), Toast.LENGTH_SHORT)
						.show();
			} else {
				resetPassword(account, pwd, vcode);

			}

			break;
		case R.id.reset_back:
			finish();
			break;
		case R.id.area_code:
			Intent intent = new Intent(ResetPasswordActivity.this,
					CountryActivity.class);
			startActivityForResult(intent, 13);
			break;

		case R.id.btnTab001:
			setTabSelected(resetPhone);
			num = 0;
			rl_email.setVisibility(View.GONE);
			rl_phone.setVisibility(View.VISIBLE);

			break;
		case R.id.btnTab002:
			setTabSelected(resetEmail);
			num = 1;
			rl_email.setVisibility(View.VISIBLE);
			rl_phone.setVisibility(View.GONE);

			break;

		}

	}

	/*
	 * 重置密码
	 */
	private void resetPassword(String account2, String pwd2, String vcode2) {
		accountMgr.resetPassword(account2, pwd2, vcode2,
				new PayloadCallback<ACUserInfo>() {

					@Override
					public void error(ACException e) {
						DBOXException.errorCode(ResetPasswordActivity.this, e.getErrorCode());
					}

					@Override
					public void success(ACUserInfo arg0) {
						Toast.makeText(ResetPasswordActivity.this,
								getString(R.string.success_reset_password),
								Toast.LENGTH_SHORT).show();
						ResetPasswordActivity.this.finish();
					}
				});

	}

	// 发送验证码
	private void sendVerifyCode(String account) {
		/**
		 * @param telephone或email
		 * @param callback
		 */
		int code = 1;
		if (num ==0 ) {
			code=1;
		}else if (num==1) {
			code=2;
		}
		accountMgr.sendVerifyCode(account,code, new VoidCallback() {
			@Override
			public void success() {
				Pop.popToast(ResetPasswordActivity.this,
						getString(R.string.register_aty_fetch_vercode_success));
				CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(
						vcodeBtn, 60000, 1000);
				mCountDownTimerUtils.start();
			}

			@Override
			public void error(ACException e) {
//				Pop.popToast(ResetPasswordActivity.this, e.getErrorCode()
//						+ "-->" + e.getMessage());
				DBOXException.errorCode(ResetPasswordActivity.this, e.getErrorCode());
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 13:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String countryName = bundle.getString("countryName");
				countryNumber = bundle.getString("countryNumber");
				PreferencesUtils.putString(ResetPasswordActivity.this,
						"areacode", countryNumber);// 保存区号

				areaCode.setText(countryNumber);

			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
