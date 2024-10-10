import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class AllLog {
	public static void main(String[] args) {
		addLog();
		sendLog();
	}
	
	static void addLog() {
		File fLog = new File("../log/000.log"), fAll = new File("../../role-addAIwolf/allLog/s.log");
		if(fLog.exists() && fAll.exists()) {
			if(read(fLog).equals(read(fAll))) {
				System.out.println("already update");
				return;
			}
		}
		File dir = new File("../../role-addAIwolf/allLog/");
		int count = dir.listFiles().length - 1;
		if(count == -1) {
			count = 0;
		}
		fLog = new File("../log/000.log");
		int c = 0;
		String strNum = "000";
		while(fLog.exists()) {
			Path p1 = Paths.get("../log/" + strNum + ".log");
			Path p2 = Paths.get("../../role-addAIwolf/allLog/" + (c + count) + ".log");
			Path p3 = Paths.get("../../role-addAIwolf/allLog/s.log");
			try {
				Files.copy(p1, p2);
				if(c == 0) {
					Files.copy(p1, p3);
				}
				System.out.println(strNum + ".log copied to " + (c + count) + ".log");
			} catch (IOException e) {
				System.out.println(e);
			}
			
			c++;
			strNum = "" + c;
			if(c < 10) strNum = "00" + strNum;
			else if (c < 100) strNum = "0" + strNum;
			fLog = new File("../log/" + strNum + ".log");
			
		}
		
	}
	
	static void sendLog() {
		int c = 0;
		File fLog = new File("../../role-addAIwolf/allLog/" + c + ".log");
		while(fLog.exists()) {
			Path p1 = Paths.get("../../role-addAIwolf/allLog/" + c + ".log");
			Path p2 = Paths.get("../../data9D/comlog/" + c + ".txt");
			try {
				Files.copy(p1, p2);
				System.out.println(c + ".log copied to " + c + ".txt");
			} catch (IOException e) {
				System.out.println(e);
			}
			c++;
			fLog = new File("../../role-addAIwolf/allLog/" + c + ".log");
		}
	}
	
	  /** ファイルを読み込んでその中身をStringで返す */
	static String read(File file) {
		String dataStr = "";
		try {
			//ファイルが存在するか確認する
			if(file.exists()) {
				//FileReaderクラスのオブジェクトを生成する
				FileReader filereader = new FileReader(file);
				//FileReaderクラスのreadメソッドでファイルを1文字ずつ読み込む
				int data;
				while((data = filereader.read()) != -1) {
					//System.out.print((char) data);
					dataStr = dataStr + (char)data;
				}
				//ファイルクローズ
				filereader.close();
			} 
			else {
				System.out.print("ファイルは存在しません");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
		return dataStr;
	}

	
}
