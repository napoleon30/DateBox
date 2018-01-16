package com.accloud.ac_service_android_demo.fragment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.activity.PagerActivity;
import com.accloud.ac_service_android_demo.activity.ThermActivity;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
import com.accloud.ac_service_android_demo.utils.VibratorUtil;
import com.accloud.utils.PreferencesUtils;

public class ThermFragment extends Fragment {
	public static final String TAG = "ThermFragment";
	private LocalBroadcastManager mLBM;
	private DetailBroadCastReceiver detailBroadCastReceiver;

	SendData senddata;

	private TextView therm;
	private EditText setTime;
	private ToggleButton start_or_open;
	private EditText alarmTem;
	String time;
	float payload_therm;

	LinearLayout layout;

	private String deviceName, deviceID, physicalDeviceId, subDomain;

	private GraphicalView chart;
//	private Timer timer = new Timer();
//	private TimerTask task;
	private Float addY;
	private String addX;

	String[] xkedu = new String[5];// x轴数据缓冲

	Float[] ycache = new Float[5];

	// private final static int SERISE_NR = 1; //曲线数量
	private XYSeries series;// 用来清空第一个再加下一个
	private XYMultipleSeriesDataset dataset1;// xy轴数据源
	private XYMultipleSeriesRenderer render;
	SimpleDateFormat shijian = new SimpleDateFormat("hh:mm:ss");

	Handler handler2;

	public ThermFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		subDomain = PreferencesUtils.getString(MainApplication.getInstance(),
				"subDomain", Config.SUBDOMAIN);
		InitDetailBroadCastReceiver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fg_therm, container, false);
		deviceName = ((PagerActivity) getActivity()).getDeviceName();
		deviceID = ((PagerActivity) getActivity()).getDeviceId();
		physicalDeviceId = ((PagerActivity) getActivity())
				.getPhysicalDeviceId();
		Log.e(TAG, deviceID + "*" + physicalDeviceId + "*" + deviceName);

		senddata = new SendData(subDomain, physicalDeviceId);
		
		alarmTem = (EditText) view.findViewById(R.id.et_alarm_tem_num);

		therm = (TextView) view.findViewById(R.id.txt_content);
		setTime = (EditText) view.findViewById(R.id.et_set_time);
		if (!setTime.getText().toString().trim().equals("")) {
			time = ItonAdecimalConver.algorismToHEXString(Integer
					.parseInt(setTime.getText().toString().trim()));
			
		} else {
			time = "0003";
		}
		Log.e("time", time);

		senddata.sendData("66C40C" + "000000000000000000000000" + "99");//关闭温度上报

		start_or_open = (ToggleButton) view
				.findViewById(R.id.btn_start_or_close);
		start_or_open.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!setTime.getText().toString().trim().equals("")) {
					time = ItonAdecimalConver.algorismToHEXString(Integer
							.parseInt(setTime.getText().toString().trim()));
					if (time.length()==2) {
						time = "00"+time;
					}else if (time.length()==3) {
						time = "0"+time;
					}
					
				} else {
					time = "0003";
				}
				Log.e("time", time);

				if (isChecked) {
					senddata.sendData("66C30C" + time + "00000000000000000000"
							+ "99");
				} else {
					senddata.sendData("66C40C" + "000000000000000000000000"
							+ "99");
					therm.setText("");
				}
			}
		});

		// 制作曲线图
		LinearLayout linearLayout = (LinearLayout) view
				.findViewById(R.id.chart);
		chart = ChartFactory.getLineChartView(MainApplication.getInstance(),
				getdemodataset(), getdemorenderer());
		linearLayout.removeAllViews();// 先remove再add可以实现统计图更新
		linearLayout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

//		handler2 = new Handler() {
//			public void handleMessage(Message msg) {
//				updatechart();
//			}
//
//		};
//		task = new TimerTask() {
//			public void run() {
//				Message msg = new Message();
//				msg.what = 200;
//				handler2.sendMessage(msg);
//			}
//		};
//		timer.schedule(task, 0, Long.parseLong(millisecond));

		return view;
	}

	/**
	 * 初始化Detail广播LocalBroadcastReceiver
	 */
	private void InitDetailBroadCastReceiver() {
		mLBM = LocalBroadcastManager.getInstance(getActivity());
		detailBroadCastReceiver = new DetailBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("PagerPayload");
		mLBM.registerReceiver(detailBroadCastReceiver, intentFilter);
	}

	/**
	 * 广播接收处理函数
	 * 
	 * @author admin
	 * 
	 */
	public class DetailBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String payload = (String) intent.getExtras().get("payload");
			Log.e("温度", payload);
			if (payload.substring(2, 4).equals("C2")) {
				payload = payload.substring(6, 10);
				payload_therm = (float) ItonAdecimalConver
						.hexStringToAlgorism(payload) / 10;
				
				BigDecimal b = new BigDecimal(payload_therm);
				float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
				therm.setText(f1 + "℃");
				Log.e("wendu--", f1+"℃");
				String alarm;
				if (alarmTem.getText().toString().trim().equals("")) {
					alarm = "30";
				}else {
					alarm = alarmTem.getText().toString().trim();
				}
				if (f1>Float.parseFloat(alarm)) {
					Log.e(TAG, f1+"**"+Float.parseFloat(alarm));
					therm.setTextColor(Color.RED);
					VibratorUtil.Vibrate((PagerActivity) getActivity(), 300);//震动
				}else {
					therm.setTextColor(Color.BLACK);
				}
				updatechart();

			}
		}

	}

	// 更新折线图
	private void updatechart() {
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳5个，所以当点数超过5时，长度永远是5
		int length = series.getItemCount();
		int a = length;
		if (length > 5) {
			length = 5;
		}
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//要不要判断再说 } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		addX = shijian.format(new java.util.Date());

		addY = (float)(Math.round(payload_therm*100))/100;
//		Log.e("addY", addY+"");
		// 移除数据集中旧的点集
		dataset1.removeSeries(series);
		if (a < 5)// 当数据集中不够五个点的时候直接添加就好，因为初始化的时候只有一个点，所以前几次更新的时候直接添加
		{
			series.add(a + 1, addY);// 第一个参数代表第几个点，要与下面语句中的第一个参数对应
			render.addXTextLabel(a + 1, addX);
			xkedu[a] = addX;
		} else // 超过了五个点要去除xcache【0】换成【1】的.....
		{
			// 将旧的点集中x和y的数值取出来放入backup中，造成曲线向左平移的效果
			for (int i = 0; i < length - 1; i++) {
				ycache[i] = (float) series.getY(i + 1);
				xkedu[i] = xkedu[i + 1];
			}

			// 点集先清空，为了做成新的点集而准备
			series.clear();
			// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中

			for (int k = 0; k < length - 1; k++) {
				series.add(k + 1, ycache[k]);
				render.addXTextLabel(k + 1, xkedu[k]);
			}
			xkedu[4] = addX;
			series.add(5, addY);
			render.addXTextLabel(5, addX);
		}
		// 在数据集中添加新的点集
		dataset1.addSeries(series);
		// 视图更新，没有这一步，曲线不会呈现动态
		chart.invalidate();
	}

	private XYMultipleSeriesRenderer getdemorenderer() {
			// TODO Auto-generated method stub
			render = new XYMultipleSeriesRenderer();
			render.setChartTitle("温度时间曲线");
			render.setChartTitleTextSize(30);//设置整个图表标题文字的大小
			render.setAxisTitleTextSize(18);//设置轴标题文字的大小
			render.setAxesColor(Color.BLACK);
			render.setXTitle("时间");
			render.setYTitle("温度");
			
			render.setLabelsTextSize(25);//设置轴刻度文字的大小
			render.setLabelsColor(Color.BLACK);
			render.setXLabelsColor(Color.BLACK);
			render.setYLabelsColor(0, Color.BLACK);
			render.setLegendTextSize(25);//设置图例文字大小  
			render.setShowLegend(false);//显示不显示在这里设置，非常完美,对应第184行
			
			// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
			XYSeriesRenderer r = new XYSeriesRenderer();//设置颜色和点类型
			r.setColor(Color.RED);
			r.setPointStyle(PointStyle.CIRCLE);//设置点的样式
			r.setFillPoints(true);
			r.setLineWidth(3);// 这是线宽
			r.setChartValuesSpacing(3);
			r.setChartValuesTextSize(30);//设置数值的字体大小 
			
			DecimalFormat df1 = new DecimalFormat(".0");
	        r.setChartValuesFormat(df1);//设置折线点的值的格式,显示小数点后1位
			
			r.setDisplayChartValues(true);//显示折线上点的数值****
			
			render.addSeriesRenderer(r);
			render.setYLabelsAlign(Align.RIGHT);//刻度值相对于刻度的位置
			render.setShowGrid(true);//显示网格
			render.setYAxisMax(40);//设置y轴的范围
			render.setYAxisMin(0);
			render.setYLabels(5);//分七等份
			render.getSeriesRendererAt(0);
//			((XYSeriesRenderer) render.getSeriesRendererAt(0))).setDisplayChartValues(true); //显示折线上点的数值
			render.setInScroll(true);
			render.setLabelsTextSize(25);
			render.setLabelsColor(Color.BLACK);
			
			
			
			render.setPanEnabled(false,true);//false表示禁止报表的拖动，X轴、Y轴平移限制
			render.setPointSize(5f);//设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
			render.setMargins(new int[]{60,40,40,40}); //设置图形四周的留白（上左下右）
			render.setMarginsColor(Color.WHITE);
			render.setXLabels(0);// 取消X坐标的数字zjk,只有自己定义横坐标是才设为此值
			
			return render;
		}

	private XYMultipleSeriesDataset getdemodataset() {
		// TODO Auto-generated method stub
		dataset1 = new XYMultipleSeriesDataset();// xy轴数据源
		series = new XYSeries("温度 ");// 这个事是显示多条用的，显不显示在上面render设置
		// 这里相当于初始化，初始化中无需添加数据，因为如果这里添加第一个数据的话，
		// 很容易使第一个数据和定时器中更新的第二个数据的时间间隔不为两秒，所以下面语句屏蔽
		// 这里可以一次更新五个数据，这样的话相当于开始的时候就把五个数据全部加进去了，但是数据的时间是不准确或者间隔不为二的
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//横坐标date数据类型，纵坐标随即数等待更新

		dataset1.addSeries(series);
		return dataset1;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroy() {
//		timer.cancel();
		super.onDestroy();
		mLBM.unregisterReceiver(detailBroadCastReceiver);
	}

}
