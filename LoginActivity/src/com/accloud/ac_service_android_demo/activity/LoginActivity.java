package com.accloud.ac_service_android_demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.country.CountryActivity;
import com.accloud.ac_service_android_demo.shimmer.Shimmer;
import com.accloud.ac_service_android_demo.shimmer.ShimmerTextView;
import com.accloud.ac_service_android_demo.utils.DBOXException;
import com.accloud.ac_service_android_demo.utils.DensityUtils;
import com.accloud.ac_service_android_demo.utils.Pop;
import com.accloud.ac_service_android_demo.utils.ResourceReader;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

/**
 * 登录页面
 * 
 * 
 */
public class LoginActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "LoginActivity";

	public static final int BINARY = 0;
	public static final int JSON = 1;
	int num = 0;

	String countryNumber = "+86";

	private EditText editEmail;
	private EditText editTel;
	private EditText editPwd;

	private RelativeLayout rl_phone;
	private RelativeLayout rl_email;

	private TextView register_phone;
	private TextView forgetPwd;
	private Button loginBtn;
	private TextView areaCode;
	private CheckBox cb_pass;

	private Button loginPhone;
	private Button loginEmail;
	private LinearLayout layout_tab;

	private String tel, ema;
	String phone_or_email;
	String phone_or_email_hx;
	private String pwd;
	// 账号管理器
	ACAccountMgr accountMgr;

	ProgressDialog loginDialog;

	ShimmerTextView tv;
	Shimmer shimmer;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				// 完成主界面更新,拿到数据
				loginDialog.dismiss();

				break;
			case 1:
				loginDialog.dismiss();
				int data = (Integer) msg.obj;
				Log.e(TAG, data+"**-*-*-");
				DBOXException.errorCode(LoginActivity.this, data);
				break;
			}
		}

	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Log.e(TAG, "onCreate");
		PushAgent.getInstance(this).onAppStart();
		num = PreferencesUtils.getInt(LoginActivity.this, "num", 0);
		tel = PreferencesUtils.getString(LoginActivity.this, "phone");
		ema = PreferencesUtils.getString(LoginActivity.this, "email");

		Log.e(TAG, "num=" + num + ";tel=" + tel + ";ema=" + ema);

		tv = (ShimmerTextView) findViewById(R.id.shimmer_tv);
		if (shimmer != null && shimmer.isAnimating()) {
			shimmer.cancel();
		} else {
			shimmer = new Shimmer();
			shimmer.start(tv);
		}

		rl_phone = (RelativeLayout) findViewById(R.id.user_phone);
		rl_email = (RelativeLayout) findViewById(R.id.user_email);
		editEmail = (EditText) findViewById(R.id.login_edit_email);
		if (!TextUtils.isEmpty(ema)) {
			editEmail.setText(ema);
		}

		editTel = (EditText) findViewById(R.id.login_edit_tel);
		if (!TextUtils.isEmpty(tel)) {
			editTel.setText(tel);
		}

		loginPhone = (Button) findViewById(R.id.login_btnTab001);
		loginEmail = (Button) findViewById(R.id.login_btnTab002);
		layout_tab = (LinearLayout) findViewById(R.id.layout_tab);
		loginPhone.setOnClickListener(this);
		loginEmail.setOnClickListener(this);

		if (num == 0) {
			setTabSelected(loginPhone);
			rl_phone.setVisibility(View.VISIBLE);
			rl_email.setVisibility(View.GONE);
		} else {
			setTabSelected(loginEmail);
			rl_phone.setVisibility(View.GONE);
			rl_email.setVisibility(View.VISIBLE);
		}

		editPwd = (EditText) findViewById(R.id.login_edit_pwd);
		register_phone = (TextView) findViewById(R.id.register);

		forgetPwd = (TextView) findViewById(R.id.forget_password);
		loginBtn = (Button) findViewById(R.id.login);
		areaCode = (TextView) findViewById(R.id.area_code);
		areaCode.setText(countryNumber);

		areaCode.setOnClickListener(this);
		register_phone.setOnClickListener(this);
		forgetPwd.setOnClickListener(this);
		loginBtn.setOnClickListener(this);

		// 通过AC获取账号管理器
		accountMgr = AC.accountMgr();

		loginDialog = new ProgressDialog(LoginActivity.this);

		cb_pass = (CheckBox) findViewById(R.id.cb_pass);
		cb_pass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					editPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					editPwd.setSelection(editPwd.getText().length());
				} else {
					editPwd.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
					editPwd.setSelection(editPwd.getText().length());
				}
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		PreferencesUtils.putString(LoginActivity.this, "domain", "wuhanxl");
		PreferencesUtils
				.putString(LoginActivity.this, "subDomain", "xinlian01");
		PreferencesUtils.putLong(LoginActivity.this, "domainId", 5287);
		PreferencesUtils.putInt(LoginActivity.this, "formatType", BINARY);
		PreferencesUtils.putInt(LoginActivity.this, "deviceType",
				AC.DEVICE_AI6060H);

		tel = PreferencesUtils.getString(LoginActivity.this, "phone");

		countryNumber = PreferencesUtils.getString(LoginActivity.this,
				"areacode", "+86");
		areaCode.setText(countryNumber);
		if (!countryNumber.equals("+86")) {
			phone_or_email = countryNumber.replace("+", "00") + tel;
			Log.e(TAG + "****", phone_or_email);
		} else {
			phone_or_email = tel;
			if (tel != null) {
				Log.e(TAG + "****", phone_or_email);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		tel = PreferencesUtils.getString(LoginActivity.this, "phone");
		ema = PreferencesUtils.getString(LoginActivity.this, "email");
		if (tel != null) {
			editTel.setText(tel);
			editPwd.requestFocus();// 获取光标
			Log.e("tel", tel);
		}
		if (ema != null) {
			editEmail.setText(ema);
			editPwd.requestFocus();
			Log.e("ema", ema);
		}

		/**
		 * 一、如果ablecloud和环信均已登录，则跳转至主界面 二、如果ablecloud登录，环信未登录，则登录环信，跳转至主界面
		 */
		if (EMClient.getInstance().isLoggedInBefore() && accountMgr.isLogin()) {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			Log.e(TAG, "已登录1");
			startActivity(intent);
			LoginActivity.this.finish();
		}
		// else if (!EMClient.getInstance().isLoggedInBefore()
		// && accountMgr.isLogin()) {
		// hxLogin();// 环信登录
		// Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		// Log.e(TAG, "已登录2");
		// startActivity(intent);
		// LoginActivity.this.finish();
		// }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		shimmer.cancel();
	}

	private void setTabSelected(Button btnSelected) {
		Drawable selectedDrawable = ResourceReader.readDrawable(this,
				R.drawable.shape_nav_indicator);
		int screenWidth = DensityUtils.getScreenSize(LoginActivity.this)[0];
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
	public void onClick(View view) {
		Intent intent = null;
		switch (view.getId()) {
		case R.id.register:
			intent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivity(intent);
			break;
		case R.id.forget_password:
			intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
			startActivity(intent);
			break;
		case R.id.login:
			ema = editEmail.getText().toString().trim();
			tel = editTel.getText().toString().trim();
			pwd = editPwd.getText().toString().trim();

			if (num == 0) {
				if (countryNumber.equals("+86")) {
					phone_or_email = tel;
				} else {
					phone_or_email = countryNumber.replace("+", "00") + tel;
				}
				if (tel.length() == 0 || pwd.length() == 0) {
					Pop.popToast(
							LoginActivity.this,
							getString(R.string.login_aty_username_or_pwd_cannot_be_empty));
				} else {
					loginDialog = ProgressDialog.show(LoginActivity.this,
							getResources().getString(R.string.logining),
							getResources().getString(R.string.logining_wait),
							false);

					new Thread(new Runnable() {

						@Override
						public void run() {
							if (!EMClient.getInstance().isLoggedInBefore()) {
								hxLogin(phone_or_email);// 环信登录
							}
							login();// AbleCloud登录

						}
					}).start();
				}
			} else if (num == 1) {
				phone_or_email = ema;
				phone_or_email_hx = ema.replace("@", "");
				phone_or_email_hx = phone_or_email_hx.replace(".", "");
				if (phone_or_email.length() == 0 || pwd.length() == 0) {
					Pop.popToast(
							LoginActivity.this,
							getString(R.string.login_aty_username_or_pwd_cannot_be_empty));
				} else {
					loginDialog = ProgressDialog.show(LoginActivity.this,
							getResources().getString(R.string.logining),
							getResources().getString(R.string.logining_wait),
							false);

					new Thread(new Runnable() {

						@Override
						public void run() {
							if (!EMClient.getInstance().isLoggedInBefore()) {
								hxLogin(phone_or_email_hx);// 环信登录
							}
							login();// AbleCloud登录

						}
					}).start();
				}
			}

			break;
		case R.id.area_code:
			intent = new Intent(LoginActivity.this, CountryActivity.class);
			startActivityForResult(intent, 12);
			break;

		case R.id.login_btnTab001:
			setTabSelected(loginPhone);
			rl_phone.setVisibility(View.VISIBLE);
			rl_email.setVisibility(View.GONE);
			num = 0;
			PreferencesUtils.putInt(LoginActivity.this, "num", num);
			break;
		case R.id.login_btnTab002:
			setTabSelected(loginEmail);
			rl_phone.setVisibility(View.GONE);
			rl_email.setVisibility(View.VISIBLE);
			num = 1;
			PreferencesUtils.putInt(LoginActivity.this, "num", num);
			break;
		}
	}

	private void hxLogin(String account) {
		EMClient.getInstance().login(account, "123456", new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						EMClient.getInstance().groupManager().loadAllGroups();
						EMClient.getInstance().chatManager()
								.loadAllConversations();
						Log.e("环信登录", "环信登录成功");

					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.e("环信登录", "环信登录失败" + code + "/" + message);

					}
				});
	}

	public void login() {
		/**
		 * @param email或telephone
		 * @param password
		 * @param callback
		 *            <userId>
		 */
		accountMgr.login(phone_or_email, pwd,
				new PayloadCallback<ACUserInfo>() {
					@Override
					public void success(ACUserInfo userInfo) {
						Log.e("ablecloud登录", "ablecloud登录成功");
						// 耗时操作，完成之后发送消息给Handler，完成UI更新；
						mHandler.sendEmptyMessage(0);

						PushAgent mPushAgent = MainApplication.push();
						mPushAgent.addAlias(
								String.valueOf(userInfo.getUserId()),
								"ablecloud", new UTrack.ICallBack() {
									@Override
									public void onMessage(boolean isSuccess,
											String message) {
										Log.e("****123", message);
									}
								});

						PreferencesUtils.putLong(LoginActivity.this, "userId",
								userInfo.getUserId());
						Intent intent = new Intent(LoginActivity.this,
								MainActivity.class);
						startActivity(intent);
						PreferencesUtils.putString(LoginActivity.this, "phone",
								tel);
						PreferencesUtils.putString(LoginActivity.this, "email",
								ema);
						LoginActivity.this.finish();
					}

					@Override
					public void error(ACException e) {
						Log.e(TAG, e.getErrorCode()+"");

						// 需要数据传递，用下面方法；
						Message msg = new Message();
						msg.obj = e.getErrorCode();// 可以是基本类型，可以是对象，可以是List、map等；
						msg.what=1;
						mHandler.sendMessage(msg);

					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 12:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String countryName = bundle.getString("countryName");
				countryNumber = bundle.getString("countryNumber");
				PreferencesUtils.putString(LoginActivity.this, "areacode",
						countryNumber);// 保存区号

				areaCode.setText(countryNumber);

			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
