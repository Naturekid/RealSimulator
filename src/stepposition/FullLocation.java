package stepposition;

import android.location.Location;

/**
 * 用来返回location和当前定位是完全GPS还是相对GPS定位
 * @author thedevilking
 *
 */
public class FullLocation 
{
	Location location=null;
	boolean gpsisvalue=true;
	
	//测试使用
//	public String s="";
	
	public FullLocation(Location location,boolean gpsisvalue)
	{
		this.location=location;
		this.gpsisvalue=gpsisvalue;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public boolean isFullGps()
	{
		return gpsisvalue;
	}
}
