package com.superman.util.SQLite;

import android.provider.BaseColumns;

/**
 * 
 * <p>Title: com.superman.util.SQLite.SmsIntercept.java</p>
 *
 * <p>Description: Sms intercept SQL</p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-6 下午10:43:20
 */
public class SmsIntercept implements BaseColumns {
	public static final String TABLE_NAME = "sms_intercept";
	public static final String ID = "id";
	public static final String KEY_WORD = "keyword";
	public static final String PHONENUMBER = "phonenumber";
	public static final String CREATE_TIME = "create_time";
	public static final String IS_USE = "is_use";//1启用，0停用
	public static final String IS_UPDATE = "is_update";//add since v2.4.6.4 是否有新拦截的消息还未被查看
	public static final String IS_RING = "is_ring";//add since v2.4.6.8.11 是否在Notification中响铃, 1启用， 0停用
	
	public static final String SQL_CREATE_SMS_INTERCEPT =   
			"create table "+TABLE_NAME+"( "
			+ ID + " integer primary key, "
			+ KEY_WORD + " varchar(255), "
			+ PHONENUMBER + " varchar(255), "
			+ CREATE_TIME + " varchar(255), "
			 + IS_USE + " integer,"
			+ IS_UPDATE + " integer, "
			+ IS_RING + " integer"
			+ ") ";

	public static final String SQL_DELETE_SMS_INTERCEPT = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
}
