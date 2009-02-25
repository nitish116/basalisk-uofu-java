package basilisk;

import java.io.File;
import java.util.Scanner;

public class FileHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(fileToString(new File("muc3-listfile")));
	}
	
	public static String fileToString(File f){
		String result = "";
		
		//Make sure the file exists
		if(!f.exists()){
			System.err.println("Trouble opening file: " + f.getAbsolutePath());
		}
		
		Scanner in = null;
		
		try{
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		while(in.hasNextLine()){
			if(result.equals("")){
				result += in.nextLine();
			}
			else{
				result += "\n" + in.nextLine();
			}
		}
		
		in.close();
		return result;
	}

}
