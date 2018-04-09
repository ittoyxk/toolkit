package com.xk.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Util {

	/**
	 * base64编码
	 * @param value 待编码值，charset默认utf-8
	 * @return
	 */
	public static String encode(final String value){
		try {
			return Base64.encodeBase64String(value.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * base64解码
	 * @param value base64码值
	 * @return 解码后字符串 charset默认utf-8
	 */
	public static String decode(final String value){
		try {
			return new String(Base64.decodeBase64(value.getBytes("utf-8")),"utf-8");
		} catch (final UnsupportedEncodingException e) {
			return null;
		}
	}
	public static void main(String[] args)
	{
		try {
			// String string =
			// HttpClientUtil.get(HttpConfig.custom().url("https://www.baidu.com"));
			// System.out.println(string);
//			System.out.println("小康");
			String decode = Base64Util.encode("xiaokang");
			System.out.println(decode);
			String encode = Base64Util.decode(decode);
			System.out.println(encode);
//			System.out.println(Md5Encrypt.getMD5Mac("xiaokang"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
