/**
 * 
 */
package com.superman.util;

/**
 * <p>Title: com.superman.util.ConstantUtil.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-30 下午7:39:38
 */

public class ConstantUtil {
	
	public static final int SMS_CLOCK = 0;
	public static final int SMS_INTERCEPT = 1; 
	public static final String HOME_PATH = "SmsAlarm/";
	public static final String BACKUP_PATH = HOME_PATH + "backup/";
	public static final String LOG_PATH = HOME_PATH + "log/"; 
	public static final String BACKUP_CLOCK_FILE_NAME = "clock.xml";
	public static final String BACKUP_INTERCEPT_FILE_NAME = "intercept.xml";
	public static final String BACKUP_HISTORY_FILE_NAME = "history.xml";
	public static final String BACKUP_SETTING_FILE_NAME = "setting.xml";//add since v2.4.6.8.8
	public static final String BACKUP_PERIODLISTEN_FILE_NAME = "periodlisten.xml";//add since v2.4.6.8.8
	
	public static final String UPGRADE_BACKUP_CLOCK_FILE_NAME = "upgrade_clock.xml";
	public static final String UPGRADE_BACKUP_INTERCEPT_FILE_NAME = "upgrade_intercept.xml";
	public static final String UPGRADE_BACKUP_HISTORY_FILE_NAME = "upgrade_history.xml";
	public static final String UPGRADE_BACKUP_SETTING_FILE_NAME = "upgrade_setting.xml";//add since v2.4.6.8.8
	public static final String UPGRADE_BACKUP_PERIODLISTEN_FILE_NAME = "upgrade_periodlisten.xml";//add since v2.4.6.8.8
	
	public static final String IS_BACKUP_FINISH = "is_backup_finish";
	public static final String IS_RESTORE_FINISH = "is_restore_finish";
	public static final String CLOCK_COUNT = "clock_count";
	public static final String INTERCEPT_COUNT = "intercept_count";
	public static final String HISTORY_COUNT = "history_count";
	public static final String PERIODLISTEN_COUNT = "periodlisten_count";//add since v2.4.6.8.8
	
	public static final String FRAGMENT_POSITION = "position";
	
	public static final String ALARM_ARRAY = "alarm_array";
	
	public static final String ALL_NUMBER = "**所有号码**";//过滤关键字，不过滤号码
	public static final String ALL_KEYWORD = "**不过滤关键字**"; //过滤号码不过滤关键字
}
