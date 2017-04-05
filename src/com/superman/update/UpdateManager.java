package com.superman.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.superman.smsalarm.R;
import com.superman.smsalarm.backup.Backup;
import com.superman.smsalarm.setting.SettingActivity;
import com.superman.smsalarm.setting.SettingActivity.MyHandler;
import com.superman.util.LogUtil;
import com.superman.util.SmsAlarmDao;
import com.superman.util.SQLite.SmsSetting;

/**
 * 
 * <p>
 * Title: com.superman.update.UpdateManager.java
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2001-2013 Newland SoftWare Company
 * </p>
 * 
 * <p>
 * Company: Newland SoftWare Company
 * </p>
 * 
 * @author Lewis.Lynn
 * 
 * @version 1.0 CreateTime：2014-3-17 下午3:08:25
 */
public class UpdateManager {
	//开始下载
	private static final int START_DOWNLOAD = 0;
	/* 下载中 */
	private static final int DOWNLOADING = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	
	//最新版
	private static final int NEWEST = 3;
	
	/* 保存解析的XML信息 */
	HashMap<String, String> mHashMap;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;
	
	private ProgressDialog progressBar;
	
	private boolean isUpdate;
	
	private int versionUpdate = 0;//version descript
	
	private String version;
	
	private String content ;//update description

	private MyHandler mHandler;
	
	private SQLiteDatabase db;

	public UpdateManager(Context context, boolean isUpdate, SQLiteDatabase db) {
		this.mContext = context;
		mHandler = new MyHandler();
		this.isUpdate = isUpdate;
		this.db = db;
	}

	/**
	 * 检查软件是否有更新版本
	 * 
	 * @return
	 */
	public boolean checkUpdate() {
		
		if(!SmsAlarmDao.isNetworkAvailable(this.mContext)){
			if(isUpdate){
				Toast.makeText(mContext, "无法连接到网络......", Toast.LENGTH_SHORT).show();
			}
			return false;
		}
		
		if(isUpdate){
			progressBar = new ProgressDialog(mContext);
			progressBar.setCancelable(true);
			progressBar.setMessage("检查新版本");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.show();
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 0;
				
				// 获取当前软件版本
				int versionCode = getVersionCode(mContext);
				// 把version.xml放到网络上，然后获取文件信息
				InputStream inStream = null;
				try {
					URL url = new URL("http://115.28.93.210/apk/version.xml");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					//设置超时时间
					conn.setConnectTimeout(6*1000);
					//对响应码进行判断
					if (conn.getResponseCode() != 200)    //从Internet获取网页,发送请求,将网页以流的形式读回来
						throw new RuntimeException("请求url失败");
					conn.connect();
					inStream = conn.getInputStream();
					// InputStream inStream =
					// ParseXmlService.class.getClassLoader().getResourceAsStream("http://localhost:8099/docs/version.xml");
					// 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
					ParseXmlService service = new ParseXmlService();
					try {
						mHashMap = service.parseXml(inStream);
					} catch (Exception e) {
						e.printStackTrace();
						LogUtil.saveLog(e.toString());
					}
					inStream.close();
					conn.disconnect();
				} catch (MalformedURLException e1) {
					Log.e("isUpdate", e1.toString());
					LogUtil.saveLog(e1.toString());
				} catch (IOException e1) {
					Log.e("isUpdate", e1.toString());
					LogUtil.saveLog(e1.toString());
				} catch (Exception e){
					e.printStackTrace();
				}
				
				if (null != mHashMap) {
					int serviceCode = Integer.valueOf(mHashMap.get("version"));
					content = mHashMap.get("content");
					Log.d("com.superman.update.UpdateManager.isUpdate()", "service code is " + serviceCode + ", current version code is " + versionCode);
					// 版本判断
					if(serviceCode > versionCode){
						if(isUpdate){
							progressBar.dismiss();
						}
						versionUpdate = 1;
						mHandler.sendEmptyMessage(START_DOWNLOAD);
					}else {
						if(isUpdate){
							progressBar.dismiss();
						}
						mHandler.sendEmptyMessage(NEWEST);
					}
				}
			}
		}).start();
		
		
		return false;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			PackageInfo info = context.getPackageManager().getPackageInfo(
					"com.superman.smsalarm", 0);
			versionCode = info.versionCode;
			version = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			LogUtil.saveLog(e.toString());
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		String[] descript = content.split("###");
		StringBuilder buff = new StringBuilder();
		if(descript.length > 0){
			for (int i = 0; i < descript.length; i++) {
				buff.append(descript[i] + "\n");
			}
		}
		builder.setMessage(buff.toString());
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 显示下载对话框
						showDownloadDialog();
					}
				});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.layout_dialog_softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 设置取消状态
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "download";
					URL url = new URL(mHashMap.get("url"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, mHashMap.get("name"));
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOADING);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				LogUtil.saveLog(e.toString());
			} catch (IOException e) {
				String ex = e.toString();
				System.out.println(ex);
				LogUtil.saveLog(e.toString());
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, mHashMap.get("name"));
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
	
	public class MyHandler extends Handler{
		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_DOWNLOAD:
				ContentValues values = new ContentValues();
				values.put(SmsSetting.IS_UPDATE, versionUpdate);
				db.update(SmsSetting.TABLE_NAME, values, null, null);
//				if(isUpdate){
					//备份数据 add since v2.4.6.8
//					Backup backup = new Backup(mContext, db);
//					backup.upgradeBackup();
					
					showNoticeDialog();
//				}
				break;
			// 正在下载
			case DOWNLOADING:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				
				// 安装文件
				installApk();
				break;
			case NEWEST:
				ContentValues values2 = new ContentValues();
				values2.put(SmsSetting.IS_UPDATE, versionUpdate);
				db.update(SmsSetting.TABLE_NAME, values2, null, null);
				if(isUpdate){
					Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
		}
	}
}
