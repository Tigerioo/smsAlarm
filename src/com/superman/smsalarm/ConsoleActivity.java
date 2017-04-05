package com.superman.smsalarm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.superman.util.SmsAlarmDao;

public class ConsoleActivity extends Activity {

	private SQLiteDatabase db;
	private Button keyWordButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.layout_activity_console);
		super.onCreate(savedInstanceState);

		db = SmsAlarmDao.getDbInstance(this);
		
		keyWordButton = (Button)findViewById(R.id.myButton1);
		
		keyWordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText keyEdit = (EditText)findViewById(R.id.keyEdit);
				String newKeyword = keyEdit.getText().toString();
				 //接收服务器的相应
	            StringBuilder reply = new StringBuilder();
				try {
		            //1.建立客户端socket连接，指定服务器位置及端口
		            Socket socket =new Socket("115.28.93.210",5678);
		            //2.得到socket读写流
		            OutputStream os=socket.getOutputStream();
		            PrintWriter pw=new PrintWriter(os);
		            //输入流
		            InputStream is=socket.getInputStream();
		            byte[] bt = new byte[1024];
		            int r = 0;
		            while((r = is.read(bt)) != -1){
		            	reply.append((char)r);
		            }
		            
		            //3.利用流按照一定的操作，对socket进行读写操作
		            pw.write(newKeyword);
		            pw.flush();
		            socket.shutdownOutput();
		            
//		            Toast.makeText(ConsoleActivity.this, "接收到的服务器消息：" + reply, Toast.LENGTH_SHORT).show();
		            //4.关闭资源
		            is.close();
		            pw.close();
		            os.close();
		            socket.close();
		        } catch (UnknownHostException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
				
				TextView text = (TextView)findViewById(R.id.myText);
				text.setText(reply.toString());
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

}
