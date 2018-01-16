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

	String[] xkedu = new String[5];// x�����ݻ���

	Float[] ycache = new Float[5];

	// private final static int SERISE_NR = 1; //��������
	private XYSeries series;// ������յ�һ���ټ���һ��
	private XYMultipleSeriesDataset dataset1;// xy������Դ
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

		senddata.sendData("66C40C" + "000000000000000000000000" + "99");//�ر��¶��ϱ�

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

		// ��������ͼ
		LinearLayout linearLayout = (LinearLayout) view
				.findViewById(R.id.chart);
		chart = ChartFactory.getLineChartView(MainApplication.getInstance(),
				getdemodataset(), getdemorenderer());
		linearLayout.removeAllViews();// ��remove��add����ʵ��ͳ��ͼ����
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
	 * ��ʼ��Detail�㲥LocalBroadcastReceiver
	 */
	private void InitDetailBroadCastReceiver() {
		mLBM = LocalBroadcastManager.getInstance(getActivity());
		detailBroadCastReceiver = new DetailBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("PagerPayload");
		mLBM.registerReceiver(detailBroadCastReceiver, intentFilter);
	}

	/**
	 * �㲥���մ�����
	 * 
	 * @author admin
	 * 
	 */
	public class DetailBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String payload = (String) intent.getExtras().get("payload");
			Log.e("�¶�", payload);
			if (payload.substring(2, 4).equals("C2")) {
				payload = payload.substring(6, 10);
				payload_therm = (float) ItonAdecimalConver
						.hexStringToAlgorism(payload) / 10;
				
				BigDecimal b = new BigDecimal(payload_therm);
				float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
				therm.setText(f1 + "��");
				Log.e("wendu--", f1+"��");
				String alarm;
				if (alarmTem.getText().toString().trim().equals("")) {
					alarm = "30";
				}else {
					alarm = alarmTem.getText().toString().trim();
				}
				if (f1>Float.parseFloat(alarm)) {
					Log.e(TAG, f1+"**"+Float.parseFloat(alarm));
					therm.setTextColor(Color.RED);
					VibratorUtil.Vibrate((PagerActivity) getActivity(), 300);//��
				}else {
					therm.setTextColor(Color.BLACK);
				}
				updatechart();

			}
		}

	}

	// ��������ͼ
	private void updatechart() {
		// �жϵ�ǰ�㼯�е����ж��ٵ㣬��Ϊ��Ļ�ܹ�ֻ������5�������Ե���������5ʱ��������Զ��5
		int length = series.getItemCount();
		int a = length;
		if (length > 5) {
			length = 5;
		}
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//Ҫ��Ҫ�ж���˵ } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		addX = shijian.format(new java.util.Date());

		addY = (float)(Math.round(payload_therm*100))/100;
//		Log.e("addY", addY+"");
		// �Ƴ����ݼ��оɵĵ㼯
		dataset1.removeSeries(series);
		if (a < 5)// �����ݼ��в���������ʱ��ֱ����Ӿͺã���Ϊ��ʼ����ʱ��ֻ��һ���㣬����ǰ���θ��µ�ʱ��ֱ�����
		{
			series.add(a + 1, addY);// ��һ����������ڼ����㣬Ҫ����������еĵ�һ��������Ӧ
			render.addXTextLabel(a + 1, addX);
			xkedu[a] = addX;
		} else // �����������Ҫȥ��xcache��0�����ɡ�1����.....
		{
			// ���ɵĵ㼯��x��y����ֵȡ��������backup�У������������ƽ�Ƶ�Ч��
			for (int i = 0; i < length - 1; i++) {
				ycache[i] = (float) series.getY(i + 1);
				xkedu[i] = xkedu[i + 1];
			}

			// �㼯����գ�Ϊ�������µĵ㼯��׼��
			series.clear();
			// ���²����ĵ����ȼ��뵽�㼯�У�Ȼ����ѭ�����н�����任���һϵ�е㶼���¼��뵽�㼯��

			for (int k = 0; k < length - 1; k++) {
				series.add(k + 1, ycache[k]);
				render.addXTextLabel(k + 1, xkedu[k]);
			}
			xkedu[4] = addX;
			series.add(5, addY);
			render.addXTextLabel(5, addX);
		}
		// �����ݼ�������µĵ㼯
		dataset1.addSeries(series);
		// ��ͼ���£�û����һ�������߲�����ֶ�̬
		chart.invalidate();
	}

	private XYMultipleSeriesRenderer getdemorenderer() {
			// TODO Auto-generated method stub
			render = new XYMultipleSeriesRenderer();
			render.setChartTitle("�¶�ʱ������");
			render.setChartTitleTextSize(30);//��������ͼ��������ֵĴ�С
			render.setAxisTitleTextSize(18);//������������ֵĴ�С
			render.setAxesColor(Color.BLACK);
			render.setXTitle("ʱ��");
			render.setYTitle("�¶�");
			
			render.setLabelsTextSize(25);//������̶����ֵĴ�С
			render.setLabelsColor(Color.BLACK);
			render.setXLabelsColor(Color.BLACK);
			render.setYLabelsColor(0, Color.BLACK);
			render.setLegendTextSize(25);//����ͼ�����ִ�С  
			render.setShowLegend(false);//��ʾ����ʾ���������ã��ǳ�����,��Ӧ��184��
			
			// ����ͼ�������߱������ʽ��������ɫ����Ĵ�С�Լ��ߵĴ�ϸ��
			XYSeriesRenderer r = new XYSeriesRenderer();//������ɫ�͵�����
			r.setColor(Color.RED);
			r.setPointStyle(PointStyle.CIRCLE);//���õ����ʽ
			r.setFillPoints(true);
			r.setLineWidth(3);// �����߿�
			r.setChartValuesSpacing(3);
			r.setChartValuesTextSize(30);//������ֵ�������С 
			
			DecimalFormat df1 = new DecimalFormat(".0");
	        r.setChartValuesFormat(df1);//�������ߵ��ֵ�ĸ�ʽ,��ʾС�����1λ
			
			r.setDisplayChartValues(true);//��ʾ�����ϵ����ֵ****
			
			render.addSeriesRenderer(r);
			render.setYLabelsAlign(Align.RIGHT);//�̶�ֵ����ڿ̶ȵ�λ��
			render.setShowGrid(true);//��ʾ����
			render.setYAxisMax(40);//����y��ķ�Χ
			render.setYAxisMin(0);
			render.setYLabels(5);//���ߵȷ�
			render.getSeriesRendererAt(0);
//			((XYSeriesRenderer) render.getSeriesRendererAt(0))).setDisplayChartValues(true); //��ʾ�����ϵ����ֵ
			render.setInScroll(true);
			render.setLabelsTextSize(25);
			render.setLabelsColor(Color.BLACK);
			
			
			
			render.setPanEnabled(false,true);//false��ʾ��ֹ������϶���X�ᡢY��ƽ������
			render.setPointSize(5f);//���õ�Ĵ�С(ͼ����ʾ�ĵ�Ĵ�С��ͼ���е�Ĵ�С���ᱻ����)
			render.setMargins(new int[]{60,40,40,40}); //����ͼ�����ܵ����ף��������ң�
			render.setMarginsColor(Color.WHITE);
			render.setXLabels(0);// ȡ��X���������zjk,ֻ���Լ�����������ǲ���Ϊ��ֵ
			
			return render;
		}

	private XYMultipleSeriesDataset getdemodataset() {
		// TODO Auto-generated method stub
		dataset1 = new XYMultipleSeriesDataset();// xy������Դ
		series = new XYSeries("�¶� ");// ���������ʾ�����õģ��Բ���ʾ������render����
		// �����൱�ڳ�ʼ������ʼ��������������ݣ���Ϊ���������ӵ�һ�����ݵĻ���
		// ������ʹ��һ�����ݺͶ�ʱ���и��µĵڶ������ݵ�ʱ������Ϊ���룬���������������
		// �������һ�θ���������ݣ������Ļ��൱�ڿ�ʼ��ʱ��Ͱ��������ȫ���ӽ�ȥ�ˣ��������ݵ�ʱ���ǲ�׼ȷ���߼����Ϊ����
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//������date�������ͣ��������漴���ȴ�����

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
