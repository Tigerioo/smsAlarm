/**
 * 
 */
package com.superman.smsalarm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * <p>Title: com.superman.smsalarm.WelcomeActivity.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-3-30 下午6:36:21
 */

public class WelcomeActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_welcome);
		
		// get a ActionBar
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowTitleEnabled(false);// show titile
		actionBar.setDisplayShowHomeEnabled(false);
		
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		Intent i = new Intent();
		i.setClass(WelcomeActivity.this, MainActivity.class);
		startActivity(i);
	}
}
