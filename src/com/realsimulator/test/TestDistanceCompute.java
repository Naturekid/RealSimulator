package com.realsimulator.test;

import junit.framework.TestCase;
import android.util.Log;

import com.realsimulator.Util.Distance;

/** 
 * @author wwtao thedevilking@qq.com: 
 * @version 创建时间：2015-8-10 上午11:42:18 
 * 说明 
 */
public class TestDistanceCompute extends TestCase
{
	public void testDistanceCompute()	
	{
		//测试距离(按照真实距离计算)
		double x1=39.753866d;
		double y1=116.095979d;
		
		double x2=39.752109d;
		double y2=116.100721d;
		
		double dis=Distance.getDistance(x1, y1, x2, y2);
		System.out.println(String.format("dis:%f", dis));
		Log.e("test",String.valueOf(dis));
		assertEquals((int)(dis), 450);
		
		//测试距离(错误例子)
		x1=39.752109d;
		y1=116.100721d;
		
		x2=39.753866d;
		y2=116.095979d;
		dis=Distance.getDistance(x1, y1, x2, y2);
		System.out.println(String.format("dis:%f", dis));
		Log.e("test",String.valueOf(dis));
		assertEquals((int)(dis), 450);
	}
}
