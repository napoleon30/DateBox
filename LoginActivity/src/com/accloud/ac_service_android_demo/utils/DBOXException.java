package com.accloud.ac_service_android_demo.utils;

import com.accloud.ac_service_android_demo.R;

import android.content.Context;
import android.widget.Toast;

public class DBOXException {
	Context context;
	String exception;
	public static void errorCode(Context context,int code){
		switch (code) {
		case 1986:
			Pop.popToast(context,context.getString(R.string.no_wifi));
			break;
		case 1987:
			Pop.popToast(context,context.getString(R.string.no_net));
			break;
		case 1989:
			Pop.popToast(context,context.getString(R.string.decryption_error));
			break;
		case 1990:
			Pop.popToast(context,context.getString(R.string.unmatched_device));
			break;
		case 1991:
			Pop.popToast(context,context.getString(R.string.invalid_parameter));
			break;
		case 1993:
			Pop.popToast(context,context.getString(R.string.request_timeout));
			break;
		case 1998:
			Pop.popToast(context,context.getString(R.string.network_error1));
			break;
		case 1999:
			Pop.popToast(context,context.getString(R.string.internal_error));
			break;
		case 3000:
			Pop.popToast(context,context.getString(R.string.system_error));
			break;
		case 3002:
			Pop.popToast(context,context.getString(R.string.illegal_request));
			break;
		case 3003:
			Pop.popToast(context,context.getString(R.string.unsupport_request));
			break;
		case 3004:
			Pop.popToast(context,context.getString(R.string.unallowed_request));
			break;
		case 3005:
			Pop.popToast(context,context.getString(R.string.request_without_permission));
			break;
		case 3009:
			Pop.popToast(context,context.getString(R.string.request_not_exist));
			break;
		case 30011:
			Pop.popToast(context,context.getString(R.string.service_unavailable));
			break;
		case 30012:
			Pop.popToast(context,context.getString(R.string.request_timeout1));
			break;
		case 30013:
			Pop.popToast(context,context.getString(R.string.net_anomaly));
			break;
		case 30018:
			Pop.popToast(context,context.getString(R.string.service_error));
			break;
		case 3501:
			Pop.popToast(context,context.getString(R.string.account_no_exist));
			break;
		case 3502:
			Pop.popToast(context,context.getString(R.string.account_exist));
			break;
		case 3503:
			Pop.popToast(context,context.getString(R.string.account_illegal));
			break;
		case 3504:
			Pop.popToast(context,context.getString(R.string.password_error));
			break;
		case 3505:
			Pop.popToast(context,context.getString(R.string.ver_code_error));
			break;
		case 3506:
			Pop.popToast(context,context.getString(R.string.ver_code_invalid));
			break;
		case 3507:
			Pop.popToast(context,context.getString(R.string.email_illegal));
			break;
		case 3508:
			Pop.popToast(context,context.getString(R.string.phone_illegal));
			break;
		case 3509:
			Pop.popToast(context,context.getString(R.string.account_anomaly));
			break;
		case 3510:
			Pop.popToast(context,context.getString(R.string.account_bound));
			break;
		case 3802:
			Pop.popToast(context,context.getString(R.string.device_no_exist));
			break;
		case 3803:
			Pop.popToast(context,context.getString(R.string.device_exist));
			break;
		case 3804:
			Pop.popToast(context,context.getString(R.string.message_illegal));
			break;
		case 3807:
			Pop.popToast(context,context.getString(R.string.device_no_online));
			break;
		case 3811:
			Pop.popToast(context,context.getString(R.string.device_bound));
			break;
		case 3812:
			Pop.popToast(context,context.getString(R.string.device_no_bound));
			break;
		case 3813:
			Pop.popToast(context,context.getString(R.string.device_abnormality));
			break;
		case 3814:
			Pop.popToast(context,context.getString(R.string.response_timeout));
			break;
		case 3818:
			Pop.popToast(context,context.getString(R.string.bind_timeout));
			break;
		case 3822:
			Pop.popToast(context,context.getString(R.string.device_not_activated));
			break;
		case 3823:
			Pop.popToast(context,context.getString(R.string.device_busy));
			break;
		case 3858:
			Pop.popToast(context,context.getString(R.string.device_unauthorized));
			break;
		
		
		}
	}
	

}
