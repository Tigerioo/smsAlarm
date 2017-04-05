/**
 * 
 */
package com.superman.smsalarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.superman.util.SmsAlarmDao;

/**
 * <p>Title: com.superman.smsalarm.HistoryActivity.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-7-9 下午11:01:25
 */

@SuppressLint("ValidFragment")
public class SearchActivity extends Activity{
	
	private SQLiteDatabase db;
	private SearchView searchView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_search);
		
		db = SmsAlarmDao.getDbInstance(this);
		searchView = (SearchView)findViewById(R.id.search_view);
		searchView.setIconifiedByDefault(true);
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint("keyword");
		
		// 获取编辑框焦点
		searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        (new Handler()).postDelayed(new Runnable() {
		            public void run() {
                    InputMethodManager imm = (InputMethodManager)
            		searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//		                    if(isFocus){
//                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//		                    }
//		                    else{
		                        imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);
//		                    }
		            }
		        }, 100);
			}
		});
		searchView.setFocusable(true);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backToHome();
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			backToHome();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void backToHome(){
		Intent intent = new Intent();
		intent.setClass(SearchActivity.this, MainActivity.class);
		startActivity(intent);
		SearchActivity.this.finish();
	}
	
}
