package scorer;

import java.util.*;
import java.io.*;

public class LexiconScorer {

	/**
	 * Entry point for the lexicon learner
	 * @param args Input args, ordered as such:
	 * 				<semantic_keyfile> <slist_of_lexicons_to_score>
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("Wrong number of input arguments. Please supply: <semantic_keyfile> <slist_of_lexicons_to_score> ");
			return;
		}
		
		//Initialize the lexicon scorer
		LexiconScorer ls = new LexiconScorer(args[0], args[1]);

	}
	
	public LexiconScorer(String semKeyFileName, String lexiconSlistFileName){
		//Load up our semantic key dictionary
		Map<String, Set<String>> semKeyDictionary = new HashMap<String, Set<String>>();
		semKeyDictionary = loadSemKeyDictionary(semKeyFileName);
	}

	/**
	 * Loads up the semantic key dictionary from a file. This is just a set of strings (categories) that map to a list of other strings (the 
	 * words that belong to each category). 	 * 
	 * 
	 * @param semKeyFileName - File name of the semantic key. Each line of the file should be of the form: CATEGORYMEMBER CATEGORY
	 * @return A map relating strings (categories) to sets of other strings (category members) 
	 */
	private Map<String, Set<String>> loadSemKeyDictionary(String semKeyFileName) {
		//Attempt to read the keyfile
		File keyFile = new File(semKeyFileName);
		if(!keyFile.exists()){
			System.err.println("Key file could not be found: " + keyFile.getAbsolutePath());
			return null;
		}
		
		//Create a scanner for the file
		Scanner keyScanner = null;
		try{
			keyScanner = new Scanner(keyFile);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		//Create our map
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		
		//Scan each line
		while(keyScanner.hasNext()){
			String catMember = keyScanner.next().trim().toLowerCase();
			String category = keyScanner.next().trim().toLowerCase();
			
			//If the category already has members, update it with the new entry
			if(result.get(category) != null){
				result.get(category).add(catMember);
			}
			//Otherwise, create a new set and add that to the map
			else{
				HashSet<String> memberList = new HashSet<String>();
				memberList.add(catMember);
				result.put(category, memberList);
			}
			System.out.println(category + " : " + catMember);
		}
		return result;
	}

}
