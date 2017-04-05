/**
 * 
 */
package com.superman.smsalarm.model;

import java.util.List;

/**
 * <p>Title: com.superman.smsalarm.model.DisturbModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-29 下午9:00:23
 */

public class DisturbModel {
	
	private boolean isUse;
	private boolean isCustom;
	private String dateString;
	private int begin_hour;
	private int begin_min;
	private int end_hour;
	private int end_min;
	
	public boolean isUse() {
		return isUse;
	}
	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}
	public boolean isCustom() {
		return isCustom;
	}
	public void setCustom(boolean isCustom) {
		this.isCustom = isCustom;
	}
	public String getDateString() {
		return dateString;
	}
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	public int getBegin_hour() {
		return begin_hour;
	}
	public void setBegin_hour(int begin_hour) {
		this.begin_hour = begin_hour;
	}
	public int getBegin_min() {
		return begin_min;
	}
	public void setBegin_min(int begin_min) {
		this.begin_min = begin_min;
	}
	public int getEnd_hour() {
		return end_hour;
	}
	public void setEnd_hour(int end_hour) {
		this.end_hour = end_hour;
	}
	public int getEnd_min() {
		return end_min;
	}
	public void setEnd_min(int end_min) {
		this.end_min = end_min;
	}
	
	
}
