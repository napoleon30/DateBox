package com.accloud.ac_service_android_demo.utils;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.accloud.ac_service_android_demo.activity.AI6060HActivity;
import com.accloud.ac_service_android_demo.activity.ConfigurationActivity;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.utils.PreferencesUtils;

public class SendDataBle {
	
	String subDomain,physicalDeviceId;
	String message3;
	String message;
	String message1 = "6974637A";
	String message5;
	
	

	public SendDataBle(String subDomain, String physicalDeviceId) {
		super();
		this.subDomain = subDomain;
		this.physicalDeviceId = physicalDeviceId;
	}

	/**
	 * 发送数据
	 * 
	 * @param string2
	 *            Commant_t
	 * @param string4
	 *            控制命令
	 */
	public void sendData(String string2, String string4) {
		message5 = checkSum(string4);// 计算校验位

		if (string4 == "") {
			message3 = "0000";
		} else if (string4 != "") {
			if (string4.length() >= 32) {
				message3 = "00" + Integer.toHexString(string4.length() / 2);
			} else {
				message3 = "000" + Integer.toHexString(string4.length() / 2);// ???????????????十进制int转十六进制字符串
			}
		}
		message = message1 + string2 + message3 + string4 + message5;
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
					public void error(ACException e) {
						// Toast.makeText(MainActivity.this,
						// e.getErrorCode() + "-->" + e.getMessage(),
						// Toast.LENGTH_LONG).show();
					}
				});
		// Log.e("发送的消息", getDeviceMsg(b) + "");
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
		return PreferencesUtils.getInt(MainApplication.getInstance(), "formatType",
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
				// Log.e("AAAAA", ints[i] + "");
				// Log.e("sum", sum + "");
			}
			int res = 255 - sum + 1; // 十进制计算
			// Log.e("res", res + "");

			if (res > 0) { // 如果为正，则校验码为其十六进制数
				results = Integer.toHexString(res);// 十进制int转十六进制字符串
				results = results.substring(results.length() - 2);
			} else if (res < 0) {
				String tenToBinary = Integer.toBinaryString(-res);// 十进制转二进制字符串
				if (tenToBinary.length() >= 8) {
					String substr = tenToBinary
							.substring(tenToBinary.length() - 8);
					// Log.e("string1", substr);
					byte[] bytes1 = substr.getBytes();
					Log.e("bytes[i]", Arrays.toString(bytes1));
					for (int i = 0; i < bytes1.length; i++) {
						if (bytes1[i] == 49) { // 49表示1
							bytes1[i] = 48; // 48表示0
							// Log.e("bytes[i]", bytes1[i] + "");
						} else if (bytes1[i] == 48) {
							bytes1[i] = 49;
							// Log.e("bytes[i]", bytes1[i] + "");
						}
					}
					String sub = new String(bytes1);
					// Log.e("sub", sub);
					int in = Integer.parseInt(sub, 2);
					in = in + 1;
					// Log.e("in", in + "");
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
					// Log.e("pingjie", pingjie);
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

}
