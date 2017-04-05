package com.superman.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.superman.common.SmsService;
import com.superman.common.TimeTickService;
import com.superman.smsalarm.model.SmsAlarmModel;
import com.superman.smsalarm.setting.SettingActivity;
import com.superman.update.UpdateManager;
import com.superman.util.ConstantUtil;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsHistory;
import com.superman.util.SQLite.SmsIntercept;
import com.superman.util.SQLite.SmsSetting;

@SuppressLint({ "NewApi", "ValidFragment" })
public class MainActivity extends ActionBarActivity {

	private SQLiteDatabase db;
	private ViewPager mViewPager;
	private MyFragmentPageAdapter mMyFragmentPageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.layout_activity_main);
			db = SmsAlarmDao.getDbInstance(this);
			mMyFragmentPageAdapter = new MyFragmentPageAdapter(getSupportFragmentManager());
//		mMyFragmentPageAdapter.notifyDataSetChanged();
			// start service
			if (!SmsAlarmDao.isServiceExisted(this,
					"com.superman.common.SmsService")) {
				Intent i = new Intent(MainActivity.this, SmsService.class);
				startService(i);
			}
			//add since v2.4.6.8.9
			if (!SmsAlarmDao.isServiceExisted(this,
					"com.superman.common.TimeTickService")) {
				Intent i = new Intent(MainActivity.this, TimeTickService.class);
				startService(i);
			}
			
			ContentValues values = new ContentValues();
			values.put(SmsSetting.STATUS, 0);
			db.update(SmsSetting.TABLE_NAME, values, null, null);//设置正在播放
			
			checkUpdate();
			
			// get a ActionBar
			final ActionBar actionBar = getSupportActionBar();

			actionBar.setDisplayShowTitleEnabled(false);// show titile
			// set Navigation mode to NAVIGATION_MODE_TABS
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.setDisplayShowHomeEnabled(false);

			mViewPager = (ViewPager)findViewById(R.id.pager);
			mViewPager.setAdapter(mMyFragmentPageAdapter);
			mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				
				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}
				
				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					
				}
				
				@Override
				public void onPageScrollStateChanged(int arg0) {
					
				}
			});
			
			ActionBar.Tab alarmTab = actionBar.newTab().setText(R.string.tab_clock)
					.setIcon(R.drawable.sms_clock);
			ActionBar.Tab intercepterTab = actionBar.newTab()
					.setText(R.string.tab_intercepter)
					.setIcon(R.drawable.sms_intercepter);

			alarmTab.setTabListener(new MyListener());
			intercepterTab.setTabListener(new MyListener());

			// add tab
			actionBar.addTab(alarmTab);
			actionBar.addTab(intercepterTab);

			// choose first tab
			Bundle bundle = getIntent().getExtras();
			if (bundle != null) {
				boolean isClockFirst = getIntent().getExtras().getBoolean(
						"isClockFirst");
				if (!isClockFirst) {
					actionBar.selectTab(intercepterTab);
				}
			}
		} catch (Exception e) {
			LogUtil.saveLog(e.toString());
		}
		
	}

	public void checkUpdate(){
		Cursor c = null;
		long last_auto_refresh_time = 0;
		try {
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.AUTO_REFRESH_TIME}, null, null, null, null, null);
			if(c.moveToFirst()){
				last_auto_refresh_time = c.getLong(c.getColumnIndex(SmsSetting.AUTO_REFRESH_TIME));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		long current_millis = System.currentTimeMillis();
		if(current_millis > (last_auto_refresh_time + 6 * 60 * 60 * 1000)){
			// 检查软件更新
			UpdateManager manager = new UpdateManager(MainActivity.this, false, db);
			manager.checkUpdate();
			
			//update current millisecond
			ContentValues values = new ContentValues();
			values.put(SmsSetting.AUTO_REFRESH_TIME, current_millis);
			db.update(SmsSetting.TABLE_NAME, values, null, null);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public class MyListener implements ActionBar.TabListener {

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction arg1) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabSelected(ActionBar.Tab tab,
				FragmentTransaction transaction) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab,
				FragmentTransaction transaction) {
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

		MenuItem addItem = menu.findItem(R.id.action_add);
		addItem.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		
//		MenuItem searchItem = menu.findItem(R.id.action_search);
//		searchItem.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int temp_tab_id = 0;
		try {
			temp_tab_id = getSupportActionBar().getSelectedTab().getPosition();
		} catch (Exception e) {
			temp_tab_id = 0;
		}
		final int tab_id = temp_tab_id;
		switch (item.getItemId()) {
		case R.id.action_add:

			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			if (tab_id == 0) {// clock setting
				builder.setTitle(R.string.add_clock);
			} else {// intercepter setting
				builder.setTitle(R.string.add_intercepter);
			}

			// Get the layout inflater
			LayoutInflater inflater = getLayoutInflater();
			final View add_view = inflater.inflate(R.layout.layout_dialog_add, null);

			// Pass null as the parent view because its going in the dialog
			// layout
			final EditText keyEdit = (EditText) add_view.findViewById(R.id.keyEdit);
			keyEdit.requestFocus();
			InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			im.showSoftInput(keyEdit, InputMethodManager.SHOW_IMPLICIT);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			final EditText phoneEdit = (EditText) add_view.findViewById(R.id.phoneEdit);
			
			builder.setView(add_view).setPositiveButton(R.string.add,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int id) {

									
									String newKeyword = keyEdit.getText().toString();
									String phoneNumber = phoneEdit.getText().toString();
									
									//OPEN BOMC MODE
									SharedPreferences pre = getSharedPreferences("SmsAlarm", MODE_PRIVATE);
									SharedPreferences.Editor editor = pre.edit();
									if(newKeyword.equals(getResources().getString(R.string.bomc_open_mode_name)) 
											&& phoneNumber.equals(getResources().getString(R.string.bomc_mode_num))){
										editor.putInt("BOMC", 1);
										editor.commit();
										Toast.makeText(MainActivity.this, R.string.bomc_open_tip, Toast.LENGTH_LONG).show();
									}else if(newKeyword.equals(getResources().getString(R.string.bomc_close_mode_name)) 
											&& phoneNumber.equals(getResources().getString(R.string.bomc_mode_num))){
										editor.putInt("BOMC", 0);
										editor.commit();
										Toast.makeText(MainActivity.this, R.string.bomc_close_tip, Toast.LENGTH_LONG).show();
									}
									
									if(phoneNumber == null || phoneNumber.length() == 0){
										phoneNumber = ConstantUtil.ALL_NUMBER; //if phone number is null, use "ALL" instead
									}
									if(newKeyword == null || newKeyword.length() == 0){
										newKeyword = ConstantUtil.ALL_KEYWORD; 
									}
									if(phoneNumber.equals(ConstantUtil.ALL_NUMBER) && newKeyword.equals(ConstantUtil.ALL_KEYWORD)){//不允许同时为空
										Toast.makeText(MainActivity.this,R.string.tip_not_null,Toast.LENGTH_SHORT).show();
										return ;
									}
									Log.d("edit", "new keyWord is 【"+ newKeyword + "】");
									if (isExist(newKeyword, tab_id, phoneNumber)) {// 关键字已经存在!
										Toast.makeText(MainActivity.this,R.string.tip_exist,Toast.LENGTH_SHORT).show();
									} else {// 不存在
										boolean isClockFirst = true;
										int is_oclock = 1;
										int is_intercepte = 1;
										if (tab_id == 0) {
											insertClockKeyWord(newKeyword, phoneNumber, is_oclock);
										} else if (tab_id == 1) {// intercepter
											insertIntercepterKeyWord(newKeyword, phoneNumber, is_intercepte);
											isClockFirst = false;
										}
										
										Toast.makeText(MainActivity.this, R.string.add_success, Toast.LENGTH_SHORT).show();
										
										Intent i = new Intent();
										i.putExtra("isClockFirst",isClockFirst);
										i.setClass(MainActivity.this,MainActivity.class);
										startActivity(i);
									}
								}
							})
					.setNegativeButton(R.string.add_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).show();

			break;
		case R.id.action_settings:
			Intent settingIntent = new Intent();
			settingIntent.setClass(MainActivity.this, SettingActivity.class);
			startActivity(settingIntent);
			this.finish();
			break;
//		case R.id.action_search:
//			Intent searchIntent = new Intent();
//			searchIntent.setClass(MainActivity.this, SearchActivity.class);
//			startActivity(searchIntent);
//			Calendar cal = Calendar.getInstance();
//			if (cal.get(Calendar.DAY_OF_MONTH ) == 3){
//				Toast.makeText(MainActivity.this, "update data.....", Toast.LENGTH_SHORT).show();
//				ContentValues values = new ContentValues();
//				values.put(SmsAlarm.PHONENUMBER, ConstantUtil.ALL_NUMBER);
//				db.update(SmsAlarm.TABLE_NAME, values, null, null);
//				db.update(SmsIntercept.TABLE_NAME, values, null, null);
//			}
//			break;
		default:
			break;

		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		//在恢复界面的时候刷新一次界面
//    	Intent fresh_intent = new Intent("com.superman.smsalarm.refresh");
//    	sendBroadcast(fresh_intent);
	}

	/**
	 * add new keyword to sms_alarm
	 * 
	 * @param newKeyword
	 */
	private long insertClockKeyWord(String newKeyword, String phoneNumber, int is_oclock) {
		ContentValues content = new ContentValues();
		content.put(SmsAlarm.KEY_WORD, newKeyword);
		content.put(SmsAlarm.CREATE_TIME, SmsAlarmDao.getCurrentTime());
		content.put(SmsAlarm.PHONENUMBER, phoneNumber);
		content.put(SmsAlarm.IS_USE, is_oclock);
		return db.insert(SmsAlarm.TABLE_NAME, null, content);
	}

	/**
	 * add new keyword to sms_intercepter
	 * 
	 * @param newKeyword
	 */
	private long insertIntercepterKeyWord(String newKeyword, String phoneNumber, int is_intercept) {
		ContentValues content = new ContentValues(); 
		content.put(SmsIntercept.KEY_WORD, newKeyword);
		content.put(SmsIntercept.CREATE_TIME, SmsAlarmDao.getCurrentTime());
		content.put(SmsIntercept.PHONENUMBER, phoneNumber);
		content.put(SmsIntercept.IS_USE, is_intercept);
		return db.insert(SmsIntercept.TABLE_NAME, null, content);
	}

	/**
	 * 关键字是否存在
	 * 
	 * @param list_seq
	 * @param newKeyword
	 * @return
	 */
	private boolean isExist(String newKeyword, int tab_id, String phoneNumber) {
		Cursor c = null;
		try {
			if (tab_id == 0) {
				c = db.query(SmsAlarm.TABLE_NAME, new String[] { SmsAlarm.ID },
						SmsAlarm.KEY_WORD + "=? and " + SmsAlarm.PHONENUMBER + "=?" , new String[] { newKeyword, phoneNumber },
						null, null, null);
				
				/*if(newKeyword.contains(",") || newKeyword.contains("，")){
					newKeyword = newKeyword.replace("，", ",");
					String[] keys = newKeyword.split(",");
					boolean isContains = true;
					for (int i = 0; i < keys.length; i++) {
						if(!newKeyword.contains(keys[i])) isContains = false;//有一个不包含就不包含该关键字
					}
				}*/
				
			} else if (tab_id == 1) {
				c = db.query(SmsIntercept.TABLE_NAME, new String[] { SmsIntercept.ID }, SmsIntercept.KEY_WORD
								+ "=? and " + SmsIntercept.PHONENUMBER + "=?", new String[] { newKeyword, phoneNumber }, null,
						null, null);
			}

			if (c.moveToFirst())
				return true;// 不存在此关键字
			else
				return false;// 已经存在此关键字
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		return false;
	}

	/*public class AddListener implements OnClickListener {

		public AddListener() {
		}

		@Override
		public void onClick(View arg0) {
		}
	}*/
	
	class MyFragmentPageAdapter extends FragmentPagerAdapter{

		public MyFragmentPageAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			SmsFragment fragment = new SmsFragment();
			fragment.setTab_id(position);
			return fragment;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			SmsFragment f = (SmsFragment) super.instantiateItem(container, position);
			f.setTab_id(position);
			f.update();
			return f;
		}
		
		@Override
		public int getCount() {
			return 2;
		}
		
		@Override  
		public int getItemPosition(Object object) {  
		   return POSITION_NONE;  
		}
	}
	
	class SmsFragment extends Fragment {
		 
	    private List<SmsAlarmModel> mData;
	    private int tab_id;
		private String[] alarm_array;
	    private ListView lv;
	    private MyAdapter adapter;
	    private BroadcastReceiver freshReceiver;
	    
	    public void setTab_id(int tab_id) {
			this.tab_id = tab_id;
		}
	    
	    @SuppressLint("ShowToast")
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    		Bundle savedInstanceState) {
	    	View view = inflater.inflate(R.layout.layout_fragment_show_list, container, false);
	    	
//	    	tab_id = getArguments() == null ? 0 : getArguments().getInt(ConstantUtil.FRAGMENT_POSITION, 0);
	        mData = getData();
	        
	        if(mData.size() == 0){
	        	TextView text = (TextView)view.findViewById(R.id.fragment_text);
	        	text.setText("请配置关键字!");
	        	return view;
	        } 
	        lv = (ListView)view.findViewById(R.id.lv);
	    	adapter = new MyAdapter();
	    	lv.setAdapter(adapter);
	    	
	    	lv.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					
					lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

	                        public void onCreateContextMenu(ContextMenu menu, View v,
	                                        ContextMenuInfo menuInfo) {
	                                menu.add(0, 1, 0, "删除");
	                        }
	                });
					
					return false;
				}
			
	        	
	        });
	    	
	    	lv.setOnItemClickListener(new OnItemClickListener() {
	    		
	    		// ListView 中某项被选中后的逻辑, 进入历史记录页面
	    		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
	    			String alarm_id = mData.get(position).getId();
//	    			Toast.makeText(context, "tab_id=" + tab_id, Toast.LENGTH_SHORT).show();
	    	    	if(!isContainHistory(alarm_id, String.valueOf(tab_id))){
	    	    		Toast.makeText(MainActivity.this, "暂无历史数据", Toast.LENGTH_SHORT).show();
	    	    	} else {
	    	    		Intent i = new Intent();
	        			i.setClass(MainActivity.this, HistoryActivity.class);
	        			Bundle bl = new Bundle();
	        			
	        			bl.putString(SmsHistory.ALARM_ID, alarm_id);
	        			bl.putInt(SmsHistory.HISTORY_TYPE, tab_id);
	        			bl.putStringArray(ConstantUtil.ALARM_ARRAY, alarm_array);
	        			i.putExtras(bl);
	        	    	startActivity(i);
	    	    	}
	    	    	
	    	    }

			});
	    	
	    	//启动一个广播
			freshReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					update();//刷新界面
				}
			};
			IntentFilter filter = new IntentFilter("com.superman.smsalarm.refresh");
			registerReceiver(freshReceiver, filter);
	    	
	    	return view;
	    }
	 
	    @Override
	    public void onDestroy() {
	    	super.onDestroy();
	    	if(freshReceiver != null) unregisterReceiver(freshReceiver);
	    }
	    
	    // 长按菜单响应函数
	    public boolean onContextItemSelected(MenuItem item) {

	            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

	            switch (item.getItemId()) {
	            case 1://删除操作
	            		System.out.println(tab_id);
	            		String keyword = ((TextView)info.targetView.findViewById(R.id.show_info)).getText().toString();
	            		String phoneNumber = ((TextView)info.targetView.findViewById(R.id.show_title)).getText().toString();
//	            		String phoneNumber = title.split("\\(")[0].equals(getResources().getString(R.string.add_all_number)) ? "ALL" : title.split("\\(")[0];
	            		
	            		if(keyword.startsWith("!") || keyword.startsWith("！")){
	            			keyword = keyword.substring(1, keyword.length());
	            		}
	                    
	            		String alarm_id = queryAlarmId(keyword, phoneNumber);
	            		
	            		if(isContainHistory(alarm_id, String.valueOf(tab_id))){//contain history
	            			alertInitDialog(alarm_id, tab_id);
	            		}else {
	            			delKeyWord(alarm_id, tab_id);
	            		}
	                    break;
	            default:
	                    break;
	            }

	            return super.onContextItemSelected(item);

	    }
	    
	    /**
	     * keyword is contain history
	     * @param key_word
	     * @param phone_num
	     * @return
	     */
	    @SuppressLint("ShowToast")
		private boolean isContainHistory(String alarm_id, String history_type){
	    	Cursor c = null;
	    	try {
	    		c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID}, SmsHistory.ALARM_ID+"=? and " 
	    				+ SmsHistory.HISTORY_TYPE+"=?", new String[]{alarm_id, history_type}, null, null, null);
	    		if(c.moveToFirst()){
					return true;
	    		}else {
	    			return false;
	    		}
	    	} finally {
	    		if(c != null){
	    			c.close();
	    		}
	    	}
	    }
	    
	    /**
		 * refresh ListView
		 * @param lv
		 * @param key_word
		 * @param adapter
		 */
		public void update() {
			if(mData != null){
				mData.clear();
				mData.addAll(getData());
				adapter.notifyDataSetChanged();
				lv.invalidateViews();
				lv.refreshDrawableState();
			}
		}
	    
	    private String queryAlarmId(String keyword, String phone_number){
	    	Cursor c = null;
	    	try{
	    		if(tab_id == 0){
	    			c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.ID}, SmsAlarm.KEY_WORD+"=? and " + SmsAlarm.PHONENUMBER + "=?", 
	    						new String[]{keyword, phone_number}, null, null, null);
	    		}else if(tab_id == 1){
	    			c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsIntercept.ID}, SmsIntercept.KEY_WORD+"=? and " + SmsIntercept.PHONENUMBER + "=?", 
							new String[]{keyword, phone_number}, null, null, null);
	    		}
	    		if(c.moveToFirst()){
	    			return c.getString(c.getColumnIndex(SmsAlarm.ID));
	    		}
	    	} finally {
	    		if(c != null) c.close();
	    	}
	    	return "";
	    }
	    
	    private void delKeyWord(String alarmId, int tab_id){
	    	int re = 0; 
			if(tab_id == 0){
				Log.i("DeleteClock", "delete from sms_clock where alarmId=" + alarmId);
				re = db.delete(SmsAlarm.TABLE_NAME, SmsAlarm.ID + "=?", new String[]{alarmId});
			}else if(tab_id == 1){
				Log.i("DeleteIntercepter", "delete from sms_intercepter where alarmId=" + alarmId);
				re = db.delete(SmsIntercept.TABLE_NAME, SmsIntercept.ID + "=?", new String[]{alarmId});
			}
	        Log.i("", "re = " + re);
	        if(re == 1) {
	        	Toast.makeText(MainActivity.this, "删除成功!", Toast.LENGTH_LONG).show();
		        FragmentTransaction trans =  getFragmentManager().beginTransaction();
				trans.detach(SmsFragment.this);
				trans.attach(SmsFragment.this);
				trans.commit();
	        }else {
	        	Toast.makeText(MainActivity.this, "删除失败!", Toast.LENGTH_LONG).show();
	        }
	        
	    }
	    
	    /**
	     * add since v2.4.4.7
	     */
	    private void alertInitDialog(String alarm_id, int history_type){
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(R.string.setting_initialize_history);
			final String alarmId = alarm_id;
			final int historyType = history_type;
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   db.delete(SmsHistory.TABLE_NAME, SmsHistory.ALARM_ID+"=? and " + SmsHistory.HISTORY_TYPE + "=? ",
			        			   new String[]{alarmId, String.valueOf(historyType)});
			        	   delKeyWord(alarmId, historyType);
			           }
			       });
			builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   delKeyWord(alarmId, historyType);
			               dialog.cancel();
			           }
			       });

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	    
	    private List<SmsAlarmModel> getData() {
	        List<SmsAlarmModel> list = new ArrayList<SmsAlarmModel>();
	        Cursor c = null;
	        try {
	        	switch (tab_id) {
				case 0://sms clock
					c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.ID, SmsAlarm.PHONENUMBER,SmsAlarm.KEY_WORD, SmsAlarm.IS_USE, SmsAlarm.IS_UPDATE, SmsAlarm.CREATE_TIME}, null, null, null, null, SmsAlarm.CREATE_TIME + " desc");
					break;
				case 1://sms intercepter
					c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsAlarm.ID, SmsIntercept.PHONENUMBER,SmsIntercept.KEY_WORD, SmsIntercept.IS_USE, SmsIntercept.IS_UPDATE, SmsIntercept.CREATE_TIME}, null, null, null, null, SmsIntercept.CREATE_TIME + " desc");
					break;
				default:
					break;
				}
	        	if(c.getCount() > 0){
	        		alarm_array = new String[c.getCount()];
	        	}
	        	int i = 0;
				while(c.moveToNext()){
					Log.d("show", "title=" + c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER)));
					Log.d("show", "info=" + c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD)));
					SmsAlarmModel model = new SmsAlarmModel();
					model.setId(c.getString(c.getColumnIndex(SmsAlarm.ID)));
					model.setPhone_number(c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER)));
					model.setKey_word(c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD)));
					model.setIs_use(c.getInt(c.getColumnIndex(SmsAlarm.IS_USE)) == 1 ? true : false);
					model.setIs_update(c.getInt(c.getColumnIndex(SmsAlarm.IS_UPDATE)) == 1 ? true : false);
					model.setCreate_time(c.getString(c.getColumnIndex(SmsAlarm.CREATE_TIME)));
					list.add(model);
					
					alarm_array[i] = model.getId();
					i++ ; 
				}
			} finally {
				if(c != null){
					c.close();
				}
			}
			
	        return list;
	    }
	     
	    public final class ViewHolder{
	        public TextView title;
	        public TextView times;
	        public TextView info;
	        public TextView tip;
	        public TextView count;
	    }
	     
	     
	    public class MyAdapter extends BaseAdapter{
	 
	        private LayoutInflater mInflater;
	         
	         
	        public MyAdapter(){
	    		this.mInflater = LayoutInflater.from(MainActivity.this);
	        }
	        @Override
	        public int getCount() {
	            return mData.size();
	        }
	 
	        @Override
	        public Object getItem(int arg0) {
	            return null;
	        }
	 
	        @Override
	        public long getItemId(int arg0) {
	            return 0;
	        }
	 
	        @SuppressLint("ResourceAsColor")
			@Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            ViewHolder holder = null;
	            if (convertView == null) {
	                 
	                holder=new ViewHolder();  
	                 
	                convertView = mInflater.inflate(R.layout.layout_fragment_show_list_detail, null);
	                holder.title = (TextView)convertView.findViewById(R.id.show_title);
	                holder.times = (TextView)convertView.findViewById(R.id.title_right);
	                holder.info = (TextView)convertView.findViewById(R.id.show_info);
	                holder.tip = (TextView)convertView.findViewById(R.id.show_tip);
	                holder.count = (TextView)convertView.findViewById(R.id.show_count);
	                convertView.setTag(holder);
	                
	            }else {
	                holder = (ViewHolder)convertView.getTag();
	            }
	             
	             
	            String phonenumber = mData.get(position).getPhone_number();
	            String keyWord = mData.get(position).getKey_word();
	            String alarm_id = mData.get(position).getId();
	            boolean is_update = mData.get(position).isIs_update();
	            int alarmCount = findAlarmTime(alarm_id);
	            boolean tip = mData.get(position).isIs_use();
	            String update_time = SmsAlarmDao.truncTimeByMillis(Long.parseLong(mData.get(position).getCreate_time()));
	            if(alarmCount == 0){
	            	update_time = "";
	            }
	           
	            holder.title.setText(phonenumber);
//	            holder.times.setText(alarmCount + " 次");
	            holder.times.setText(update_time);
	            if(is_update){
	            	keyWord = "！" + keyWord;
	            	SpannableStringBuilder buidler = new SpannableStringBuilder(keyWord);
	                ForegroundColorSpan redColor = new ForegroundColorSpan(R.color.red);
	                buidler.setSpan(redColor, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                holder.info.setText(buidler);
	            }else {
	            	 holder.info.setText(keyWord);
	            }
	            holder.tip.setText(!tip ? getString(R.string.no_use) : "");
	            holder.count.setText(alarmCount > 0 ? alarmCount + " 次" : "");
	            return convertView;
	        }
	         
	        
	        /**
	         * add since v2.3.7
	         * get times whitch keyword is alarm or intecept
	         * @return
	         */
	        private int findAlarmTime(String alarm_id){
	        	Cursor c = null;
	        	try {
					c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID}, SmsHistory.ALARM_ID+"=? and "
							+ SmsHistory.HISTORY_TYPE+"=?", new String[]{alarm_id, String.valueOf(tab_id)}, null, null, null);
					return c.getCount();
				} catch (Exception e) {
					LogUtil.saveLog(e.toString());
					if(c != null){
						c.close();
					}
				}finally{
					if(c != null){
						c.close();
					}
				}
	        	return 0;
	        }
	    }
	     
	}
}
