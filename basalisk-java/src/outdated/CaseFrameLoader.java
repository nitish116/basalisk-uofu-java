package outdated;

import java.io.File;
import java.util.*;

import word_tools.FileHelper;



public class CaseFrameLoader {
	
	private static final String _cfNameLineRegex = "^Name.*[0-9]+$";
	
	public static void main(String[] args){
		System.out.println(isCaseFrameNameLine("Name: <subj>_ActVp_Dobj__CREATING_SITUATIONS_91"));
		System.out.println(stripNameFormatting("Name: <subj>_ActVp_Dobj__CREATING_SITUATIONS_91"));
		
		HashSet<CaseFrame> duplicateTest = new HashSet<CaseFrame>();
		
		String s1 = "<subj>_ActVp_Dobj__CREATING_SITUATIONS";
		String s2 = "<subj>_ActVp_Dobj__CREATING_SITUATIONS";
		
		
		CaseFrame c1 = new CaseFrame("<subj>_ActVp_Dobj__CREATING_SITUATIONS");
		CaseFrame c2 = new CaseFrame("<subj>_ActVp_Dobj__CREATING_SITUATIONS");
		
		duplicateTest.add(c1);
		duplicateTest.add(c2);
		
		System.out.println(duplicateTest);
		System.out.println(c1.equals(c2));
	}
	
	public static Set<String> loadFromFile(String caseFrameFileName){
		File f = new File(caseFrameFileName);
		return loadFromString(FileHelper.fileToString(f));
	}
	
	public static Set<String> loadFromString(String input){
		Set<String> result = new HashSet<String>();
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			String nextLine = in.nextLine();
			if(isCaseFrameNameLine(nextLine)){
				result.add(stripNameFormatting(nextLine).toLowerCase());
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
