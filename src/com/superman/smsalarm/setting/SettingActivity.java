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
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.superman.common.SmsService;
import com.superman.smsalarm.DisturbActivity;
import com.superman.smsalarm.MainActivity;
import com.superman.smsalarm.R;
import com.superman.smsalarm.R.array;
import com.superman.smsalarm.R.color;
import com.superman.smsalarm.R.id;
import com.superman.smsalarm.R.layout;
import com.superman.smsalarm.R.string;
import com.superman.smsalarm.backup.Backup;
import com.superman.smsalarm.backup.Restore;
import com.superman.smsalarm.periodlisten.PeriodListenActivity;
import com.superman.update.UpdateManager;
import com.superman.util.ConstantUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsHistory;
import com.superman.util.SQLite.SmsIntercept;
import com.superman.util.SQLite.SmsSetting;

/**
 * <p>
 * Title: com.superman.smsalarm.SettingActivity.java
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
 * @version 1.0 CreateTime：2014-3-16 下午6:50:09
 */

public class SettingActivity extends Activity {

	private List<String> mData;
	private SQLiteDatabase db;
	private int tip;//BOMC MODE tip
	private final int RING_RESULT = 0;
	private Map<String, Object> settingMap;
	private MyAdapter adapter;
	private ListView settingListView;
	
	private AlertDialog backup_dialog;
	
	private MyHandler myHandler = new MyHandler();
	
//	private SharedPreferences mSharedPreferences;
//	private SharedPreferences.Editor editor;
	
	@SuppressLint({ "CutPasteId", "ResourceAsColor" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences pre = getSharedPreferences(getResources().getString(R.string.app_en_name), MODE_PRIVATE);
		tip = pre.getInt("BOMC", 0);
		
		db = SmsAlarmDao.getDbInstance(this);
		setContentView(R.layout.layout_activity_setting_list);
		mData = getData();
		settingMap = querySetting();
		ActionBar actionbar = getActionBar();
//		LayoutInflater li = LayoutInflater.from(this);
//		View titleView = li.inflate(R.layout.layout_setting_title_bar, null);
//		actionbar.setCustomView(titleView);
		
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setTitle(getString(R.string.setting_title));
//		actionbar.setHomeButtonEnabled(true);
//		actionbar.setDisplayShowCustomEnabled(true);
		
		// 获取屏幕密度（方法1）  
//		DisplayMetrics dm=new DisplayMetrics();
//        super.getWindowManager().getDefaultDisplay().getMetrics(dm);
//        String strOpt="手机屏幕分辨率为："+dm.widthPixels+"*"+dm.heightPixels;
//		Toast.makeText(SettingActivity.this, strOpt, Toast.LENGTH_LONG).show();
		
		//set title
//		TextView titleText = (TextView)titleView.findViewById(R.id.title_bar_title_text);
//		titleText.setTextAppearance(this, android.R.attr.textAppearanceLarge);
//		titleText.setTextColor(R.color.title_gray);
//		titleText.setText(R.string.setting_title);
		
		settingListView = (ListView) findViewById(R.id.list_setting);
		adapter = new MyAdapter(this);
		settingListView.setAdapter(adapter);
		
		settingListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> l, View view, int position, long id) {
				switch (position) {
				case 0://add since v2.4.6.8.8
					Intent periodListenIntent = new Intent();
					periodListenIntent.setClass(SettingActivity.this, PeriodListenActivity.class);
					startActivity(periodListenIntent);
					break;
				case 1://use oclock detail
					Intent alarmIntent = new Intent();
					alarmIntent.putExtra("keyword_type", 0);
					alarmIntent.setClass(SettingActivity.this, SettingDetailActivity.class);
					startActivity(alarmIntent);
					break;
				case 2://use intecept detail
					Intent interceptIntent = new Intent();
					interceptIntent.putExtra("keyword_type", 1);
					interceptIntent.setClass(SettingActivity.this, SettingDetailActivity.class);
					startActivity(interceptIntent);
					break;
				case 3://拦截响铃设置
					
					break;
				case 4://choose ring
					AlertDialog.Builder audioBuilder = new AlertDialog.Builder(SettingActivity.this);
					audioBuilder.setTitle(getResources().getString(R.string.setting_choose_ring));
					View chooseView = getLayoutInflater().inflate(R.layout.layout_dialog_choose_audio, null);
					final AlertDialog audioDialog = audioBuilder.create();
					audioDialog.setView(chooseView);
					audioDialog.show();
					TextView defaultView = (TextView)chooseView.findViewById(R.id.dialog_choose_audio_default);
					TextView ringView = (TextView)chooseView.findViewById(R.id.dialog_choose_audio_ring);
					defaultView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							//重置状态
							ContentValues values = new ContentValues();
							values.put(SmsSetting.STATUS, 0);
							db.update(SmsSetting.TABLE_NAME, values, null, null);
							
							ContentValues contentValues = new ContentValues();
							contentValues.put(SmsSetting.RING_PATH, "");//first time , initialize data
							contentValues.put(SmsSetting.RING_NAME, getResources().getString(R.string.setting_ring_default));
							int re  = db.update(SmsSetting.TABLE_NAME, contentValues, null, null);
							if(re == 1){
								Toast.makeText(SettingActivity.this, getResources().getString(R.string.setting_choose_ring_success) + getResources().getString(R.string.setting_ring_default), Toast.LENGTH_SHORT).show();
							}
							audioDialog.dismiss();
							reloadListView();
						}
					});
					
					ringView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent rIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
							startActivityForResult(rIntent, RING_RESULT);
							audioDialog.dismiss();
						}
					});
					
					break;
				case 5://not disturbing interval
					Intent disturbIntent = new Intent();
					disturbIntent.setClass(SettingActivity.this, DisturbActivity.class);
					startActivity(disturbIntent);
					break;
				case 6://data backup
					
					AlertDialog.Builder backup_builder = new AlertDialog.Builder(SettingActivity.this);
					backup_builder.setTitle(getString(R.string.setting_backup_restore));
					backup_builder.setPositiveButton(R.string.backup_tip, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							Backup backup = new Backup(SettingActivity.this, db, getFilesDir().getAbsolutePath(), myHandler);
							backup.backup();
						}
					});
					backup_builder.setNegativeButton(R.string.restore_tip, new DialogInterface.OnClickListener(){
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							Restore restore = new Restore(SettingActivity.this, db, myHandler);
							restore.restore();
						}
					});
					backup_dialog = backup_builder.create();
					backup_dialog.show();
					break;
				case 7://soft update
					UpdateManager manager = new UpdateManager(SettingActivity.this, true, db);
					// 检查软件更新
					manager.checkUpdate();
					break;
				case 8://initialize
					AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
					builder.setTitle(R.string.setting_is_initialize);
					// Add the buttons
					builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   alertInitDialog();
					           }
					       });
					builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					               dialog.cancel();
					           }
					       });

					// Create the AlertDialog
					AlertDialog dialog = builder.create();
					dialog.show();
				default:
					break;
				}
			}
			
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent i = new Intent();
			i.setClass(SettingActivity.this, MainActivity.class);
			startActivity(i);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void reloadListView(){
		settingMap = querySetting();
		adapter.notifyDataSetChanged();
		settingListView.invalidateViews();
		settingListView.refreshDrawableState();
	}
	
	private List<String> getData(){
		String[] setting_array = getResources().getStringArray(R.array.setting_array);
		List<String> reList = new ArrayList<String>();
		for (int i = 0; i < setting_array.length; i++) {
			reList.add(setting_array[i]);
		}
		return reList;
	}
	
	private Map<String, Object> querySetting(){
		Cursor c = null;
		Map<String, Object> reMap = new HashMap<String, Object>();
		int is_oclock = 1;
		int is_intercept = 1;
		int is_period_listen = 1;//add since v2.4.6.8.8
		int is_disturb = 0;
		String disturb_date = "";
		String disturb_begin_interval = "";
		String disturb_end_interval = "";
		String last_backup_time = "";
		int is_update = 0;
		//add since v2.4.1
		String ring_path = "";
		String ring_name = "";
		try {
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.IS_DISTURB, SmsSetting.DISTURB_DATE, SmsSetting.DISTURB_BEGIN_INTERVAL, 
					SmsSetting.DISTURB_END_INTERVAL, SmsSetting.LAST_BACKUP_TIME, SmsSetting.IS_UPDATE, 
					SmsSetting.RING_PATH, SmsSetting.RING_NAME, SmsSetting.IS_PERIOD_LISTEN}, null, null, null, null, null);
			if(c.moveToFirst()){
				is_oclock = isAlarm(SmsAlarm.TABLE_NAME) ? 1 : 0;
				is_intercept = isAlarm(SmsIntercept.TABLE_NAME) ? 1 : 0;
				is_disturb = c.getInt(c.getColumnIndex(SmsSetting.IS_DISTURB));
				disturb_date = c.getString(c.getColumnIndex(SmsSetting.DISTURB_DATE));
				disturb_begin_interval = c.getString(c.getColumnIndex(SmsSetting.DISTURB_BEGIN_INTERVAL));
				disturb_end_interval = c.getString(c.getColumnIndex(SmsSetting.DISTURB_END_INTERVAL));
				last_backup_time = c.getString(c.getColumnIndex(SmsSetting.LAST_BACKUP_TIME));
				is_update = c.getInt(c.getColumnIndex(SmsSetting.IS_UPDATE));
				ring_path = c.getString(c.getColumnIndex(SmsSetting.RING_PATH));
				ring_name = c.getString(c.getColumnIndex(SmsSetting.RING_NAME));
				is_period_listen = c.getInt(c.getColumnIndex(SmsSetting.IS_PERIOD_LISTEN));
			}else {//if table is empty, insert a default data 
				ContentValues contentValues = new ContentValues();
				contentValues.put(SmsSetting.ID, 1);
				contentValues.put(SmsSetting.IS_DISTURB, "0");
				contentValues.put(SmsSetting.DISTURB_DATE, "");
				contentValues.put(SmsSetting.DISTURB_BEGIN_INTERVAL, "");
				contentValues.put(SmsSetting.DISTURB_END_INTERVAL, "");
				contentValues.put(SmsSetting.LAST_BACKUP_TIME, "");
				contentValues.put(SmsSetting.IS_UPDATE, 0);
				contentValues.put(SmsSetting.RING_PATH, "");//first time , initialize data
				contentValues.put(SmsSetting.RING_NAME, getResources().getString(R.string.setting_ring_default));
				contentValues.put(SmsSetting.IS_PERIOD_LISTEN, 0);
				db.insert(SmsSetting.TABLE_NAME, null, contentValues);
			}
		} finally {
			if(c != null){
				c.close();
			}
		}
		reMap.put(SmsSetting.IS_OCLOCK, is_oclock);
		reMap.put(SmsSetting.IS_INTERCEPT, is_intercept);
		reMap.put(SmsSetting.IS_DISTURB, is_disturb);
		reMap.put(SmsSetting.DISTURB_DATE, disturb_date);
		reMap.put(SmsSetting.DISTURB_BEGIN_INTERVAL, disturb_begin_interval);
		reMap.put(SmsSetting.DISTURB_END_INTERVAL, disturb_end_interval);
		reMap.put(SmsSetting.LAST_BACKUP_TIME, last_backup_time);
		reMap.put(SmsSetting.IS_UPDATE, is_update);
		reMap.put(SmsSetting.RING_PATH, ring_path);
		reMap.put(SmsSetting.RING_NAME, ring_name);
		reMap.put(SmsSetting.IS_PERIOD_LISTEN, is_period_listen);
		return reMap;
	}
	
	/**
	 * query if sms_alarm had is_use equal 1
	 * @return
	 */
	private boolean isAlarm(String table_name){
		Cursor c = null;
		try {
			c = db.query(table_name, new String[]{SmsAlarm.ID}, SmsAlarm.IS_USE+"=1", null, null, null, null);
			if(c.moveToFirst()) 
				return true;
			else 
				return false;
		} finally {
			if(c != null){
				c.close();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RING_RESULT:
			if(resultCode == RESULT_OK){//choose ok
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String absolutePath = "";
				if(uri == null){
					Toast.makeText(SettingActivity.this, getResources().getString(R.string.setting_choose_ring_not_null), Toast.LENGTH_SHORT).show();
					break;
				}else {
					absolutePath = getRealPathFromURI(SettingActivity.this, uri);
				}
				
				String[] split_str = absolutePath.split("\\/");
				String name = split_str[split_str.length-1];
				String path = absolutePath.replace(name, "");
				
				ContentValues content = new ContentValues();
				content.put(SmsSetting.RING_PATH, path);
				content.put(SmsSetting.RING_NAME, name);
				int re  = db.update(SmsSetting.TABLE_NAME, content, null, null);
				if(re == 1){
					Toast.makeText(SettingActivity.this, getResources().getString(R.string.setting_choose_ring_success) + name, Toast.LENGTH_SHORT).show();
				}
				//reload current activity
				reloadListView();
			}
			break;

		default:
			break;
		}
	}
	
	
	/**
	 * get real path from uri
	 * @param context
	 * @param contentUri
	 * @return
	 */
	public String getRealPathFromURI(Context context, Uri contentUri) {
		  Cursor cursor = null;
		  try { 
		    String[] proj = { MediaStore.Audio.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		  } finally {
		    if (cursor != null) {
		      cursor.close();
		    }
		  }
		}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.setting_menu, menu);
//		return true;
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent();
			intent.setClass(SettingActivity.this, MainActivity.class);
			startActivity(intent);
			SettingActivity.this.finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * add since v2.4.2
	 */
	private void alertInitDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
		builder.setTitle(R.string.setting_initialize_history);
		// Add the buttons
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   db.delete(SmsHistory.TABLE_NAME, null, null);
		        	   init();
		           }
		       });
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   init();
		               dialog.cancel();
		           }
		       });

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * add since v2.4.2
	 * initialize data
	 */
	private void init(){
		db.delete(SmsAlarm.TABLE_NAME, null, null);
		db.delete(SmsIntercept.TABLE_NAME, null, null);
		
		if(tip == 1){//OPEN BOMC MODE
			String[] str_alarm = getResources().getStringArray(R.array.bomc_alarm_keyword);
			for (int i = 0; i < str_alarm.length; i++) {
				insertClockKeyWord(str_alarm[i], ConstantUtil.ALL_NUMBER, 1);
			}
			
			String[] str = getResources().getStringArray(R.array.bomc_intecept_keyword);
			for (int i = 0; i < str.length; i++) {
				insertIntercepterKeyWord(str[i], ConstantUtil.ALL_NUMBER, 1);
			}
		}
		
		Toast.makeText(SettingActivity.this, "初始化完成！", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * add new keyword to sms_alarm
	 * 
	 * @param newKeyword
	 */
	private long insertClockKeyWord(String newKeyword, String phoneNumber, int is_oclock) {
		ContentValues content = new ContentValues();
		content.put(SmsAlarm.KEY_WORD, newKeyword);
		content.put(SmsAlarm.CREATE_TIME, SmsAlarmDao.getCurrentTime());
		content.put(SmsAlarm.PHONENUMBER, phoneNumber);
		content.put(SmsAlarm.IS_USE, is_oclock);
		return db.insert(SmsAlarm.TABLE_NAME, null, content);
	}
	
	/**
	 * add new keyword to sms_intercepter
	 * 
	 * @param newKeyword
	 */
	private long insertIntercepterKeyWord(String newKeyword, String phoneNumber, int is_intercept) {
		ContentValues content = new ContentValues();
		content.put(SmsIntercept.KEY_WORD, newKeyword);
		content.put(SmsIntercept.CREATE_TIME, SmsAlarmDao.getCurrentTime());
		content.put(SmsIntercept.PHONENUMBER, phoneNumber);
		content.put(SmsIntercept.IS_USE, is_intercept);
		return db.insert(SmsIntercept.TABLE_NAME, null, content);
	}

	public final class ViewHolder {
		public TextView info;
		public Switch tSwitch;
	}
	
	public final class ViewHolder2 {
		public TextView info;
		public TextView tipInfo;
	}

	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			return (position < 4) ? 0 : 1;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			int theType = getItemViewType(position);
			String setting_tip = (String) mData.get(position);//具体设置项名称
			int is_oclock = (Integer)settingMap.get(SmsSetting.IS_OCLOCK);//是否启用闹钟
			int is_intercept = (Integer)settingMap.get(SmsSetting.IS_INTERCEPT);//是否启用拦截
			int is_period_listen = (Integer)settingMap.get(SmsSetting.IS_PERIOD_LISTEN);//是否启用周期短信监听 add since v2.4.6.8.8
			int is_disturb = (Integer)settingMap.get(SmsSetting.IS_DISTURB);//是否启用失效时段
			String disturb_date = (String)settingMap.get(SmsSetting.DISTURB_DATE);//失效日期
			String disturb_begin_interval = (String)settingMap.get(SmsSetting.DISTURB_BEGIN_INTERVAL);//失效开始时段
			String disturb_end_interval = (String)settingMap.get(SmsSetting.DISTURB_END_INTERVAL);//失效结束时段
			String last_backup_time = (String)settingMap.get(SmsSetting.LAST_BACKUP_TIME);//最后更新时间
			int is_update = (Integer)settingMap.get(SmsSetting.IS_UPDATE);//是否更新
			//add since v2.4.1
			String ring_path = (String)settingMap.get(SmsSetting.RING_PATH);//铃声路径
			String ring_name = (String)settingMap.get(SmsSetting.RING_NAME);//铃声名字
			switch (theType) {
			case 0://带troggle的选项
				ViewHolder holder = null;
				
				if(convertView == null){
					holder = new ViewHolder();
					convertView = mInflater.inflate(R.layout.layout_activity_setting_list_detail_switch, null);
					holder.info = (TextView) convertView.findViewById(R.id.setting_info);
					holder.tSwitch = (Switch) convertView.findViewById(R.id.setting_switch);
					holder.tSwitch.setOnClickListener(new ToggleButtonListener(holder.tSwitch, position));
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				
				holder.info.setText(setting_tip);
				//setting ToggleButton status
				if(position == 0){
					holder.tSwitch.setChecked(is_period_listen == 1 ? true : false);
				}else if(position == 1){//oclock
					holder.tSwitch.setChecked(is_oclock == 1 ? true : false);
				}else if(position == 2){//intercept
					holder.tSwitch.setChecked(is_intercept == 1 ? true : false);
				}else if(position == 3){//intecept ring
//					holder.tSwitch.setChecked(is_intercept == 1 ? true : false);
				}
				return convertView;
				
			case 1:
				
				ViewHolder2 holder2 = null;
				
				if(convertView == null){
					holder2 = new ViewHolder2();
					convertView = mInflater.inflate(R.layout.layout_activity_setting_list_detail_text, null);
					holder2.info = (TextView) convertView.findViewById(R.id.setting_text_info);
					holder2.tipInfo = (TextView)convertView.findViewById(R.id.setting_tip_info);
					convertView.setTag(holder2);
				} else {
					holder2 = (ViewHolder2) convertView.getTag();
				}
				
				holder2.info.setText(setting_tip);
				if(position == 4){//choose music
					if(ring_name == null || ring_name.length() == 0){
						ring_name = getResources().getString(R.string.setting_ring_default);
					}
					holder2.tipInfo.setText(ring_name);
				}else if(position == 5){//disturb interval
					if(is_disturb == 1){
						String tip = "";
						disturb_date = disturb_date.replace("#", "");
						if(disturb_date.length() > 0){
							if(disturb_date.equals("1234567")){
								tip = getString(R.string.every_day);
							}else if(disturb_date.equals("12345")){
								tip = getString(R.string.workday);
							}else if(disturb_date.equals("67")){
								tip = getString(R.string.weekend);
							}else {
								tip = getString(R.string.custom);
							}
						}
						String[] begin_array = disturb_begin_interval.split(":");
						String begin_str = SmsAlarmDao.truncSingleTime(Integer.parseInt(begin_array[0].trim()), Integer.parseInt(begin_array[1].trim()));
						
						String[] end_array = disturb_end_interval.split(":");
						String end_str = SmsAlarmDao.truncSingleTime(Integer.parseInt(end_array[0].trim()), Integer.parseInt(end_array[1].trim()));
						
						holder2.tipInfo.setText(tip + " " + begin_str + "-" + end_str);
					}else {
						holder2.tipInfo.setText( R.string.setting_tip_not_use);
					}
				}else if(position == 6){//last backup time
					if(last_backup_time != null && last_backup_time.length() > 0){
						holder2.tipInfo.setText(last_backup_time + getString(R.string.backup_tip));
					}else {
						holder2.tipInfo.setText(R.string.setting_no_backup);
					}
				}else if(position == 7){//is update
					if(is_update == 1){
						Log.d("UPDATE", "red.........................red.........................red");
						holder2.tipInfo.setTextColor(R.color.red);
					}
					holder2.tipInfo.setText(is_update == 1 ? getResources().getString(R.string.soft_update_yes) : getResources().getString(R.string.soft_update_no));
				
				}else if(position == 8){//data initialize
					if(tip == 1){
						holder2.tipInfo.setText(getResources().getString(R.string.bomc_open_tip));
					}
				}
				return convertView;
				
			default:
				break;
			}
			
			return convertView;
		}
		
	}

	
	public class ToggleButtonListener implements OnClickListener {

		private Switch tSwitch;
		private int position;

		public ToggleButtonListener(Switch tSwitch, int position) {
			this.tSwitch = tSwitch;
			this.position = position;
		}

		@Override
		public void onClick(View arg0) {
			ContentValues settingValues = new ContentValues();;//setting contentValues
			ContentValues alarmValues = new ContentValues();//oclock and intercepter contentValues
			
			switch (position) {
			case 0://周期短信监听
				if(tSwitch.isChecked()){
					settingValues.put(SmsSetting.IS_PERIOD_LISTEN, 1);
				}else {
					settingValues.put(SmsSetting.IS_PERIOD_LISTEN, 0);
				}
				db.update(SmsSetting.TABLE_NAME, settingValues, SmsSetting.ID + "=1", null);
				break;
			case 1://deal oclock
				if(tSwitch.isChecked()){
					
					restartMyService();
					
//					settingValues.put(SmsSetting.IS_OCLOCK, 1);
					alarmValues.put(SmsAlarm.IS_USE, 1);
				}else {
//					settingValues.put(SmsSetting.IS_OCLOCK, 0);
					alarmValues.put(SmsAlarm.IS_USE, 0);
				}
				// update setting table
//				db.update(SmsSetting.TABLE_NAME, settingValues, SmsSetting.ID + "=?", new String[]{"1"});
				
				// update smsAlarm table
				db.update(SmsAlarm.TABLE_NAME, alarmValues, null, null);
				
				break;
			case 2://deal intecept
				if(tSwitch.isChecked()){
					
					restartMyService();
					
//					settingValues.put(SmsSetting.IS_INTERCEPT, 1);
					alarmValues.put(SmsIntercept.IS_USE, 1);
				}else {
//					settingValues.put(SmsSetting.IS_INTERCEPT, 0);
					alarmValues.put(SmsIntercept.IS_USE, 0);
				}
				// update setting table
//				db.update(SmsSetting.TABLE_NAME, settingValues, SmsSetting.ID + "=?", new String[]{"1"});
				
				//update sms intecepter table
				db.update(SmsIntercept.TABLE_NAME, alarmValues, null, null);
				
				break;
			case 3://deal intecept ring
				
				break;
			default:
				break;
			}
		}
		
		private void restartMyService(){
			Intent itent = new Intent();
			itent.setClass(SettingActivity.this, SmsService.class);
			stopService(itent);
			startService(itent);
		}

	}
	
	public class MyHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Bundle bundle = msg.getData();
			boolean is_backup_finish = bundle.getBoolean(ConstantUtil.IS_BACKUP_FINISH);
			boolean is_restore_finish = bundle.getBoolean(ConstantUtil.IS_RESTORE_FINISH);
			long clock_count = bundle.getLong(ConstantUtil.CLOCK_COUNT, 0);
			long intercept_count = bundle.getLong(ConstantUtil.INTERCEPT_COUNT, 0);
			long history_count = bundle.getLong(ConstantUtil.HISTORY_COUNT, 0);
			long periodlisten_count = bundle.getLong(ConstantUtil.PERIODLISTEN_COUNT, 0);//add since v2.4.6.8.8
			
			long total = clock_count + intercept_count + history_count + periodlisten_count;//modify since v2.4.6.8.8
			
			if(is_backup_finish){
				if(total == 0){
					Toast.makeText(SettingActivity.this, "暂无数据，不进行备份", Toast.LENGTH_SHORT).show();
				}else {
					showBuckupSuccessDialog(clock_count, intercept_count, history_count,periodlisten_count, "备份");
				}
			}
			if(is_restore_finish){
				if(total == 0){
					Toast.makeText(SettingActivity.this, "请先备份数据", Toast.LENGTH_SHORT).show();
				}else {
					showBuckupSuccessDialog(clock_count, intercept_count, history_count, periodlisten_count, "恢复");
				}
			}
		}
		
		/**
		 * modify since v2.4.6.8.8
		 * @param clock_count
		 * @param intercept_count
		 * @param history_count
		 * @param periodlisten_count
		 * @param tip
		 */
		private void showBuckupSuccessDialog(long clock_count, long intercept_count, long history_count,
											long periodlisten_count, String tip){
			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle("提示");
			TextView tv = new TextView(SettingActivity.this);
			String content = tip + "闹钟关键字【"+clock_count+"】条 \n"
							+ tip +  "拦截关键字【"+intercept_count+"】条 \n"
							+ tip +  "历史记录【"+history_count+"】条 \n"
							+ tip + "周期短信监控【"+periodlisten_count+"】条";
			tv.setText(content);
			builder.setView(tv);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					reloadListView();
				}
			});
			builder.create().show();
		}
	}
}
