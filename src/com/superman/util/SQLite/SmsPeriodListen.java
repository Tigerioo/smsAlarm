/**
 * 
 */
package com.superman.util.SQLite;

/**
 * <p>Title: com.superman.util.SQLite.SmsPeriodListen.java</p>
 *
 * <p>Description: add since v2.4.6.8.8</p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-10-2 上午10:48:22
 */

public class SmsPeriodListen {
	
	public static final String TABLE_NAME = "sms_periodlisten";//表名
	public static final String ID = "id";//序列号
	public static final String KEYWORD = "keyword";//监听的关键字
	public static final String NUMBER = "number";//电话号码
	public static final String PERIOD_TYPE = "period_type";//0:minute, 1:hour, 2:day, 3:month
	public static final String PERIOD = "period";//周期， 数值型, 表示几个周期， 单位是 PERIOD_TYPE 字段
	public static final String LISTEN_TIME_DAY = "listen_time_day";//监听基准时间(从该时间开始周期计算), 天 -- 给周期为月的监听使用
	public static final String LISTEN_TIME_HOUR = "listen_time_hour";//监听基准时间(从该时间开始周期计算), 小时 -- 给周期为 月,天 的监听使用
	public static final String LISTEN_TIME_MINUTE = "listen_time_minute";//监听基准时间(从该时间开始周期计算), 分钟 -- 给周期为 月,天,小时 的监听使用
	public static final String LISTEN_PERIOD = "listen_period";//监听周期，几个周期未收到后响铃
	public static final String IS_DELAY_ALARM = "is_delay_alarm";//是否延迟响铃
	public static final String DELAY_PERIOD_TYPE = "delay_period_type";//0:minute, 1:hour, 2:day, 3:month
	public static final String DELAY_PERIOD = "delay_period";//延迟周期, 数值型
	public static final String LAST_REV_TIME = "last_rev_time";//上次接收时间
	public static final String IS_USE = "is_use";//是否启用
	public static final String CREATE_TIME = "create_time";//创建时间
	public static final String MISSING_PERIOD = "missing_period";//已经缺失的周期
	
	public static final String SQL_CREATE_SMS_PERIOD_LISTEN = 
							"create table " + TABLE_NAME + "("
							 + ID + " integer primary key, "
							 + KEYWORD + " text, " 
							 + NUMBER + " text, "
							 + PERIOD_TYPE + " integer, "
							 + PERIOD + " integer, "
							 + LISTEN_TIME_DAY + " integer, "
							 + LISTEN_TIME_HOUR + " integer, "
							 + LISTEN_TIME_MINUTE + " integer, "
							 + LISTEN_PERIOD + " integer, "
							 + IS_DELAY_ALARM + " integer, "
							 + DELAY_PERIOD_TYPE + " integer, "
							 + DELAY_PERIOD + " integer, "
							 + LAST_REV_TIME + " integer, "
							 + IS_USE + " integer, "
							 + CREATE_TIME + " integer, "
							 + MISSING_PERIOD + " integer "
							 + ")";
				
	public static final String SQL_DELETE_SMS_PERIOD_LISTEN = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
}
