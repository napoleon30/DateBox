package com.accloud.ac_service_android_demo.activity;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.Pop;
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

public class SendDataActivity extends Activity implements OnClickListener {
	private Button sendData;
	private Button moduleChoose;
	private EditText sendText;
	private TextView receText;
	private PopupWindow mPopupWindow;
	int bridge = 0;
	String message1 = "6974637A";// 命令头
	String message2;// command_t
	String message3;// 命令长度
	String message4;// 控制命令（十六进制）
	String message5;// 校验位
	String message;// 完整命令

	private String subDomain;
	String physicalDeviceId;


	private TextView test;
	private Button subscribe;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_data);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		
		subDomain = PreferencesUtils.getString(
				MainApplication.getInstance(), "subDomain", Config.SUBDOMAIN);

		sendData = (Button) findViewById(R.id.btn_sendButoon);
		moduleChoose = (Button) findViewById(R.id.btn_module_choose);
		sendText = (EditText) findViewById(R.id.et_sendText);
		sendData.setOnClickListener(this);
		moduleChoose.setOnClickListener(this);
		receText = (TextView) findViewById(R.id.receText);
		
		test = (TextView) findViewById(R.id.tv_test);
		subscribe = (Button) findViewById(R.id.btn_subscribe);
		subscribe.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		message4 = sendText.getText().toString();
		switch (v.getId()) {
		case R.id.btn_module_choose:// 命令模式选择
			receText.setText("");
			sendText.setText("");
			showPopupWindow();
			break;
		case R.id.btn_subscribe:
		
			AC.customDataMgr().subscribe(Topic.customTopic("subDomain", "topic_type", "deviceId"), new VoidCallback() {
				
				@Override
				public void error(ACException arg0) {
					// TODO Auto-generated method stub
					Log.e("/*/*/*","订阅失败");
					
				}
				
				@Override
				public void success() {
					// TODO Auto-generated method stub
					Log.e("/*/*/*","订阅成功");
				}
			});
	    	Receiver<TopicData> receiver = new Receiver<TopicData>() {
				
				@Override
				public void onReceive(TopicData arg0) {
					// TODO Auto-generated method stub
					Log.e("***///***", arg0.toString());
					test.setText(arg0.toString());
					
				}
			};
	    	AC.customDataMgr().registerDataReceiver(receiver);
	    	AC.customDataMgr().unregisterDataReceiver(receiver);
	    	break;

		case R.id.btn_sendButoon:// 发送命令
			if (bridge == 1) {
				message2 = "0000";
			}else if (bridge == 2) {
				message2 = "0001";
			}else if (bridge == 3) {
				message2 = "0002";
			}else if (bridge == 4) {
				message2= "0042";
			}else if (bridge==5) {
				message2 = "0043";
			}else if (bridge==6) {
				message2 = "0044";
			}else if (bridge==7) {
				message2 = "0045";
			}else if (bridge==8) {
				message2 = "0046";
			}else if (bridge==9) {
				message2 = "0047";
			}else if (bridge==10) {
				message2 = "0048";
			}else if (bridge==11) {
				message2 = "0049";
			}else if (bridge==12) {
				message2 = "004a";
			}else if (bridge==13) {
				message2 = "004b";
			}else if (bridge==14) {
				message2 = "004c";
			}
			message5 = checkSum(message4);// 计算校验位

			if (message4 == "") {
				message3 = "0000";
			} else if (message4 != "") {
				if (message4.length() >= 32) {
					message3 = "00"
							+ Integer.toHexString(message4.length() / 2);
				} else {
					message3 = "000"
							+ Integer.toHexString(message4.length() / 2);// ???????????????十进制int转十六进制字符串
				}
			}
			message = message1 + message2 + message3 + message4 + message5;
			Log.e("发送的数据（十六进制）", message);

			byte[] midbytes = message.getBytes();
			Log.e("发送的数据byte[]（十进制）", "**************" + Arrays.toString(midbytes));
			byte[] b = new byte[midbytes.length / 2];
			for (int i = 0; i < midbytes.length / 2; i++) {
				b[i] = uniteBytes(midbytes[i * 2], midbytes[i * 2 + 1]);
				Log.e("test", "**************" + b[i]);
			}

			
			AC.bindMgr().sendToDeviceWithOption(subDomain, physicalDeviceId,
					getDeviceMsg(b), AC.LOCAL_FIRST,
					new PayloadCallback<ACDeviceMsg>() {
						@Override
						public void success(ACDeviceMsg msg) {
							if (parseDeviceMsg(msg)) {
								Log.e("返回消息","返回成功，"+ msg + "");
								
								receText.setText(ItonAdecimalConver.byte2hex(msg.getContent()));
							} else {
								Log.e("返回消息","返回失败");
							}
						}

						@Override
						public void error(ACException e) {
							Toast.makeText(SendDataActivity.this,
									e.getErrorCode() + "-->" + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}
					});
			Log.e("发送的消息", getDeviceMsg(b) + "");
			break;
			
			
		case R.id.tv_communication_test:// 设备通信测试
			moduleChoose.setText("设备通信测试");
			bridge = 1;
			sendText.setText("54455354");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_get_version:// 获取版本
			moduleChoose.setText("获取版本");
			bridge=2;
			sendText.setText("");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_module_reset:// 模块复位
			moduleChoose.setText("模块复位");
			bridge = 3;
			sendText.setText("");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_scanning_device:// LE使能扫描周围设备
			moduleChoose.setText("LE使能扫描周围设备");
			bridge = 4;
			sendText.setText("");
			sendText.setHint("请输入00(不使能扫描设备)/01(使能扫描设备)");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_connect_device:// LE连接特定地址设备
			moduleChoose.setText("LE连接特定地址设备");
			bridge = 5;
			sendText.setText("80eacaa0100100");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_exit_connect_device:// LE退出连接特定地址设备
			moduleChoose.setText("LE退出连接特定地址设备");
			bridge=6;
			sendText.setText("80eacaa01001");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_disconnect_device:// LE断开特定设备连接
			moduleChoose.setText("LE断开特定设备连接");
			bridge = 7;
			sendText.setText("80eacaa01001");
			mPopupWindow.dismiss();
			break;
		case R.id.tv_find_LE:// LE查找服务
			moduleChoose.setText("LE查找服务");
            bridge = 8;
            sendText.setText("80eacaa01001");
            mPopupWindow.dismiss();
			break;
		case R.id.tv_find_attribute:// LE查找属性
			moduleChoose.setText("LE查找属性");
            bridge = 9;
            sendText.setText("80eacaa01001");
            mPopupWindow.dismiss();
			break;
		case R.id.tv_find_describe:// LE查找描述
			moduleChoose.setText("LE查找描述");
            bridge = 10;
            sendText.setText("80eacaa01001");
            mPopupWindow.dismiss();
			break;
		case R.id.tv_LE_notify:// LE设置使能通知功能
			moduleChoose.setText("LE设置使能通知功能");
            bridge = 11;
            sendText.setText("80eacaa01001001601");
            mPopupWindow.dismiss();
			break;
		case R.id.tv_LE_principal_and_subordinate:// LE作为主机角色收发数据，接收从机通知数据
			moduleChoose.setText("LE作为主机角色收发数据，接收从机通知数据");
            bridge = 12;
            sendText.setText("80eacaa0100100160100000000");
            mPopupWindow.dismiss();
			break;
		case R.id.tv_LE_readAttribute:// LE读属性
			 moduleChoose.setText("LE读属性");
             bridge = 13;
             sendText.setHint("请输入8个十六进制数");
             mPopupWindow.dismiss();
			break;
		case R.id.tv_LE_query_device:// LE查询当前连接的设备
			moduleChoose.setText("LE查询当前连接的设备");
            bridge = 14;
            sendText.setText("");
            mPopupWindow.dismiss();
			break;
		}

	}

	protected boolean parseDeviceMsg(ACDeviceMsg msg) {
		// 注意：实际开发的时候请选择其中的一种消息格式即可
				switch (getFormatType()) {
				case ConfigurationActivity.BINARY:
					byte[] bytes = msg.getContent();
					if (bytes != null)
						return bytes[0] == 0x69 && bytes[1] == 0x74 ? true : false;///////////-------------------------
				case ConfigurationActivity.JSON:
					try {
						JSONObject object = new JSONObject(new String(msg.getContent()));
						return object.optBoolean("result");
					} catch (Exception e) {
					}
				}
				return false;
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
		return PreferencesUtils.getInt(SendDataActivity.this, "formatType",
				ConfigurationActivity.BINARY);
	}

	/**
	 * 根据输入的十六进制数据，计算校验位
	 */
	public String checkSum(String string) {
		/**
		 * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
		 * 0xD9}
		 * 
		 * @param src
		 * @return byte[]
		 */
		String results = null;
		String pingjie = null;
		if (string == null) {
			results = "00";
		} else if (string != null) {
			byte[] bytes = new byte[string.length() / 2]; // 数组长度为result.length()/2
			int[] ints = new int[string.length() / 2];
			byte[] tmp = string.getBytes();
			int sum = 0;
			for (int i = 0; i < string.length() / 2; i++) {
				bytes[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
				ints[i] = bytes[i];
				if (ints[i] < 0) {
					ints[i] += 256;
				}
				sum += ints[i];
//				Log.e("AAAAA", ints[i] + "");
//				Log.e("sum", sum + "");
			}
			int res = 255 - sum + 1; // 十进制计算
//			Log.e("res", res + "");

			if (res > 0) { // 如果为正，则校验码为其十六进制数
				results = Integer.toHexString(res);// 十进制int转十六进制字符串
				results = results.substring(results.length() - 2);
			} else if (res < 0) {
				String tenToBinary = Integer.toBinaryString(-res);// 十进制转二进制字符串
				if (tenToBinary.length() >= 8) {
					String substr = tenToBinary
							.substring(tenToBinary.length() - 8);
//					Log.e("string1", substr);
					byte[] bytes1 = substr.getBytes();
					Log.e("bytes[i]", Arrays.toString(bytes1));
					for (int i = 0; i < bytes1.length; i++) {
						if (bytes1[i] == 49) { // 49表示1
							bytes1[i] = 48; // 48表示0
//							Log.e("bytes[i]", bytes1[i] + "");
						} else if (bytes1[i] == 48) {
							bytes1[i] = 49;
//							Log.e("bytes[i]", bytes1[i] + "");
						}
					}
					String sub = new String(bytes1);
//					Log.e("sub", sub);
					int in = Integer.parseInt(sub, 2);
					in = in + 1;
//					Log.e("in", in + "");
					results = algorismToHEXString(in);// 十进制int转十六进制字符串

				} else if (tenToBinary.length() < 8) {
					if (tenToBinary.length() == 1) {
						pingjie = "1000000" + tenToBinary;
					} else if (tenToBinary.length() == 2) {
						pingjie = "100000" + tenToBinary;
					} else if (tenToBinary.length() == 3) {
						pingjie = "10000" + tenToBinary;
					} else if (tenToBinary.length() == 4) {
						pingjie = "1000" + tenToBinary;
					} else if (tenToBinary.length() == 5) {
						pingjie = "100" + tenToBinary;
					} else if (tenToBinary.length() == 6) {
						pingjie = "10" + tenToBinary;
					} else if (tenToBinary.length() == 7) {
						pingjie = "1" + tenToBinary;
					}
//					Log.e("pingjie", pingjie);
					int BinaryToTen = binaryToAlgorism(pingjie); // 二进制字符串转十进制int
					results = algorismToHEXString(BinaryToTen);// 十进制int转十六进制字符串
				}

			} else if (res == 0) {
				results = "00";
			}
		}
		return results;
	}

	/**
	 * 十进制转换为十六进制字符串
	 * 
	 * @param algorism
	 *            int 十进制的数字
	 * @return String 对应的十六进制字符串
	 */
	public static String algorismToHEXString(int algorism) {
		String result = "";
		result = Integer.toHexString(algorism);

		if (result.length() % 2 == 1) {
			result = "0" + result;

		}
		result = result.toUpperCase();

		return result;
	}

	/**
	 * 二进制字符串转十进制
	 * 
	 * @param binary
	 *            二进制字符串
	 * @return 十进制数值
	 */
	public static int binaryToAlgorism(String binary) {
		int max = binary.length();
		int result = 0;
		for (int i = max; i > 0; i--) {
			char c = binary.charAt(i - 1);
			int algorism = c - '0';
			result += Math.pow(2, max - i) * algorism;
		}
		return result;
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

	private void showPopupWindow() {
		// TODO Auto-generated method stub
		// 设置contentView
		View contentView = LayoutInflater.from(this).inflate(
				R.layout.popupwindow, null);
		mPopupWindow = new PopupWindow(contentView,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setContentView(contentView);
		// 点击空白位置PopupWindow消失
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);
		// 各个item的点击响应
		TextView communicatino_test = (TextView) contentView
				.findViewById(R.id.tv_communication_test);
		TextView get_version = (TextView) contentView
				.findViewById(R.id.tv_get_version);
		TextView module_reset = (TextView) contentView
				.findViewById(R.id.tv_module_reset);
		TextView scannning_device = (TextView) contentView
				.findViewById(R.id.tv_scanning_device);
		TextView connect_device = (TextView) contentView
				.findViewById(R.id.tv_connect_device);
		TextView exit_connect_device = (TextView) contentView
				.findViewById(R.id.tv_exit_connect_device);
		TextView disconnect_device = (TextView) contentView
				.findViewById(R.id.tv_disconnect_device);
		TextView find_LE = (TextView) contentView.findViewById(R.id.tv_find_LE);
		TextView find_attribute = (TextView) contentView
				.findViewById(R.id.tv_find_attribute);
		TextView find_describe = (TextView) contentView
				.findViewById(R.id.tv_find_describe);
		TextView LE_notify = (TextView) contentView
				.findViewById(R.id.tv_LE_notify);
		TextView LE_principal_and_subordinate = (TextView) contentView
				.findViewById(R.id.tv_LE_principal_and_subordinate);
		TextView LE_readAttribute = (TextView) contentView
				.findViewById(R.id.tv_LE_readAttribute);
		TextView LE_query_device = (TextView) contentView
				.findViewById(R.id.tv_LE_query_device);
		communicatino_test.setOnClickListener(this);
		get_version.setOnClickListener(this);
		module_reset.setOnClickListener(this);
		scannning_device.setOnClickListener(this);
		connect_device.setOnClickListener(this);
		exit_connect_device.setOnClickListener(this);
		disconnect_device.setOnClickListener(this);
		find_LE.setOnClickListener(this);
		find_attribute.setOnClickListener(this);
		find_describe.setOnClickListener(this);
		LE_notify.setOnClickListener(this);
		LE_principal_and_subordinate.setOnClickListener(this);
		LE_readAttribute.setOnClickListener(this);
		LE_query_device.setOnClickListener(this);

		// 显示PopupWindow
		View rootview = LayoutInflater.from(this).inflate(
				R.layout.activity_send_data, null);
		mPopupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
	}

}
