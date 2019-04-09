package db.mysql;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestDate {
	
	public static void main(String[] args) {
		String pattern = "HH:mm zzz MM-dd-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		
		Calendar calendar = Calendar.getInstance();
		Date cal = calendar.getTime();
		
		String dateProcessed = simpleDateFormat.format(new Date());
		String calProcessed = simpleDateFormat.format(cal);
		
		System.out.println("date: " + dateProcessed);
		System.out.println("calTodate: " + calProcessed);
	}
}
