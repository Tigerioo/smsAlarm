/**
 * 
 */
package com.superman.smsalarm.model;

/**
 * <p>Title: com.superman.smsalarm.model.AlarmModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-28 下午2:16:07
 */

public class SmsAlarmModel {
	
	private String id;
	private String phone_number;
	private String key_word;
	private boolean is_use;
	private String create_time;//毫秒数
	private int alarm_type;
	private boolean is_update;
	private int is_ring;//add since v2.4.6.8.11 拦截专用
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	public String getKey_word() {
		return key_word;
	}
	public void setKey_word(String key_word) {
		this.key_word = key_word;
	}
	public boolean isIs_use() {
		return is_use;
	}
	public void setIs_use(boolean is_use) {
		this.is_use = is_use;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public int getAlarm_type() {
		return alarm_type;
	}
	public void setAlarm_type(int alarm_type) {
		this.alarm_type = alarm_type;
	}
	public boolean isIs_update() {
		return is_update;
	}
	public void setIs_update(boolean is_update) {
		this.is_update = is_update;
	}
	public int getIs_ring() {
		return is_ring;
	}
	public void setIs_ring(int is_ring) {
		this.is_ring = is_ring;
	}
}
