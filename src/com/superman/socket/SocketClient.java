package com.superman.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
	
    public static String sendToServer(String content) {
    	String reply="";
        try {
            //1.建立客户端socket连接，指定服务器位置及端口
            Socket socket =new Socket("115.28.93.210",5678);
            //2.得到socket读写流
            OutputStream os=socket.getOutputStream();
            PrintWriter pw=new PrintWriter(os);
            //输入流
            InputStream is=socket.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            //3.利用流按照一定的操作，对socket进行读写操作
            pw.write(content);
            pw.flush();
            socket.shutdownOutput();
            //接收服务器的相应
            while(!((reply=br.readLine())==null)){
            	return reply;
            }
            //4.关闭资源
            br.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "无法获取返回数据";
    }
}