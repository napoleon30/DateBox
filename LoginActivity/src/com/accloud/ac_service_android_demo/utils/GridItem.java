package com.accloud.ac_service_android_demo.utils;

import com.accloud.ac_service_android_demo.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GridItem extends RelativeLayout implements Checkable {
	private Context mContext;
	private boolean mChecked;// 判断该选项是否被选上的标志量
	private ImageView mImgView = null; // 点击前的图片
	private ImageView mSecletView = null; // 点击红球的图片
	int textColor;

	public GridItem(Context context) {
		this(context, null, 0);
	}

	public GridItem(Context context, int color) {
		this(context, null, 0);
		this.textColor = color;
	}

	public GridItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GridItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.gridview_item1, this); // 初始化item布局
		mImgView = (ImageView) findViewById(R.id.textview_item); // 初始化原始布局id
		mSecletView = (ImageView) findViewById(R.id.textview_item_bg); // 红球的id
	}

	@Override
	public void setChecked(boolean checked) {
		// TODO Auto-generated method stub
		mChecked = checked;
		mSecletView.setVisibility(checked ? View.VISIBLE : View.GONE);// 选上了图片变成红色
//		mSecletView.setBackgroundColor(textColor);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}
	
	public void setImgResId(int resId) {
        if (mImgView != null) {
            mImgView.setBackgroundResource(resId);//设置背景
        }
    }
	
	public void setBackgroudColor(int color){
		mSecletView.setBackgroundColor(color);
	}
	
	
}
