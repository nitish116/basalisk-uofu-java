package basilisk;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractedNounToCaseFrameMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(loadFromString("CaseFrame: <subj>_ActVp__REPORTED_3\nTrigger(s): (REPORTED)\nSUBJ_Extraction = \"THE ARCE BATTALION COMMAND\" [OTHER-STATEMENT]"));
		System.out.println(loadFromFile("sample-texts/muc-out/DEV-MUC3-0001.cases"));
	}
	private static final String _cfNameLineRegex = "^CaseFrame:.+[0-9]+$";
	
	public static HashMap<String, HashSet<CaseFrame>> loadFromMultipleFiles(String listFileName){
		HashMap<String, HashSet<CaseFrame>> result = new HashMap<String, HashSet<CaseFrame>>();
		
		File f = new File(listFileName);
		
		if (!f.exists()){
			System.err.println("Error opening list pointing to .cases files while trying to map nouns to caseframes: " + f.getAbsolutePath());
			return null;
		}
		
		Scanner in = null;
		
		try {
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		while(in.hasNextLine()){
			if(result.size() == 0)
				result = loadFromFile(in.nextLine());
			else{
				result = combineNounMaps(result, loadFromFile(in.nextLine()));
			}
		}
		
		in.close();
		return result;
	}
	

	private static HashMap<String, HashSet<CaseFrame>> combineNounMaps(HashMap<String, HashSet<CaseFrame>> m1, HashMap<String, HashSet<CaseFrame>> m2) {
		HashMap<String, HashSet<CaseFrame>> result = m1;
		
		//Iterate through the 2nd input, seeing if duplicates already exist in the HashMap
		for(String noun: m2.keySet()){
			if(result.get(noun) != null){
				result.get(noun).addAll(m2.get(noun));
			}
			else {
				result.put(noun, m2.get(noun));
			}
		}
		
		return result;
	}


	public static HashMap<String, HashSet<CaseFrame>> loadFromFile(String fileName){
		File f = new File(fileName);
		
		if(!f.exists()){
			System.err.println("Error opening .cases file to map nouns to case frames: " + f.getAbsolutePath());
			return null;
		}
		
		System.out.println("Mapping extracted nouns to caseframes in file: " + f.getAbsolutePath());
		return (loadFromString(FileHelper.fileToString(f)));
	}
	
	public static HashMap<String, HashSet<CaseFrame>> loadFromString(String input){
		HashMap<String, HashSet<CaseFrame>> result = new HashMap<String, HashSet<CaseFrame>>();
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			String line = in.nextLine();
			if(isCaseFrameNameLine(line)){
				CaseFrame cf = new CaseFrame(stripNameFormatting(line));
				//Advance the scanner two lines - to the noun line
				String extNounLine = in.nextLine();
				extNounLine = in.nextLine();
				
				//Get the noun out of the quotes
				String noun = extNP(extNounLine);
				
				//Add the word to our frequency count
				if(result.get(noun) != null){
					result.get(noun).add(cf);
				}
				else{
					HashSet<CaseFrame> listCF = new HashSet<CaseFrame>();
					listCF.add(cf);
					result.put(noun, listCF);
				}
			}
		}
		
		return result;
	}
	
	public static boolean isCaseFrameNameLine(String line){
		if(line.matches(_cfNameLineRegex))
			return true;
		return false;
	}
	
	private static String stripNameFormatting(String line) {
		line = line.replaceAll("CaseFrame:", "");
		line = line.replaceAll("_[0-9]+$", "");
		line = line.trim();
		
		return line;
	}
	
	private static String extNP(String extNounLine) {
		Pattern p = Pattern.compile("\"[^\"\r\n]*\"");
		
		Matcher m = p.matcher(extNounLine);
		m.find();
		
		String substr = extNounLine.substring(m.start()+1, m.end()-1);
		return substr;
	}
}