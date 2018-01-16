package com.accloud.ac_service_android_demo.activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.R.layout;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.GsonUtil;
import com.accloud.ac_service_android_demo.activity.AI6060HActivity.OnReceive;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendData;
import com.accloud.ac_service_android_demo.utils.SlideSwitch;
import com.accloud.ac_service_android_demo.utils.SlideSwitch.SlideListener;
import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class PowerActivity extends Activity implements OnClickListener {
	private static final String TAG = PowerActivity.class.getSimpleName();

	private String deviceName, deviceID, physicalDeviceId;
	private String subDomain;
	Receiver<TopicData> receiver;// 订阅
	SendData senddata;
	String rece = null;
	float elec = 0.000f;

	TextView title;
	Button back;
	TextView rece_content;
	Button bt_clear;
	Switch elec_switch;

	boolean isElec = false;

	TextView tv_electricity;
	LinearLayout ll_electricity;

	TextView tv_power;
	LinearLayout ll_power;

	private GraphicalView chart_electricity;
	private Float addY_electricity;
	private String addX_electricity;
	String[] xkedu_electricity = new String[5];// x轴数据缓冲

	Float[] ycache_electricity = new Float[5];

	// private final static int SERISE_NR = 1; //曲线数量
	private XYSeries series_electricity;// 用来清空第一个再加下一个
	private XYMultipleSeriesDataset dataset1_electricity;// xy轴数据源
	private XYMultipleSeriesRenderer render_electricity;
	SimpleDateFormat shijian = new SimpleDateFormat("hh:mm:ss");

	private GraphicalView chart_power;
	private Float addY_power;
	private String addX_power;
	String[] xkedu_power = new String[5];// x轴数据缓冲

	Float[] ycache_power = new Float[5];

	// private final static int SERISE_NR = 1; //曲线数量
	private XYSeries series_power;// 用来清空第一个再加下一个
	private XYMultipleSeriesDataset dataset1_power;// xy轴数据源
	private XYMultipleSeriesRenderer render_power;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_power);
		PushAgent.getInstance(this).onAppStart();

		Intent intent = getIntent();
		deviceName = intent.getStringExtra("deviceName");
		deviceID = intent.getStringExtra("deviceId");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");

		Log.e("CarActivity", deviceID + "*" + physicalDeviceId + "*"
				+ deviceName);

		title = (TextView) findViewById(R.id.header_title);
		title.setText(deviceName);
		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(this);
		rece_content = (TextView) findViewById(R.id.tv_receive_content);
		bt_clear = (Button) findViewById(R.id.btn_clear);
		bt_clear.setOnClickListener(this);

		elec_switch = (Switch) findViewById(R.id.switch_elec);
		elec_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					isElec = true;
				} else {
					isElec = false;
				}

			}
		});

		initElectricity();
		initPower();

	}

	private void initPower() {
		tv_power = (TextView) findViewById(R.id.tv_power);
		ll_power = (LinearLayout) findViewById(R.id.ll_power_chart);

		chart_power = ChartFactory.getLineChartView(
				MainApplication.getInstance(), getdemodataset_power(),
				getdemorenderer_power());
		ll_power.removeAllViews();// 先remove再add可以实现统计图更新
		ll_power.addView(chart_power, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	}

	private void initElectricity() {
		tv_electricity = (TextView) findViewById(R.id.tv_electricity);
		ll_electricity = (LinearLayout) findViewById(R.id.ll_electricity_chart);

		chart_electricity = ChartFactory.getLineChartView(
				MainApplication.getInstance(), getdemodataset_electricity(),
				getdemorenderer_electricity());
		ll_electricity.removeAllViews();// 先remove再add可以实现统计图更新
		ll_electricity.addView(chart_electricity, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;

		case R.id.btn_clear:
			rece_content.setText("");
			break;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		subscribe("xinlian01", "topic_type", deviceID);// 订阅，可获取到返回值

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		senddata = new SendData(subDomain, physicalDeviceId);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AC.customDataMgr().unregisterDataReceiver(receiver);
	}

	/**
	 * 订阅方法和获取返回值
	 * 
	 * @param submain
	 * @param topic_type
	 * @param deviceId
	 */
	private void subscribe(String submain, String topic_type,
			final String deviceId) {

		AC.customDataMgr().subscribe(
				Topic.customTopic(submain, topic_type, deviceId),
				new VoidCallback() {

					@Override
					public void error(ACException arg0) {
						Log.e("/*/*/*", "订阅失败");
					}

					@Override
					public void success() {
						Log.e("/*/*/*", "订阅成功");
					}
				});
		receiver = new Receiver<TopicData>() {

			@Override
			public void onReceive(TopicData arg0) {
				Log.e("订阅onReceive_PowerActivity", arg0.getValue());
				String jsonData = arg0.getValue();
				OnReceive onRece = GsonUtil.parseJsonWithGson(jsonData,
						OnReceive.class);
				String[] pay = onRece.getPayload();
				byte[] arraysPay = Base64.decode(pay[0], 0);
				String payload = ItonAdecimalConver.byte2hex(arraysPay)
						.replace(" ", "");
				Log.e("接收到的值_PowerActivity", payload);
				if (isElec == true) {
					rece = ItonAdecimalConver.AsciiStringToString(payload);
					Log.e(TAG, "rece1:" + rece);
					if (rece.length() >= 6) {

						Log.e("接收到的值_rece", rece);
						rece = rece.substring(0, 6);
						rece_content.append(payload + "\n");

						if (rece.startsWith("0")) {
							rece = rece.substring(1);
							Log.e(TAG, "RECE2:" + rece);
						}
						BigDecimal bd = new BigDecimal(
								Double.parseDouble(rece) / 1000);
						elec = bd.setScale(3, BigDecimal.ROUND_HALF_UP)
								.floatValue();
						Log.e(TAG, "ELEC:" + elec);
						tv_electricity.setText(elec + " A");

						BigDecimal bd2 = new BigDecimal(elec * 110);
						float f_power = bd2.setScale(3,
								BigDecimal.ROUND_HALF_UP).floatValue();

						tv_power.setText(f_power + " W");

						updatechart_electricity();// ////////////////////////////////////
						updatechart_power();// ////////////////////////////////////

					}
				}
			}

		};
		AC.customDataMgr().registerDataReceiver(receiver);
	}

	// 更新折线图
	private void updatechart_power() {
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳5个，所以当点数超过5时，长度永远是5
		int length = series_power.getItemCount();
		int a = length;
		if (length > 5) {
			length = 5;
		}
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//要不要判断再说 } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		addX_power = shijian.format(new java.util.Date());

		BigDecimal bd = new BigDecimal(elec * 220);
		addY_power = bd.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();

		// Log.e("addY", addY+"");
		// 移除数据集中旧的点集
		dataset1_power.removeSeries(series_power);
		if (a < 5)// 当数据集中不够五个点的时候直接添加就好，因为初始化的时候只有一个点，所以前几次更新的时候直接添加
		{
			series_power.add(a + 1, addY_power);// 第一个参数代表第几个点，要与下面语句中的第一个参数对应
			render_power.addXTextLabel(a + 1, addX_power);
			xkedu_power[a] = addX_power;
		} else // 超过了五个点要去除xcache【0】换成【1】的.....
		{
			// 将旧的点集中x和y的数值取出来放入backup中，造成曲线向左平移的效果
			for (int i = 0; i < length - 1; i++) {
				ycache_power[i] = (float) series_power.getY(i + 1);
				xkedu_power[i] = xkedu_power[i + 1];
			}

			// 点集先清空，为了做成新的点集而准备
			series_power.clear();
			// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中

			for (int k = 0; k < length - 1; k++) {
				series_power.add(k + 1, ycache_power[k]);
				render_power.addXTextLabel(k + 1, xkedu_power[k]);
			}
			xkedu_power[4] = addX_power;
			series_power.add(5, addY_power);
			render_power.addXTextLabel(5, addX_power);
		}
		// 在数据集中添加新的点集
		dataset1_power.addSeries(series_power);
		// 视图更新，没有这一步，曲线不会呈现动态
		chart_power.invalidate();
	}

	private XYMultipleSeriesRenderer getdemorenderer_power() {
		// TODO Auto-generated method stub
		render_power = new XYMultipleSeriesRenderer();
		render_power.setChartTitle(this.getString(R.string.power_time));
		render_power.setChartTitleTextSize(50);// 设置整个图表标题文字的大小
		render_power.setAxisTitleTextSize(35);// 设置轴标题文字的大小
		render_power.setAxesColor(Color.BLACK);
		render_power.setXTitle(this.getString(R.string.time));
		render_power.setYTitle(this.getString(R.string.power1));

		render_power.setLabelsTextSize(35);// 设置轴刻度文字的大小
		render_power.setLabelsColor(Color.BLACK);
		render_power.setXLabelsColor(Color.BLACK);
		render_power.setYLabelsColor(0, Color.BLACK);
		render_power.setLegendTextSize(25);// 设置图例文字大小
		render_power.setShowLegend(false);// 显示不显示在这里设置

		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer r = new XYSeriesRenderer();// 设置颜色和点类型
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		r.setFillPoints(true);
		r.setLineWidth(5);// 这是线宽
		r.setChartValuesSpacing(10);// 显示的点的值与图的距离
		r.setChartValuesTextSize(30);// 设置数值的字体大小

		DecimalFormat df1 = new DecimalFormat("0.000");
		r.setChartValuesFormat(df1);// 设置折线点的值的格式,显示小数点后3位

		r.setDisplayChartValues(true);// 显示折线上点的数值****

		render_power.addSeriesRenderer(r);
		render_power.setYLabelsAlign(Align.RIGHT);// 刻度值相对于刻度的位置
		render_power.setShowGrid(true);// 显示网格
		render_power.setYAxisMax(1650);// 设置y轴的范围
		render_power.setYAxisMin(-550);
		render_power.setYLabels(5);// 分5等份
		render_power.getSeriesRendererAt(0);
		((XYSeriesRenderer) render_power.getSeriesRendererAt(0))
				.setDisplayChartValues(true); // 显示折线上点的数值
		render_power.setInScroll(true);
		render_power.setLabelsTextSize(25);
		render_power.setLabelsColor(Color.BLACK);

		render_power.setPanEnabled(true, false);// false表示禁止报表的拖动，X轴、Y轴平移限制
		render_power.setPointSize(8f);// 设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
		render_power.setMargins(new int[] { 60, 80, 40, 40 }); // 设置图形四周的留白（上左下右）
		render_power.setMarginsColor(Color.WHITE);
		render_power.setXLabels(0);// 取消X坐标的数字zjk,只有自己定义横坐标是才设为此值

		return render_power;
	}

	private XYMultipleSeriesDataset getdemodataset_power() {
		// TODO Auto-generated method stub
		dataset1_power = new XYMultipleSeriesDataset();// xy轴数据源
		series_power = new XYSeries(this.getString(R.string.power));// 这个事是显示多条用的，显不显示在上面render设置
		// 这里相当于初始化，初始化中无需添加数据，因为如果这里添加第一个数据的话，
		// 很容易使第一个数据和定时器中更新的第二个数据的时间间隔不为两秒，所以下面语句屏蔽
		// 这里可以一次更新五个数据，这样的话相当于开始的时候就把五个数据全部加进去了，但是数据的时间是不准确或者间隔不为二的
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//横坐标date数据类型，纵坐标随即数等待更新

		dataset1_power.addSeries(series_power);
		return dataset1_power;
	}

	// 更新折线图
	private void updatechart_electricity() {
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳5个，所以当点数超过5时，长度永远是5
		int length = series_electricity.getItemCount();
		int a = length;
		if (length > 5) {
			length = 5;
		}
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//要不要判断再说 } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		addX_electricity = shijian.format(new java.util.Date());

		addY_electricity = elec;
		// Log.e("addY", addY+"");
		// 移除数据集中旧的点集
		dataset1_electricity.removeSeries(series_electricity);
		if (a < 5)// 当数据集中不够五个点的时候直接添加就好，因为初始化的时候只有一个点，所以前几次更新的时候直接添加
		{
			series_electricity.add(a + 1, addY_electricity);// 第一个参数代表第几个点，要与下面语句中的第一个参数对应
			render_electricity.addXTextLabel(a + 1, addX_electricity);
			xkedu_electricity[a] = addX_electricity;
		} else // 超过了五个点要去除xcache【0】换成【1】的.....
		{
			// 将旧的点集中x和y的数值取出来放入backup中，造成曲线向左平移的效果
			for (int i = 0; i < length - 1; i++) {
				ycache_electricity[i] = (float) series_electricity.getY(i + 1);
				xkedu_electricity[i] = xkedu_electricity[i + 1];
			}

			// 点集先清空，为了做成新的点集而准备
			series_electricity.clear();
			// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中

			for (int k = 0; k < length - 1; k++) {
				series_electricity.add(k + 1, ycache_electricity[k]);
				render_electricity.addXTextLabel(k + 1, xkedu_electricity[k]);
			}
			xkedu_electricity[4] = addX_electricity;
			series_electricity.add(5, addY_electricity);
			render_electricity.addXTextLabel(5, addX_electricity);
		}
		// 在数据集中添加新的点集
		dataset1_electricity.addSeries(series_electricity);
		// 视图更新，没有这一步，曲线不会呈现动态
		chart_electricity.invalidate();
	}

	private XYMultipleSeriesRenderer getdemorenderer_electricity() {
		// TODO Auto-generated method stub
		render_electricity = new XYMultipleSeriesRenderer();
		render_electricity.setChartTitle(this
				.getString(R.string.electricity_time));
		render_electricity.setChartTitleTextSize(50);// 设置整个图表标题文字的大小
		render_electricity.setAxisTitleTextSize(35);// 设置轴标题文字的大小
		render_electricity.setAxesColor(Color.BLACK);
		render_electricity.setXTitle(this.getString(R.string.time));
		render_electricity.setYTitle(this.getString(R.string.electricity1));

		render_electricity.setLabelsTextSize(35);// 设置轴刻度文字的大小
		render_electricity.setLabelsColor(Color.BLACK);
		render_electricity.setXLabelsColor(Color.BLACK);
		render_electricity.setYLabelsColor(0, Color.BLACK);
		render_electricity.setLegendTextSize(25);// 设置图例文字大小
		render_electricity.setShowLegend(false);// 显示不显示在这里设置

		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer r = new XYSeriesRenderer();// 设置颜色和点类型
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		r.setFillPoints(true);
		r.setLineWidth(5);// 这是线宽
		r.setChartValuesSpacing(10);// 显示的点的值与图的距离
		r.setChartValuesTextSize(30);// 设置数值的字体大小

		DecimalFormat df1 = new DecimalFormat("0.000");
		r.setChartValuesFormat(df1);// 设置折线点的值的格式,显示小数点后3位

		r.setDisplayChartValues(true);// 显示折线上点的数值****

		render_electricity.addSeriesRenderer(r);
		render_electricity.setYLabelsAlign(Align.RIGHT);// 刻度值相对于刻度的位置
		render_electricity.setShowGrid(true);// 显示网格
		render_electricity.setYAxisMax(15);// 设置y轴的范围
		render_electricity.setYAxisMin(-5);
		render_electricity.setYLabels(5);// 分5等份
		render_electricity.getSeriesRendererAt(0);
		((XYSeriesRenderer) render_electricity.getSeriesRendererAt(0))
				.setDisplayChartValues(true); // 显示折线上点的数值
		render_electricity.setInScroll(true);
		render_electricity.setLabelsTextSize(25);
		render_electricity.setLabelsColor(Color.BLACK);

		render_electricity.setPanEnabled(true, false);// false表示禁止报表的拖动，X轴、Y轴平移限制
		render_electricity.setPointSize(8f);// 设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
		render_electricity.setMargins(new int[] { 60, 80, 40, 40 }); // 设置图形四周的留白（上左下右）
		render_electricity.setMarginsColor(Color.WHITE);
		render_electricity.setXLabels(0);// 取消X坐标的数字zjk,只有自己定义横坐标是才设为此值

		return render_electricity;
	}

	private XYMultipleSeriesDataset getdemodataset_electricity() {
		// TODO Auto-generated method stub
		dataset1_electricity = new XYMultipleSeriesDataset();// xy轴数据源
		series_electricity = new XYSeries(this.getString(R.string.electricity));// 这个事是显示多条用的，显不显示在上面render设置
		// 这里相当于初始化，初始化中无需添加数据，因为如果这里添加第一个数据的话，
		// 很容易使第一个数据和定时器中更新的第二个数据的时间间隔不为两秒，所以下面语句屏蔽
		// 这里可以一次更新五个数据，这样的话相当于开始的时候就把五个数据全部加进去了，但是数据的时间是不准确或者间隔不为二的
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//横坐标date数据类型，纵坐标随即数等待更新

		dataset1_electricity.addSeries(series_electricity);
		return dataset1_electricity;
	}
}
