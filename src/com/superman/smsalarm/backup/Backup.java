/**
 * 
 */
package com.superman.smsalarm.backup;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.superman.smsalarm.R;
import com.superman.smsalarm.model.HistoryModel;
import com.superman.smsalarm.model.PeriodListenModel;
import com.superman.smsalarm.model.SettingModel;
import com.superman.smsalarm.model.SmsAlarmModel;
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
 * Title: com.superman.util.Backup.java
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
 * @version 1.0 CreateTime：2014-3-30 下午7:18:15
 */

public class Backup {

	private Context context;
	private SQLiteDatabase db;
	private String currentDir;

	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandle = new Handler();
	private long total_size;
	private long size;//backup size
	
	private long clock_count, intercept_count, history_count, periodlisten_count;//modify since v2.4.6.8.8
	
	private Handler handler;

	public Backup(Context context, SQLiteDatabase db, String currentDir, Handler handler) {
		this.context = context;
		this.db = db;
		this.currentDir = currentDir;
		this.handler = handler;
	}
	
	public Backup(Context context, SQLiteDatabase db) {
		this.context = context;
		this.db = db;
	}

	/**
	 * 升级的时候备份使用，无进度条
	 */
	public void upgradeBackup() {
		if(db == null){
			db = SmsAlarmDao.getDbInstance(context);
		}
		
		final List<SmsAlarmModel> clockList = querySmsAlarmData();
		final List<SmsAlarmModel> interceptList = querySmsInterceptData();
		final List<HistoryModel> historyList = querySmsHistory();
		final List<SettingModel> settingList = querySmsSetting();//add since v2.4.6.8.8
		final List<PeriodListenModel> periodlistenList = querySmsPeriodListen();//add since v2.4.6.8.8
		
		WriteXml write = new WriteXml();
		String smsClockXml = createSmsClockXml(clockList);
		write.writeXml(smsClockXml, ConstantUtil.UPGRADE_BACKUP_CLOCK_FILE_NAME, currentDir);
		
		String smsInterceptXml = createSmsInterceptXml(interceptList);
		write.writeXml(smsInterceptXml, ConstantUtil.UPGRADE_BACKUP_INTERCEPT_FILE_NAME, currentDir);
		
		String smsHistoryXml = createSmsHistoryXml(historyList);
		write.writeXml(smsHistoryXml, ConstantUtil.UPGRADE_BACKUP_HISTORY_FILE_NAME, currentDir);
		
		/*
		 * add since v2.4.6.8.8
		 */
		String smsSettingXml = createSettingXml(settingList);
		write.writeXml(smsSettingXml, ConstantUtil.UPGRADE_BACKUP_SETTING_FILE_NAME, currentDir);
		
		/*
		 * add since v2.4.6.8.8
		 */
		String smsPeriodListenXml = createPeriodListenXml(periodlistenList);
		write.writeXml(smsPeriodListenXml, ConstantUtil.UPGRADE_BACKUP_PERIODLISTEN_FILE_NAME, currentDir);
				
	}
	
	
	/**
	 * add since v2.4.5.1 backup all data
	 */
	public void backup() {
		if(db == null){
			db = SmsAlarmDao.getDbInstance(context);
		}
		
		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(true);
		progressBar.setMessage(context.getString(R.string.setting_backup));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();

		progressBarStatus = 0;
		
		final List<SmsAlarmModel> clockList = querySmsAlarmData();
		final List<SmsAlarmModel> interceptList = querySmsInterceptData();
		final List<HistoryModel> historyList = querySmsHistory();
		
		final List<SettingModel> settingList = querySmsSetting();//add since v2.4.6.8.8
		final List<PeriodListenModel> periodlistenList = querySmsPeriodListen();//add since v2.4.6.8.8
		
		clock_count = clockList.size();
		intercept_count = interceptList.size();
		history_count = historyList.size();
		periodlisten_count = periodlistenList.size();//add since v2.4.6.8.8
		total_size = size = clock_count + intercept_count + history_count + periodlisten_count;//modify since v2.4.6.8.8
		
		if(total_size == 0){
			progressBar.dismiss();
			sendHandler();
			return ;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				WriteXml write = new WriteXml();
				String smsClockXml = createSmsClockXml(clockList);
				write.writeXml(smsClockXml, ConstantUtil.BACKUP_CLOCK_FILE_NAME, currentDir);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				String smsInterceptXml = createSmsInterceptXml(interceptList);
				write.writeXml(smsInterceptXml, ConstantUtil.BACKUP_INTERCEPT_FILE_NAME, currentDir);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				String smsHistoryXml = createSmsHistoryXml(historyList);
				write.writeXml(smsHistoryXml, ConstantUtil.BACKUP_HISTORY_FILE_NAME, currentDir);
				
				/*
				 * add since v2.4.6.8.8
				 */
				String smsSettingXml = createSettingXml(settingList);
				write.writeXml(smsSettingXml, ConstantUtil.BACKUP_SETTING_FILE_NAME, currentDir);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
				
				/*
				 * add since v2.4.6.8.8
				 */
				String smsPeroidListenXml = createPeriodListenXml(periodlistenList);
				write.writeXml(smsPeroidListenXml, ConstantUtil.BACKUP_PERIODLISTEN_FILE_NAME, currentDir);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LogUtil.saveLog(e.toString());
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				
				while(progressBarStatus < 100){
					
					// deal progress
					progressBarStatus = dealBackUp();
					
					// sleep 1 second
					try {
						Thread.sleep(500);
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
						
						updateBackTime();
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
		bundle.putBoolean(ConstantUtil.IS_BACKUP_FINISH, true);
		bundle.putBoolean(ConstantUtil.IS_RESTORE_FINISH, false);
		bundle.putLong(ConstantUtil.CLOCK_COUNT, clock_count);
		bundle.putLong(ConstantUtil.INTERCEPT_COUNT, intercept_count);
		bundle.putLong(ConstantUtil.HISTORY_COUNT, history_count);
		bundle.putLong(ConstantUtil.PERIODLISTEN_COUNT, periodlisten_count);
		message.setData(bundle);
		handler.sendMessage(message);
	}
	
	private void updateBackTime(){
		ContentValues values = new ContentValues();
		values.put(SmsSetting.LAST_BACKUP_TIME, SmsAlarmDao.getCurrentTruncDate());
		db.update(SmsSetting.TABLE_NAME, values, null, null);
	}
	
	private String createSmsClockXml(List<SmsAlarmModel> list){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<sms_alarm>");
		for (SmsAlarmModel alarmModel : list) {
			buff.append("<rcd>");
			buff.append("<id>" + alarmModel.getId() + "</id>");
			buff.append("<phonenumber>" + alarmModel.getPhone_number() + "</phonenumber>");
			buff.append("<keyword>" + alarmModel.getKey_word() + "</keyword>");
			buff.append("<create_time>" + alarmModel.getCreate_time() + "</create_time>");
			buff.append("<is_use>" + alarmModel.isIs_use() + "</is_use>");
			buff.append("<is_update>" + alarmModel.isIs_update() + "</is_update>");
			buff.append("</rcd>");
			
			size--;
		}
		buff.append("</sms_alarm>");
		return buff.toString();
	}
	
	private String createSmsInterceptXml(List<SmsAlarmModel> list){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<sms_intercept>");
		for (SmsAlarmModel alarmModel : list) {
			buff.append("<rcd>");
			buff.append("<id>" + alarmModel.getId() + "</id>");
			buff.append("<phonenumber>" + alarmModel.getPhone_number() + "</phonenumber>");
			buff.append("<keyword>" + alarmModel.getKey_word() + "</keyword>");
			buff.append("<create_time>" + alarmModel.getCreate_time() + "</create_time>");
			buff.append("<is_use>" + alarmModel.isIs_use() + "</is_use>");
			buff.append("<is_update>" + alarmModel.isIs_update() + "</is_update>");
			buff.append("<is_ring>" + alarmModel.getIs_ring() + "</is_ring>");
			buff.append("</rcd>");
			
			size--;
		}
		buff.append("</sms_intercept>");
		return buff.toString();
	}
	
	private String createSmsHistoryXml(List<HistoryModel> list){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<sms_history>");
		for (HistoryModel historyModel : list) {
			buff.append("<rcd>");
			buff.append("<id>" + historyModel.getId() + "</id>");
			buff.append("<sms_text>" + historyModel.getSms_text() + "</sms_text>");
			buff.append("<recv_phonenumber>" + historyModel.getRecv_phonenumber() + "</recv_phonenumber>");
			buff.append("<receiver_time>" + historyModel.getReceiver_time() + "</receiver_time>");
			buff.append("<history_type>" + historyModel.getHistory_type() + "</history_type>");
			buff.append("<alarm_id>" + historyModel.getAlarm_id() + "</alarm_id>");
			buff.append("</rcd>");
			
			size--;
		}
		buff.append("</sms_history>");
		return buff.toString();
	}
	
	/**
	 * 创建setting 的xml文件 add since v2.4.6.8.8
	 * @param list
	 * @return
	 */
	private String createSettingXml(List<SettingModel> list){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<sms_setting>");
		for (SettingModel model : list) {
			buff.append("<rcd>");
			buff.append("<id>" + model.getId() + "</id>");
			buff.append("<is_oclock>" + model.getIs_oclock() + "</is_oclock>");
			buff.append("<is_disturb>" + model.getIs_disturb() + "</is_disturb>");
			buff.append("<disturb_date>" + model.getDisturb_date() + "</disturb_date>");
			buff.append("<disturb_begin_interval>" + model.getDisturb_begin_interval() + "</disturb_begin_interval>");
			buff.append("<disturb_end_interval>" + model.getDisturb_end_interval() + "</disturb_end_interval>");
			buff.append("<last_backup_time>" + model.getLast_backup_time() + "</last_backup_time>");
			buff.append("<is_update>" + model.getIs_update() + "</is_update>");
			buff.append("<ring_path>" + model.getRing_path() + "</ring_path>");
			buff.append("<ring_name>" + model.getRing_name() + "</ring_name>");
			buff.append("<status>" + model.getStatus() + "</status>");
			buff.append("<auto_refresh_time>" + model.getAuto_refresh_time() + "</auto_refresh_time>");
			buff.append("<is_period_listen>" + model.getIs_period_listen() + "</is_period_listen>");
			buff.append("</rcd>");
		}
		buff.append("</sms_setting>");
		return buff.toString();
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @param list
	 * @return
	 */
	private String createPeriodListenXml(List<PeriodListenModel> list){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<sms_periodlisten>");
		for (PeriodListenModel model : list) {
			buff.append("<rcd>");
			buff.append("<id>" + model.getId() + "</id>");
			buff.append("<keyword>" + model.getKeyword() + "</keyword>");
			buff.append("<number>" + model.getNumber() + "</number>");
			buff.append("<period_type>" + model.getPeriod_type() + "</period_type>");
			buff.append("<period>" + model.getPeriod() + "</period>");
			buff.append("<listen_time_day>" + model.getListen_time_day() + "</listen_time_day>");
			buff.append("<listen_time_hour>" + model.getListen_time_hour() + "</listen_time_hour>");
			buff.append("<listen_time_minute>" + model.getListen_time_minute() + "</listen_time_minute>");
			buff.append("<listen_period>" + model.getListen_period() + "</listen_period>");
			buff.append("<is_delay_alarm>" + model.getIs_delay_alarm() + "</is_delay_alarm>");
			buff.append("<delay_period_type>" + model.getDelay_period() + "</delay_period_type>");
			buff.append("<delay_period>" + model.getDelay_period() + "</delay_period>");
			buff.append("<last_rev_time>" + model.getLast_rev_time() + "</last_rev_time>");
			buff.append("<is_use>" + model.getIs_use() + "</is_use>");
			buff.append("<create_time>" + model.getCreate_time() + "</create_time>");
			buff.append("<missing_period>" + model.getMissing_period() + "</missing_period>");
			buff.append("</rcd>");
			
			size--;
		}
		buff.append("</sms_periodlisten>");
		return buff.toString();
	}
	
	/**
	 * add since v2.4.5.1 backup data
	 * 
	 * @return
	 */
	private int dealBackUp() {
		return Math.round(100 - (((float)size / (float)total_size) * 100));
	}

	private List<SmsAlarmModel> querySmsAlarmData() {
		List<SmsAlarmModel> reList = new ArrayList<SmsAlarmModel>();
		Cursor c = null;
		try {
			c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.ID, SmsAlarm.PHONENUMBER, SmsAlarm.KEY_WORD,
						SmsAlarm.CREATE_TIME, SmsAlarm.IS_USE, SmsAlarm.IS_UPDATE}, null, null, null, null, null);
			while(c.moveToNext()){
				SmsAlarmModel model = new SmsAlarmModel();
				model.setAlarm_type(ConstantUtil.SMS_CLOCK);
				model.setId(c.getString(c.getColumnIndex(SmsAlarm.ID)));
				model.setPhone_number(c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER)));
				model.setKey_word(SmsAlarmDao.transSpecChar(c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD))));
				model.setCreate_time(c.getString(c.getColumnIndex(SmsAlarm.CREATE_TIME)));
				model.setIs_use(c.getInt(c.getColumnIndex(SmsAlarm.IS_USE)) == 1 ? true : false);
				model.setIs_update(c.getInt(c.getColumnIndex(SmsAlarm.IS_UPDATE)) == 1 ? true : false);
				
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		return reList;
	}

	private List<SmsAlarmModel> querySmsInterceptData() {
		List<SmsAlarmModel> reList = new ArrayList<SmsAlarmModel>();
		Cursor c = null;
		try {
			c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsIntercept.ID, SmsIntercept.PHONENUMBER, SmsIntercept.KEY_WORD,
					SmsIntercept.CREATE_TIME, SmsIntercept.IS_USE, SmsIntercept.IS_UPDATE, SmsIntercept.IS_RING}, null, null, null, null, null);
			while(c.moveToNext()){
				SmsAlarmModel model = new SmsAlarmModel();
				model.setAlarm_type(ConstantUtil.SMS_INTERCEPT);
				model.setId(c.getString(c.getColumnIndex(SmsIntercept.ID)));
				model.setPhone_number(c.getString(c.getColumnIndex(SmsIntercept.PHONENUMBER)));
				model.setKey_word(SmsAlarmDao.transSpecChar(c.getString(c.getColumnIndex(SmsIntercept.KEY_WORD))));
				model.setCreate_time(c.getString(c.getColumnIndex(SmsIntercept.CREATE_TIME)));
				model.setIs_use(c.getInt(c.getColumnIndex(SmsIntercept.IS_USE)) == 1 ? true : false);
				model.setIs_update(c.getInt(c.getColumnIndex(SmsIntercept.IS_UPDATE)) == 1 ? true : false);
				model.setIs_ring(c.getInt(c.getColumnIndex(SmsIntercept.IS_RING)));
				
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		return reList;
	}
	
	private List<HistoryModel> querySmsHistory(){
		List<HistoryModel> reList = new ArrayList<HistoryModel>();
		Cursor c = null; 
		try {
			c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID, SmsHistory.RECEIVER_TIME, SmsHistory.HISTORY_TYPE,
						SmsHistory.RECV_PHONENUMBER, SmsHistory.ALARM_ID, SmsHistory.SMS_TEXT}, null, null, null, null, null);
			while(c.moveToNext()){
				HistoryModel model = new HistoryModel();
				model.setId(c.getInt(c.getColumnIndex(SmsHistory.ID)));
				model.setHistory_type(c.getInt(c.getColumnIndex(SmsHistory.HISTORY_TYPE)));
				model.setAlarm_id(c.getInt(c.getColumnIndex(SmsHistory.ALARM_ID)));
				model.setReceiver_time(c.getString(c.getColumnIndex(SmsHistory.RECEIVER_TIME)));
				model.setRecv_phonenumber(c.getString(c.getColumnIndex(SmsHistory.RECV_PHONENUMBER)));
				model.setSms_text(SmsAlarmDao.transSpecChar(c.getString(c.getColumnIndex(SmsHistory.SMS_TEXT))));
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
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
		Cursor c = null; 
		try {
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.ID, SmsSetting.IS_OCLOCK, SmsSetting.IS_DISTURB, 
						SmsSetting.DISTURB_DATE, SmsSetting.DISTURB_BEGIN_INTERVAL, SmsSetting.DISTURB_END_INTERVAL,
						SmsSetting.LAST_BACKUP_TIME, SmsSetting.IS_UPDATE, SmsSetting.RING_PATH, SmsSetting.RING_NAME,
						SmsSetting.STATUS, SmsSetting.AUTO_REFRESH_TIME, SmsSetting.IS_PERIOD_LISTEN}, null, null, null, null, null);
			while(c.moveToNext()){
				SettingModel model = new SettingModel();
				model.setId(c.getLong(c.getColumnIndex(SmsSetting.ID)));
				model.setIs_oclock(c.getInt(c.getColumnIndex(SmsSetting.IS_OCLOCK)));
				model.setIs_disturb(c.getInt(c.getColumnIndex(SmsSetting.IS_DISTURB)));
				model.setDisturb_date(c.getString(c.getColumnIndex(SmsSetting.DISTURB_DATE)));
				model.setDisturb_begin_interval(c.getString(c.getColumnIndex(SmsSetting.DISTURB_BEGIN_INTERVAL)));
				model.setDisturb_end_interval(c.getString(c.getColumnIndex(SmsSetting.DISTURB_END_INTERVAL)));
				model.setLast_backup_time(c.getString(c.getColumnIndex(SmsSetting.LAST_BACKUP_TIME)));
				model.setIs_update(c.getInt(c.getColumnIndex(SmsSetting.IS_UPDATE)));
				model.setRing_path(c.getString(c.getColumnIndex(SmsSetting.RING_PATH)));
				model.setRing_name(c.getString(c.getColumnIndex(SmsSetting.RING_NAME)));
				model.setStatus(0);//默认为0，不在播放音乐
				model.setAuto_refresh_time(c.getLong(c.getColumnIndex(SmsSetting.AUTO_REFRESH_TIME)));
				model.setIs_period_listen(c.getInt(c.getColumnIndex(SmsSetting.IS_PERIOD_LISTEN)));
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		
		return reList;
	}
	
	/**
	 * add since v2.4.6.8.8
	 * @return
	 */
	private List<PeriodListenModel> querySmsPeriodListen(){
		List<PeriodListenModel> reList = new ArrayList<PeriodListenModel>();
		Cursor c = null; 
		try {
			c = db.query(SmsPeriodListen.TABLE_NAME, new String[]{SmsPeriodListen.ID, SmsPeriodListen.KEYWORD,
					SmsPeriodListen.PERIOD_TYPE, SmsPeriodListen.PERIOD, SmsPeriodListen.LISTEN_PERIOD,
					SmsPeriodListen.IS_DELAY_ALARM, SmsPeriodListen.DELAY_PERIOD_TYPE, 
					SmsPeriodListen.DELAY_PERIOD, SmsPeriodListen.LAST_REV_TIME, 
					SmsPeriodListen.IS_USE, SmsPeriodListen.CREATE_TIME, SmsPeriodListen.NUMBER,
					SmsPeriodListen.LISTEN_TIME_DAY, SmsPeriodListen.LISTEN_TIME_HOUR, SmsPeriodListen.LISTEN_TIME_MINUTE, SmsPeriodListen.MISSING_PERIOD}, null, null, null, null, null);
			while(c.moveToNext()){
				PeriodListenModel model = new PeriodListenModel();
				model.setId(c.getLong(c.getColumnIndex(SmsPeriodListen.ID)));
				model.setKeyword(c.getString(c.getColumnIndex(SmsPeriodListen.KEYWORD)));
				model.setPeriod_type(c.getInt(c.getColumnIndex(SmsPeriodListen.PERIOD_TYPE)));
				model.setPeriod(c.getInt(c.getColumnIndex(SmsPeriodListen.PERIOD)));
				model.setListen_period(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_PERIOD)));
				model.setIs_delay_alarm(c.getInt(c.getColumnIndex(SmsPeriodListen.IS_DELAY_ALARM)));
				model.setDelay_period(c.getInt(c.getColumnIndex(SmsPeriodListen.DELAY_PERIOD)));
				model.setDelay_period_type(c.getInt(c.getColumnIndex(SmsPeriodListen.DELAY_PERIOD_TYPE)));
				model.setLast_rev_time(c.getLong(c.getColumnIndex(SmsPeriodListen.LAST_REV_TIME)));
				model.setIs_use(c.getInt(c.getColumnIndex(SmsPeriodListen.IS_USE)));
				model.setCreate_time(c.getLong(c.getColumnIndex(SmsPeriodListen.CREATE_TIME)));
				model.setNumber(c.getString(c.getColumnIndex(SmsPeriodListen.NUMBER)));
				model.setListen_time_day(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_DAY)));
				model.setListen_time_hour(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_HOUR)));
				model.setListen_time_minute(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_MINUTE)));
				model.setMissing_period(c.getInt(c.getColumnIndex(SmsPeriodListen.MISSING_PERIOD)));
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		
		return reList;
	}
}
