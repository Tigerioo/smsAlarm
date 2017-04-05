/**
 * 
 */
package com.superman.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.superman.smsalarm.model.HistoryModel;
import com.superman.smsalarm.model.SmsAlarmModel;
import com.superman.util.ConstantUtil;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsHistory;
import com.superman.util.SQLite.SmsIntercept;

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
public class HistoryActivity extends ActionBarActivity{
	
	private SQLiteDatabase db;
	private int history_type;
    private String alarm_id;
    private String[] idArray;//记录所有ALARM_ID的集合
	private ViewPager mViewPager;
	private TextView titleText;
	private MyFragmentStatePageAdapter myFragmentStatePageAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_history);
		
		db = SmsAlarmDao.getDbInstance(this);
		Bundle bundle = getIntent().getExtras();
		history_type = bundle.getInt(SmsHistory.HISTORY_TYPE);
		alarm_id = bundle.getString(SmsHistory.ALARM_ID);
		idArray = bundle.getStringArray(ConstantUtil.ALARM_ARRAY);
		
		// get a ActionBar
		ActionBar actionBar = getSupportActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View titleView = li.inflate(R.layout.layout_activity_history_title_bar, null);
		actionBar.setCustomView(titleView);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
//		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		
		titleText = (TextView)titleView.findViewById(R.id.history_title_bar_title_text);
		titleText.setText(getTitleName());
		
		myFragmentStatePageAdapter = new MyFragmentStatePageAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager)findViewById(R.id.his_pager);
		mViewPager.setAdapter(myFragmentStatePageAdapter);
		mViewPager.setCurrentItem(getCurrentPosition());
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				alarm_id = idArray[position];
				titleText.setText(getTitleName());
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int position) {
			}
		});
		
	}
	
	private String getTitleName(){
		Cursor c = null;
		try{
			if(history_type == 0){
				c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.KEY_WORD, SmsAlarm.PHONENUMBER}, SmsAlarm.ID + "=?", new String[]{alarm_id}, null, null, null);
			}else if(history_type == 1){
				c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsIntercept.KEY_WORD, SmsAlarm.PHONENUMBER}, SmsIntercept.ID + "=?", new String[]{alarm_id}, null, null, null);
			}
			if(c.moveToFirst()){
				String keyword = c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD));
				String phone = c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER));
				if(keyword.equals(ConstantUtil.ALL_KEYWORD))
					return phone;
				return keyword;
			}
		} catch(Exception e){
			String error = e.toString();
			System.err.println(error);
			LogUtil.saveLog(e.toString());
		}finally {
			if(c != null){
				c.close();
			}
		}
		return "";
	}
	
	private int getCurrentPosition(){
		for (int i = 0; i < idArray.length; i++) {
			System.out.println(idArray[i]);
			if(alarm_id.equals(idArray[i])) return i;
		}
		return 0;
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
		if(history_type == 0) {
			intent.putExtra("isClockFirst", true);
		}else if(history_type == 1){
			intent.putExtra("isClockFirst", false);
		}
		intent.setClass(HistoryActivity.this, MainActivity.class);
		startActivity(intent);
		HistoryActivity.this.finish();
	}
	
	private String getNextAlarmId(int flag){
		int tag = 0;//记录当前的alarm_id在第几个
        for (int j = 0; j < idArray.length; j++) {
			if(idArray[j].equals(alarm_id)){
				tag = j;
				break;
			}
		}
		
        if(idArray.length <= 1) {
        	return "";
        }
        switch (flag) {
		case 1://1表示前一个
			if(tag == 0){//0表示当前是第一个，没有前一个
				return "";
			}
			return idArray[tag-1];
		default://默认是2，表示后一个
			if(tag == (idArray.length-1)){//最后一个
				return "";
			}
			return idArray[tag+1];
		}
    }
	
	class MyFragmentStatePageAdapter extends FragmentStatePagerAdapter{

		public MyFragmentStatePageAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			HistoryFragment f = new HistoryFragment();
			f.setAlarmid(idArray[position]);
			return f;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			HistoryFragment f = (HistoryFragment) super.instantiateItem(container, position);
			f.setAlarmid(idArray[position]);
			f.refreshListView();
			return f;
		}
		
		@Override
		public int getCount() {
			return idArray.length;
		}
		
		@Override  
		public int getItemPosition(Object object) {  
		   return POSITION_NONE;  
		}
	}
	
	class HistoryFragment extends Fragment implements OnTouchListener{
		
		private List<HistoryModel> mData;
	    private boolean isShowCheckBox = false;//is show checkBox flag
	    private SmsAlarmModel alarmModel;
	    private ListView lv;
	    private MyAdapter adapter;
	    private String currentAlarmId;
	    private View delView;
	    
	    public void setAlarmid(String alarm_id){
	    	this.currentAlarmId = alarm_id;
	    }
	    
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    	View view = inflater.inflate(R.layout.layout_activity_history_list, container, false);
			
			alarmModel = queryAlarmInfoById(currentAlarmId);
			
			//更新状态
			updateStatus();
			
			//set title
			/*TextView titleText = (TextView)view.findViewById(R.id.title_bar_title_text);
			if(alarmModel.getKey_word().length() > 0){
				String key_word = alarmModel.getKey_word();
				if(key_word.length() > 14){
					key_word = key_word.substring(0, 13) + "...";
				}
				titleText.setText("【" + key_word + "】");
				
			}else {
				if(history_type == 0){
					titleText.setText(R.string.history_alarm);
				}else if(history_type == 1){
					titleText.setText(R.string.history_intecept);
				}
			}*/
			
	        mData = getData(history_type, alarmModel.getId());
	        delView = (View)view.findViewById(R.id.history_delete_tip);
	        lv = (ListView)view.findViewById(R.id.history_list);
	    	adapter = new MyAdapter();
	    	lv.setAdapter(adapter);
	    	lv.setSelection(mData.size() - 1);
	    	
	    	lv.setOnTouchListener(this);
	    	
	    	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
	
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					
					lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	
	                        public void onCreateContextMenu(ContextMenu menu, View v,
	                                        ContextMenuInfo menuInfo) {
	                                menu.add(0, 1, 0, "复制");
	                                menu.add(0, 2, 0, "删除");
//	                                menu.add(0, 3, 0, "恢复到系统短信");
	                        }
	                });
					
					return false;
				}
			
	        	
	        });
	    	
	    	lv.setOnItemClickListener(new OnItemClickListener() {
	    		
	    		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
	    	    	HistoryModel model = mData.get(position);
	    	    	model.setSelected(!model.isSelected());
	    	    	adapter.notifyDataSetChanged();
	    	    }
			});
			return view;
	    }
	
	/**
	 * add since v2.4.6.4
	 * 更新Sms_alarm sms_intercept表的is_update状态为0
	 */
	private void updateStatus(){
		ContentValues values = new ContentValues();
		
		if(history_type == 0){
			values.put(SmsAlarm.IS_UPDATE, 0);
			db.update(SmsAlarm.TABLE_NAME, values, SmsAlarm.ID+"=?", new String[]{alarm_id});
		}else {
			values.put(SmsIntercept.IS_UPDATE, 0);
			db.update(SmsIntercept.TABLE_NAME, values, SmsIntercept.ID+"=?", new String[]{alarm_id});
		}
	}
	
	private SmsAlarmModel queryAlarmInfoById(String alarm_id){
		Cursor c = null;
		SmsAlarmModel model = new SmsAlarmModel();
		try{
			if(history_type == 0){
				c = db.query(SmsAlarm.TABLE_NAME, new String[]{SmsAlarm.KEY_WORD, SmsAlarm.PHONENUMBER}, SmsAlarm.ID + "=?", new String[]{alarm_id}, null, null, null);
			}else if(history_type == 1){
				c = db.query(SmsIntercept.TABLE_NAME, new String[]{SmsIntercept.KEY_WORD, SmsIntercept.PHONENUMBER}, SmsIntercept.ID + "=?", new String[]{alarm_id}, null, null, null);
			}
			if(c.moveToFirst()){
				model.setKey_word(c.getString(c.getColumnIndex(SmsAlarm.KEY_WORD)));
				model.setPhone_number(c.getString(c.getColumnIndex(SmsAlarm.PHONENUMBER)));
				model.setId(alarm_id);
			}
		} catch(Exception e){
			String error = e.toString();
			System.err.println(error);
			LogUtil.saveLog(e.toString());
		}finally {
			if(c != null){
				c.close();
			}
		}
		return model;
	}
	
	/**
	 * refresh ListView
	 * @param lv
	 * @param key_word
	 * @param adapter
	 */
	public void refreshListView(){
		if(mData == null) return;
		mData.clear();
		mData.addAll(getData(history_type, alarmModel.getId()));
		adapter.notifyDataSetChanged();
		lv.invalidateViews();   
		lv.refreshDrawableState();
	}
	
	// 长按菜单响应函数
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case 1://copy to clipboard
        	String content = ((TextView)info.targetView.findViewById(R.id.history_info)).getText().toString();
        	   ClipboardManager cm = (ClipboardManager)HistoryActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
               cm.setText(content);
               Toast.makeText(HistoryActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                break;
        case 2:
        	isShowCheckBox = true;//display checkbox
			adapter.notifyDataSetChanged();
			
			//set all checkbox false
			for (HistoryModel model : mData) {
				model.setSelected(false);
			}
			
			delView.setVisibility(View.VISIBLE);
			
			//deal Listener with delete textView
			TextView deleteView = (TextView)delView.findViewById(R.id.history_delete_tip_deleteSingle);
			deleteView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					AlertDialog.Builder deleteAllBuilder = new AlertDialog.Builder(HistoryActivity.this);
					int total_count = 0;
					for (HistoryModel model : mData) {
						if(model.isSelected()){
							total_count += 1;
						}
					}
					deleteAllBuilder.setTitle(getResources().getString(R.string.tip_delete_start) + total_count + getResources().getString(R.string.tip_delete_end));
					deleteAllBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							for (HistoryModel model : mData) {
								if(model.isSelected()){
									db.delete(SmsHistory.TABLE_NAME, SmsHistory.ID + "=?", new String[]{String.valueOf(model.getId())});
								}
							}
							refreshListView();
							if(mData.size() == 0){
								delView.setVisibility(View.GONE);
							}
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.cancel();
						}
					}).create().show(); 
					
				}
			});
			
			//deal listener with delete all textView
			TextView deleteAllView = (TextView)delView.findViewById(R.id.history_delete_tip_deleteAll);
			deleteAllView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					AlertDialog.Builder deleteAllBuilder = new AlertDialog.Builder(HistoryActivity.this);
					deleteAllBuilder.setTitle(R.string.tip_delete_all);
					deleteAllBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							db.delete(SmsHistory.TABLE_NAME, SmsHistory.ALARM_ID + "=? and " + SmsHistory.HISTORY_TYPE + "=?", 
									new String[]{alarmModel.getId(), String.valueOf(history_type)});
							refreshListView();
							if(mData.size() == 0){
								delView.setVisibility(View.GONE);
							}
							Toast.makeText(HistoryActivity.this, "Delete All Success", Toast.LENGTH_SHORT).show();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.cancel();
						}
					}).create().show(); 
					
				}
			});
        		break;
        default:
                break;
        }

        return super.onContextItemSelected(item);

    }
	
	private List<HistoryModel> getData(int history_type, String alarm_id) {
        List<HistoryModel> list = new ArrayList<HistoryModel>();
        Cursor c = null;
        try {
    		c = db.query(SmsHistory.TABLE_NAME, new String[]{SmsHistory.ID, SmsHistory.SMS_TEXT, SmsHistory.RECV_PHONENUMBER, SmsHistory.RECEIVER_TIME}, SmsHistory.HISTORY_TYPE+"=? and " 
        			+ SmsHistory.ALARM_ID + "=?", new String[]{String.valueOf(history_type), alarm_id}, null, null, SmsHistory.ID + " asc");
        	
			while(c.moveToNext()){
				HistoryModel model = new HistoryModel();
				model.setId(c.getInt(c.getColumnIndex(SmsHistory.ID)));
				model.setSms_text(c.getString(c.getColumnIndex(SmsHistory.SMS_TEXT)));
				model.setReceiver_time(c.getString(c.getColumnIndex(SmsHistory.RECEIVER_TIME)));
				model.setRecv_phonenumber(c.getString(c.getColumnIndex(SmsHistory.RECV_PHONENUMBER)));
				model.setSelected(false);
				list.add(model);
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
        public TextView info;
        public CheckBox checkBox;
        public TextView id;//save history_id
    }
     
     
    public class MyAdapter extends BaseAdapter{
 
        private LayoutInflater mInflater;
         
         
        public MyAdapter(){
            this.mInflater = LayoutInflater.from(HistoryActivity.this);
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
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	View view = null;
            if (convertView == null) {
                 
            	final ViewHolder holder = new ViewHolder();  
                 
            	view = mInflater.inflate(R.layout.layout_activity_history_list_detail, null);
                holder.checkBox = (CheckBox)view.findViewById(R.id.history_ItemCheckBox);
                holder.title = (TextView)view.findViewById(R.id.history_title);
                holder.info = (TextView)view.findViewById(R.id.history_info);
                holder.id = (TextView)view.findViewById(R.id.history_id);
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						HistoryModel element = (HistoryModel) holder.checkBox.getTag();
			            element.setSelected(buttonView.isChecked());
					}
				});
                
                view.setTag(holder);
                holder.checkBox.setTag(mData.get(position));
            }else {
                 
                view = convertView;
                ((ViewHolder) view.getTag()).checkBox.setTag(mData.get(position));
            }
             
            ViewHolder holder = (ViewHolder) view.getTag();
            HistoryModel model = mData.get(position);
            String phoneNumber = model.getRecv_phonenumber();
            String sms_text = model.getSms_text();
            String receiver_time = model.getReceiver_time();
            String id = String.valueOf(model.getId());
            holder.title.setText(phoneNumber + "(" + receiver_time + "): \n");
            String txt = "\n" + sms_text + "\n";
            holder.info.setText(txt);
            holder.id.setText(id);
            if(isShowCheckBox){
            	holder.checkBox.setVisibility(View.VISIBLE);
            }else {
            	holder.checkBox.setVisibility(View.GONE);
            }
            holder.checkBox.setChecked(model.isSelected());
            
            return view;
        }
         
    }


	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	
	}
}
