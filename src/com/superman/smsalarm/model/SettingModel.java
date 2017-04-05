/**
 * 
 */
package com.superman.smsalarm.model;

/**
 * <p>Title: com.superman.smsalarm.model.SettingModel.java</p>
 *
 * <p>Description: 设置表Model， add since v2.4.6.8.8</p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-10-2 上午9:09:29
 */

public class SettingModel {
	
	private long id;
	private int is_oclock;
	private int is_disturb;
	private String disturb_date;
	private String disturb_begin_interval;
	private String disturb_end_interval;
	private String last_backup_time;
	private int is_update;
	private String ring_path;
	private String ring_name;
	private int status;
	private long auto_refresh_time;
	private int is_period_listen;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getIs_oclock() {
		return is_oclock;
	}
	public void setIs_oclock(int is_oclock) {
		this.is_oclock = is_oclock;
	}
	public int getIs_disturb() {
		return is_disturb;
	}
	public void setIs_disturb(int is_disturb) {
		this.is_disturb = is_disturb;
	}
	public String getDisturb_date() {
		return disturb_date;
	}
	public void setDisturb_date(String disturb_date) {
		this.disturb_date = disturb_date;
	}
	public String getDisturb_begin_interval() {
		return disturb_begin_interval;
	}
	public void setDisturb_begin_interval(String disturb_begin_interval) {
		this.disturb_begin_interval = disturb_begin_interval;
	}
	public String getDisturb_end_interval() {
		return disturb_end_interval;
	}
	public void setDisturb_end_interval(String disturb_end_interval) {
		this.disturb_end_interval = disturb_end_interval;
	}
	public String getLast_backup_time() {
		return last_backup_time;
	}
	public void setLast_backup_time(String last_backup_time) {
		this.last_backup_time = last_backup_time;
	}
	public int getIs_update() {
		return is_update;
	}
	public void setIs_update(int is_update) {
		this.is_update = is_update;
	}
	public String getRing_path() {
		return ring_path;
	}
	public void setRing_path(String ring_path) {
		this.ring_path = ring_path;
	}
	public String getRing_name() {
		return ring_name;
	}
	public void setRing_name(String ring_name) {
		this.ring_name = ring_name;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getAuto_refresh_time() {
		return auto_refresh_time;
	}
	public void setAuto_refresh_time(long auto_refresh_time) {
		this.auto_refresh_time = auto_refresh_time;
	}
	public int getIs_period_listen() {
		return is_period_listen;
	}
	public void setIs_period_listen(int is_period_listen) {
		this.is_period_listen = is_period_listen;
	}
	
}
