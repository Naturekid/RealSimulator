package com.realsimulator.ConfigLocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;

import com.realsimulator.Main.NodeInfo;
import com.realsimulator.Util.ParseConfigFile;

/**
 * 
 * @author Cai
 * 从配置文件读取节点位置
 * 
 */
public class ConfigLocation {
	
	//private String configPath = new String("/sdcard/RealSimulation/track.txt");
	private String configPath = new String("/sdcard/RealSimulator/track.txt");
	public ParseConfigFile lastLoc = null;
	public ParseConfigFile curLoc = null;
	public boolean eof = false;
	
	///*特殊处理时间以匹配配置文件的时间
	long sub ;
	
	//要读取的配置文件及读取数据的缓存区
	FileReader fr  = null; 
	BufferedReader buf =null;
	
	//单例模式
	private static ConfigLocation instance=null;
	private static final String TAG = "ConfigLocation";
	
	public ConfigLocation()
	{
		lastLoc = new ParseConfigFile();
		curLoc = new ParseConfigFile();
	}
	
	//切记代码结束时要调用关闭连接代码，在哪调用呢？确定一下
	public void delConfigLocation()
	{
		//设置instance为null，以便在此调用时可以重新new一耳光
		instance = null;
		//要不要先判断下是否已到文件末被关闭了？
		try { 
		      buf.close(); 
		      fr.close(); 
		    } 
		    catch (IOException ex2) { 
		      ex2.printStackTrace(); 
		    } 
	}
	
	public static ConfigLocation getInstance() {
		if (instance == null)
			instance = new ConfigLocation();
		return instance;
	}
	
	public ParseConfigFile getFirstLocation()
	{
		
		
		String curLine = null;
			
		//节点编号
		NodeInfo node = NodeInfo.getInstance();
				
		/* 配置文件的第一行，为模拟器的第一个时间
		 * 若请求时间恰好为模拟器开始时间或者小于模拟器的开始时间
		 * 设置节点位置为此位置
		 */
		
		//按行读取文件内容
		try { 
			
			long queryTime = System.currentTimeMillis();
			System.out.println("query time = "+Long.toString(queryTime));
			
			//构造一个FileReader和BUfferedReader的对象
			fr = new FileReader(configPath); 
			buf = new BufferedReader(fr);
			
			curLine = buf.readLine(); 
			
			///*
			//最好能使用同步算法将其同步为配置文件的开始时间，否则需要特殊处理配置文件的时间或系统时间
			
			//*/
			
			
			
			
			while(curLine!=null) //当curLine==null时，说明已到文件末，设置节点位置为配置文件里相关的最后一个位置。 
			{
				//解析配置文件的一行数据
				ParseConfigFile pcf = new ParseConfigFile(curLine);
				//配置文件某行出错
				if(pcf.gotTime == -1){
					System.out.println("Wrong Config Line");
					curLine =buf.readLine(); 
					continue;
				}
				
				//寻找本节点的第一个位置
				if(pcf.nodeNo == node.nodeNo)
				{
					//上次位置设置为当前询问时间及第一次询问到的位置
					lastLoc.copy(pcf);
					lastLoc.gotTime = queryTime;
					curLoc.copy(pcf);
					System.out.println(curLine); 
					///*
					this.sub = queryTime - pcf.gotTime;
					System.out.print("getFirstConfigLocation");
					System.out.println(Long.toString(queryTime)+" "+Long.toString(pcf.gotTime)+" "+this.sub);
					//*/
					return curLoc;
				}
				
				curLine =buf.readLine(); 

			}
		} 
		catch (IOException ex1) { 
			 ex1.printStackTrace(); 
		}
			
		return null;
	}
	
	public ParseConfigFile getConfigLocation()
	{
		//请求时间
		long queryTime = System.currentTimeMillis();
		
		///*特殊处理配置时间
		queryTime = queryTime - this.sub;
		System.out.println("getConfigLocation CL " +Long.toString(queryTime));
		//*/
		if(this.eof)//文件已到文件末
		{
			this.curLoc.gotTime = queryTime;
			Log.i(TAG, "经纬度："+curLoc.latitude + " " + curLoc.longitude);
			return curLoc;
		}
	

		String curLine = null;
			
		//节点编号
		NodeInfo node = NodeInfo.getInstance();
			
		//从上次读取到的部分继续读取下一次的位置
		//先判断前两次位置的时间与当前请求时间的关系
		ParseConfigFile curPcf = null;
		
		if(queryTime < curLoc.gotTime)//节点未运动到上次所读取到的时间点，匀速计算位置
		{
			curPcf = calLocation(queryTime);
			System.out.print("queryTime < curLoc.gotTime");
		}
		else if(queryTime == curLoc.gotTime)//节点恰好运动到上次所读位置
		{
			curPcf.copy(curLoc);
			System.out.print("queryTime == curLoc.gotTime");
		}
		else//节点已过上次所读位置，继续读取文件以获得新位置
		{
			//curLine = buf.readLine();
			lastLoc.copy(curLoc);	
			System.out.print("queryTime > curLoc.gotTime");
			//按行读取文件内容
			try { 
				
				curLine = buf.readLine(); 
				while(curLine!=null) //当curLine==null时，说明已到文件末，设置节点位置为配置文件里相关的最后一个位置。 
				{
				      System.out.println(curLine);
				      ParseConfigFile pcf = new ParseConfigFile(curLine);
				      
				      //非本节点数据直接跳过，读取下一行
				      if(pcf.nodeNo != node.nodeNo)
				      {
				    	  curLine = buf.readLine();
				    	  continue;
				      }
				    	  
				      if(queryTime == pcf.gotTime)
				      {
				    	  curLoc.copy(pcf);
				    	  Log.i(TAG, "经纬度："+curLoc.latitude + " " + curLoc.longitude);
				  		System.out.println("in cl  " + curLoc.gotTime + "  " +curLoc.latitude + "  " + curLoc.longitude);
				    	  return curLoc;
				      }
				      else if(queryTime < pcf.gotTime)//计算位置
				      {
				    	  curLoc.copy(pcf);
				    	  curPcf = calLocation(queryTime);
				    	  //跳出读取信息循环
				    	  break;
				      }
				      else if(queryTime > pcf.gotTime)
				      {
				    	  lastLoc.copy(pcf);
				      }
				      curLine =buf.readLine(); 
				}
				if(curLine==null)
				{
					//读取到文件末，设置节点位置为配置文件里相关的最后一个位置
					this.eof = true;
					curLoc.gotTime = queryTime;
					curPcf.copy(curLoc);
					//关闭连接
					try {
						
						buf.close();
						fr.close(); 
					}
					catch (IOException ex2) {
						ex2.printStackTrace(); 
					} 
				}
				
				
			}
			catch (IOException ex1) { 
				ex1.printStackTrace(); 
			}
			//关闭对象连接
			/*
			 finally{
				
			    try { 
			      buf.close(); 
			      fr.close(); 
			    } 
			    catch (IOException ex2) { 
			      ex2.printStackTrace(); 
			    } 
			}
			*/
				
		}
		
		Log.i(TAG, "经纬度："+curPcf.latitude + " " + curPcf.longitude);
		System.out.println(curPcf.gotTime + "  " +curPcf.latitude + "  " + curPcf.longitude);
		return curPcf;
	}
	
	ParseConfigFile calLocation(long queryTime)
	{
		double ratio = (queryTime - this.lastLoc.gotTime)*1.0/(this.curLoc.gotTime - this.lastLoc.gotTime) ; 
		//按比例计算新的纬度
		double latitude = lastLoc.latitude + (curLoc.latitude - lastLoc.latitude)*ratio;
		//按比例计算新的经度
		double longitude = lastLoc.longitude + (curLoc.longitude - lastLoc.longitude)*ratio;
		
		ParseConfigFile pcf = new ParseConfigFile(queryTime,latitude,longitude,curLoc.nodeNo);
		
		return pcf;
	}

}
