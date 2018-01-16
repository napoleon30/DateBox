package com.accloud.ac_service_android_demo.activity;



import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.accloud.ac_service_android_demo.R;
import com.accloud.ac_service_android_demo.application.MainApplication;
import com.accloud.ac_service_android_demo.config.Config;
import com.accloud.ac_service_android_demo.utils.ItonAdecimalConver;
import com.accloud.ac_service_android_demo.utils.SendDataBle;
import com.accloud.ac_service_android_demo.utils.VibratorUtil;
import com.accloud.cloudservice.AC;
import com.accloud.service.Receiver;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;
import com.hyphenate.chat.EMClient;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThermActivity extends Activity implements OnClickListener {
	public static final String TAG= "ThermActivity";
	
	private ReceiveBroadCast receiveBroadCast;
	SendDataBle sendDataBle;

	String macAddress;
	String physicalDeviceId, deviceId;
	Receiver<TopicData> receiver;// 订阅

	private Button back;
	TextView xText;// 显示温度
	float temp;// 温度值

	String message1, message3, message5, message;
	private String subDomain;
	
	private GraphicalView chart;
	private Timer timer = new Timer();
	private TimerTask task;
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


	public class ReceiveBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 得到广播中得到的数据，并显示出来
			if (intent.getAction().equals("101")) {
				String tem = intent.getStringExtra("tem");
				String Command_t = intent.getStringExtra("Command_t");

				if (tem != null) {
					temp = (float) ItonAdecimalConver.hexStringToAlgorism(tem) / 100;
					if ((temp + "").length() < 5) {
						xText.setText(temp + "0");
					} else {
						xText.setText(temp + "");
					}
					if (temp>38) {
						xText.setTextColor(Color.RED);
						VibratorUtil.Vibrate(ThermActivity.this, 300);//震动
					}else {
						xText.setTextColor(Color.BLACK);
					}
					Log.e("温度", temp + "");
					
					updatechart();

				}
			} else if (intent.getAction().equals("102")) {
				String broad_message = intent
						.getStringExtra("message_login_another_device");
				showDialog(broad_message);
			}
		}
	}
	
	private void showDialog(String broad_message) {
		// TODO Auto-generated method stub
		if (!ThermActivity.this.isFinishing()) {
			AlertDialog.Builder builder = new Builder(ThermActivity.this);
			builder.setTitle(getResources().getString(R.string.offline_notification)).setMessage(broad_message);
			builder.setPositiveButton(getResources().getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
							EMClient.getInstance().logout(true);
							AC.accountMgr().logout();
							Log.e(TAG, "退出登录");
							Long userid = PreferencesUtils.getLong(ThermActivity.this, "userId");
							PushAgent mPushAgent = MainApplication.push();
							//userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
							mPushAgent.removeAlias(String.valueOf(userid), "ablecloud", new UTrack.ICallBack(){
							    @Override
							    public void onMessage(boolean isSuccess, String message) {

							    }
							});
							
							dialog.dismiss();
							ThermActivity.this.finish();
							Intent intent = new Intent(ThermActivity.this,
									LoginActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
									| Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
			AlertDialog alert = builder.create();
			alert.setCanceledOnTouchOutside(false);//dialog点击空白不消失
			alert.setCancelable(false);//dialog点击返回键不消失
			alert.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_therm);
		PushAgent.getInstance(this).onAppStart();
		
		Intent intent = getIntent();
		macAddress = intent.getStringExtra("macAddress");
		physicalDeviceId = intent.getStringExtra("physicalDeviceId");
		deviceId = intent.getStringExtra("deviceId");
		
		Log.e("onCreate--macAddress", macAddress);
		myThread.start();


		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		xText = (TextView) findViewById(R.id.sheshidu);

		// 注册广播接收
		receiveBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("101"); // 只有持有相同的action的接受者才能接收此广播
		filter.addAction("102");
		registerReceiver(receiveBroadCast, filter);
		
		
		// 制作曲线图
				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chart);
				chart = ChartFactory.getLineChartView(MainApplication.getInstance(),
						getdemodataset(), getdemorenderer());
				linearLayout.removeAllViews();// 先remove再add可以实现统计图更新
				linearLayout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));

//				handler2 = new Handler() {
//					public void handleMessage(Message msg) {
//						updatechart();
//					}
//
//				};
//				task = new TimerTask() {
//					public void run() {
//						Message msg = new Message();
//						msg.what = 200;
//						handler2.sendMessage(msg);
//					}
//				};
//				timer.schedule(task, 0, 3000);
	}



	Thread myThread = new Thread(new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sendDataBle.sendData("004a", macAddress + "0030" + "010100");// 作为主机收发数据
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendDataBle.sendData("0049", macAddress + "003001");// 使能通知功能

		}
	});

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

			addY = (float)(Math.round(temp*100))/100;
//			Log.e("addY", addY+"");
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
				
				DecimalFormat df1 = new DecimalFormat(".00");
		        r.setChartValuesFormat(df1);//设置折线点的值的格式,显示小数点后2位
				
				r.setDisplayChartValues(true);//显示折线上点的数值****
				
				render.addSeriesRenderer(r);
				render.setYLabelsAlign(Align.RIGHT);//刻度值相对于刻度的位置
				render.setShowGrid(true);//显示网格
				render.setYAxisMax(40);//设置y轴的范围
				render.setYAxisMin(25);
				render.setYLabels(5);//分七等份
				render.getSeriesRendererAt(0);
//				((XYSeriesRenderer) render.getSeriesRendererAt(0))).setDisplayChartValues(true); //显示折线上点的数值
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
			finish();
			break;

		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		subDomain = PreferencesUtils.getString(this, "subDomain",
				Config.SUBDOMAIN);
		sendDataBle = new SendDataBle(subDomain, physicalDeviceId);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		timer.cancel();
		super.onDestroy();
		unregisterReceiver(receiveBroadCast);
		if (myThread != null) {
			myThread.interrupt();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			sendDataBle.sendData("0045", macAddress);// 断开特定设备连接
			finish();
			return true;
		}
		return false;

	}
}
