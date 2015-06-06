package com.realsimulator.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

import com.realsimulator.Main.InteractorThread;

public class testGetLocation implements Runnable{

	private static testGetLocation instance = null;
	private DatagramSocket send_loc_query = null;
	private Thread thread;
	
	/**
	 * 单例模式
	 */
	public static testGetLocation getInstace() {
		if (instance == null)
			instance = new testGetLocation();
		return instance;
	}
	
	private testGetLocation()
	{
		try {
			send_loc_query = new DatagramSocket(InteractorThread.QUERY_LOCATION_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init() {
		start();
	}
	
	/**
	 * 线程开启
	 */
	private void start() {
		thread = new Thread(this);
		thread.run();

	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		String str = new String("try to get location");
		byte[] buf = str.getBytes();
		
		while(true)
		{
			DatagramPacket packet =  null;
			try {
				packet = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(InteractorThread.host),InteractorThread.QUERY_LOCATION_PORT);
				
				send_loc_query.send(packet);
				
				Log.i("testSendQuery", "try to get location");
				Log.i("testSendQuery", "try to get location");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	/*
	private void start() {
		thread = new Thread(this);
		thread.start();

	}
	*/
	

}
