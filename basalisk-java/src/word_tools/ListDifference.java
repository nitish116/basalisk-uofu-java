package word_tools;

import java.util.*;
import java.io.*;
public class ListDifference {

	/**
	 * <file> <file_to_subtract>
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length != 2){
			System.err.println("Missing input args. Requires 2 input args.");
		}
		ListDifference ld = new ListDifference(args[0], args[1]);
	}
	
	public ListDifference(String fileName, String fileToSubtractName){
		Set<String> uniqueWords = loadSetFromFile(fileName);
		Set<String> wordsToSubtract = loadSetFromFile(fileToSubtractName);
		
		uniqueWords.removeAll(wordsToSubtract);
		
		PrintStream uniqueTrace = null;
		
		try{
			uniqueTrace = new PrintStream(fileName + ".unique");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		for(String uniqueW : uniqueWords){
			uniqueTrace.println(uniqueW);
		}
		
		uniqueTrace.close();
		
	}
	public Set<String> loadSetFromFile(String fileName){
		Set<String> result = new HashSet<String>();
		
		File f = new File(fileName);
		if(!f.exists())
			System.err.println("File does not exist: " + f.getAbsolutePath());
		
		BufferedReader br = null;
		
		try{
			br = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.toLowerCase().trim();
				result.add(line);
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		return result;
	}
}
