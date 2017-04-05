package com.superman.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.superman.smsalarm.HistoryActivity;
import com.superman.smsalarm.MediaActivity;
import com.superman.smsalarm.R;
import com.superman.smsalarm.model.PeriodListenModel;
import com.superman.util.ConstantUtil;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsHistory;
import com.superman.util.SQLite.SmsIntercept;
import com.superman.util.SQLite.SmsPeriodListen;

public class SmsService extends Service{

	private SQLiteDatabase db;
	private SMSreceiver receiver;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		db = SmsAlarmDao.getDbInstance(this);
		/*
		 * 动态注册
		 */
		receiver = new SMSreceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(Integer.MAX_VALUE);
		registerReceiver(receiver, filter);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private class SMSreceiver extends BroadcastReceiver {
		private static final String mACTION = "android.provider.Telephony.SMS_RECEIVED";
		
		/**
		 * 判断该短信是否包含关键字
		 * @param list_seq
		 * @param sms_text
		 * @return
		 */
		private String[] checkKeyword(String sms_text, String phone_number){
			Log.i("TAG", sms_text);
			Cursor c = null;
			String[] arrString = new String[2];
			try {
				c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.ID, SmsAlarm.PHONENUMBER, SmsAlarm.KEY_WORD}, SmsAlarm.IS_USE+"=1", null, null, null, SmsAlarm.ID + " asc");
				while(c.moveToNext()){
					String id = c.getString(c.getColumnIndex(SmsAlarm.ID));
					String key_word = c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD));
					String localPhoneNum = c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER));
					if(sms_text.trim().contains(key_word) && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))) {
						arrString[0] = id;
						arrString[1] = key_word;
						return arrString;
					}
					key_word = key_word.replace("，", ",");
					if(key_word.contains(",")){//包含逗号，说明是多个关键字
						String[] keys = key_word.split(",");
						boolean isContain = true;
						for (int i = 0; i < keys.length; i++) {
							if(!sms_text.contains(keys[i])) isContain = false;//有一个不包含则都不包含
						}
						if(isContain && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))){//符合多个关键字条件
							arrString[0] = id;
							arrString[1] = key_word;
							return arrString;
						}
					}
					
					if(ConstantUtil.ALL_KEYWORD.equals(key_word) && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))) {
						arrString[0] = id;
						arrString[1] = key_word;
						return arrString;
					}
				}
			} catch (Exception e) {
				LogUtil.saveLog(e.toString());
				LogUtil.saveLog(e.toString());
			} finally {
				if(c != null){
					c.close();
				}
			}
			return arrString;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("SMS", "Receive----------------------------------");
//			LogUtil.saveLog("Receive----------------------------------");
			if (intent.getAction().equals(mACTION)) {
				
				StringBuilder sb = new StringBuilder();
				Bundle bundle = intent.getExtras();

				boolean isAlarm = false;
				String sms_text = "";
				String phone_number = "";
				String reTime = SmsAlarmDao.getCurrentTruncTime();
				if (bundle != null) {
					Object[] myOBJpdus = (Object[]) bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[myOBJpdus.length];
					for (int i = 0; i < myOBJpdus.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) myOBJpdus[i]);
					}
					
					for (SmsMessage currentMessage : messages) {
						// sb.append("Phone Number:\n");
						phone_number = currentMessage.getDisplayOriginatingAddress();
//						sb.append(currentMessage.getDisplayOriginatingAddress());
//						sb.append(":");
						sb.append(currentMessage.getDisplayMessageBody());
					}
					
					sms_text = sb.toString();
					
					//更新周期监听表时间
					updatePeriodListen(sms_text, phone_number);
					
//					LogUtil.saveLog("sms_text=" + sms_text + ",phone_number=" + phone_number);
					/*
					 * 判断是否有匹配的关键字
					 */
					String[] clockArray = checkKeyword(sms_text, phone_number);
					if (clockArray[0] != null && clockArray[1] != null) {
						isAlarm = true;
						String clock_id = clockArray[0];
//						String key_word = clockArray[1];
						
						//save to history
						ContentValues content = new ContentValues();
						content.put(SmsHistory.SMS_TEXT, sms_text);
						content.put(SmsHistory.RECV_PHONENUMBER, phone_number);
						content.put(SmsHistory.RECEIVER_TIME, reTime);
						content.put(SmsHistory.HISTORY_TYPE, 0);
						content.put(SmsHistory.ALARM_ID, clock_id);
						db.insert(SmsHistory.TABLE_NAME, null, content);
						
						ContentValues content2 = new ContentValues();
						content2.put(SmsAlarm.CREATE_TIME, System.currentTimeMillis());
						content2.put(SmsAlarm.IS_UPDATE, 1);
						db.update(SmsAlarm.TABLE_NAME, content2, SmsAlarm.ID+"=?", new String[]{clock_id});
					}
					String[] interceptArr = checkIntercepter(sms_text, phone_number);
					
					if(interceptArr[0] != null && interceptArr[1] != null){//isIntercepter
						String intercept_id = interceptArr[0];
						String keyWord = interceptArr[1];
						//save to history
						ContentValues content = new ContentValues();
						content.put(SmsHistory.SMS_TEXT, sms_text);
						content.put(SmsHistory.RECV_PHONENUMBER, phone_number);
						content.put(SmsHistory.RECEIVER_TIME, reTime);
						content.put(SmsHistory.HISTORY_TYPE, 1);
						content.put(SmsHistory.ALARM_ID, intercept_id);
						db.insert(SmsHistory.TABLE_NAME, null, content);
						
						ContentValues content2 = new ContentValues();
						content2.put(SmsIntercept.CREATE_TIME, System.currentTimeMillis());
						content2.put(SmsIntercept.IS_UPDATE, 1);
						db.update(SmsIntercept.TABLE_NAME, content2, SmsIntercept.ID+"=?", new String[]{intercept_id});
						Toast.makeText(context, "【拦截】" + sms_text, Toast.LENGTH_LONG).show();
						int notify_id = 001;
				    	Intent intent_show = new Intent();
				    	Bundle his_bundle = new Bundle();
				    	his_bundle.putInt(SmsHistory.HISTORY_TYPE, 1);
				    	his_bundle.putString(SmsHistory.ALARM_ID, intercept_id);
				    	his_bundle.putStringArray(ConstantUtil.ALARM_ARRAY, new String[]{intercept_id});
		    			intent_show.putExtras(his_bundle);
		    			intent_show.setClass(SmsService.this, HistoryActivity.class);
						PendingIntent pi = PendingIntent.getActivity(SmsService.this, 0, intent_show, PendingIntent.FLAG_UPDATE_CURRENT);
						
						String tip = "";
						if(keyWord.equals(ConstantUtil.ALL_KEYWORD)){//针对号码拦截所有关键字
							tip = "拦截到号码【"+phone_number+"】,已拦截" + getCurrentInterceptCount(intercept_id) + "条!";
						}else {
							tip = "拦截到【"+keyWord+"】,已拦截" + getCurrentInterceptCount(intercept_id) + "条!";
						}
						
				    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SmsService.this)
				    		    .setSmallIcon(R.drawable.ic_launcher)
				    		    .setContentTitle("已为您拦截" + getTotalIntercept()	+ "条短信")
				    		    .setAutoCancel(true)
				    		    .setContentText(tip)
				    		    .setTicker(sms_text);
				    	mBuilder.setContentIntent(pi);
				    	
				    	NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				    	notificationManager.notify(notify_id, mBuilder.build());
						this.abortBroadcast();//abort sms Broadcast
					}
				}

				if (isAlarm) {
					Toast.makeText(context, sms_text, Toast.LENGTH_LONG).show();
					Intent i = new Intent(context, MediaActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra(SmsAlarm.KEY_WORD, sms_text);
					context.startActivity(i);
				}
				
				//发送界面刷新广播
		    	Intent fresh_intent = new Intent("com.superman.smsalarm.refresh");
		    	sendBroadcast(fresh_intent);
			}
		}
	}

	/**
	 * 更新SmsPeriodListen的上次接收时间, 并且对监听时间未设置的的进行初次设置
	 * @param keyword
	 * @param number
	 */
	private void updatePeriodListen(String sms_text, String number){
		Cursor c = null;
		c = db.query(SmsPeriodListen.TABLE_NAME, new String[]{SmsPeriodListen.ID, SmsPeriodListen.KEYWORD, SmsPeriodListen.NUMBER, SmsPeriodListen.PERIOD_TYPE, SmsPeriodListen.LISTEN_TIME_MINUTE},
				SmsPeriodListen.IS_USE + "=1", null, null, null, null);
		while(c.moveToNext()){
			long id = c.getLong(c.getColumnIndex(SmsPeriodListen.ID));
			int periodType = c.getInt(c.getColumnIndex(SmsPeriodListen.PERIOD_TYPE));
			int listen_time_minute = c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_MINUTE));
			
			String _keyword = c.getString(c.getColumnIndex(SmsPeriodListen.KEYWORD));
			String _number = c.getString(c.getColumnIndex(SmsPeriodListen.NUMBER));
			if(_number.length() > 0){//有配置号码，需要验证号码
				if(!number.equals(_number)) continue; //号码不匹配，直接跳过
			}
			if(!sms_text.contains(_keyword)) continue;//如果短信内容不包含指定的关键字，则跳过
			
			ContentValues update = new ContentValues();
			if(listen_time_minute == -1){//还未设置监听时间，根据当前时间进行设置
				Calendar cal = Calendar.getInstance();
				int day = periodType == 3 ? cal.get(Calendar.DAY_OF_MONTH) : -1;//周期为月的才需要设置天
				int hour = (periodType == 2 || periodType == 3) ? cal.get(Calendar.HOUR_OF_DAY) : -1;//周期为天和月才需要设置小时
				int minute = cal.get(Calendar.MINUTE);
				update.put(SmsPeriodListen.LISTEN_TIME_DAY, day);
				update.put(SmsPeriodListen.LISTEN_TIME_HOUR, hour);
				update.put(SmsPeriodListen.LISTEN_TIME_MINUTE, minute);
			}
			update.put(SmsPeriodListen.LAST_REV_TIME, System.currentTimeMillis());//设置最新的短信接收时间为当前时间
			db.update(SmsPeriodListen.TABLE_NAME, update, SmsPeriodListen.ID + "=?", new String[]{String.valueOf(id)});
		}
	}
	
	/**
	 * get current ketword's intercept count
	 * @return
	 */
	private int getCurrentInterceptCount(String id){
		Cursor c = null;
		int count = 0;
		try {
			c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID}, SmsHistory.HISTORY_TYPE + "=1 and " + SmsHistory.ALARM_ID + "=?", new String[]{id}, null, null, null);
			while(c.moveToNext()) {
				count ++;
			}
		} catch (Exception e) {
			Log.e("com.superman.common.SmsService.getCurrentInterceptCount(String)", e.toString());
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		return count;
	}
	
	private int getTotalIntercept(){
		Cursor c = null;
		try {
			c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID}, SmsHistory.HISTORY_TYPE+"=1", null, null, null, null);
			return c.getCount();
		} catch (Exception e) {
			Log.e("SmsService", e.toString());
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null) c.close();
		}
		return 0;
	}
	
	/**
	 * the keyword isIntercepter 
	 * @param keyword
	 * @return
	 */
	private String[] checkIntercepter(String sms_text, String phone_number){
		Log.i("interceper", "isIntercepter");
		Cursor c = null;
		String[] reArr = new String[2];
		try {
			c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsIntercept.ID, SmsIntercept.PHONENUMBER, SmsIntercept.KEY_WORD}, SmsIntercept.IS_USE + "=1", null, null, null, SmsIntercept.ID + " asc");
			while(c.moveToNext()){
				String id = c.getString(c.getColumnIndex(SmsIntercept.ID));
				String key_word = c.getString(c.getColumnIndex(SmsIntercept.KEY_WORD));
				String localPhoneNum = c.getString(c.getColumnIndex(SmsIntercept.PHONENUMBER));
				if(sms_text.trim().contains(key_word) && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))) {
					reArr[0] = id;
					reArr[1] = key_word;
					return reArr;
				}
				
				key_word = key_word.replace("，", ",");
				if(key_word.contains(",")){//包含逗号，说明是多个关键字
					String[] keys = key_word.split(",");
					boolean isContain = true;
					for (int i = 0; i < keys.length; i++) {
						if(!sms_text.contains(keys[i])) isContain = false;//有一个不包含则都不包含
					}
					if(isContain && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))){//符合多个关键字条件
						reArr[0] = id;
						reArr[1] = key_word;
						return reArr;
					}
				}
				
				if(ConstantUtil.ALL_KEYWORD.contains(key_word) && (phone_number.equals(localPhoneNum) || localPhoneNum.equals(ConstantUtil.ALL_NUMBER))) {//add since v2.4.6.8.5
					reArr[0] = id;
					reArr[1] = key_word;
					return reArr;
				}
			}
		} catch (Exception e) {
			Log.e("isIntercepter", e.toString());
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		return reArr;
	}
}
