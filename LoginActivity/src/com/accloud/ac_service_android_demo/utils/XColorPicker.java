package com.accloud.ac_service_android_demo.utils;

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.MotionEvent;
import com.accloud.ac_service_android_demo.R;

@SuppressLint({ "Recycle", "DrawAllocation", "ClickableViewAccessibility" })
public class XColorPicker extends LinearLayout {

	private Bitmap bitmap;

	private int[] colors;
	private Paint mPaint;// 渐变色环画笔
	private float width;
	private float height;
	private float rectWidth;
	private float rectHeight;
	private float imgWidth;
	private float imgHeight;

	private float cursorX;
	private float lastX;
	private boolean isMove;

	public XColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.XColorPicker);
		rectHeight = a.getDimensionPixelSize(
				R.styleable.XColorPicker_rectHeight, 0);
		Log.e("rectHeight", "rectHeight = " + rectHeight);
		imgHeight = rectHeight * 69 / 12;
		imgWidth = rectHeight * 21 / 12;
		Log.e("滑块的宽度imgWidth", imgWidth + "");
		cursorX = 0;
		Bitmap temp = decodeResource(context, R.drawable.color_stall);
		bitmap = zoom(temp, imgHeight / temp.getHeight());
		setBackgroundColor(0x00ffffff);// 透明白色背景
		setWillNotDraw(false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		Log.e("控件的宽和高", "width = " + width + " ,height = " + height);
		rectWidth = width - imgWidth;
		Log.e("色板条的宽度rectWidth", "" + rectWidth);
		// colors = new int[] { 0xFF000000, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000,
		// 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFFFFFFFF };//带黑色
		colors = new int[] { 0xffff0000, 0xffff00ff, 0xff0000ff, 0xff00ffff,
				0xff00ff00, 0xffffff00, 0xffff0000 };// 不带黑色
		Shader shader = new LinearGradient(imgWidth / 2, 0, rectWidth
				+ imgWidth / 2, 0, colors, null, Shader.TileMode.MIRROR);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setShader(shader);
		mPaint.setStrokeWidth(rectHeight * 0.5f);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(new RectF((width - rectWidth) / 2, height / 2
				- rectHeight / 2, (width + rectWidth) / 2, height / 2
				+ rectHeight / 2), mPaint);
		canvas.drawBitmap(bitmap, cursorX, height / 2 - imgHeight / 2, null);
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			isMove = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isMove) {
				return false;
			}
			float dx = event.getX() - lastX;
			if (cursorX + dx <= 0) {
				dx = -cursorX;
			}
			if (cursorX + dx >= width - imgWidth) {
				dx = width - imgWidth - cursorX;
			}
			cursorX = cursorX + dx;
			lastX = event.getX();
			int color = interpRectColor(colors, cursorX);
			// Log.e("滑块位置", cursorX + "");

			// 色板条分为六份，每份再分为一百份
			if (listener != null) {
				listener.OnColorChanged(color);
				String byte2 = "00";
				String byte3 = "00";
				if (cursorX >= 0 && cursorX <= rectWidth / 6) {
					for (int i = 0; i <= 100; i++) {
						if (rectWidth * i / 600 <= cursorX
								&& cursorX < (i + 1) * rectWidth / 600) {
							listener.setByteTwo("00");
							listener.setByteThree(i + "");
							byte2 = "00";
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {

								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				} else if (cursorX > rectWidth / 6 && cursorX <= rectWidth / 3) {

					for (int i = 0; i <= 100; i++) {
						if (rectWidth * (i + 100) / 600 < cursorX
								&& cursorX <= rectWidth * (i + 101) / 600) {
							listener.setByteTwo("01");
							listener.setByteThree(i + "");
							byte2 = "01";
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {

								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				} else if (cursorX > rectWidth / 3 && cursorX <= rectWidth / 2) {

					for (int i = 0; i <= 100; i++) {
						if (rectWidth * (i + 200) / 600 < cursorX
								&& cursorX <= (i + 201) * rectWidth / 600) {
							listener.setByteTwo("02");
							listener.setByteThree(i + "");
							byte2 = "02";
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {

								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				} else if (cursorX > rectWidth / 2
						&& cursorX <= rectWidth * 2 / 3) {

					for (int i = 0; i <= 100; i++) {
						if (rectWidth * (i + 300) / 600 < cursorX
								&& cursorX <= (i + 301) * rectWidth / 600) {
							listener.setByteTwo("03");
							listener.setByteThree(i + "");
							byte2 = "03";
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {

								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				} else if (cursorX > rectWidth * 2 / 3
						&& cursorX <= rectWidth * 5 / 6) {

					for (int i = 0; i <= 100; i++) {
						if (rectWidth * (i + 400) / 600 < cursorX
								&& cursorX <= (i + 401) * rectWidth / 600) {
							listener.setByteTwo("04");
							listener.setByteThree(i + "");
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {
								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				} else if (cursorX > rectWidth * 5 / 6 && cursorX <= rectWidth) {

					for (int i = 0; i <= 100; i++) {
						if (rectWidth * (i + 500) / 600 < cursorX
								&& cursorX <= (i + 501) * rectWidth / 600) {
							listener.setByteTwo("05");
							listener.setByteThree(i + "");
							byte2 = "05";
							byte3 = i + "";
							if (i <= 16) {
								byte3 = "0"
										+ Integer.toHexString(Integer.valueOf(
												byte3).intValue());
							} else {

								byte3 = Integer.toHexString(Integer.valueOf(
										byte3).intValue());
							}
						}
					}
				}
				listener.SendData(byte2, byte3);

			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			isMove = false;
			break;
		}
		return true;
	}

	/**
	 * 获取渐变块上颜色
	 * 
	 * @param colors
	 * @param x
	 * @return
	 */
	private int interpRectColor(int colors[], float x) {

		float unit = x / rectWidth;

		if (unit <= 0) {
			return colors[0];
		}
		if (unit >= 1) {
			return colors[colors.length - 1];
		}

		float p = unit * (colors.length - 1);
		int i = (int) p;
		p -= i;

		int c0 = colors[i];
		int c1 = colors[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		return Color.argb(a, r, g, b);
	}

	private int ave(int s, int d, float p) {
		return s + Math.round(p * (d - s));
	}

	public boolean contains(RectF rect, float x, float y) {
		return x >= rect.left && x < rect.right && y >= rect.top
				&& y < rect.bottom;
	}

	public Bitmap decodeResource(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/**
	 * Bitmap缩小的方法
	 */
	private Bitmap zoom(Bitmap bitmap, float ratio) {
		Log.d("", "ratio = " + ratio);
		Matrix matrix = new Matrix();
		matrix.postScale(ratio, ratio); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	private OnColorChangedListener listener;

	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.listener = listener;
	}

	public interface OnColorChangedListener {
		/**
		 * 回调函数
		 * 
		 * @param color
		 *            选中的颜色
		 */
		void OnColorChanged(int color);

		String setByteTwo(String byte2);

		String setByteThree(String byte3);

		void SendData(String byte2, String byte3);
	}
}
