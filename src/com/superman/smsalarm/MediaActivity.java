package com.superman.smsalarm;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsAlarm;
import com.superman.util.SQLite.SmsSetting;

public class MediaActivity extends Activity {

	private SQLiteDatabase db;
	private TextView textView;
	private Button stopButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_media);
		
		db = SmsAlarmDao.getDbInstance(this);
		
		Intent i = this.getIntent();
		String smsText = i.getStringExtra(SmsAlarm.KEY_WORD);
		textView = (TextView)findViewById(R.id.mediaText);
		textView.setText(smsText);

		int status = 0;
		Cursor c = null;
		try {
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.STATUS}, null, null, null, null, null);
			status = 0;
			while(c.moveToNext()){
				status = c.getInt(c.getColumnIndex(SmsSetting.STATUS));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		} finally {
			if(c != null){
				c.close();
			}
		}
		
		if(status == 0){ 
			String[] ringPath = queryRingPath();
			
			MediaPlayer temp_player = null;
			if(ringPath[0] == null || ringPath[0].length() == 0){
				temp_player = MediaPlayer.create(this, R.raw.alarm);
			}else {
				Uri uri = Uri.parse(ringPath[0] + ringPath[1]);
				temp_player = MediaPlayer.create(this, uri);
			}
			final MediaPlayer player = temp_player;
			player.start();
			ContentValues values = new ContentValues();
			values.put(SmsSetting.STATUS, 1);
			db.update(SmsSetting.TABLE_NAME, values, null, null);//设置正在播放
			player.setLooping(true);//设置循环播放
			stopButton = (Button)findViewById(R.id.mediaButton);
			stopButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(player.isPlaying()){//如果歌曲正在播放，则停止
						player.stop();
						ContentValues values = new ContentValues();
						values.put(SmsSetting.STATUS, 0);
						db.update(SmsSetting.TABLE_NAME, values, null, null);//设置正在播放
					}
					MediaActivity.this.finish();
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.addCategory(Intent.CATEGORY_HOME);
					startActivity(i);
				}
			});
			
			player.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					player.release();
				}
			});
		}else {//当前没有歌曲在播放,只显示页面
			stopButton = (Button)findViewById(R.id.mediaButton);
			stopButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MediaActivity.this.finish();
				}
			});
			
		}
	}
	
	/**
	 * add since v2.4.4.3
	 * @return
	 */
	private String[] queryRingPath(){
		Cursor c = null;
		String[] ringPath = new String[2];
		try{
			c = db.query(SmsSetting.TABLE_NAME, new String[]{SmsSetting.RING_PATH, SmsSetting.RING_NAME}, null, null, null, null, null);
			if(c.moveToFirst()){
				ringPath[0] = c.getString(c.getColumnIndex(SmsSetting.RING_PATH));
				ringPath[1] = c.getString(c.getColumnIndex(SmsSetting.RING_NAME));
			}
		}finally {
			if(c != null) c.close();
		}
		return ringPath ;
	}
}
