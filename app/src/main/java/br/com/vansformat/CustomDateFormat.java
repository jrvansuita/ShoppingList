package br.com.vansformat;

import java.text.DateFormat;
import java.util.Date;

public class CustomDateFormat {

	
	public static String getFormatedDate(Date date) {
		return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
	}

	
	public static String getFormatedTime(Date date) {
		return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
	}
	
	public static String getFormatedCompletedDate(Date date) {
		return getFormatedDate(date) + " " + getFormatedTime(date);
	}
	
}
