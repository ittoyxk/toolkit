package com.xk.util;

public class VersionUtil {

	/**
	 * 判断指定参数v2版本是否比版本v1大
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean isInc(String v1,String v2){
		int iv1 = Integer.valueOf(v1.substring(1));
		int iv2 = Integer.valueOf(v2.substring(1));
		return iv2 > iv1;
	}
	
}
