/**
 * 
 */
package com.superman.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;

/**
 * <p>Title: com.superman.util.LogUtil.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-4-17 上午7:17:51
 */

public class LogUtil {
	
	private static String savePath ;
	
	public static void saveLog(String error_log){
		String logPath = SmsAlarmDao.findDefaultLogPath();
		OutputStream os = null;
		BufferedOutputStream bos = null;
		try {
			File savePathFile = new File(logPath);
			if(!savePathFile.exists()){
				savePathFile.mkdir();
			}
			savePath = logPath + SmsAlarmDao.getCurrentTruncTime("yyyyMMdd") + ".log";
			File file = new File(savePath);
			String content = readString() + error_log + "\n";
			os = new FileOutputStream(file);
			bos = new BufferedOutputStream(os);
			bos.write(content.getBytes("UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bos != null){
					bos.close();
				}
				if(os != null){
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String readString(){
		StringBuilder buffer = new StringBuilder();
		try {
			File file = new File(savePath);
			if(!file.exists()) return "";
			InputStream is = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			String line = new String();
			while((line = reader.readLine()) != null ){
				buffer.append(line + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
}
