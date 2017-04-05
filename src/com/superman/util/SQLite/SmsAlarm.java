package com.superman.util.SQLite;

import android.provider.BaseColumns;

/* Inner class that defines the table contents */
public class SmsAlarm implements BaseColumns {
	public static final String TABLE_NAME = "sms_alarm";
	public static final String ID = "id";
	public static final String KEY_WORD = "keyword";
	public static final String PHONENUMBER = "phonenumber";
	public static final String CREATE_TIME = "create_time";//timemillis
	public static final String IS_USE = "is_use";//1启用，0停用
	public static final String IS_UPDATE = "is_update";//add since v2.4.6.4 是否有新拦截的消息还未被查看
	
	public static final String SQL_CREATE_SMS_ALARM =   
			"create table "+TABLE_NAME+"( "
			+ ID + " integer primary key, "
			+ KEY_WORD + " varchar(255), "
			+ PHONENUMBER + " varchar(255), "
			+ CREATE_TIME + " varchar(255), "
			 + IS_USE + " integer,"
			+ IS_UPDATE + " integer"
			+ ") ";

	public static final String SQL_DELETE_SMS_ALARM = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
}
