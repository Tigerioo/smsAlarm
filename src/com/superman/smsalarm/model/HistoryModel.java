/**
 * 
 */
package com.superman.smsalarm.model;

import com.superman.util.SQLite.SmsHistory;

/**
 * <p>Title: com.superman.smsalarm.model.HistoryModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-27 上午7:36:33
 */

public class HistoryModel {
	
	private int id;
	private String recv_phonenumber;
	private String sms_text;
	private String receiver_time;
	private int history_type;
	private int alarm_id;
	private boolean isSelected;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRecv_phonenumber() {
		return recv_phonenumber;
	}
	public void setRecv_phonenumber(String recv_phonenumber) {
		this.recv_phonenumber = recv_phonenumber;
	}
	public String getSms_text() {
		return sms_text;
	}
	public void setSms_text(String sms_text) {
		this.sms_text = sms_text;
	}
	public String getReceiver_time() {
		return receiver_time;
	}
	public void setReceiver_time(String receiver_time) {
		this.receiver_time = receiver_time;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public int getHistory_type() {
		return history_type;
	}
	public void setHistory_type(int history_type) {
		this.history_type = history_type;
	}
	public int getAlarm_id() {
		return alarm_id;
	}
	public void setAlarm_id(int alarm_id) {
		this.alarm_id = alarm_id;
	}
}
