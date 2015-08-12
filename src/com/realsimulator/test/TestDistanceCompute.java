package com.realsimulator.test;

import com.realsimulator.Util.Distance;

import junit.framework.TestCase;

/** 
 * @author wwtao thedevilking@qq.com: 
 * @version 创建时间：2015-8-10 上午11:42:18 
 * 说明 
 */
public class TestDistanceCompute extends TestCase
{
	public void testDistanceCompute()	
	{
		//测试距离1
		double x1=23.45d;
		double y1=134.23d;
		
		double x2=-34.234d;
		double y2=-56.342d;
		
		double dis=Distance.getDistance(x1, y1, x2, y2);
		System.out.println(String.format("dis:%f", dis));
		assertEquals((int)(dis), 18457275);
	}
}
