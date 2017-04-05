/**
 * 
 */
package com.superman.smsalarm.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.superman.smsalarm.R;
import com.superman.smsalarm.model.SmsAlarmModel;
import com.superman.smsalarm.model.HistoryModel;
import com.superman.smsalarm.model.PeriodListenModel;
import com.superman.smsalarm.model.SettingModel;
import com.superman.util.ConstantUtil;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsHistory;
import com.superman.util.SQLite.SmsIntercept;
import com.superman.util.SQLite.SmsPeriodListen;
import com.superman.util.SQLite.SmsSetting;

/**
 * <p>
 * Title: com.superman.smsalarm.backup.Restore.java
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
 * @version 1.0 CreateTime：2014-3-31 上午11:30:03
 */

public class Restore {

	private Context context;
	private SQLiteDatabase db;
	private String currentDir;

	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandle = new Handler();
	private long total_size;
	private long size;// backup size

	private long clock_count, intercept_count, history_count, periodlisten_count;
	private ParseXmlService parse = new ParseXmlService();
	private Handler handler;

	/**
	 * 
	 */
	public Restore(Context context, SQLiteDatabase db, Handler handler) {
		this.context = context;
		this.db = db;
		this.handler = handler;
	}

	public void restore() {
		if (db == null) {
			db = SmsAlarmDao.getDbInstance(context);
		}
		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(true);
		progressBar.setMessage(context.getString(R.string.setting_restore));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();

		progressBarStatus = 0;

		final List<SmsAlarmModel> clockList = querySmsAlarmData(ConstantUtil.BACKUP_CLOCK_FILE_NAME);
		final List<SmsAlarmModel> interceptList = querySmsInterceptData(ConstantUtil.BACKUP_INTERCEPT_FILE_NAME);
		final List<HistoryModel> historyList = querySmsHistory();
		final List<SettingModel> settingList = querySmsSetting();//add since v2.4.6.8.8
		final List<PeriodListenModel> periodlistenList = queryPeriodListen();//add since v2.4.6.8.8
		

		clock_count = clockList == null ? 0 : clockList.size();
		intercept_count = interceptList == null ? 0 : interceptList.size();
		history_count = historyList == null ? 0 : historyList.size();
		periodlisten_count = periodlistenList == null ? 0 : periodlistenList.size();//add since v2.4.6.8.8
		
		total_size = size = clock_count + intercept_count + history_count + periodlisten_count;//modify since v2.4.6.8.8
		
		if(total_size == 0){
			progressBar.dismiss();
			sendHandler();
			return ;
		}
		
		clearAllData();//clear data

		new Thread(new Runnable() {

			@Override
			public void run() {
				saveClockRecord(clockList);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				saveInteceptRecord(interceptList);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				saveHistoryRecord(historyList);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				//add since v2.4.6.8.8
				saveSettingRecord(settingList);
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				//add since v2.4.6.8.8
				savePeriodListenRecord(periodlistenList);
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				while (progressBarStatus < 100) {

					// deal progress
					progressBarStatus = dealBackUp();

					// sleep 1 second
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						LogUtil.saveLog(e.toString());
					}

					// update status
					progressBarHandle.post(new Runnable() {

						@Override
						public void run() {
							progressBar.setProgress(progressBarStatus);
						}
					});

					if (progressBarStatus >= 100) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sendHandler();
						progressBar.dismiss();
					}
				}
			}
		}).start();

	}
	
	private void sendHandler(){
		//deal handler
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putBoolean(ConstantUtil.IS_BACKUP_FINISH, false);
		bundle.putBoolean(ConstantUtil.IS_RESTORE_FINISH, true);
		bundle.putLong(ConstantUtil.CLOCK_COUNT, clock_count);
		bundle.putLong(ConstantUtil.INTERCEPT_COUNT, intercept_count);
		bundle.putLong(ConstantUtil.HISTORY_COUNT, history_count);
		bundle.putLong(ConstantUtil.PERIODLISTEN_COUNT, periodlisten_count);//add since v2.4.6.8.8
		message.setData(bundle);
		handler.sendMessage(message);
	}
	
	/**
	 * clear all data
	 * @param list
	 */
	private void clearAllData(){
		db.delete(SmsAlarm.TABLE_NAME, null, null);
		db.delete(SmsIntercept.TABLE_NAME, null, null);
		db.delete(SmsHistory.TABLE_NAME, null, null);
		db.delete(SmsSetting.TABLE_NAME, null, null);
		db.delete(SmsPeriodListen.TABLE_NAME, null, null);
	}
	
	//save clock record to database
	private void saveClockRecord(List<SmsAlarmModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (SmsAlarmModel model : list) {
			ContentValues values = new ContentValues();
			values.put(SmsAlarm.ID, model.getId());
			values.put(SmsAlarm.IS_UPDATE, model.isIs_update() ? 1 : 0);
			values.put(SmsAlarm.IS_USE, model.isIs_use() ? 1 : 0);
			values.put(SmsAlarm.KEY_WORD, model.getKey_word());
			values.put(SmsAlarm.PHONENUMBER, model.getPhone_number());
			values.put(SmsAlarm.CREATE_TIME, model.getCreate_time());
			db.insert(SmsAlarm.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
		
	}
	
	//save intercept record to database
	private void saveInteceptRecord(List<SmsAlarmModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (SmsAlarmModel model : list) {
			ContentValues values = new ContentValues();
			values.put(SmsIntercept.ID, model.getId());
			values.put(SmsIntercept.IS_UPDATE, model.isIs_update() ? 1 : 0);
			values.put(SmsIntercept.IS_USE, model.isIs_use() ? 1 : 0);
			values.put(SmsIntercept.KEY_WORD, model.getKey_word());
			values.put(SmsIntercept.PHONENUMBER, model.getPhone_number());
			values.put(SmsIntercept.CREATE_TIME, model.getCreate_time());
			values.put(SmsIntercept.IS_RING, model.getIs_ring());
			db.insert(SmsIntercept.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
	}
	
	//save intercept record to database
	private void saveHistoryRecord(List<HistoryModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (HistoryModel model : list) {
			ContentValues values = new ContentValues();
			values.put(SmsHistory.ID, model.getId());
			values.put(SmsHistory.SMS_TEXT, model.getSms_text());
			values.put(SmsHistory.RECV_PHONENUMBER, model.getRecv_phonenumber());
			values.put(SmsHistory.RECEIVER_TIME, model.getReceiver_time());
			values.put(SmsHistory.HISTORY_TYPE, model.getHistory_type());
			values.put(SmsHistory.ALARM_ID, model.getAlarm_id());
			db.insert(SmsHistory.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @param list
	 */
	private void saveSettingRecord(List<SettingModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (SettingModel model : list) {
			ContentValues values = new ContentValues();
			values.put(SmsSetting.ID, model.getId());
			values.put(SmsSetting.IS_OCLOCK, model.getIs_oclock());
			values.put(SmsSetting.IS_DISTURB, model.getIs_disturb());
			values.put(SmsSetting.DISTURB_DATE, model.getDisturb_date());
			values.put(SmsSetting.DISTURB_BEGIN_INTERVAL, model.getDisturb_begin_interval());
			values.put(SmsSetting.DISTURB_END_INTERVAL, model.getDisturb_end_interval());
			values.put(SmsSetting.LAST_BACKUP_TIME, model.getLast_backup_time());
			values.put(SmsSetting.IS_UPDATE, model.getIs_update());
			values.put(SmsSetting.RING_PATH, model.getRing_path());
			values.put(SmsSetting.RING_NAME, model.getRing_name());
			values.put(SmsSetting.STATUS, model.getStatus());
			values.put(SmsSetting.AUTO_REFRESH_TIME, model.getAuto_refresh_time());
			values.put(SmsSetting.IS_PERIOD_LISTEN, model.getIs_period_listen());
			db.insert(SmsSetting.TABLE_NAME, null, values);
		}
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @param list
	 */
	private void savePeriodListenRecord(List<PeriodListenModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (PeriodListenModel model : list) {
			ContentValues values = new ContentValues();
			values.put(SmsPeriodListen.ID, model.getId());
			values.put(SmsPeriodListen.KEYWORD, model.getKeyword());
			values.put(SmsPeriodListen.NUMBER, model.getNumber());
			values.put(SmsPeriodListen.PERIOD_TYPE, model.getPeriod_type());
			values.put(SmsPeriodListen.PERIOD, model.getPeriod());
			values.put(SmsPeriodListen.LISTEN_PERIOD, model.getListen_period());
			values.put(SmsPeriodListen.IS_DELAY_ALARM, model.getIs_delay_alarm());
			values.put(SmsPeriodListen.DELAY_PERIOD_TYPE, model.getDelay_period_type());
			values.put(SmsPeriodListen.DELAY_PERIOD, model.getDelay_period());
			values.put(SmsPeriodListen.LAST_REV_TIME, model.getLast_rev_time());
			values.put(SmsPeriodListen.IS_USE, model.getIs_use());
			values.put(SmsPeriodListen.CREATE_TIME, model.getCreate_time());
			values.put(SmsPeriodListen.LISTEN_TIME_DAY, model.getListen_time_day());
			values.put(SmsPeriodListen.LISTEN_TIME_HOUR, model.getListen_time_hour());
			values.put(SmsPeriodListen.LISTEN_TIME_MINUTE, model.getListen_time_minute());
			values.put(SmsPeriodListen.MISSING_PERIOD, model.getMissing_period());
			db.insert(SmsPeriodListen.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
	}
	
	/**
	 * add since v2.4.5.1 backup data
	 * 
	 * @return
	 */
	private int dealBackUp() {
		return Math.round(100 - (((float)size / (float)total_size) * 100));
	}

	/**
	 * query sms clock data from xml
	 * @throws FileNotFoundException 
	 */
	private List<SmsAlarmModel> querySmsAlarmData(String backup_name) {
		List<SmsAlarmModel> reList = new ArrayList<SmsAlarmModel>();
		String file_name = SmsAlarmDao.findDefaultBackupPath() + backup_name;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				SmsAlarmModel model = new SmsAlarmModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(value);
					}else if("phonenumber".equals(key)){
						model.setPhone_number(value);
					}else if("keyword".equals(key)){
						model.setKey_word(value);
					}else if("create_time".equals(key)){
						model.setCreate_time(value);
					}else if("is_update".equals(key)){
						model.setIs_update(Boolean.parseBoolean(value));
					}else {
						model.setIs_use(Boolean.parseBoolean(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			}
		}
		
		return reList;
	}
	
	/**
	 * add since v2.4.6.8.11
	 * @param backup_name
	 * @return
	 */
	private List<SmsAlarmModel> querySmsInterceptData(String backup_name) {
		List<SmsAlarmModel> reList = new ArrayList<SmsAlarmModel>();
		String file_name = SmsAlarmDao.findDefaultBackupPath() + backup_name;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				SmsAlarmModel model = new SmsAlarmModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(value);
					}else if("phonenumber".equals(key)){
						model.setPhone_number(value);
					}else if("keyword".equals(key)){
						model.setKey_word(value);
					}else if("create_time".equals(key)){
						model.setCreate_time(value);
					}else if("is_use".equals(key)){
						model.setIs_use(Boolean.parseBoolean(value));
					}else if("is_update".equals(key)){
						model.setIs_update(Boolean.parseBoolean(value));
					}else if("is_ring".equals(key)){
						model.setIs_ring(Integer.parseInt(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			}
		}
		
		return reList;
	}

	private List<HistoryModel> querySmsHistory(){
		List<HistoryModel> reList = new ArrayList<HistoryModel>();

		String file_name = SmsAlarmDao.findDefaultBackupPath() + ConstantUtil.BACKUP_HISTORY_FILE_NAME;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				HistoryModel model = new HistoryModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(Integer.parseInt(value));
					}else if("sms_text".equals(key)){
						model.setSms_text(value);
					}else if("recv_phonenumber".equals(key)){
						model.setRecv_phonenumber(value);
					}else if("receiver_time".equals(key)){
						model.setReceiver_time(value);
					}else if("history_type".equals(key)){
						model.setHistory_type(Integer.parseInt(value));
					}else {
						model.setAlarm_id(Integer.parseInt(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			}
		}
		
		return reList;
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @return
	 */
	private List<SettingModel> querySmsSetting(){
		List<SettingModel> reList = new ArrayList<SettingModel>();

		String file_name = SmsAlarmDao.findDefaultBackupPath() + ConstantUtil.BACKUP_SETTING_FILE_NAME;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				SettingModel model = new SettingModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(Long.parseLong(value));
					}else if("is_oclock".equals(key)){
						model.setIs_oclock(Integer.parseInt(value));
					}else if("is_disturb".equals(key)){
						model.setIs_disturb(Integer.parseInt(value));
					}else if("disturb_date".equals(key)){
						model.setDisturb_date(value);
					}else if("disturb_begin_interval".equals(key)){
						model.setDisturb_begin_interval(value);
					}else if("disturb_end_interval".equals(key)){
						model.setDisturb_end_interval(value);
					}else if("last_backup_time".equals(key)){
						model.setLast_backup_time(value);
					}else if("is_update".equals(key)){
						model.setIs_update(Integer.parseInt(value));
					}else if("ring_path".equals(key)){
						model.setRing_path(value);
					}else if("ring_name".equals(key)){
						model.setRing_name(value);
					}else if("status".equals(key)){
						model.setStatus(Integer.parseInt(value));
					}else if("auto_refresh_time".equals(key)){
						model.setAuto_refresh_time(Long.parseLong(value));
					}else if("is_period_listen".equals(key)){
						model.setIs_period_listen(Integer.parseInt(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			}
		}
		
		return reList;
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @return
	 */
	private List<PeriodListenModel> queryPeriodListen(){
		List<PeriodListenModel> reList = new ArrayList<PeriodListenModel>();

		String file_name = SmsAlarmDao.findDefaultBackupPath() + ConstantUtil.BACKUP_PERIODLISTEN_FILE_NAME;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				PeriodListenModel model = new PeriodListenModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(Long.parseLong(value));
					}else if("keyword".equals(key)){
						model.setKeyword(value);
					}else if("number".equals(key)){
						model.setNumber(value);
					}else if("period_type".equals(key)){
						model.setPeriod_type(Integer.parseInt(value));
					}else if("period".equals(key)){
						model.setPeriod(Integer.parseInt(value));
					}else if("listen_period".equals(key)){
						model.setListen_period(Integer.parseInt(value));
					}else if("is_delay_alarm".equals(key)){
						model.setIs_delay_alarm(Integer.parseInt(value));
					}else if("delay_period_type".equals(key)){
						model.setDelay_period_type(Integer.parseInt(value));
					}else if("delay_period".equals(key)){
						model.setDelay_period(Integer.parseInt(value));
					}else if("last_rev_time".equals(key)){
						model.setLast_rev_time(Long.parseLong(value));
					}else if("is_use".equals(key)){
						model.setIs_use(Integer.parseInt(value));
					}else if("create_time".equals(key)){
						model.setCreate_time(Long.parseLong(value));
					}else if("listen_time_day".equals(key)){
						model.setListen_time_day(Integer.parseInt(value));
					}else if("listen_time_hour".equals(key)){
						model.setListen_time_hour(Integer.parseInt(value));
					}else if("listen_time_minute".equals(key)){
						model.setListen_time_minute(Integer.parseInt(value));
					}else if("missing_period".equals(key)){
						model.setMissing_period(Integer.parseInt(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			}
		}
		
		return reList;
	}
}
