/**
 * 
 */
package com.superman.smsalarm.periodlisten;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.superman.smsalarm.R;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsPeriodListen;

/**
 * <p>Title: com.superman.smsalarm.periodlisten.Add.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-10-2 下午3:38:33
 */

public class Add extends Activity{
	
	private SQLiteDatabase db;
	private TextView cancel; //标题栏取消
	private TextView save;//标题栏保存
	private EditText numberEdit;//电话号码
	private EditText keywordEdit;//关键字
	private Spinner periodSpinner;//周期, 包含：每30分钟、每小时、每天、每月
//	private Spinner periodTypeSpinner;//周期类型
	private Spinner delayPeriodSpinner;//延迟周期数
	private Spinner delayPeriodTypeSpinner;//延迟周期类型
	private RadioButton noDelayRadio, yesDelayRadio;//是否延迟监控
	private View delayConfigView;//延迟的整个View
	private Spinner listenPeriodSpinner;//触发响铃的周期
	/*
	 * add since v2.4.6.8.9
	 */
	private View listen_time_View;//三个Spinner的View
	private RadioButton listen_time_custom_radio, listen_time_next_radio;//周期初始时间设置， 是否启用具体配置
	private Spinner listen_time_day_Spinner, listen_time_hour_Spinner, listen_time_minute_Spinner;//周期初始时间设置，分别适用于各个周期，每个月几号， 每个小时几分等等
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_setting_list_is_period_listen_detail_list_add);
		
		db = SmsAlarmDao.getDbInstance(this);//initialize SQLite
		initView();
		initSpinner();
		
		periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//根据周期类型不同设置周期
//				ArrayAdapter<String> periodAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, getPeriodList(position));
//				periodAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
//				periodSpinner.setAdapter(periodAdapter);
				
				//根据周期类型不同设置周期初始时间的Spinner
				switch (position) {
				case 0://分钟
					listen_time_day_Spinner.setClickable(false);//不需要选择天
					listen_time_hour_Spinner.setClickable(false);//不需要选择小时
					break;
				case 1://小时
					listen_time_day_Spinner.setClickable(false);//不需要选择天
					listen_time_hour_Spinner.setClickable(false);//不需要选择小时
					break;
				case 2://天
					listen_time_day_Spinner.setClickable(false);//不需要选择天
					listen_time_hour_Spinner.setClickable(true);//记得恢复小时 
					break;
				default://月
					listen_time_day_Spinner.setClickable(true);//记得恢复天
					listen_time_hour_Spinner.setClickable(true);//记得恢复小时 
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		delayPeriodTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, 
					int position, long arg3) {
				ArrayAdapter<String> periodAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, getDelayPeriodList(position));
				periodAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
				delayPeriodSpinner.setAdapter(periodAdapter);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		//延迟监控监听
		noDelayRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					delayConfigView.setVisibility(View.GONE);
				}else {
					delayConfigView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		listen_time_custom_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					listen_time_View.setVisibility(View.VISIBLE);
				}else {
					listen_time_View.setVisibility(View.GONE);
				}
			}
		});
		
		//取消，返回上级
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent cancel = new Intent();
				cancel.setClass(Add.this, PeriodListenActivity.class);
				startActivity(cancel);
				Add.this.finish();
			}
		});
		
		//保存数据
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//关键字的为空判断
				String keyword = keywordEdit.getText().toString();
				if(keyword.length() == 0){
					Toast.makeText(Add.this, "关键字不允许为空!", Toast.LENGTH_SHORT).show();
					return;
				}
				//保存入库
				int periodType = periodSpinner.getSelectedItemPosition();
				int listen_time_hour = -1;
				if(periodType == 2 || periodType == 3) {//只有天和月才需要设置小时
					listen_time_hour = Integer.parseInt(listen_time_hour_Spinner.getSelectedItem().toString());
				}
				ContentValues insert = new ContentValues();
				insert.put(SmsPeriodListen.KEYWORD, keywordEdit.getText().toString());
				insert.put(SmsPeriodListen.NUMBER, numberEdit.getText().toString());
				insert.put(SmsPeriodListen.PERIOD_TYPE, periodType);//刚好是分钟、小时、天、月的排列
				insert.put(SmsPeriodListen.PERIOD, periodSpinner.getSelectedItemPosition() == 0 ? 30 : 1);//只有第一个是30分钟，其他都是1
				insert.put(SmsPeriodListen.LISTEN_TIME_DAY, periodType == 3 ? Integer.parseInt(listen_time_day_Spinner.getSelectedItem().toString()) : -1);//只有每月的时候才需要设置
				insert.put(SmsPeriodListen.LISTEN_TIME_HOUR, listen_time_hour);
				//如果不是选择自定义，则加入-1
				insert.put(SmsPeriodListen.LISTEN_TIME_MINUTE, listen_time_custom_radio.isChecked() ? Integer.parseInt(listen_time_minute_Spinner.getSelectedItem().toString()) : -1);
				insert.put(SmsPeriodListen.LISTEN_PERIOD, Integer.parseInt(listenPeriodSpinner.getSelectedItem().toString()));
				insert.put(SmsPeriodListen.IS_DELAY_ALARM, yesDelayRadio.isChecked() ? 1 : 0);//yes被选中，则有延迟
				insert.put(SmsPeriodListen.DELAY_PERIOD_TYPE, delayPeriodTypeSpinner.getSelectedItemPosition());
				insert.put(SmsPeriodListen.DELAY_PERIOD, Integer.parseInt(delayPeriodSpinner.getSelectedItem().toString()));
				insert.put(SmsPeriodListen.IS_USE, 1);
				insert.put(SmsPeriodListen.CREATE_TIME, System.currentTimeMillis());
				db.insert(SmsPeriodListen.TABLE_NAME, null, insert);
				
				//关闭当前页并且返回
				Intent back = new Intent();
				back.setClass(Add.this, PeriodListenActivity.class);
				startActivity(back);
				Add.this.finish();
			}
		});
	};
	
	private void initView(){
		cancel = (TextView)findViewById(R.id.setting_period_listen_add_title_bar_cancel);
		save = (TextView)findViewById(R.id.setting_period_listen_add_title_bar_save);
		numberEdit = (EditText)findViewById(R.id.setting_period_listen_add__number_edit);
		keywordEdit = (EditText)findViewById(R.id.setting_period_listen_add__keyword_edit);
		periodSpinner = (Spinner)findViewById(R.id.setting_period_listen_add_period);
		delayPeriodSpinner = (Spinner)findViewById(R.id.setting_period_listen_add_delay_config_count);
		delayPeriodTypeSpinner = (Spinner)findViewById(R.id.setting_period_listen_add_delay_config_type);
		noDelayRadio = (RadioButton)findViewById(R.id.setting_period_listen_add_delay_no);
		yesDelayRadio = (RadioButton)findViewById(R.id.setting_period_listen_add_delay_yes);
		delayConfigView = (View)findViewById(R.id.setting_period_listen_add_delay_config);
		listenPeriodSpinner = (Spinner)findViewById(R.id.setting_period_listen_add_listen_period_count);
		/*
		 * 	private RadioButton listen_time_custom_radio, listen_time_next_radio;//周期初始时间设置， 是否启用具体配置
	private Spinner listen_time_day_Spinner, listen_time_hour_Spinner, listen_time_minute_Spinner;//周期初始时间设置，分别适用于各个周期，每个月几号， 每个小时几分等等
		 */
		listen_time_custom_radio = (RadioButton)findViewById(R.id.setting_period_listen_add_listen_time_radioGroup_custom);
		listen_time_next_radio = (RadioButton)findViewById(R.id.setting_period_listen_add_listen_time_radioGroup_sms);
		
		listen_time_day_Spinner = (Spinner)findViewById(R.id.setting_period_listen_add_listen_time_day);
		listen_time_hour_Spinner = (Spinner)findViewById(R.id.setting_period_listen_add_listen_time_hour);
		listen_time_minute_Spinner = (Spinner)findViewById(R.id.setting_period_listen_add_listen_time_minute);
		
		listen_time_View = (View)findViewById(R.id.setting_period_listen_add_listen_time);
	}
	
	private void initSpinner(){
		List<String> periodList = new ArrayList<String>();
		periodList.add("每30分钟");
		periodList.add("每小时");
		periodList.add("每天");
		periodList.add("每月");
		ArrayAdapter<String> periodAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, periodList);
		periodAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		periodSpinner.setAdapter(periodAdapter);
		
		
		List<String> delayPeriodTypeList = new ArrayList<String>();
		delayPeriodTypeList.add("分钟");
		delayPeriodTypeList.add("小时");
		delayPeriodTypeList.add("天");
		ArrayAdapter<String> delayPeriodAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, delayPeriodTypeList);
		delayPeriodAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		delayPeriodTypeSpinner.setAdapter(delayPeriodAdapter);//延迟的周期类型
		
		/*
		 * 设置周期的Spinner内容， 初始值为分钟
		 */
		
		ArrayAdapter<String> delayPeriodTypeAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, getDelayPeriodList(0));
		delayPeriodTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		delayPeriodSpinner.setAdapter(delayPeriodTypeAdapter);//延迟的周期选项
		
		/*
		 * 设置触发响铃周期数
		 */
		List<String> listenPeriodList = new ArrayList<String>();
		for (int i = 1; i <= 10; i++) {
			listenPeriodList.add(String.valueOf(i));
		}
		ArrayAdapter<String> listenPeriodAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, listenPeriodList);
		listenPeriodAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listenPeriodSpinner.setAdapter(listenPeriodAdapter);
		
		//设置listen_time_day_Spinner的下拉内容
		List<String> listenTimeDayList = new ArrayList<String>();
		for (int i = 1; i <= 31; i++) {
			listenTimeDayList.add(String.valueOf(i));
		}
		ArrayAdapter<String> listenTimeDayAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, listenTimeDayList);
		listenTimeDayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listen_time_day_Spinner.setAdapter(listenTimeDayAdapter);
		
		//设置listen_time_hour_Spinner的下拉内容
		List<String> listenTimeHourList = new ArrayList<String>();
		for (int i = 0; i <= 23; i++) {
			listenTimeHourList.add(String.valueOf(i));
		}
		ArrayAdapter<String> listenTimeHourAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, listenTimeHourList);
		listenTimeHourAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listen_time_hour_Spinner.setAdapter(listenTimeHourAdapter);
		
		//设置listen_time_minute_Spinner的下拉内容
		List<String> listenTimeMinuteList = new ArrayList<String>();
		for (int i = 0; i <= 59; i++) {
			listenTimeMinuteList.add(String.valueOf(i));
		}
		ArrayAdapter<String> listenTimeMinuteAdapter = new ArrayAdapter<String>(Add.this, android.R.layout.simple_spinner_item, listenTimeMinuteList);
		listenTimeMinuteAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listen_time_minute_Spinner.setAdapter(listenTimeMinuteAdapter);
	}
	
	/**
	 * 周期类型为分钟的周期数
	 * @param int 类型， 0: 分钟， 1：小时， 2：天， 3：月
	 * @return
	 */
	private List<String> getDelayPeriodList(int periodDelayType){
		List<String> periodList = new ArrayList<String>();
		
		switch (periodDelayType) {
		case 0://分钟
			for (int i = 5; i <= 55; i+=5) {
				periodList.add(String.valueOf(i));
			}
			break;
		case 1://小时
			for (int i = 1; i < 24; i++) {
				periodList.add(String.valueOf(i));
			}
			break;
		case 2://天
			for (int i = 1; i <= 10; i++) {
				periodList.add(String.valueOf(i));
			}
			break;
		default:
			break;
		}
		
		return periodList;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent back = new Intent();
			back.setClass(Add.this, PeriodListenActivity.class);
			startActivity(back);
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
