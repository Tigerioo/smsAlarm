/**
 * 
 */
package com.superman.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Notification;
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
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.superman.smsalarm.MainActivity;
import com.superman.smsalarm.MediaActivity;
import com.superman.smsalarm.R;
import com.superman.smsalarm.model.PeriodListenModel;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsPeriodListen;

/**
 * <p>Title: com.superman.common.TimeTickService.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version v2.4.6.8.9 CreateTime：2014-10-2 下午11:56:30
 */

public class TimeTickService extends Service{
	
	private SQLiteDatabase db;
	private TimeTickreceiver receiver;
	private List<PeriodListenModel> mData;

	@Override
	public void onCreate() {
		super.onCreate();
		db = SmsAlarmDao.getDbInstance(TimeTickService.this);
		/*
		 * 动态注册
		 */
		receiver = new TimeTickreceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
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

	private class TimeTickreceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.i("SMS", "Receive----------------------------------");
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
//				Toast.makeText(context, new Date().toString(), Toast.LENGTH_LONG).show();
				mData = getData();//初始化数据
				Calendar cal = Calendar.getInstance();//日历类
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);//今天几号
				int hour = cal.get(Calendar.HOUR_OF_DAY);//几点
				int minute = cal.get(Calendar.MINUTE);//几分
				
				//开始进行周期判断
				for (PeriodListenModel model : mData) {
					//对于从未接收的，直接跳过
//					if(model.getLast_rev_time() == 0) continue;
					//如果是listenTimeMinute-1，则说明配置的是下次短信接收到的时间为准，说明还未接收到该短信，未重置周期开始时间
					if(model.getListen_time_minute() == -1) continue;
					
					//这里已经过滤掉了不符合条件的数据，开始正式进行判断
					long currentMillis = System.currentTimeMillis();//当前时间
					long lastMillis = model.getLast_rev_time();//最后更新时间
					
					//预计监听的时间
					int listenTimeDay = model.getListen_time_day() == -1 ? day : model.getListen_time_day();//如果是-1则是未配置，用今天的时间
					int listenTimeHour = model.getListen_time_hour() == -1 ? hour : model.getListen_time_hour();//如果是-1则是未配置，用今天的时间
					int listenTimeMinute = model.getListen_time_minute();
					
					//针对30分钟周期的情况，如果是前30分钟，则到下个30分钟的周期，加上30分钟，如果不是则不变
					if(model.getPeriod_type() == 0 && listenTimeMinute < 30 && minute > 30){
						listenTimeMinute += 30;
					}else if(model.getPeriod_type() == 0 && listenTimeMinute > 30 && minute < 30){
						listenTimeMinute -= 30;
					}
					//设置成未延时的时候监听的时间
					cal.set(Calendar.DAY_OF_MONTH, listenTimeDay);
					cal.set(Calendar.HOUR_OF_DAY, listenTimeHour);
					cal.set(Calendar.MINUTE, listenTimeMinute);
					
					//如果listenTimeMinute刚好是等于30，并且周期是分钟，则到下个小时的0分
					if(model.getPeriod_type() == 0 && listenTimeMinute == 30 && minute < 30){
						cal.add(Calendar.HOUR_OF_DAY, 1);//加一个小时
						cal.set(Calendar.MINUTE, 0);//设置成0分钟
					}
					
					//针对延迟进行计算，看看是否符合监听时间, 监听的时间加上延迟的时间 是否等于现在的时间
					//默认加1分钟，从下个分钟的开始，及上个分钟的最后开始监控
					int delayMinute = model.getIs_delay_alarm() == 0 ? 1 : getDelayMinute(model.getDelay_period(), model.getDelay_period_type()) + 1;//获取延迟分钟数
					
					cal.add(Calendar.MINUTE, delayMinute);//加上延迟的分钟数
					
					//获取加上延迟时间后的时间，和现在时间进行比对，如果符合，进行监控, 因为加上延迟的时间之后，可能会改变月份或者年份，所以要加上year和month
					int delay_year = cal.get(Calendar.YEAR);//延迟后的时间
					int delay_month = cal.get(Calendar.MONTH);//延迟后的时间
					int delay_day = cal.get(Calendar.DAY_OF_MONTH);//延迟后的时间
					int delay_hour = cal.get(Calendar.HOUR_OF_DAY);//延迟后的时间
					int delay_minute = cal.get(Calendar.MINUTE);//延迟后的时间
					
					//符合当前时间，则开始进行判断是否响铃
					if(year == delay_year && month == delay_month && day == delay_day
							&& hour == delay_hour && minute == delay_minute){
						
						//如果从来没收到过短信，说明延迟了，直接告警
						if(lastMillis == 0){
							alarm(context, model);
							continue;
						}
						
						long invalMillis = currentMillis - lastMillis;//距离上次收到短信间隔多少时间
						//上次响铃时间
						
						if(invalMillis > delayMinute * 60 * 1000) {//超过指定延迟的时间，开始响铃
							alarm(context, model);
						}else {
							updateMissingPeriod(0, model.getId());//重置缺失周期数
							showNotification();
						}
					}
					
				}
				
			}
		}
	}
	
	/**
	 * 判断响铃
	 * @param context
	 * @param model
	 */
	private void alarm(Context context, PeriodListenModel model){
		//缺失几个周期后响铃
		int listen_period = model.getListen_period();
		//已经缺失了几个周期
		int missing_period = model.getMissing_period();
		missing_period ++;
		updateMissingPeriod(missing_period, model.getId());//更新缺失周期
		if(missing_period >= listen_period){//如果缺失周期数等于设置的监听缺失周期数,则响铃
			showMedia(context, model.getKeyword(), missing_period);
		}
	}
	
	/**
	 * 更新已经缺失数
	 * @param missing_period
	 */
	private void updateMissingPeriod(int missing_period, long id){
		ContentValues update = new ContentValues();
		update.put(SmsPeriodListen.MISSING_PERIOD, missing_period);
		db.update(SmsPeriodListen.TABLE_NAME, update, SmsPeriodListen.ID + "=?", new String[]{String.valueOf(id)});
	}
	
	/**
	 * 跳转到闹铃页面
	 * @param context
	 * @param keyWord
	 */
	private void showMedia(Context context, String keyWord, int missing_period){
		Intent i = new Intent(context, MediaActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(SmsAlarm.KEY_WORD, "【" + keyWord + "】 超过 " + missing_period + " 个周期未收到短信！！！");
		context.startActivity(i);
	}
	
	private void showNotification(){
		Intent intent_show = new Intent();
		intent_show.setClass(TimeTickService.this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(TimeTickService.this, 0, intent_show, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				TimeTickService.this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(new Date().toString())
				.setAutoCancel(true).setContentText("系统时间变更setContentText")
				.setTicker("系统时间变更setTicker");
//				.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setContentIntent(pi);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(1, mBuilder.build());
	}
	
	/**
	 * 延迟的分钟数
	 * @param delayPeriod
	 * @param delayPeriodType
	 * @return
	 */
	private int getDelayMinute(int delayPeriod, int delayPeriodType){
		switch (delayPeriodType) {
		case 0://分钟
			return delayPeriod;
		case 1://小时
			return delayPeriod * 60;
		default://天
			return delayPeriod * 24 * 60;
		}
	}
	
	private List<PeriodListenModel> getData(){
		List<PeriodListenModel> reList = new ArrayList<PeriodListenModel>();
		Cursor c = null; 
		try {
			c = db.query(SmsPeriodListen.TABLE_NAME, new String[]{SmsPeriodListen.ID, SmsPeriodListen.KEYWORD,
					SmsPeriodListen.PERIOD_TYPE, SmsPeriodListen.PERIOD, SmsPeriodListen.LISTEN_PERIOD,
					SmsPeriodListen.IS_DELAY_ALARM, SmsPeriodListen.DELAY_PERIOD_TYPE, 
					SmsPeriodListen.DELAY_PERIOD, SmsPeriodListen.LAST_REV_TIME, 
					SmsPeriodListen.IS_USE, SmsPeriodListen.CREATE_TIME, SmsPeriodListen.NUMBER,
					SmsPeriodListen.LISTEN_TIME_DAY, SmsPeriodListen.LISTEN_TIME_HOUR, SmsPeriodListen.LISTEN_TIME_MINUTE, SmsPeriodListen.MISSING_PERIOD},
					SmsPeriodListen.IS_USE + "=1", null, null, null, null);
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
