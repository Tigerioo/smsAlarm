package com.superman.util.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsAlarmDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 34;
    public static final String DATABASE_NAME = "SmsAlarm.db";

    public SmsAlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SmsAlarm.SQL_CREATE_SMS_ALARM);
        db.execSQL(SmsIntercept.SQL_CREATE_SMS_INTERCEPT);
        db.execSQL(SmsHistory.SQL_CREATE_SMS_HISTORY);//add since v2.3.5
        db.execSQL(SmsSetting.SQL_CREATE_SMS_SETTING);//add since v2.3.8 2014-03-19 16:32:05
        db.execSQL(SmsPeriodListen.SQL_CREATE_SMS_PERIOD_LISTEN);//add since v2.4.6.8.8
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SmsAlarm.SQL_DELETE_SMS_ALARM);
        db.execSQL(SmsIntercept.SQL_DELETE_SMS_INTERCEPT);
        db.execSQL(SmsHistory.SQL_DELETE_SMS_HISTORY);//add since v2.3.5
        db.execSQL(SmsSetting.SQL_DELETE_SMS_SETTING);//add since v2.3.8 2014-03-19 16:32:05
        db.execSQL(SmsPeriodListen.SQL_DELETE_SMS_PERIOD_LISTEN);//add since v2.4.6.8.8
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
	@Override
	public synchronized void close() {
		super.close();
	}
    
}
