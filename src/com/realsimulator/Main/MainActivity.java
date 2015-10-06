package com.realsimulator.Main;

import stepposition.FullLocation;
import stepposition.GpsPosition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.realsimulator.ConfigLocation.ConfigLocation;
import com.realsimulator.Util.ParseConfigFile;

public class MainActivity extends Activity {
	
	private RadioGroup groupMode;
    private RadioButton radioGPS , radioConfig;
    private NodeInfo node;
    
  //本进程Contex
	Context thisContext = this;
	
	GpsPosition gpsposition=null;
    
	//表示主activity是否还活着
	public static boolean alive=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		System.out.println("testSimulator: onCreate");
		alive=true;
		
		//检测GPS是否打开，添加跳转到打开GPS设置的判断
/*		LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		if(locationManager==null)
		{
			System.out.println("locationManager is null");
			Log.e("com.realsimulator.Main","获取到的LocationManager为空");
		}
		else
		{
			try
			{
				boolean re=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				if(!re)
				{
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		            dialog.setMessage("请打开GPS");
		            dialog.setPositiveButton("确定",
		                    new android.content.DialogInterface.OnClickListener() {
		 
		                        @Override
		                        public void onClick(DialogInterface arg0, int arg1) {
		 
		                            // 转到手机设置界面，用户设置GPS
		                            Intent intent = new Intent(
		                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		                            startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
		 
		                        }
		                    });
		            dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {
		                 
		                @Override
		                public void onClick(DialogInterface arg0, int arg1) {
		                    arg0.dismiss();
		                }
		            } );
		            dialog.show();
				}
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		*/
		node = NodeInfo.getInstance();
		//node.setMode(NodeInfo.GPS_Mode);
		
		Handler gpsHandler=new Handler(thisContext.getMainLooper())
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
					test = "GPS未获得首次定位！"+System.currentTimeMillis();
					
					tv.setText(test);
				super.handleMessage(msg);
			}
			
		};
		
		
		//默认启动GPS
//		gpsposition=new GpsPosition(thisContext, gpsHandler);//普通方式

//		gpsposition=new GpsPosition(thisContext, gpsHandler,node.defualtLongitude,node.defualtLatitude);//有默认初始值的方式
//		gpsposition.start();
		
		if(node.Mode==NodeInfo.GPS_Mode)//已启动
		{
			System.out.println("GPS MODE");
		}
		else if(node.Mode==NodeInfo.Config_Mode)
		{
			//确定首个配置位置
			TextView tv = (TextView)findViewById(R.id.testText);
			ConfigLocation configLoc = ConfigLocation.getInstance();
			ParseConfigFile pcf = configLoc.getFirstLocation();
			
			String test = new String();
			test ="首次定位  Mode:Config  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
					+ Double.toString(pcf.latitude) + Double.toString(pcf.longitude);
			tv.setText(test);
		}
		else
		{
			try {
		        throw new Exception();
	        } catch (Exception e) {  
		        Log.e("Mode Error", "Exception: 错误的启动模式", e); 
		    } 
		}
		
		//启动监听位置请求的线程
        InteractorThread queryListener = InteractorThread.getInstace();
        queryListener.init();
		
        //用不了。不能使用同一接口
		//启动测试定期请求的测试线程
        //testGetLocation testLoc = testGetLocation.getInstace();
        //testLoc.init();
        
        //测试配置模式
        /*
        node.setMode(NodeInfo.Config_Mode);
        
        ConfigLocation configLoc = ConfigLocation.getInstance();
		
		//测试，正式运行可删除
		TextView tv = (TextView)findViewById(R.id.testText);
		
		//删除GPS定位
		//gpsHandler.removeCallbacks(handlerThread);
		
		//暂停gps定位模式
		gpsposition.setPause();
		//gpsposition.myWait();
		//gpsposition.mySleep();
		//gpsposition.setDead();
		
		
		
		//确定首个配置位置
		ParseConfigFile pcf = configLoc.getFirstLocation();
		
		String test = new String();
		test ="首次定位  Mode:Config  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
				+ Double.toString(pcf.latitude) + Double.toString(pcf.longitude);
		tv.setText(test);
        */
		
		
		
		
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        //根据ID找到RadioGroup实例
        groupMode = (RadioGroup)this.findViewById(R.id.location_mode);
        radioGPS = (RadioButton)findViewById(R.id.mode_GPS);
        radioConfig = (RadioButton)findViewById(R.id.mode_Config);
        if(node.Mode==NodeInfo.Config_Mode)
        	radioConfig.setChecked(true);
        else radioGPS.setChecked(true);
        //声明监听器
        groupMode.setOnCheckedChangeListener(modeChangeListener);
            
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		//关闭文件或者gps定位
		//gpsposition.close();
		alive=false;
		System.out.println("testSimulator: MainAcitivity onDestroy");
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
				
				
//				gpsposition=new GpsPosition(thisContext, gpsHandler);//普通方式
//				gpsposition=new GpsPosition(this, handler,0,0);//有默认初始值的方式
//				gpsposition.start();
				
				
				//重启gps定位模式
				//gpsposition.wakeUp();
				
				String test = new String();
				//进行GPS的首次定位
				
				try{
					
				FullLocation flocation=gpsposition.getLocation();
				if(flocation!=null)
				{
					Location location=flocation.getLocation();
					
					test = "Mode:GPS  NodeNo:" + node.nodeNo + "  纬度，经度 ：" 
							+ Double.toString(location.getLatitude()) 
							+ " " + Double.toString(location.getLongitude());
				}
				else 
					test = "GPS未获得首次定位！"+System.currentTimeMillis();
				
				tv.setText(test);
				}
				finally
				{
					System.out.println("End First GPS Location");
				}
				
				
				
				
			}
			else if(checkedId == radioConfig.getId()){
				
				node.setMode(NodeInfo.Config_Mode);
				
				
				
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
