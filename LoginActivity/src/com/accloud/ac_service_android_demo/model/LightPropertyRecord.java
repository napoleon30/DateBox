package com.accloud.ac_service_android_demo.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wangkun on 04/01/2017.
 */
public class LightPropertyRecord extends PropertyRecord {
    //������Դ
    public int source;
    //����
    @SerializedName("switch")
    public int light_on_off;
}
