package basilisk;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import word_tools.WordTools;

public class ExtractedNounFreqCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(countExtractedNounsFromString("CaseFrame: <subj>_ActVp__REPORTED_3\nTrigger(s): (REPORTED)\nSUBJ_Extraction = \"THE ARCE BATTALION COMMAND\" [OTHER-STATEMENT]"));
		System.out.println(countExtractedNounsFromFile("sample-texts/muc-out/DEV-MUC3-0001.cases"));
		//System.out.println(countExtractedNounsFromMultipleFiles("muc3-listfile.cases"));

		printCountSortedListToFile(countExtractedNounsFromMultipleFiles("muc3-listfile.cases"), "mucFrequentWords.txt", true);
	}
	
	private static final String _cfNameLineRegex = "^CaseFrame:.+[0-9]+$";
	
	public static void printCountSortedListToFile(Map<String, Integer> countTable, String fileName, boolean includeCount){
		List<Map.Entry<String, Integer>> sortByValue = new ArrayList<Map.Entry<String, Integer>>(countTable.entrySet());
		Collections.sort(sortByValue, new Comparator<Map.Entry>(){
			public int compare(Map.Entry e1, Map.Entry e2){
				Integer i1 = (Integer) e1.getValue();
				Integer i2 = (Integer) e2.getValue();
				return i2.compareTo(i1);
			}
		});
		
//      System.out.println("letter - freq");
//      System.out.println("-------------");
//      for(Map.Entry e : sortByValue) {
//          System.out.printf("%-15s%d%n", e.getKey(), e.getValue());
//      }
		
		//Create a file writer to print word count
		PrintStream out = null;
		try{
			out = new PrintStream(fileName);
			
			System.out.println("Writing frequency count to file: " + fileName);
	        for(Map.Entry e : sortByValue) {
	        	if(!includeCount)
	        		out.print(e.getKey().toString()  + "\n");
	        	else
	        		out.printf("%-25s%d%n", e.getKey(), e.getValue());
	        }
			out.close();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public static Map<String, Integer> countExtractedNounsFromMultipleFiles(String listFileName){
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		File f = new File(listFileName);
		
		if (!f.exists()){
			System.err.println("Error opening list pointing to .cases files while trying to count extracted noun frequency: " + f.getAbsolutePath());
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
				result = countExtractedNounsFromFile(in.nextLine());
			else{
				result = combineFrequencyCounts(result, countExtractedNounsFromFile(in.nextLine()));
			}
		}
		
		in.close();
		return result;
	}
	

	private static Map<String, Integer> combineFrequencyCounts(Map<String, Integer> m1, Map<String, Integer> m2) {
		Map<String, Integer> result = m1;
		
		//Iterate through the 2nd input, seeing if duplicates already exist in the hashtable
		for(String s: m2.keySet()){
			if(result.get(s) != null){
				result.put(s, result.get(s) + m2.get(s));
			}
			else {
				result.put(s, m2.get(s));
			}
		}
		
		return result;
	}


	public static Map<String, Integer> countExtractedNounsFromFile(String fileName){
		File f = new File(fileName);
		
		if(!f.exists()){
			System.err.println("Error opening .cases file to count extracted nouns: " + f.getAbsolutePath());
			return null;
		}
		
		System.out.println("Counting frequency of extracted nouns in file: " + f.getAbsolutePath());
		return (countExtractedNounsFromString(FileHelper.fileToString(f)));
	}
	
	public static Map<String, Integer> countExtractedNounsFromString(String input){
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			String line = in.nextLine();
			if(isCaseFrameNameLine(line)){
				//Advance the scanner two lines
				String extNounLine = in.nextLine();
				extNounLine = in.nextLine();
				
				//Get the noun out of the quotes
				String noun = extNP(extNounLine).toLowerCase();
				
				//Add the word to our frequency count
				if(result.get(noun) != null){
					result.put(noun, result.get(noun) + 1);
				}
				else{
					result.put(noun, 1);
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
	
	private static String extNP(String extNounLine) {
		Pattern p = Pattern.compile("\"[^\"\r\n]*\"");
		
		Matcher m = p.matcher(extNounLine);
		m.find();
		
		String substr = extNounLine.substring(m.start()+1, m.end()-1);
		return substr;
	}
	

}
