/**
 * 
 */
package com.superman.util.SQLite;

import android.provider.BaseColumns;

/**
 * <p>Title: com.superman.util.SQLite.SmsSetting.java</p>
 *
 * <p>Description: 
 * create table sms_setting (
isOclock int--0: 停用 1：启用
isIntecep int ---0: 停用 1：启用
disturb_interval varchar2(255)
last_backup_time varchar2(255)
is_update int --- 0: no 1:yes
)
 * </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-19 下午4:14:29
 */

public class SmsSetting implements BaseColumns{
	public static final String ID = "id";
	public static final String TABLE_NAME = "sms_setting";
	public static final String IS_OCLOCK = "is_oclock";
	public static final String IS_INTERCEPT = "is_intercept";
	public static final String IS_DISTURB = "is_disturb";
	public static final String DISTURB_DATE = "disturb_date";
	public static final String DISTURB_BEGIN_INTERVAL = "disturb_begin_interval";
	public static final String DISTURB_END_INTERVAL = "disturb_end_interval";
	public static final String LAST_BACKUP_TIME = "last_backup_time";
	public static final String IS_UPDATE = "is_update";//1启用，0停用
	public static final String RING_PATH = "ring_path";
	public static final String RING_NAME = "ring_name";//add since v2.4.1
	public static final String STATUS = "ring_play_status";
	public static final String AUTO_REFRESH_TIME = "auto_refresh_time";//add since v2.4.5.2 save millisecond
	public static final String IS_PERIOD_LISTEN = "is_period_listen";//add since v2.4.6.8.8 是否启用周期短信监听  0: 停用， 1：启用
	
	public static final String SQL_CREATE_SMS_SETTING =   
			"create table "+TABLE_NAME+"( "
			+ ID + " integer, "
			+ IS_OCLOCK + " integer, "
			+ IS_INTERCEPT + " integer, "
			+ IS_DISTURB + " integer, "
			+ DISTURB_DATE + " varchar(255), "
			+ DISTURB_BEGIN_INTERVAL + " varchar(255), "
			+ DISTURB_END_INTERVAL + " varchar(255), "
			+ LAST_BACKUP_TIME + " varchar(255), "
			+ IS_UPDATE + " integer, "
			+ RING_PATH + " varchar(255), "
			+ RING_NAME + " varchar(255), "
			+ STATUS + " integer, "
			+ AUTO_REFRESH_TIME + " integer, "
			+ IS_PERIOD_LISTEN + " integer"
			+ ") ";

	public static final String SQL_DELETE_SMS_SETTING = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
}
