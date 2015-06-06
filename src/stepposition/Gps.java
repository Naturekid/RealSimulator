package stepposition;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * 定位的代码
 * 实际情况中可以采取线程来周期访问获取地理位置
 * @author TheDevilKing
 *
 */
public class Gps 
{
	Context context=null;
	LocationManager locationmanager=null;
	Location nowlocation=null;
	
//	boolean gpsalive=false;
	
	public Gps(Context context)
	{
		this.context=context;
		locationmanager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
		LocationListener locationListener=new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				/*switch(status)
				{
				case LocationProvider.AVAILABLE:
					gpsalive=true;
					break;
					
				case LocationProvider.OUT_OF_SERVICE:
					gpsalive=false;
					break;
					
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					gpsalive=false;
					break;
				}*/
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				nowlocation=locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
//				gpsalive=false;
			}
			
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				nowlocation=location;
				nowlocation.setTime(System.currentTimeMillis());
			}
		};
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
	}
	
	//获取location类
	public Location getlocation()
	{
//		Location location= locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		return nowlocation;
	}
	
	/*public boolean getGpsAlive()
	{
		return gpsalive;
	}*/
	
	/**
	 * 这几部分代码是在类的外面使用的，获取了location后可以通过下面的三种方式获取到他们的经纬度以及当时的时间
	 */
	/*//获取经度
	public double getLongitude()
	{
		return location.getLongitude();
	}
	
	//获取纬度
	public double getLatitude()
	{
		return location.getLatitude();
	}
	
	//获取定位时间
	public long getTime()
	{
		return location.getTime();
	}*/
}
