package basilisk;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractedNounLoader {

	private static final String _cfNameLineRegex = "^CaseFrame:.+[0-9]+$";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//These should all return true
		System.out.println(isCaseFrameNameLine("CaseFrame: <subj>_PassVp__KIDNAPPED_1"));
		System.out.println(isCaseFrameNameLine("CaseFrame: <subj>_PassVp__TAKEN_2"));
		System.out.println(isCaseFrameNameLine("CaseFrame: infinitive_verb_<dobj>__INCORPORATE_1"));

		//These should print out content minus the beginnig and end formating
		System.out.println(stripNameFormatting("CaseFrame: <subj>_PassVp__KIDNAPPED_1"));
		System.out.println(stripNameFormatting("CaseFrame: <subj>_PassVp__TAKEN_2"));
		System.out.println(stripNameFormatting("CaseFrame: infinitive_verb_<dobj>__INCORPORATE_1"));
		
		//This should print out the content inside the quotes
		System.out.println(extNP("SUBJ_Extraction = \"MEMBERS OF THAT SECURITY GROUP\""));
		
		//This should print out the contents of a hashmap
		String sampleExtText = "CaseFrame: ActVp_<dobj>__REPELLED_6\nTrigger(s): (REPELLED)\nDOBJ_Extraction = \"A TERRORIST ATTACK\" [ATTACK]";
		System.out.println(loadFromString(sampleExtText));
		
		//Test it with a file
		System.out.println(loadFromFile(new File("sample-texts/muc-out/DEV-MUC3-0001.cases")));
		//
	}

	public static HashMap<CaseFrame, HashMap<String, Integer>> loadFromFile(File extNounFile){
		return loadFromString(FileHelper.fileToString(extNounFile));
	}
	
	public static HashMap<CaseFrame, HashMap<String, Integer>> loadFromString(String input){
		HashMap<CaseFrame, HashMap<String, Integer>> result = new HashMap<CaseFrame, HashMap<String, Integer>>();
		
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			String nextLine = in.nextLine();
			if(isCaseFrameNameLine(nextLine)){
				CaseFrame cf = new CaseFrame(stripNameFormatting(nextLine));
				//The extracted noun should be somewhere two lines after the current line
				String extNounLine = in.nextLine();
				extNounLine = in.nextLine();
				
				//Get the noun out of the quotes
				String noun = extNP(extNounLine);
				
				//Add the results to our HashMap
				if(result.get(cf) != null){
					System.out.println("CaseFrame: " + cf + " extracted more than one noun");
					if(cf.equals(new CaseFrame("<subj>_ActVp__REPORTED"))){
						String s = "";
					}
						
					HashMap<String, Integer> oldCountMap = result.get(cf);
					//Check to see if the noun was already added for this caseframe
					if(oldCountMap.get(noun) != null){
						Integer oldCount = oldCountMap.get(noun);
						oldCountMap.put(noun, oldCount + 1);
					}
					else{
						oldCountMap.put(noun, 1);
					}
					
				}
				else{
					HashMap<String, Integer> newCountMap = new HashMap<String, Integer>();
					newCountMap.put(noun, 1);
					result.put(cf, newCountMap);
				}
			}
		}
		
		in.close();
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
