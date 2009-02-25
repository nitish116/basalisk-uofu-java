package basilisk;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseFrameMapToExtractedNoun {

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
		
		//Test it with a single file
		System.out.println(loadFromFile("sample-texts/muc-out/DEV-MUC3-0001.cases"));
		
		//Test with multiple files
		System.out.println(loadFromMultipleFiles("muc3-listfile.cases"));
	}

	private static HashMap<CaseFrame, HashMap<String, Integer>> loadFromMultipleFiles(String listFile) {
		HashMap<CaseFrame, HashMap<String, Integer>> result = new HashMap<CaseFrame, HashMap<String, Integer>>();
		
		File f = new File(listFile);
		
		if(!f.exists()){
			System.err.println("Error proccessing list of Case file. File could not be found: " + f.getAbsolutePath());
			return null;
		}
		
		Scanner in = null;
		
		try{
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		while(in.hasNextLine()){
			if(result.size() == 0){
				result = loadFromFile(in.nextLine());
			}
			else{
				result = combineHashMaps(result, loadFromFile(in.nextLine()));
			}
		}
		
		return result;
	}

	private static HashMap<CaseFrame, HashMap<String, Integer>> combineHashMaps(HashMap<CaseFrame, HashMap<String, Integer>> m1, HashMap<CaseFrame, HashMap<String, Integer>> m2) {
		HashMap<CaseFrame, HashMap<String, Integer>> result = m1;
		
		//Iterate through the 2nd map, adding entries intelligently
		for(CaseFrame cf: m2.keySet()){
			//If the case frame is already in the existing count dictionary
			if(result.get(cf) != null){
				HashMap<String, Integer> currCountDict = result.get(cf);
				HashMap<String, Integer> newCountDict = m2.get(cf);
				
				System.out.println("Combining dictionaries, duplicate caseframes for: " + cf);
				System.out.println("Count dictionary for curr dictionary: " + currCountDict);
				System.out.println("Count dictionary for curr dictionary: " + newCountDict);
				
				//Iterate through all the noun counts, checking to see if they are already in the current Count Dictionary
				for(String s: newCountDict.keySet()){
					//If the word is already in our count dictionary...add + 1
					if(currCountDict.get(s) != null){
						System.out.println("Same case frame extracted same np in two documents: " + cf);
						System.out.println("Count dictionary for curr dictionary: " + currCountDict);
						System.out.println("Count dictionary for curr dictionary: " + newCountDict);
						currCountDict.put(s, currCountDict.get(s) + newCountDict.get(s));
					}
					else{
						currCountDict.put(s, newCountDict.get(s));
					}
				}
			}
			else{
				result.put(cf, m2.get(cf));
			}
		}
		return result;
	}

	public static HashMap<CaseFrame, HashMap<String, Integer>> loadFromFile(String extNounFileName){
		File in = new File(extNounFileName);
		
		if(!in.exists()){
			System.err.println("Error parsing .cases file. File could not be found: " + in.getAbsolutePath());
			return null;
		}
		System.out.println("Loading count dictionary from .cases file: " + in.getAbsolutePath());
		return loadFromString(FileHelper.fileToString(in));
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
					//System.out.println("CaseFrame: " + cf + " extracted more than one noun");
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
