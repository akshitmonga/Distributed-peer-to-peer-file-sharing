import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;


public class CreateLogFile {
	static FileOutputStream fos;
	static OutputStreamWriter osw;
	
	public static void writeLog(String str) {
		try {
			osw.write(str + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void create(String str) throws Exception {
		fos = new FileOutputStream(str);
		osw = new OutputStreamWriter(fos, "UTF-8");

	}
	
	public static void end() {
		try {
			osw.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
