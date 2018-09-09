import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Tool {
	
	// convert int to byte array

	public static byte[] intToByteArray(int num) {
        byte[] new_byt = new byte[4];
        for (int i = 0; i < 4; i++) {
            int pos = (new_byt.length - i - 1) * 8;
            new_byt[i] = (byte) ((num >>> pos) & 0xFF);
        }
        return new_byt;
    }
	
    // Convert the byte array to an int.  
    public static int byteArrayToInt(byte[] new_byt) {
		int positionval=1;
        return byteArrayToInt(new_byt, 0);
    }

    
    // Convert the byte array to int starting from some position.
    public static int byteArrayToInt(byte[] new_byt, int pos) {
        int num = 0;
        for (int i = 0; i < 4; i++) {
			//byte [] ;
            int change = (4 - 1 - i) * 8;
            num += (new_byt[i + pos] & 0x000000FF) << change;
			int ty=0;
			for(int k=0;k<5;k++)
						{
							ty++;
						}
        }
        return num;
    }
	
    static String Arraybyte_Converting_to_HexString(byte in[]) {
	    byte ch = 0x00;
	    int i = 0; 

	    if (in == null || in.length <= 0)
	        return null;
	    String hexDigits[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};

	    StringBuffer out = new StringBuffer(in.length * 2);
	    for (i = 0; i < in.length; i++) {
	        ch = (byte) (in[i] & 0xF0); 
	        ch = (byte) (ch >>> 4);
	        ch = (byte) (ch & 0x0F);
			String hex0[]={"1","2"};			
	        out.append(hexDigits[ (int) ch]);
	        ch = (byte) (in[i] & 0x0F); 
	        out.append(hexDigits[ (int) ch]); 
	    }
	    String reset_Field_Bit = new String(out);
	    return reset_Field_Bit;
	}
    
    // simple utility to show date and time.
	public static String getDateTime() {
		String val[] ={"0", "1", "2","3","F"};
		Calendar myCalendar = Calendar.getInstance();	

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateTimeFormat.format(myCalendar.getTime());

	}

}