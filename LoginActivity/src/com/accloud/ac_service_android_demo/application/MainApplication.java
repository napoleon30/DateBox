package com.accloud.ac_service_android_demo.application;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.cloudservice.AC;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

public class MainApplication extends Application {
	private static MainApplication mInstance;
	public static Context applicationContext;
	private String domain;
	private long domainId;
	static PushAgent mPushAgent;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mPushAgent = PushAgent.getInstance(this);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 注册推送服务，每次调用register方法都会回调该接口
				mPushAgent.register(new IUmengRegisterCallback() {

					@Override
					public void onSuccess(String deviceToken) {
						// 注册成功会返回device token
						Log.e("deviceToken/******/", deviceToken);
					}

					@Override
					public void onFailure(String s, String s1) {
						Log.e("deviceToken_error", s+"**"+s1);
					}
				});
			}
		}).start();

		applicationContext = this;
		mInstance = this;

		// 读取本地存储的配置信息,开发者在实际开发中直接调用AC.init()方法即可
		domain = PreferencesUtils.getString(this, "domain", Config.MAJORDOAMIN);
		domainId = PreferencesUtils.getLong(this, "domainId",
				Config.MAJORDOMAINID);
		String routerAddr = PreferencesUtils.getString(this, "routerAddr", "");
		if (!routerAddr.equals("")) {
			AC.setRouterAddress(routerAddr);
		}

		AC.init(this, domain, domainId, AC.TEST_MODE);

		EMOptions options = new EMOptions();
		// 默认添加好友时，是不需要验证的，改成需要验证
		options.setAcceptInvitationAlways(false);

		int pid = android.os.Process.myPid();
		String processAppName = getAppName(pid);
		// 如果APP启用了远程的service，此application:onCreate会被调用2次
		// 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
		// 默认的APP会在以包名为默认的process name下运行，如果查到的process name不是APP的process
		// name就立即返回

		if (processAppName == null
				|| !processAppName.equalsIgnoreCase(applicationContext
						.getPackageName())) {
			Log.e("MainApplication", "enter the service process!");

			// 则此application::onCreate 是被service 调用的，直接返回
			return;
		}

		// 初始化
		EMClient.getInstance().init(applicationContext, options);
		// 在做打包混淆时，关闭debug模式，避免消耗不必要的资源
		EMClient.getInstance().setDebugMode(true);

		

	}

	public static PushAgent push() {
		return mPushAgent;
	}

	public static MainApplication getInstance() {
		return mInstance;
	}

	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
					.next());
			try {
				if (info.pid == pID) {
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
				// Log.d("Process", "Error>> :"+ e.toString());
			}
		}
		return processName;
	}
}
