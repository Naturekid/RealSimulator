package com.realsimulator.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.realsimulator.ConfigLocation.ConfigLocation;
import com.realsimulator.Util.ParseConfigFile;
import stepposition.FullLocation;
import stepposition.GpsPosition;
import android.location.Location;
import android.util.Log;

/**
 * 
 * @author Cai
 * 为不同模式（GPS/配置文件）的定位方式提供统一接口
 *
 */
public class rsLocation {
	
	/*
	public 
		double latitude;
		double longitude;
	*/
		
		public Location rsLoc;
		private static rsLocation instance = null;
		private static final String TAG = "rsLocation";
		
		public rsLocation()
		{
			
		}

		public rsLocation(double latitude,double longitude)
		{
			this.rsLoc.setAltitude(latitude);
			this.rsLoc.setLongitude(longitude);
		}
		
		
		public static rsLocation getInstance()
		{
			if(instance == null)
				instance = new rsLocation();
			return instance;	
		}
		
		/*
		double getLatitude()
		{
			return latitude;
			
		}
		
		double getLongitude()
		{
			return longitude;
		}
		*/
		
		Location getRsLocation()
		{
			//根据模式选择获取位置的方式
			rsLoc = null;
			NodeInfo node= NodeInfo.getInstance();
			switch(node.Mode)
			{
			case NodeInfo.Config_Mode:
				rsLoc = this.getConfigLocation();
				break;
			case NodeInfo.GPS_Mode:
				rsLoc = this.getGpsLocation();
				break;
			default :
				System.out.println("Error Mode!");
			}
			return rsLoc;
		}
		
		private Location getGpsLocation()
		{
			GpsPosition gpsposition = GpsPosition.getInstance();//null;
			FullLocation flocation=gpsposition.getLocation();
			Location location = null;
			
			if(flocation != null)
			{
				location=flocation.getLocation();
			}
			
			if(location != null)
			{
				String str = new String("纬度：");
				str += location.getLatitude();
				str += "	经度：";
				str += location.getLongitude();
				Log.i("GpsLocation:", str);
				System.out.println(str);
			}
			return location;
		}
		
		private Location getConfigLocation()
		{
			//启动时记得先获取第一个位置
			Location location = new Location("");
			ConfigLocation configLocation = ConfigLocation.getInstance();
			ParseConfigFile pcf = configLocation.getConfigLocation();
			//测试一下
			location.setLatitude(pcf.latitude);
			location.setLongitude(pcf.longitude);
			System.out.println(location.getLatitude());
			return location;
		}
		
		

}
