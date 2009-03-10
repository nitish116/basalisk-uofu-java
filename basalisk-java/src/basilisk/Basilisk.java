package basilisk;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.*;

public class Basilisk {

	private Set<String> _caseFrameList;
	private Map<String, Map<String, Integer>> _caseFrameToNounMap;
	private Map<String, Set<String>> _nounToCaseFrameMap;
	private Set<String> _locations;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String caseFrameFile = "texts/terrorism/caseframes/all.aslog";
		String casesSlist = "texts/terrorism/slists/cases.slist";
		String locationSeedFile = "texts/terrorism/seed-lists/locations.seed";
		Basilisk b = new Basilisk(caseFrameFile, casesSlist, locationSeedFile);
		
	}
	
	public Basilisk(String fileWithOnlyCaseFrames, String casesSlist, String locationSeedFile){
		//_caseFrameList = CaseFrameLoader.loadFromFile(fileWithOnlyCaseFrames);
		_caseFrameToNounMap = CaseFrameToExtractedNounMap.loadFromSlist(casesSlist);
		_nounToCaseFrameMap = ExtractedNounToCaseFrameMap.loadFromSlist(casesSlist);
		_locations = SeedWordLoader.loadFromFile(locationSeedFile);
		List<String> learnedLocationLexicon = new ArrayList<String>();
		
		
		//Run the bootstrapping 5 times
		for(int i = 0; i < 5; i++){
			//Score the caseFrameList
			Map<String, Double> locationCFToRlogFMap = scoreCaseFrames(_caseFrameToNounMap, _locations);
			//Select the top rated caseframes
			Set<String> patternPool = selectTopNCaseFrames(locationCFToRlogFMap, 20 + i);
			System.out.println("Pattern pool size: " + patternPool.size());
			//Form the candidate pool from the pattern pool
			Set<String> candidateNounPool = selectNounsFromCaseFrames(patternPool, _caseFrameToNounMap);
			System.out.println(candidateNounPool);
			System.out.println("Candidate noun pool size: " + candidateNounPool.size());
			//Score the candidate nouns
			Map<String, Double> candidateWordToScoreMap = scoreCandidateNouns(candidateNounPool, _nounToCaseFrameMap, _caseFrameToNounMap, _locations);
			//Add the top 5 candidate nouns to the lexicon
			List<String> topNewWords = selectTopNNewCandidateWords(candidateWordToScoreMap, 5);
			
			System.out.printf("%d new words were added to the lexicon on iteration #%d of bootstrapping.\n", topNewWords.size(), i);
			
			_locations.addAll(topNewWords);
			learnedLocationLexicon.addAll(topNewWords);
		}
		System.out.println(learnedLocationLexicon);
		System.out.println("Learned lexicon size:" + learnedLocationLexicon.size());

		//Print out the list of learned words to a file
		PrintStream out = null;
		try {
			out = new PrintStream("learned-locations.txt");
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		for(String learnedWord: learnedLocationLexicon){
			out.print(learnedWord + "\n");
		}
		out.close();
	}

	private List<String> selectTopNNewCandidateWords(Map<String, Double> candidateWordToScoreMap, int n) {
		List<String> result = new ArrayList<String>();
		
		//Sort the list by descending score
		List<Map.Entry<String, Double>> sortByValue = new ArrayList<Map.Entry<String, Double>>(candidateWordToScoreMap.entrySet());
		Collections.sort(sortByValue, new Comparator<Map.Entry>(){
			public int compare(Map.Entry e1, Map.Entry e2){
				Double d1 = (Double) e1.getValue();
				Double d2 = (Double) e2.getValue();
				
				return d2.compareTo(d1);
			}
		});
		
		System.out.println("Top ranked candidate words: " + sortByValue);
		int i = 0;
		while(!sortByValue.isEmpty()){
			String word = sortByValue.remove(0).getKey();
			if(word.equals("the cauca river")){
				int temp = 0;
			}
			if(!_locations.contains(word) && !result.contains(word)){
				//System.out.println("Adding new word: " + word);
				result.add(word);
				i++;
				if(i == n)
					break;
			}
		}
		
		//System.out.println("Top N words added were:" + result);
		return result;
	}

	private Map<String, Double> scoreCandidateNouns(Set<String> candidateNounPool, 
													Map<String, Set<String>> nounToCaseFrameMap,
													Map<String, Map<String, Integer>> caseFrameToNounMap, 
													Set<String> knownCategoryWords) {
		Map<String, Double> result = new HashMap<String, Double>();
		
		//Score each noun in our candidate pool
		for(String noun: candidateNounPool){
			double sumLog2 = 0.0; //Sumation of (log2(F+1))
			int numPatterns = 0; 	//P number of patterns that extracted candidate noun
			
			//Loop through each caseframe that extracted the given noun
			//Incrementing the score by log2(F+1), where F is the number of known
			//category words that caseframe extracted
			for(String cf : nounToCaseFrameMap.get(noun)){
				int FPlusOne = 1;
				//Loop through each noun extracted by the cf, checking to see if it's a known word
				for(String extractedNoun: caseFrameToNounMap.get(cf).keySet()){
					if(knownCategoryWords.contains(extractedNoun))
						FPlusOne++;
				}
				sumLog2 += Math.log(FPlusOne)/Math.log(2);
				numPatterns++;
			}
			//Average the nounScore over the number of patterns that contributed to it
			result.put(noun, (sumLog2/(double) numPatterns));
		}
		
		return result;
	}

	private Set<String> selectNounsFromCaseFrames(Set<String> patternPool, Map<String, Map<String, Integer>> caseFrameToNounMap) {
		Set<String> result = new HashSet<String>();
		for(String cf: patternPool){
			result.addAll(caseFrameToNounMap.get(cf).keySet());
		}
		
		return result;
	}

	private Set<String> selectTopNCaseFrames(Map<String, Double> caseFrameToRlogFMap, int n) {
		Set<String> result = new HashSet<String>();
		//Sort the caseFrame map by descending rlogf value
		List<Map.Entry<String, Double>> sortByValue = new ArrayList<Map.Entry<String, Double>>(caseFrameToRlogFMap.entrySet());
		Collections.sort(sortByValue, new Comparator<Map.Entry>(){
			public int compare(Map.Entry e1, Map.Entry e2){
				Double d1 = (Double) e1.getValue();
				Double d2 = (Double) e2.getValue();
				
				return d2.compareTo(d1);
			}
		});
		
		System.out.println(sortByValue);
		for(int i = 0; i < n; i++){
			result.add(sortByValue.get(i).getKey());
		}
		//System.out.println(result);
		return result;
	}

	private Map<String, Double> scoreCaseFrames(Map<String, Map<String, Integer>> caseFrameToNounMap, Set<String> knownCategoryMembers) {
		Map<String, Double> result = new HashMap<String, Double>();
		
		//Score each case frame
		for(String cf: caseFrameToNounMap.keySet()){
			
			int f = 0;	//total number of known category members extracted
			int n = 0; 	//total words extracted by pattern
			
			//Loop through each noun extracted by the caseframe
			for(String noun: caseFrameToNounMap.get(cf).keySet()){
				if(knownCategoryMembers.contains(noun)){
					f++;
				}
				n++; 
			}
			
			double rLogF = (f/(double) n)*(Math.log(f)/Math.log(2));
			
			if(Double.compare(rLogF, Double.NaN) == 0)
				rLogF = -1.0;
			
			result.put(cf.toString(), rLogF);
		}
		return result;
	}

}
