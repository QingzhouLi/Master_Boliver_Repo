package recommendation;

public class AddrAddPlus {
	public static String convert(String addr) {
		char[] array = addr.toCharArray();
		
		for(int i = 0; i < array.length; i++) {
			if(array[i] == ' ') {
				array[i] = '+';
			}
		}
		
		return new String(array, 0, array.length);
	}
}
