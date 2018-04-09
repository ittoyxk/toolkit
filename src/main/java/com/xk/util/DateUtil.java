package com.xk.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
public class DateUtil {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(DateUtil.class);

	/**
	 * 解析出生日期
	 * @param birthday 
	 * 支持yyyy-MM-dd，yyyy/MM/dd，yyyyMMdd三种格式
	 * @return
	 */
	public static Date parseBirthday(String birthday)
	{
		if(StringUtils.isEmpty(birthday))
			return null;
		Date customerBirthday = null;
		 try {
			if(birthday.contains("-"))
			 	customerBirthday = DateUtils.parseDate(birthday, "yyyy-MM-dd");
			 else if(birthday.contains("/"))
			 	customerBirthday = DateUtils.parseDate(birthday, "yyyy/MM/dd");
			 else
			 	customerBirthday = DateUtils.parseDate(birthday, "yyyyMMdd");
		} catch (ParseException e) {
			log.error("====parseBirthday Exception:{}===",birthday);
		}
		return customerBirthday;
	}
	
	/**
	 * 根据出生日期计算年龄
	 * @param birthday
	 * @return
	 */
	public static Integer getAgeByBirthday(Date birthday){
		Integer age = null;
		if(birthday != null){
			//当前时间
			Calendar now = Calendar.getInstance();
			//出生日期
			Calendar birth = Calendar.getInstance();
			//今年生日
			Calendar nowBirth = Calendar.getInstance();
			birth.setTime(birthday);
			nowBirth.setTime(birthday);
			int year = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
			nowBirth.add(Calendar.YEAR, year);
			if(nowBirth.after(now)){
				//还未过生日年龄减一
				age = year -1;
			} else {
				age = year;
			}
		}
		return age;
	}
	
	public static Date parseDate(String dateStr, String pattern){
		try {
			return DateUtils.parseDate(dateStr, pattern);
		} catch (ParseException e) {
			log.error("====parseDate Exception dateStr:{},pattern:{}===",dateStr,pattern);
			return null;
		}
	}
	
	public static String formatDate(Date date, String pattern){
		return DateFormatUtils.format(date, pattern);
	}
	
}
