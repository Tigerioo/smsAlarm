package com.superman.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.superman.util.SQLite.SmsAlarmDbHelper;

public class SmsAlarmDao {

	private static SmsAlarmDbHelper dbHelper;

	/**
	 * 获取数据库实例对象
	 * 
	 * @param context
	 * @return
	 */
	public static SQLiteDatabase getDbInstance(Context context) {
		if (dbHelper == null) {
			dbHelper = new SmsAlarmDbHelper(context);
		}
		return dbHelper.getReadableDatabase();
	}

	/**
	 * 获取当前时间的毫秒数
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * 是否包含指定服务
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isServiceExisted(Context context, String className) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		if (!(serviceList.size() > 0)) {
			return false;
		}

		for (int i = 0; i < serviceList.size(); i++) {
			RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;
			System.out.println(serviceName);
			if (serviceName.getClassName().contains(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * add since v2.3.4 get time format YYYY-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getCurrentTruncTime() {
		java.text.DateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	public static String getCurrentTruncTime(String type) {
		java.text.DateFormat format = new SimpleDateFormat(type);
		return format.format(new Date());
	}
	
	public static String getCurrentTruncDate() {
		java.text.DateFormat format = new SimpleDateFormat(
				"yyyy.MM.dd");
		return format.format(new Date());
	}
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * add since v2.4.4.9.1
	 * 
	 * @param hour
	 * @param min
	 * @return
	 */
	public static String truncSingleTime(int hour, int min) {
		String hour_str = String.valueOf(hour);
		if (hour >= 0 && hour < 10 && String.valueOf(hour).length() == 1) {
			hour_str = "0" + hour;
		}
		String min_str = String.valueOf(min);
		if (min >= 0 && min < 10 && String.valueOf(min).length() == 1) {
			min_str = "0" + min;
		}

		return hour_str + ":" + min_str;
	}
	
	public static String transSpecChar(String inStr){
		//先反转义一次，反之之前的内容已经转义过了
		inStr = inStr.replace("&amp;", "&")
					 .replace("&lt;", "<")
					 .replace("&gt;", ">")
					 .replace("&apos;", "'")
					 .replace("&quot;", "\"");
		//把特殊字符转义
		return inStr.replace("&", "&amp;")
					.replace("<", "&lt;")
					.replace(">", "&gt;")
					.replace("'", "&apos;")
					.replace("\"", "&quot;");
	}
	
	public static String findDefaultBackupPath(){
		// 判断SD卡是否存在，并且是否具有读写权限
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 获得存储卡的路径
			String sdpath = Environment.getExternalStorageDirectory()
					+ "/";
			return sdpath + ConstantUtil.BACKUP_PATH;
		}
		return null;
	}
	
	/**
	 * add since v2.4.6.2
	 * find log path
	 * @return
	 */
	public static String findDefaultLogPath(){
		// 判断SD卡是否存在，并且是否具有读写权限
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 获得存储卡的路径
			String sdpath = Environment.getExternalStorageDirectory()
					+ "/";
			return sdpath + ConstantUtil.LOG_PATH;
		}
		return null;
	}
	
	/**
	 * 把毫秒转成格式： 05-21 21:15
	 * @param millis
	 * @return
	 */
	public static String truncTimeByMillis(long millis){
		DateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
		Date date = new Date(millis);
		Calendar cal = Calendar.getInstance();//历史最新更新时间
		int current_year = cal.get(Calendar.YEAR);
		cal.setTime(date);
		int last_year = cal.get(Calendar.YEAR);
		if(current_year != last_year){
			return last_year + "年";
		}
		return format.format(new Date(millis));
		
	}
}
