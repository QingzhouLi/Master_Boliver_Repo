package external;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalETime {
	public static String calculateETime(String timeTaken) {
		// calculate estimated arrival time by adding current time with timeTaken
		Calendar calendar = Calendar.getInstance(); 
		calendar.add(Calendar.MINUTE,Integer.parseInt(timeTaken)); 
		
		// create the desired display date/time pattern
		String pattern = "HH:mm zzz MM-dd-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		
		// convert result to desired format and to string
		Date resRaw = calendar.getTime();
		String resCooked = simpleDateFormat.format(resRaw);
		
		return resCooked;
		
	}
	
	/*
	public static void main(String[] args) {
		System.out.println(CalETime.calculateETime("5"));
	}
	*/
}
