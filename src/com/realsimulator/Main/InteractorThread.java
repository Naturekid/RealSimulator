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
			//android.os.Process.killProcess(android.os.Process.myPid());   //获取PID 
			//System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
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
		byte[] recvBuf = new byte[256];
		//byte[] sendBuf = new byte[20];//纬度：经度：
		byte[] buf =new byte[256];//
		
		//DatagramPacket recvPacket = new DatagramPacket(recvBuf,recvBuf.length);
		//DatagramPacket sendPacket = null;
		
		while(true) 
		{
			try{

				DatagramPacket recvPacket = new DatagramPacket(recvBuf,recvBuf.length);
				//用以接收请求
				//DatagramPacket testpacket = new DatagramPacket(buf, buf.length,InetAddress.getByName(host),InteractorThread.REPLY_LOCATION_PORT);
				//reply_loc_socket.send(testpacket);
				
				query_loc_socket.receive(recvPacket);
		
				//String recv_str = new String(recvBuf.toString());
				
				
				//long curTime = System.currentTimeMillis();//获得接到位置请求时的系统时间
				
				//提供统一的获取位置接口
				rsLocation rsLoc = new rsLocation().getInstance();
				Location location = rsLoc.getRsLocation();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				
				//debug
				String msg = new String(Double.toString(latitude));
				msg += "	" + Double.toString(longitude);
				Log.i("经纬度", msg);
				
				//如果是节点的位置请求，直接返回
				//如果是关于邻居的位置请求，则获取邻居位置并计算距离，判断是否在通信范围内
				byte[] srcip = new byte[5];
				byte[] neigh_lat = new byte[5];
				byte[] neigh_lng = new byte[5];
				byte[] dis = new byte[5];
	
				if(ByteHelper.endian_test())//big endian
				{
					for (int k = 0; k < 4; k++) {
						srcip[k] = recvBuf[k];
						
						neigh_lat[k] = recvBuf[k + 4];
						neigh_lng[k] = recvBuf[k + 8];
						dis[k] = recvBuf[k +12];
					}
				}
				else {
					
					for (int k = 0; k < 4; k++) {
						srcip[k] = recvBuf[k];
					
						neigh_lat[3-k] = recvBuf[k + 4];
						neigh_lng[3-k] = recvBuf[k + 8];
						dis[3-k] = recvBuf[k +12];
					}
				}
				

				
				int neigh_lat_int = ByteHelper.byte_array_to_int(neigh_lat);
				int neigh_lng_int = ByteHelper.byte_array_to_int(neigh_lng);
				if( (neigh_lat_int == 0) && (neigh_lng_int==0) )//节点位置请求
				{
					System.out.println("LOCAL LOCATION QUERY");
					//要不要给dis置0
				}
				else
				{
					double neigh_lat_double = neigh_lat_int * 1.0 / Math.pow(10, accuracy);
					double neigh_lng_double = neigh_lng_int * 1.0 / Math.pow(10, accuracy);
					
					double distance = Distance.getDistance(latitude, longitude, neigh_lat_double, neigh_lng_double);
					dis = ByteHelper.int_to_byte_array((int)distance);
					/*不好，还是得让aodv自己处理
					if(!Distance.canConnected(distance))//直接丢弃吧
					{
						i_dst=0;
						dstip=ByteHelper.int_to_byte_array(i_dst);
					}
					*/
					
					
					if(Distance.canConnected(distance))//距离为m，转为int型，再转byte存入dis
					{
						
						System.out.println(dis);
						System.out.println(ByteHelper.byte_array_to_int(dis));
					}
					else//debug
					{
						System.out.println("Cannot communicate with that one!");
					}
					
					
				}
				
				//将经纬度转换成int类型便于在上下层交换,纬度为y，经度为x
				int x = (int)(longitude * Math.pow(10, accuracy));
				int y = (int)(latitude * Math.pow(10, accuracy));
				//debug
				msg = Integer.toString(x);
				msg += "		" + Integer.toString(y);
				//Log.i("int经纬度", msg);
				
				//将int型的数据转换成byte型数组
				byte[] xb = ByteHelper.int_to_byte_array(x);
				byte[] yb = ByteHelper.int_to_byte_array(y);
				//将接收到的数据复制到待发送数据区buf
				System.arraycopy(recvBuf, 0, buf, 0, recvPacket.getLength());
				if(ByteHelper.endian_test())
				{
					for(int i = 0; i < 4; i++){
						buf[i+4] = yb[i];//纬度
						buf[i+8] = xb[i];//经度
						buf[i+12] = dis[i];//距离
					
					}
				}
				else
				{
					for(int i = 0; i < 4; i++){
						buf[i+4] = yb[3-i];//纬度
						buf[i+8] = xb[3-i];//经度
						buf[i+12] = dis[3-i];//距离
					
					}
				}
				//debug
//				Log.i("recvBuf",recvBuf.toString());
//				Log.i("buf",buf.toString());
				
				DatagramPacket packet = new DatagramPacket(buf, buf.length,InetAddress.getByName(host),InteractorThread.REPLY_LOCATION_PORT);
				//DatagramPacket packet = new DatagramPacket(buf, buf.length,InetAddress.getByName(host),recvPacket.getPort());
				
				String send_str = new String(buf.toString());
				
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






