package word_tools;

import java.io.*;
import java.util.*;

public class alreadyLearnedChecker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File alreadyLearned = new File("lexicon-test/allknown-it67");
		File buildingScored = new File("lexicon-test/building-scored-it67");
		
		if(!alreadyLearned.exists())
			System.out.println("problem");
		if(!buildingScored.exists())
			System.out.println("problem");
		
		Scanner learnedScanner = null;
		Scanner buildingScanner = null;
		try{
			learnedScanner = new Scanner(alreadyLearned);
			buildingScanner = new Scanner(buildingScored);
		}
		catch(Exception e){
			System.err.println("scanner error");
		}
		
		HashSet<String> learnedWords = new HashSet<String>();
		while(learnedScanner.hasNext()){
			learnedWords.add(learnedScanner.nextLine().trim().toLowerCase());
		}
		
		HashSet<String> buildingWords = new HashSet<String>();
		while(buildingScanner.hasNext()){
			buildingWords.add(buildingScanner.nextLine().trim().toLowerCase());
		}
		
		//Check to see if all of the building words are contained in the already learned list
		for(String buildingWord: buildingWords){
			if(!learnedWords.contains(buildingWord)){
				System.out.println("Building word not already known: " + buildingWord);
			}
		}
		
		
	}

}
