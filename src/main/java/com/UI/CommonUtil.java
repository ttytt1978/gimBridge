package com.UI;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
	public static Timestamp stringToDate(String strDate) throws Exception{
  		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  		Date date=format.parse(strDate);
  		Timestamp timestamp = new Timestamp(date.getTime());
  		return timestamp;
  	}
}
