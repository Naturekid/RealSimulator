package com.realsimulator.Util;

import java.io.BufferedReader;
import java.io.FileReader;

import com.realsimulator.Main.NodeInfo;

import android.location.Location;
import android.util.Log;

/**
 * 
 * @author Cai
 * 此类用于帮助解析配置文件的各行信息，各列信息
 * 主要为了与工具生成的配置文件信息兼容
 * 工具生成的配置文件包含信息：时间，纬度，经度，速度，角度（当节点无位移时，速度和角度均为nam）
 * 在本项目中添加了节点编号信息，为第六位
 * 在本项目中速度和角度没有用处，直接设置为0
 * 
 */

public class ParseConfigFile {
	public long gotTime = -1;
	public double latitude = 0;
	public double longitude = 0;
	public double speed = 0;
	public double angle = 0;
	public int nodeNo; 
		
		public ParseConfigFile()
		{
			
		}
		
		public ParseConfigFile(long gotTime,double latitude,double longitude,int nodeNo)
		{
			this.gotTime = gotTime;
			this.latitude = latitude;
			this.longitude = longitude;
			this.nodeNo = nodeNo;
			this.speed = 0;
			this.angle = 0;
		}
		
		public ParseConfigFile(ParseConfigFile pcf)
		{
			this.gotTime = pcf.gotTime;
			this.latitude = pcf.latitude;
			this.longitude = pcf.longitude;
			this.nodeNo = pcf.nodeNo;
			this.speed = pcf.speed;
			this.angle = pcf.angle;
		}
		
		public ParseConfigFile(String line)
		{
			
			String[] strArray = line.split(" ");
			if(strArray.length == 6)//config中的每一个行可被分割为六个元素，否则出错
			{
				this.nodeNo = Integer.parseInt(strArray[0], 10);
				double dTime = Double.parseDouble(strArray[1]);
				this.gotTime = (long)dTime*1000;
				//this.gotTime = Long.parseLong(strArray[0], 10);
				this.latitude = Double.parseDouble(strArray[2]);
				this.longitude = Double.parseDouble(strArray[3]);
				if(strArray[4].equals("nan"))//表示无速度 
					this.speed = 0;
				else
					this.speed = Double.parseDouble(strArray[4]);
				if(strArray[5].equals("nan"))//表示无速度 
					this.angle = 0;
				else
					this.angle = Double.parseDouble(strArray[5]);
				/*
				//配置文件中的第一个元素为double型，转成Long型
				double dTime = Double.parseDouble(strArray[0]);
				this.gotTime = (long)dTime*1000;
				//this.gotTime = Long.parseLong(strArray[0], 10);
				this.latitude = Double.parseDouble(strArray[1]);
				this.longitude = Double.parseDouble(strArray[2]);
				if(strArray[3].equals("nan"))//表示无速度 
					this.speed = 0;
				else
					this.speed = Double.parseDouble(strArray[3]);
				if(strArray[4].equals("nan"))//表示无速度 
					this.angle = 0;
				else
					this.angle = Double.parseDouble(strArray[4]);
				this.nodeNo = Integer.parseInt(strArray[5], 10);
				*/
			}
			else
			{
				Log.e("RS Error", "ParseConfigFile Error,End of File");
			}
		}
		
		public void copy(ParseConfigFile pcf)
		{
			this.gotTime = pcf.gotTime;
			this.latitude = pcf.latitude;
			this.longitude = pcf.longitude;
			this.nodeNo = pcf.nodeNo;
			this.speed = pcf.speed;
			this.angle = pcf.angle;
		}
		

		
		
}
