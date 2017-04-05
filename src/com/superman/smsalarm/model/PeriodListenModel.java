/**
 * 
 */
package com.superman.smsalarm.model;

/**
 * <p>Title: com.superman.smsalarm.model.PeriodListenModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version v2.4.6.8.8 CreateTime：2014-10-2 上午11:08:14
 */

public class PeriodListenModel {
	
	private long id;
	private String keyword;
	private String number;
	private int period_type;
	private int period;
	private int listen_period;
	private int is_delay_alarm;
	private int delay_period_type;
	private int delay_period;
	private long last_rev_time;
	private int is_use;
	private long create_time;
	private int listen_time_day;
	private int listen_time_hour;
	private int listen_time_minute;
	private int missing_period;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public int getPeriod_type() {
		return period_type;
	}
	public void setPeriod_type(int period_type) {
		this.period_type = period_type;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public int getListen_period() {
		return listen_period;
	}
	public void setListen_period(int listen_period) {
		this.listen_period = listen_period;
	}
	public int getIs_delay_alarm() {
		return is_delay_alarm;
	}
	public void setIs_delay_alarm(int is_delay_alarm) {
		this.is_delay_alarm = is_delay_alarm;
	}
	public int getDelay_period_type() {
		return delay_period_type;
	}
	public void setDelay_period_type(int delay_period_type) {
		this.delay_period_type = delay_period_type;
	}
	public int getDelay_period() {
		return delay_period;
	}
	public void setDelay_period(int delay_period) {
		this.delay_period = delay_period;
	}
	public long getLast_rev_time() {
		return last_rev_time;
	}
	public void setLast_rev_time(long last_rev_time) {
		this.last_rev_time = last_rev_time;
	}
	public int getIs_use() {
		return is_use;
	}
	public void setIs_use(int is_use) {
		this.is_use = is_use;
	}
	public long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}
	public int getListen_time_day() {
		return listen_time_day;
	}
	public void setListen_time_day(int listen_time_day) {
		this.listen_time_day = listen_time_day;
	}
	public int getListen_time_hour() {
		return listen_time_hour;
	}
	public void setListen_time_hour(int listen_time_hour) {
		this.listen_time_hour = listen_time_hour;
	}
	public int getListen_time_minute() {
		return listen_time_minute;
	}
	public void setListen_time_minute(int listen_time_minute) {
		this.listen_time_minute = listen_time_minute;
	}
	public int getMissing_period() {
		return missing_period;
	}
	public void setMissing_period(int missing_period) {
		this.missing_period = missing_period;
	}
}
