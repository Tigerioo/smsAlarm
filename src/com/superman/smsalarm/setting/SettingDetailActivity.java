/**
 * 
 */
package com.superman.smsalarm.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.superman.smsalarm.R;
import com.superman.smsalarm.R.color;
import com.superman.smsalarm.R.id;
import com.superman.smsalarm.R.layout;
import com.superman.smsalarm.R.string;
import com.superman.smsalarm.model.HistoryModel;
import com.superman.smsalarm.model.SettingDetailModel;
import com.superman.smsalarm.setting.SettingActivity.ToggleButtonListener;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsIntercept;
import com.superman.util.SQLite.SmsSetting;

/**
 * <p>Title: com.superman.smsalarm.SettingDetailActivity.java</p>
 *
 * <p>Description: 
 * 		add since v4.4.7
 * 	detail page is use o'clock and intercept
 * </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-27 下午7:34:15
 */

public class SettingDetailActivity extends Activity{
	
	private List<SettingDetailModel> mData;
	private int keyword_type;//0: oclock, 1: intercept
	private SQLiteDatabase db;
	
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_setting_list_is_use_detail_list);
		
		keyword_type = getIntent().getIntExtra("keyword_type", 0);
		db = SmsAlarmDao.getDbInstance(this);//initialize SQLite
		mData = getData();
		
		ActionBar actionbar = getActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View titleView = li.inflate(R.layout.layout_setting_title_bar, null);
		actionbar.setCustomView(titleView);
		actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowHomeEnabled(false);
		actionbar.setDisplayShowCustomEnabled(true);
		
		//set title
		TextView titleText = (TextView)titleView.findViewById(R.id.title_bar_title_text);
		titleText.setTextAppearance(this, android.R.attr.textAppearanceLarge);
		titleText.setTextColor(R.color.title_gray);
		if(keyword_type == 0){
			titleText.setText(R.string.setting_alarm_use_detail);
		}else if(keyword_type == 1){
			titleText.setText(R.string.setting_intercepter_use_detail);
		}
		
		final ListView lv = (ListView)findViewById(R.id.list_setting_is_use);
		final MyAdapter adapter = new MyAdapter(this);
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
    		
    		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
    	    	SettingDetailModel model = mData.get(position);
    	    	model.setChecked(!model.isChecked());
    	    	adapter.notifyDataSetChanged();
    	    }
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//save modify
			for (SettingDetailModel model : mData) {
				saveModify(model.isChecked(), model.getId());
			}
			Intent intent = new Intent();
			intent.setClass(SettingDetailActivity.this, SettingActivity.class);
			startActivity(intent);
			SettingDetailActivity.this.finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			for (SettingDetailModel model : mData) {
				saveModify(model.isChecked(), model.getId());
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * initialize data
	 * @return
	 */
	private List<SettingDetailModel> getData(){ 
		Cursor c = null;
		List<SettingDetailModel> reList = new ArrayList<SettingDetailModel>();
		try {
			if(keyword_type == 0){
				c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.ID, SmsAlarm.PHONENUMBER, SmsAlarm.KEY_WORD, SmsAlarm.IS_USE}, null, null, null, null, SmsAlarm.ID + " asc");
			}else if(keyword_type == 1){
				c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsAlarm.ID, SmsIntercept.PHONENUMBER, SmsIntercept.KEY_WORD, SmsIntercept.IS_USE}, null, null, null, null, SmsIntercept.ID + " asc");
			}
			while(c.moveToNext()){
				SettingDetailModel model = new SettingDetailModel();
				model.setId(c.getString(c.getColumnIndex(SmsAlarm.ID)));
				model.setTitle(c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER)));
				model.setKey_word(c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD)));
				model.setChecked(c.getString(c.getColumnIndex(SmsAlarm.IS_USE)).equals("1") ? true : false);
				reList.add(model);
			}
		} finally {
			if(c != null) c.close();
		}
		return reList;
	}
	
	public final class ViewHolder {
		public TextView id;
		public TextView title;
		public TextView info;
		public Switch tSwitch;
	}
	
	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = null;
			
			if(convertView == null){
				
				view = mInflater.inflate(R.layout.layout_activity_setting_list_is_use_detail_switch, null);
				final ViewHolder holder = new ViewHolder();
				holder.id = (TextView) view.findViewById(R.id.setting_is_use_id);
				holder.title = (TextView) view.findViewById(R.id.setting_is_use_title);
				holder.info = (TextView) view.findViewById(R.id.setting_is_use_info);
				holder.tSwitch = (Switch) view.findViewById(R.id.setting_is_use_switch);
				holder.tSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						SettingDetailModel model = (SettingDetailModel)holder.tSwitch.getTag();
						model.setChecked(isChecked);
					}
				});
				view.setTag(holder);
				holder.tSwitch.setTag(mData.get(position));
			} else {
				view = convertView;
				((ViewHolder)view.getTag()).tSwitch.setTag(mData.get(position));
			}
			
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.id.setText(mData.get(position).getId());
			String phone_number = mData.get(position).getTitle();
			holder.title.setText(phone_number + ":");
			holder.info.setText(mData.get(position).getKey_word());
			//setting ToggleButton status
			holder.tSwitch.setChecked(mData.get(position).isChecked());
		
			return view;
		}
	}
	
	private void saveModify(boolean isChecked, String id) {
		ContentValues alarmValues = new ContentValues();//oclock and intercepter contentValues
		String table_name = "";
		switch (keyword_type) {
		case 0://deal oclock
			table_name = SmsAlarm.TABLE_NAME;
			break;
		case 1://deal intecept
			table_name = SmsIntercept.TABLE_NAME;
			break;
		default:
			break;
		}
		
		if(isChecked){
			alarmValues.put(SmsAlarm.IS_USE, 1);
		}else {
			alarmValues.put(SmsAlarm.IS_USE, 0);
		}
		
		// update smsAlarm table
		db.update(table_name, alarmValues, SmsAlarm.ID + "=?", new String[]{id});
		
	}
}
