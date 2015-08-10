package com.realsimulator.Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * 
 * @author Cai
 * 节点位置信息，包括节点获取位置的模式，节点编号，系统开始时间和结束时间，通信距离（后三个待定）
 * 
 */

public class NodeInfo {
	
	public
		static final int GPS_Mode = 1000;
		static final int Config_Mode = 1001;
		
		//节点属性
	public int Mode;
	public int nodeNo;//节点编号
	public double defualtLatitude;//默认纬度
	public double defualtLongitude;//默认纬度
	public long startTime;//以msec为单位。system.currentTimeMillis
	public long endTime;//结束时间
	public double com_Dis;//单位m
	//节点属性文件
	private String nodeInfoPath = new String("/sdcard/RealSimulator/nodeInfo.txt");
		
		//单例模式
		private static NodeInfo instance=null;
		private static final String TAG = "NodeInfo";
		
		/**
		 * 单例
		 */
		public static NodeInfo getInstance() {
			if (instance == null)
				instance = new NodeInfo();
			return instance;
		}
		
		/*
		 * 测试用构造函数，默认构造函数
		 */
		/*
		public NodeInfo()
		{
			nodeNo = 22;
			startTime = System.currentTimeMillis();
			endTime = startTime + 900000;//假设运行15分钟
			com_Dis = 20;//假设接受范围为20m
			Log.i("NodeInfo Constructor",Integer.toString(nodeNo));
		}
		*/
		
		public NodeInfo()
		{
			FileReader fr  = null; 
			BufferedReader buf =null;
			//构造一个FileReader和BUfferedReader的对象
			try {
				fr = new FileReader(nodeInfoPath);
				buf = new BufferedReader(fr);
				
				String curLine = buf.readLine(); 
				while(curLine!=null)
				{
					String attrArray[] = curLine.split(":");
					if(attrArray[0].equals("nodeNo"))
					{
						this.nodeNo = Integer.parseInt(attrArray[1]);
					}
					else if(attrArray[0].equals("com_Dis"))
					{
						this.com_Dis = Double.parseDouble(attrArray[1]);
					}
					else if(attrArray[0].equals("Mode"))
					{
						if(attrArray[1].equals("Config_Mode"))
							this.Mode = NodeInfo.Config_Mode;
						else if(attrArray[1].equals("GPS_Mode"))
							this.Mode = NodeInfo.GPS_Mode;
						else
							this.Mode = 0;
					}
					else if(attrArray[0].equals("defualtLatitude"))
					{
						this.defualtLatitude = Double.parseDouble(attrArray[1]);
					}
					else if(attrArray[0].equals("defualtLongitude"))
					{
						this.defualtLongitude = Double.parseDouble(attrArray[1]);
					}
					else if(attrArray[0].equals("startTime"))
					{
						this.startTime = Long.parseLong(attrArray[1]);
					}
					else if(attrArray[0].equals("endTime"))
					{
						this.endTime = Long.parseLong(attrArray[1]);
					}
					curLine = buf.readLine();
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			//最后关闭对象连接
			finally
			{
				 try { 
				      buf.close(); 
				      fr.close(); 
				    } 
				    catch (IOException ex2) { 
				      ex2.printStackTrace(); 
				    } 
			}
			
			
			
		}
		
		void setMode(int Mode)
		{
			this.Mode = Mode;
		}
		
		void setNode_No(int node_No)
		{
			this.nodeNo = node_No;
		}
		
		void setStartTime(long startTime)
		{
			this.startTime = startTime;
		}
		
		void endTime(long endTime)
		{
			this.endTime = endTime;
		}
		
		void setCom_Dis(double dis)
		{
			this.com_Dis = dis;
		}
		
	

	
	/*
	private void start() {
		thread_ = new Thread(this);
		thread_.start();

	}*/

}






