package com.realsimulator.Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.PriorityBlockingQueue;

import android.location.Location;
import android.util.Log;

import com.realsimulator.Util.*;

/**
 * @author Cai
 * 位置交互接口
 * 接收位置请求，反馈位置信息
 * 经纬度的表示为xxx.xxxxxx，精确到小数点后6位
 * 表示
 * 在 启动
 */


public class InteractorThread implements Runnable {
	
	public static final int QUERY_LOCATION_PORT = 10003;
	public static final int REPLY_LOCATION_PORT = 10004;
	public static final int accuracy = 6;
	public static final String host = "127.0.0.1";
	
	//private DatagramSocket sock_interact_;
	
	private Thread thread;
	private static InteractorThread instance = null;
	private DatagramSocket query_loc_socket = null;
	private DatagramSocket reply_loc_socket = null;
	
	private static final String TAG = "InteractorThread";
	
	public void init() {
		start();
	}
	
	
	/**
	 * private构造函数
	 */
	private InteractorThread() {
		try {
			query_loc_socket = new DatagramSocket(InteractorThread.QUERY_LOCATION_PORT);
			reply_loc_socket = new DatagramSocket(InteractorThread.REPLY_LOCATION_PORT);
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	};
	

	/**
	 * 单例模式
	 */
	public static InteractorThread getInstace() {
		if (instance == null)
			instance = new InteractorThread();
		return instance;
	}

	/**
	 * 线程开启
	 */
	private void start() {
		thread = new Thread(this);
		thread.start();

	}
	
	/*
	private boolean check_dup(byte[] b1, byte[] b2){
		for(int i = 0; i < b1.length; i++) {
			if (b1[i] == b2[i]) continue;
			else return false;
		}
		return true;//duplication
	}*/
	
	
	@Override
	public void run() {
		byte[] recvBuf = new byte[16];
		//byte[] sendBuf = new byte[20];//纬度：经度：
		byte[] buf =new byte[8];//
		
		DatagramPacket recvPacket = new DatagramPacket(recvBuf,recvBuf.length);
		//DatagramPacket sendPacket = null;
		
		while(true) 
		{
			try{
				
				//用以接收请求
				query_loc_socket.receive(recvPacket);
				
				System.out.println(recvPacket.toString());
				
				//测试。定时处理请求
				//thread.sleep(2000);
				
				//long curTime = System.currentTimeMillis();//获得接到位置请求时的系统时间
				
				//提供统一的获取位置接口
				rsLocation rsLoc = new rsLocation().getInstance();
				Location location = rsLoc.getRsLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				//debug
				String msg = new String(Double.toString(latitude));
				msg += "	" + Double.toString(longitude);
				//Log.i("经纬度", msg);
				
				//将经纬度转换成int类型便于在上下层交换,纬度为y，经度为x
				int x = (int)(longitude * Math.pow(10, accuracy));
				int y = (int)(latitude * Math.pow(10, accuracy));
				//debug
				msg = Integer.toString(x);
				msg = "		" + Integer.toString(y);
				//Log.i("int经纬度", msg);
				
				//将int型的数据转换成byte型数组
				byte[] xb = ByteHelper.int_to_byte_array(x);
				byte[] yb = ByteHelper.int_to_byte_array(y);
				for(int i = 0; i < 4; i++){
					buf[i] = xb[i];
					buf[i+4] = yb[i];
				}
				//debug
				//Log.i("Byte经纬度", buf.toString());
				
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(host),InteractorThread.REPLY_LOCATION_PORT);
				reply_loc_socket.send(packet);
				
				/*
				double longitude = Location.getInstance().getLongitude();
				double latitude  = Location.getInstance().getLatitude();
				int x = (int)(longitude * Math.pow(10, accuracy));
				int y = (int)(latitude * Math.pow(10, accuracy));
				String host = "127.0.0.1";
				byte[] xb = ByteHelper.int_to_byte_array(x);
				byte[] yb = ByteHelper.int_to_byte_array(y);
				for(int i = 0; i < 4; i++){
					buf[i+8] = xb[i];
					buf[i+12] = yb[i];
				}
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(host),Netw_layerInteractor.AODV_LOCATION_PORT);
				sock_loc_reply.send(packet);
				*/
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
	}
	
	
}






