package com.superman.util.SQLite;

import android.provider.BaseColumns;

/* Inner class that defines the table contents */
public class SmsHistory implements BaseColumns {
	public static final String TABLE_NAME = "sms_history";
	public static final String ID = "id";
	public static final String SMS_TEXT = "sms_text";
	public static final String RECV_PHONENUMBER = "recv_phonenumber";
	public static final String RECEIVER_TIME = "receiver_time";
	public static final String HISTORY_TYPE = "history_type";
	public static final String ALARM_ID = "alarm_id";
	
	public static final String SQL_CREATE_SMS_HISTORY =   
			"create table "+TABLE_NAME+"( "
			+ ID + " integer primary key, "
			+ SMS_TEXT + " text, "
			+ RECV_PHONENUMBER + " varchar(255), "
			+ RECEIVER_TIME + " varchar(255), "
			+ HISTORY_TYPE + " integer, "
			+ ALARM_ID + " integer "
			+ ") ";

	public static final String SQL_DELETE_SMS_HISTORY = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
}
