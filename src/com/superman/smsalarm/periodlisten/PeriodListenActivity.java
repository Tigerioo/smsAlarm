/**
 * 
 */
package com.superman.smsalarm.periodlisten;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.superman.smsalarm.R;
import com.superman.smsalarm.model.PeriodListenModel;
import com.superman.smsalarm.setting.SettingActivity;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsPeriodListen;

/**
 * <p>Title: com.superman.smsalarm.PeriodListenActivity.java</p>
 *
 * <p>Description: add since v2.4.6.8.8</p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-10-1 下午5:32:58
 */

public class PeriodListenActivity extends Activity{
	
	private SQLiteDatabase db;
	private List<PeriodListenModel> mData;//listView数据集合
	private MyAdapter adapter;
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_setting_list_is_period_listen_detail_list);
		db = SmsAlarmDao.getDbInstance(this);//initialize SQLite
		mData = getData();
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		
		adapter = new MyAdapter();
		listView = (ListView)findViewById(R.id.list_setting_is_period_listen_list);
		listView.setAdapter(adapter);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

                    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
                            menu.add(0, 1, 0, "删除");
                    }
            });
				return false;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.periodlisten_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_period_listen_add:
			Intent addIntent = new Intent();
			addIntent.setClass(PeriodListenActivity.this, Add.class);
			startActivity(addIntent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// 长按菜单响应函数
    public boolean onContextItemSelected(MenuItem item) {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
            case 1://删除操作
            		String id = ((TextView)info.targetView.findViewById(R.id.setting_list_period_listen_id)).getText().toString();
            		db.delete(SmsPeriodListen.TABLE_NAME, SmsPeriodListen.ID + "=?", new String[]{id});
            		refresh();
                    break;
            default:
                    break;
            }

            return super.onContextItemSelected(item);

    }
	
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent back = new Intent();
			back.setClass(PeriodListenActivity.this, SettingActivity.class);
			startActivity(back);
		}
		return super.onKeyDown(keyCode, event);
	}
    
    /**
     * 重载页面刷新列表
     */
    public void refresh() {
		if(mData != null){
			mData.clear();
			mData.addAll(getData());
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();
		}
	}
    
	/**
	 * 获取全部监听列表
	 * @return
	 */
	private List<PeriodListenModel> getData(){
		List<PeriodListenModel> reList = new ArrayList<PeriodListenModel>();
		Cursor c = null;
		try {
			c = db.query(SmsPeriodListen.TABLE_NAME, new String[]{SmsPeriodListen.ID, SmsPeriodListen.KEYWORD,
					SmsPeriodListen.PERIOD_TYPE, SmsPeriodListen.PERIOD, SmsPeriodListen.LISTEN_PERIOD,
					SmsPeriodListen.IS_DELAY_ALARM, SmsPeriodListen.DELAY_PERIOD_TYPE, SmsPeriodListen.DELAY_PERIOD,
					SmsPeriodListen.LAST_REV_TIME, SmsPeriodListen.IS_USE, SmsPeriodListen.CREATE_TIME, SmsPeriodListen.NUMBER,
					SmsPeriodListen.LISTEN_TIME_DAY, SmsPeriodListen.LISTEN_TIME_HOUR, SmsPeriodListen.LISTEN_TIME_MINUTE},
					null, null, null, null, SmsPeriodListen.CREATE_TIME + " asc");
			while(c.moveToNext()){
				PeriodListenModel model = new PeriodListenModel();
				model.setId(c.getLong(c.getColumnIndex(SmsPeriodListen.ID)));
				model.setKeyword(c.getString(c.getColumnIndex(SmsPeriodListen.KEYWORD)));
				model.setNumber(c.getString(c.getColumnIndex(SmsPeriodListen.NUMBER)));
				model.setPeriod_type(c.getInt(c.getColumnIndex(SmsPeriodListen.PERIOD_TYPE)));
				model.setPeriod(c.getInt(c.getColumnIndex(SmsPeriodListen.PERIOD)));
				model.setListen_period(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_PERIOD)));
				model.setIs_delay_alarm(c.getInt(c.getColumnIndex(SmsPeriodListen.IS_DELAY_ALARM)));
				model.setDelay_period(c.getInt(c.getColumnIndex(SmsPeriodListen.DELAY_PERIOD)));
				model.setDelay_period_type(c.getInt(c.getColumnIndex(SmsPeriodListen.DELAY_PERIOD_TYPE)));
				model.setLast_rev_time(c.getLong(c.getColumnIndex(SmsPeriodListen.LAST_REV_TIME)));
				model.setIs_use(c.getInt(c.getColumnIndex(SmsPeriodListen.IS_USE)));
				model.setCreate_time(c.getLong(c.getColumnIndex(SmsPeriodListen.CREATE_TIME)));
				model.setListen_time_day(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_DAY)));
				model.setListen_time_hour(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_HOUR));
				model.setListen_time_minute(c.getInt(c.getColumnIndex(SmsPeriodListen.LISTEN_TIME_MINUTE)));
				reList.add(model);
			}
		} catch (Exception e) {
			Log.e("", e.toString());
		} finally {
			if(c != null) c.close();
		}
		return reList;
	}
	
	/**
	 * adapter holder class
	 */
	public final class ViewHolder{
		public TextView id;
		public TextView number;
		public TextView lastRcvTime;
		public TextView keyword;
		public Switch switcher;
	}
	
	/**
	 * 
	 * 内部Adapter类
	 */
	public class MyAdapter extends BaseAdapter{

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
			if(convertView == null){
				view = LayoutInflater.from(PeriodListenActivity.this).
						inflate(R.layout.layout_activity_setting_list_period_listen_detail_switch, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.id = (TextView)view.findViewById(R.id.setting_list_period_listen_id);
				viewHolder.number = (TextView)view.findViewById(R.id.setting_list_period_listen_number);
				viewHolder.lastRcvTime = (TextView)view.findViewById(R.id.setting_list_period_listen_lastRcvTime);
				viewHolder.keyword = (TextView)view.findViewById(R.id.setting_list_period_listen_keyword);
				viewHolder.switcher = (Switch)view.findViewById(R.id.setting_list_period_listen_switch);
				viewHolder.switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						PeriodListenModel model = (PeriodListenModel)viewHolder.switcher.getTag();
						model.setIs_use(isChecked ? 1 : 0);
						
						ContentValues update = new ContentValues();
						update.put(SmsPeriodListen.IS_USE, model.getIs_use());
						db.update(SmsPeriodListen.TABLE_NAME, update, SmsPeriodListen.ID + "=?", new String[]{String.valueOf(model.getId())});
					}
				});
				view.setTag(viewHolder);
				viewHolder.switcher.setTag(mData.get(position));
				
			}else {
				view = convertView;
				((ViewHolder)view.getTag()).switcher.setTag(mData.get(position));
			}
			
			ViewHolder holder = (ViewHolder)view.getTag();
			PeriodListenModel model = mData.get(position);
			holder.id.setText(String.valueOf(model.getId()));
			holder.number.setText(model.getNumber().length() == 0 ? "所有号码" : model.getNumber());
			holder.lastRcvTime.setText("上次接收时间：" + truncTime(model.getLast_rev_time()));
			holder.keyword.setText(model.getKeyword());
			holder.switcher.setChecked(model.getIs_use() == 1 ? true : false);
			
			return view;
		}
		
		private String truncTime(long millis){
			if(millis == 0) return "暂未收到过短信";
			java.text.DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return format.format(new Date(millis));
		}
	}
}
