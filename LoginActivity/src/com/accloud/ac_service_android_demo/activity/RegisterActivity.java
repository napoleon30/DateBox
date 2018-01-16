package com.accloud.ac_service_android_demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.umeng.message.PushAgent;

/**
 * 注册页面
 * 
 */
public class RegisterActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "RegisterActivity";

	private EditText editPhone;
	private EditText editEmail;
	private EditText editName;
	private EditText editPwd;
	private EditText editRePwd;
	private EditText editVCode;
	private Button vCodeBtn;
	private Button registerBtn;
	private TextView back;
	private TextView areaCode;
	private RelativeLayout rl_phone;
	private RelativeLayout rl_email;

	private Button registerPhone;
	private Button registerEmail;
	private LinearLayout layout_tab;

	String phone;
	String email;
	String name;
	String pwd;
	String vcode;
	String countryNumber = "+86";

	int num;
	// 账号管理器
	ACAccountMgr accountMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		PushAgent.getInstance(this).onAppStart();
		num = PreferencesUtils.getInt(RegisterActivity.this, "num", 0);
		

		rl_phone = (RelativeLayout) findViewById(R.id.rl_phone);
		rl_email = (RelativeLayout) findViewById(R.id.rl_emali);
		editPhone = (EditText) findViewById(R.id.register_edit_tel);
		editEmail = (EditText) findViewById(R.id.register_edit_email);
		editName = (EditText) findViewById(R.id.register_edit_name);
		editPwd = (EditText) findViewById(R.id.register_edit_pwd);
		editRePwd = (EditText) findViewById(R.id.register_edit_repwd);
		editRePwd = (EditText) findViewById(R.id.register_edit_repwd);
		editVCode = (EditText) findViewById(R.id.register_edit_vcode);
		vCodeBtn = (Button) findViewById(R.id.register_vcode);
		registerBtn = (Button) findViewById(R.id.register);
		back = (TextView) findViewById(R.id.register_back);
		areaCode = (TextView) findViewById(R.id.area_code);

		registerPhone = (Button) findViewById(R.id.btnTab001);
		registerEmail = (Button) findViewById(R.id.btnTab002);
		layout_tab = (LinearLayout) findViewById(R.id.layout_tab);

		if (num==0) {
			rl_phone.setVisibility(View.VISIBLE);
			rl_email.setVisibility(View.GONE);
			setTabSelected(registerPhone);
		}else if (num==1) {
			rl_phone.setVisibility(View.GONE);
			rl_email.setVisibility(View.VISIBLE);
			setTabSelected(registerEmail);
		}
		registerPhone.setOnClickListener(this);
		registerEmail.setOnClickListener(this);

		// 通过AC获取账号管理器
		accountMgr = AC.accountMgr();

		vCodeBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		back.setOnClickListener(this);
		areaCode.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		countryNumber = PreferencesUtils.getString(RegisterActivity.this,
				"areacode", "+86");
		areaCode.setText(countryNumber);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.register_vcode:
			phone = editPhone.getText().toString().trim();
			email = editEmail.getText().toString().trim();
			PreferencesUtils.putString(RegisterActivity.this, "phone", phone);
			PreferencesUtils.putString(RegisterActivity.this, "email", email);
			if (num == 0) {
				if (phone.length() > 0) {
					if (!countryNumber.equals("+86")) {
						phone = countryNumber.replace("+", "00") + phone;
					}
					sendVerifyCode(phone);

				} else {
					Pop.popToast(
							RegisterActivity.this,
							getString(R.string.register_aty_phone_or_email_cannot_be_empty));
					return;
				}
			} else if (num == 1) {
				if (email.length() > 0) {
					sendVerifyCode(email);
				} else {
					Pop.popToast(
							RegisterActivity.this,
							getString(R.string.register_aty_phone_or_email_cannot_be_empty1));
				}
			}

			break;
		case R.id.register:
			pwd = editPwd.getText().toString().trim();
			name = editName.getText().toString().trim();
			String rePwd = editRePwd.getText().toString().trim();
			vcode = editVCode.getText().toString();

				if (pwd.equals(rePwd) && !TextUtils.isEmpty(vcode)) {
					if (num == 0) {
						hxRegister(phone);// 环信注册
					} else if (num == 1) {
						String email1 = email.replace("@", "");
						email1 = email1.replace(".", "");
						hxRegister(email1);// 环信注册
					}
					
				} else if (!pwd.equals(rePwd)) {
					Pop.popToast(RegisterActivity.this,
							getString(R.string.register_aty_repeat_pwd_incorrect));
				} else if (vcode.equals("") || vcode == null) {
					Pop.popToast(RegisterActivity.this,
							getString(R.string.please_enter_verify_code));
				}
			

			break;
		case R.id.register_back:
			RegisterActivity.this.finish();
			break;
		case R.id.area_code:
			Intent intent = new Intent(RegisterActivity.this,
					CountryActivity.class);
			startActivityForResult(intent, 14);
			break;

		case R.id.btnTab001:
			setTabSelected(registerPhone);
			num = 0;
			rl_email.setVisibility(View.GONE);
			rl_phone.setVisibility(View.VISIBLE);

			break;
		case R.id.btnTab002:
			setTabSelected(registerEmail);
			num = 1;
			
			rl_email.setVisibility(View.VISIBLE);
			rl_phone.setVisibility(View.GONE);

			break;
		}
	}

	private void setTabSelected(Button btnSelected) {
		Drawable selectedDrawable = ResourceReader.readDrawable(this,
				R.drawable.shape_nav_indicator);
		int screenWidth = DensityUtils.getScreenSize(RegisterActivity.this)[0];
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

	// 环信注册
	private void hxRegister(final String account) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run() {
				try {
					// call method in SDK
					EMClient.getInstance().createAccount(account, "123456");
					Log.e("环信注册", "环信注册成功");
					register();// AbleCloud注册
				} catch (final HyphenateException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							int errorCode = e.getErrorCode();
							if (errorCode == EMError.NETWORK_ERROR) {
								Toast.makeText(
										getApplicationContext(),
										getResources().getString(
												R.string.network_error),
										Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.USER_ALREADY_EXIST) {
								Toast.makeText(
										getApplicationContext(),
										getResources().getString(
												R.string.user_exists),
										Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
								Toast.makeText(
										getApplicationContext(),
										getResources().getString(
												R.string.register_failed),
										Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
								Toast.makeText(
										getApplicationContext(),
										getResources().getString(
												R.string.user_name_legal),
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(
										getApplicationContext(),
										getResources().getString(
												R.string.fail_to_register),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}).start();
	}

	// 发送验证码
	public void sendVerifyCode(String account) {
		/**
		 * @param account
		 *            : email或telephone
		 * @param callback
		 */
		int code = 1;
		if (num==0) {
			code=1;
		}else if (num==1) {
			code=2;
		}
		accountMgr.sendVerifyCode(account, code, new VoidCallback() {
			@Override
			public void success() {
				Pop.popToast(RegisterActivity.this,
						getString(R.string.register_aty_fetch_vercode_success));
				CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(
						vCodeBtn, 60000, 1000);
				mCountDownTimerUtils.start();
			}

			@Override
			public void error(ACException e) {
//				Pop.popToast(RegisterActivity.this, e.getErrorCode() + "-->"
//						+ e.getMessage());
				DBOXException.errorCode(RegisterActivity.this, e.getErrorCode());
			}
		});
	}

	// ablecloud注册
	public void register() {
		/**
		 * @param email
		 * @param telephone
		 * @param password
		 * @param verifyCode
		 * @param callback
		 */
		accountMgr.register(email, phone, pwd, name, vcode,
				new PayloadCallback<ACUserInfo>() {
					@Override
					public void success(ACUserInfo userInfo) {
						Log.e("ablecloud注册", "ablecloud注册成功");
						PreferencesUtils.putInt(RegisterActivity.this, "num", num);
						Pop.popToast(
								RegisterActivity.this,
								getString(R.string.register_aty_register_success));
						RegisterActivity.this.finish();
					}

					@Override
					public void error(ACException e) {
//						Pop.popToast(RegisterActivity.this, e.getErrorCode()
//								+ "-->" + e.getMessage());
						DBOXException.errorCode(RegisterActivity.this, e.getErrorCode());
					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 14:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String countryName = bundle.getString("countryName");
				countryNumber = bundle.getString("countryNumber");
				PreferencesUtils.putString(RegisterActivity.this, "areacode",
						countryNumber);// 保存区号

				areaCode.setText(countryNumber);

			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
