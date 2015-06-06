package com.realsimulator.Main;

import stepposition.FullLocation;
import stepposition.GpsPosition;

import com.realsimulation.ConfigLocation.ConfigLocation;
import com.realsimulator.Util.ParseConfigFile;
import com.realsimulator.Util.testGetLocation;
 
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private RadioGroup groupMode;
    private RadioButton radioGPS , radioConfig;
    
  //本进程Contex
	Context thisContext = this;
	
	GpsPosition gpsposition=null;
    


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		
		Handler gpsHandler=new Handler()
		{

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				
				 String test = new String();
			     TextView tv = (TextView)findViewById(R.id.testText);
			     //进行GPS的首次定位
				
				FullLocation flocation=gpsposition.getLocation();
				if(flocation!=null)
				{
					Location location=flocation.getLocation();
						
					test = "首次定位 Mode:GPS 纬度，经度 ：" 
								+ Double.toString(location.getLatitude()) 
								+ " " + Double.toString(location.getLongitude());
				}
				else 
					test = "GPS未获得首次定位！";
					
				tv.setText(test);
				
				
				
				super.handleMessage(msg);
			}
			
		};
		
		
		
		//默认启动GPS
		gpsposition=new GpsPosition(thisContext, gpsHandler);//普通方式
//		gpsposition=new GpsPosition(this, handler,0,0);//有默认初始值的方式
		gpsposition.start();
		
		
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        //根据ID找到RadioGroup实例
        groupMode = (RadioGroup)this.findViewById(R.id.location_mode);
        radioGPS = (RadioButton)findViewById(R.id.mode_GPS);
        radioConfig = (RadioButton)findViewById(R.id.mode_Config);
        //声明监听器
        groupMode.setOnCheckedChangeListener(modeChangeListener);
        
		//启动监听位置请求的线程
       // InteractorThread queryListener = InteractorThread.getInstace();
       // queryListener.run();
        
        //启动测试定期请求的测试线程
       // testGetLocation testLoc = testGetLocation.getInstace();
       // testLoc.run();
        
        
       
		
        
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		//关闭文件或者gps定位
		//gpsposition.close();
		
		super.onDestroy();
	}
	
	private RadioGroup.OnCheckedChangeListener modeChangeListener = new RadioGroup.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup arg0, int checkedId) {
			// TODO Auto-generated method stub
			
			//获取节点信息
			NodeInfo node = NodeInfo.getInstance();
			
			rsLocation loc = rsLocation.getInstance();
			
			ConfigLocation configLoc = ConfigLocation.getInstance();
			
			//测试，正式运行可删除
			TextView tv = (TextView)findViewById(R.id.testText);
			
			
			//选中某模式时将重新进行定位
			if(checkedId == radioGPS.getId()){
				
				node.setMode(NodeInfo.GPS_Mode);
				//关闭在配置模式中打开的连接对象——文件和读取缓冲区
				//根据ConfigLocation的eof，lastLoc和curLoc字段判别是否使用过该模式
				if(!configLoc.eof && (configLoc.lastLoc!=null) && (configLoc.curLoc!=null))
				{
					configLoc.delConfigLocation();
				}
				
				//GPS定位
				//gpsHandler.post(handlerThread);//GPS信息处理
				
				gpsposition = GpsPosition.getInstance();
				
//				gpsposition=new GpsPosition(thisContext, gpsHandler);//普通方式
//				gpsposition=new GpsPosition(this, handler,0,0);//有默认初始值的方式
//				gpsposition.start();
				
				
				//重启gps定位模式
				gpsposition.wakeUp();
				
				String test = new String();
				//进行GPS的首次定位
				
				try{
					
				FullLocation flocation=gpsposition.getLocation();
				if(flocation!=null)
				{
					Location location=flocation.getLocation();
					
					test = "首次定位 Mode:GPS  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
							+ Double.toString(location.getLatitude()) 
							+ " " + Double.toString(location.getLongitude());
				}
				else 
					test = "GPS未获得首次定位！";
				
				tv.setText(test);
				}
				finally
				{
					System.out.println("End First GPS Location");
				}
				
				
				
				
			}
			else if(checkedId == radioConfig.getId()){
				
				node.setMode(NodeInfo.Config_Mode);
				
				//删除GPS定位
				//gpsHandler.removeCallbacks(handlerThread);
				
				//暂停gps定位模式
				//gpsposition.setPause();
				//gpsposition.myWait();
				//gpsposition.destroy();
				gpsposition.setDead();
				
				
				//确定首个配置位置
				ParseConfigFile pcf = configLoc.getFirstLocation();
				
				String test = new String();
				test ="首次定位  Mode:Config  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
						+ Double.toString(pcf.latitude) + Double.toString(pcf.longitude);
				tv.setText(test);
				
				
			}
			
		}
	};
	
	
	
	/*
	Handler gpsHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			
			//how
			gpsHandler.post(handlerThread);
			
		}
	};
	
	Runnable handlerThread = new Runnable()
	{

		//获取节点信息
		NodeInfo node = NodeInfo.getInstance();
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i("Handler", "GPS Handler Thread!");
			
			Message msg = gpsHandler.obtainMessage();
			
			 String test = new String();
		     TextView tv = (TextView)findViewById(R.id.testText);
		     //进行GPS的首次定位
			
			FullLocation flocation=gpsposition.getLocation();
			if(flocation!=null)
			{
				Location location=flocation.getLocation();
					
				test = "首次定位 Mode:GPS  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
						+ Double.toString(location.getLatitude()) 
						+ " " + Double.toString(location.getLongitude());
			}
			else 
				test = "GPS未获得首次定位！";
				
			tv.setText(test);
			
			
			
			//super.handleMessage(msg);
			gpsHandler.handleMessage(msg);
			
		}
		
	};
	*/
	

}
