/**
 * 
 */
package com.superman.smsalarm;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.superman.smsalarm.model.DisturbModel;
import com.superman.smsalarm.model.SettingDetailModel;
import com.superman.smsalarm.setting.SettingActivity;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsSetting;

/**
 * <p>
 * Title: com.superman.smsalarm.DisturbActivity.java
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2001-2013 Newland SoftWare Company
 * </p>
 * 
 * <p>
 * Company: Newland SoftWare Company
 * </p>
 * 
 * @author Lewis.Lynn
 * 
 * @version 1.0 CreateTime：2014-3-29 下午3:29:15
 */

public class DisturbActivity extends Activity {

	private View bodyView, checkBoxView, startIntervalLayout, endIntervalLayout;
	private RadioGroup radioGroup_use, radioGroup_time, radioGroup_custom;
	private RadioButton in_use_radio, no_use_radio;
	private RadioButton every_radio, workday_radio, weekend_radio, custom_radio;
	private CheckBox monday_checkbox, tuesday_checkbox, wednesday_checkbox, 
					thursday_checkbox, friday_checkbox, saturday_checkbox, sunday_checkbox;
	private TextView start_TextView, end_TextView, start_result_TextView, end_result_TextView;
	
	private SQLiteDatabase db;
	private DisturbModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = SmsAlarmDao.getDbInstance(this);
		setContentView(R.layout.layout_activity_setting_not_disturb);
		
		ActionBar actionbar = getActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View titleView = li.inflate(R.layout.layout_disturb_title_bar, null);
		actionbar.setCustomView(titleView);
		actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		
		initModel();
		initializeView();//initialize View
		resetStatus();
		
		//save Listener
		TextView saveView = (TextView)titleView.findViewById(R.id.title_bar_disturb_save);
		saveView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(in_use_radio.isChecked()){
					model.setUse(true);
				}else if(no_use_radio.isChecked()){
					model.setUse(false);
				}
				
				if(every_radio.isChecked()){
					model.setDateString("#1#2#3#4#5#6#7");
				}else if(workday_radio.isChecked()){
					model.setDateString("#1#2#3#4#5");
				}else if(weekend_radio.isChecked()){
					model.setDateString("#6#7");
				}else if(custom_radio.isChecked()){//custom
					String check_str = "";
					if(monday_checkbox.isChecked()){
						check_str = check_str + "#1";
					}
					if(tuesday_checkbox.isChecked()){
						check_str = check_str + "#2";
					}
					if(wednesday_checkbox.isChecked()){
						check_str = check_str + "#3";
					}
					if(thursday_checkbox.isChecked()){
						check_str = check_str + "#4";
					}
					if(friday_checkbox.isChecked()){
						check_str = check_str + "#5";
					}
					if(saturday_checkbox.isChecked()){
						check_str = check_str + "#6";
					}
					if(sunday_checkbox.isChecked()){
						check_str = check_str + "#7";
					}
					model.setDateString(check_str);
				}
				String[] begin_result_array = start_result_TextView.getText().toString().split(":");
				model.setBegin_hour(Integer.parseInt(begin_result_array[0].trim()));
				model.setBegin_min(Integer.parseInt(begin_result_array[1].trim()));
				
				String[] end_result_array = end_result_TextView.getText().toString().split(":");
				model.setEnd_hour(Integer.parseInt(end_result_array[0].trim()));
				model.setEnd_min(Integer.parseInt(end_result_array[1].trim()));
				
				saveData();//save data
			}
		});
		
		//set body visible
		if(in_use_radio.isChecked()){
			bodyView.setVisibility(View.VISIBLE);
		}else {
			bodyView.setVisibility(View.GONE);
		}
		
		if(every_radio.isChecked() || workday_radio.isChecked() || weekend_radio.isChecked()){
			custom_radio.setChecked(false);
		}
		if(!custom_radio.isChecked()){
			checkBoxView.setVisibility(View.GONE);
		}
		
		radioGroup_use.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == in_use_radio.getId()){
					bodyView.setVisibility(View.VISIBLE);
				}else if(checkedId == no_use_radio.getId()){
					bodyView.setVisibility(View.GONE);
				}
			}
		});
		
		radioGroup_time.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				radioGroup_custom.clearCheck();
				checkBoxView.setVisibility(View.GONE);
			}
		});
		
		custom_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					radioGroup_time.clearCheck();
					checkBoxView.setVisibility(View.VISIBLE);
				}else {
					checkBoxView.setVisibility(View.GONE);
				}
				
			}
		});
		
		startIntervalLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimePickerDialog timerDialog = new TimePickerDialog(DisturbActivity.this, 
						new TimeListener(0), model.getBegin_hour(), model.getBegin_min(), true);
				timerDialog.show();
			}
		});
		
		endIntervalLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimePickerDialog timerDialog = new TimePickerDialog(DisturbActivity.this, 
						new TimeListener(1), model.getEnd_hour(), model.getEnd_min(), true);
				timerDialog.show();
			}
		});
	}

	
	
	private void initModel(){ 
		Cursor c = null;
		
		try {
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.IS_DISTURB,
					SmsSetting.DISTURB_DATE, SmsSetting.DISTURB_BEGIN_INTERVAL, SmsSetting.DISTURB_END_INTERVAL}, null, null, null, null, null);
			if(c.moveToFirst()) {
				if(model == null){
					model = new DisturbModel();
				}
				int is_disturb = c.getInt(c.getColumnIndex(SmsSetting.IS_DISTURB));
				String disturb_date = c.getString(c.getColumnIndex(SmsSetting.DISTURB_DATE));
				String begin_interval = c.getString(c.getColumnIndex(SmsSetting.DISTURB_BEGIN_INTERVAL));
				String end_interval = c.getString(c.getColumnIndex(SmsSetting.DISTURB_END_INTERVAL));
				model.setUse(is_disturb == 1 ? true : false);
				model.setDateString(disturb_date);
				if(begin_interval.length() > 0){
					String[] begin_interval_arr = begin_interval.split(":");
					model.setBegin_hour(begin_interval_arr[0].length() == 0 ? 0 : Integer.parseInt(begin_interval_arr[0].trim()));
					model.setBegin_min(begin_interval_arr[1].length() == 0 ? 0 : Integer.parseInt(begin_interval_arr[1].trim()));
				}else {
					model.setBegin_hour(0);
					model.setBegin_min(0);
				}
				if(end_interval.length() > 0){
					String[] end_interval_arr = end_interval.split(":");
					model.setEnd_hour(end_interval_arr[0].length() == 0 ? 0 : Integer.parseInt(end_interval_arr[0].trim()));
					model.setEnd_min(end_interval_arr[1].length() == 0 ? 0 : Integer.parseInt(end_interval_arr[1].trim()));
				}else {
					model.setEnd_hour(0);
					model.setEnd_min(0);
				}
				
			}
		} catch (Exception e) {
			Log.e("DisturbActivity", e.toString());
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null) c.close();
		}
	}
	
	private void resetStatus(){
		if(model.isUse()){
			radioGroup_use.check(in_use_radio.getId());
		}else {
			radioGroup_use.check(no_use_radio.getId());
		}
		
		String check_str = model.getDateString().replace("#", "");
		if(check_str.equals("1234567")){
			radioGroup_time.check(every_radio.getId());
		}else if(check_str.equals("12345")){
			radioGroup_time.check(workday_radio.getId());
		}else if(check_str.equals("67")){
			radioGroup_time.check(weekend_radio.getId());
		}else {
			radioGroup_time.clearCheck();
			radioGroup_custom.check(custom_radio.getId());
			
			if(check_str.contains("1")){
				monday_checkbox.setChecked(true);
			}
			if(check_str.contains("2")){
				tuesday_checkbox.setChecked(true);
			}
			if(check_str.contains("3")){
				wednesday_checkbox.setChecked(true);
			}
			if(check_str.contains("4")){
				thursday_checkbox.setChecked(true);
			}
			if(check_str.contains("5")){
				friday_checkbox.setChecked(true);
			}
			if(check_str.contains("6")){
				saturday_checkbox.setChecked(true);
			}
			if(check_str.contains("7")){
				sunday_checkbox.setChecked(true);
			}
			
		}
		
		start_result_TextView.setText(SmsAlarmDao.truncSingleTime(model.getBegin_hour(), model.getBegin_min()));
		end_result_TextView.setText(SmsAlarmDao.truncSingleTime(model.getEnd_hour(), model.getEnd_min()));
	}
	
	private void saveData(){
		ContentValues values = new ContentValues();
		values.put(SmsSetting.IS_DISTURB, model.isUse() ? 1 : 0);
		values.put(SmsSetting.DISTURB_DATE, model.getDateString());
		values.put(SmsSetting.DISTURB_BEGIN_INTERVAL, model.getBegin_hour() + ":" + model.getBegin_min());
		values.put(SmsSetting.DISTURB_END_INTERVAL, model.getEnd_hour() + ":" + model.getEnd_min());
		int re = db.update(SmsSetting.TABLE_NAME, values, null, null);
		String result = "";
		if(re == 1){
			result = getString(R.string.save_success);
		}else {
			result = getString(R.string.save_failure);
		}
		Toast.makeText(DisturbActivity.this, result, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(DisturbActivity.this, SettingActivity.class);
		startActivity(intent);
		DisturbActivity.this.finish();
	}
	
	private void initializeView() {
		// radio button
		radioGroup_use = (RadioGroup) findViewById(R.id.setting_not_disturb_radioGroup_use);
		radioGroup_time = (RadioGroup) findViewById(R.id.setting_not_disturb_radioGroup_time);
		radioGroup_custom = (RadioGroup) findViewById(R.id.setting_not_disturb_radioGroup_custom);
		in_use_radio = (RadioButton) findViewById(R.id.setting_not_disturb_in_choice);
		no_use_radio = (RadioButton) findViewById(R.id.setting_not_disturb_no_choice);
		
		every_radio = (RadioButton) findViewById(R.id.setting_not_disturb_everyDay);
		workday_radio = (RadioButton) findViewById(R.id.setting_not_disturb_radiobutton_workday);
		weekend_radio = (RadioButton) findViewById(R.id.setting_not_disturb_radiobutton_weekend);
		
		custom_radio = (RadioButton) findViewById(R.id.setting_not_disturb_radiobutton_custom);

		// body
		bodyView = (View) findViewById(R.id.setting_not_disturb_body);

		// check box layout
		checkBoxView = (View) findViewById(R.id.setting_not_disturb_checkbox);
		// check box
		monday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_monday);
		tuesday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_tuesday);
		wednesday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_wednesday);
		thursday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_thursday);
		friday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_friday);
		saturday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_saturday);
		sunday_checkbox = (CheckBox) findViewById(R.id.setting_not_disturb_checkbox_sunday);

		// start interval
		startIntervalLayout = (View) findViewById(R.id.setting_not_disturb_start_time);
		start_TextView = (TextView) findViewById(R.id.setting_not_disturb_start_time_text);
		start_result_TextView = (TextView) findViewById(R.id.setting_not_disturb_start_time_text_result);

		// end interval
		endIntervalLayout = (View) findViewById(R.id.setting_not_disturb_end_time);
		end_TextView = (TextView) findViewById(R.id.setting_not_disturb_end_time_text);
		end_result_TextView = (TextView) findViewById(R.id.setting_not_disturb_end_time_text_result);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent();
			intent.setClass(DisturbActivity.this, SettingActivity.class);
			startActivity(intent);
			DisturbActivity.this.finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	public class TimeListener implements TimePickerDialog.OnTimeSetListener{

		private int type;//0:begin interval, 1:end_interval
		
		public TimeListener(int type) {
			this.type = type;
		}
		
		@Override
		public void onTimeSet(TimePicker datePicker, int hour, int min) {
			String interval = SmsAlarmDao.truncSingleTime(hour, min);
			if(type == 0){
				model.setBegin_hour(hour);
				model.setBegin_min(min);
				start_result_TextView.setText(interval);
			}else if(type == 1){
				model.setEnd_hour(hour);
				model.setEnd_min(min);
				end_result_TextView.setText(interval);
			}
			
		}
		
	}
}
