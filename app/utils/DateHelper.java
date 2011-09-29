/**
 * 
 */
package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author chamerling
 * 
 */
public class DateHelper {

	private DateHelper() {
	}

	public static long getLong(String date) {
		long l = 0L;
		try {
			Date d = DateFormat.getDateInstance().parse(date);
			l = d.getTime();
			System.out.println(l);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}
	
	public static void main(String[] args) {
		getLong("2011-09-27T10:00:58.369+02:00");
	}

}
