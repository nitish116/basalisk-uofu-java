package basilisk;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class CaseFrameLoader {
	
	private static String _cfNameLineRegex = "^Name.*[0-9]+$";
	
	public static void main(String[] args){
		System.out.println(isCaseFrameNameLine("Name: <subj>_ActVp_Dobj__CREATING_SITUATIONS_91"));
		System.out.println(stripNameFormatting("Name: <subj>_ActVp_Dobj__CREATING_SITUATIONS_91"));
	}
	
	public static ArrayList<CaseFrame> loadFromFile(File caseFrameFile){
		ArrayList<CaseFrame> result = new ArrayList<CaseFrame>();
		
		Scanner in = null;
		
		try{
			in = new Scanner(caseFrameFile);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		while(in.hasNextLine()){
			String nextLine = in.nextLine();
			if(isCaseFrameNameLine(nextLine)){
				result.add(new CaseFrame(stripNameFormatting(nextLine)));
			}
		}
		
		in.close();
		return result;
	}
	
	private static String stripNameFormatting(String line) {
		line = line.replaceAll("Name:", "");
		line = line.replaceAll("_[0-9]+$", "");
		line = line.trim();
		
		return line;
	}

	public static boolean isCaseFrameNameLine(String line){
		if(line.matches(_cfNameLineRegex))
			return true;
		return false;
	}

}
