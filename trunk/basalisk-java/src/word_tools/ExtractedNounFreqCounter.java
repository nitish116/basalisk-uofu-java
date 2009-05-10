package word_tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import basilisk.*;






public class ExtractedNounFreqCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Basilisk b = new Basilisk();
		ExtractedNounFreqCounter c = new ExtractedNounFreqCounter(b);
	}

	public HashSet<ExtractedNoun> _nounList;
	private HashSet<Noun> _stopWords;
	public Basilisk _b;
	
	public ExtractedNounFreqCounter(Basilisk b){
		_b = b;
		_stopWords = b.loadSet("stopwords.dat");
		System.out.println("Reading case file");
		generateNounList("promed-training-all.cases");
		System.out.println("Finished reading case file.");
		
		System.out.println("Sorting the counted words");
		TreeSet<ExtractedNoun> sortedNouns = new TreeSet<ExtractedNoun>();
		for(ExtractedNoun noun: _nounList){
			sortedNouns.add(noun);
		}
		
		System.out.println("Printing the sorted files to a list");
		PrintStream out = null;
		try{
			out = new PrintStream("promed-freq-count.txt");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		for(ExtractedNoun noun: sortedNouns.descendingSet()){
			out.format("%20s %20f\n", noun, noun.getScore());
		}
		out.close();
	}
	
	public boolean generateNounList(String allCasesFile){
		
		_nounList = new HashSet<ExtractedNoun>();
		
		
		//Read in the cases file
		try{
			BufferedReader br = new BufferedReader(new FileReader(allCasesFile));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				line = line.trim();
				
				//An asteric separates the extracted noun from the pattern that extracted it
				String nounPhrase = line.split("\\*")[0].trim();
				String pattern = line.split("\\*")[1].trim();
				
				
				//Identify the head nouns in the noun phrase
				ArrayList<ExtractedNoun> headNouns = _b.identifyHeadNouns(nounPhrase, _stopWords);
				
				//Create a new word for each headnoun of the and phrase
				for(ExtractedNoun noun: headNouns){
					//Map patterns to extracted nouns
					if(_nounList.contains(noun)){
						for(ExtractedNoun seenNoun: _nounList){
							if(seenNoun.equals(noun)){
								seenNoun.setScore(seenNoun.getScore() + 1.0);
							}
						}
					}
					else{
						noun.setScore(1.0);
						_nounList.add(noun);
					}
				}

			}
			br.close();
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
}
