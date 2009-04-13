package word_tools;

import java.io.File;
import java.util.Scanner;

public class FileHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(fileToString(new File("muc3-listfile")));
		
		String noun = "this painful   moment";
		System.out.println(noun.split(" +")[noun.split(" +").length - 1]);
	}
	
	public static String fileToString(File f){
		String result = "";
		
		
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
