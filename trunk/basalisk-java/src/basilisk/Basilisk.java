package basilisk;

import java.io.File;
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
		String caseFrameFile = "sample-texts/muc-out/muc-1thru20-cfs.txt";
		String listFilesWithExtractedNouns = "muc3-listfile.cases";
		String locationSeedFile = "location-seeds";
		Basilisk b = new Basilisk(caseFrameFile, listFilesWithExtractedNouns, locationSeedFile);
		
	}
	
	public Basilisk(String fileWithOnlyCaseFrames, String listOfFilesWithExtractedNouns, String locationSeedFile){
		//_caseFrameList = CaseFrameLoader.loadFromFile(fileWithOnlyCaseFrames);
		_caseFrameToNounMap = CaseFrameToExtractedNounMap.loadFromMultipleFiles(listOfFilesWithExtractedNouns);
		_nounToCaseFrameMap = ExtractedNounToCaseFrameMap.loadFromMultipleFiles(listOfFilesWithExtractedNouns);
		_locations = SeedWordLoader.loadFromFile(locationSeedFile);
		List<String> learnedLocationLexicon = new ArrayList<String>();
		
		//Score the caseFrameList
		Map<String, Double> locationCFToRlogFMap = scoreCaseFrames(_caseFrameToNounMap, _locations);
		//Select the top rated caseframes
		Set<String> patternPool = selectTopNCaseFrames(locationCFToRlogFMap, 20);
		//Form the candidate pool from the pattern pool
		Set<String> candidateNounPool = selectNounsFromCaseFrames(patternPool, _caseFrameToNounMap);
		//Score the candidate nouns
		Map<String, Double> candidateWordToScoreMap = scoreCandidateNouns(candidateNounPool, _nounToCaseFrameMap, _caseFrameToNounMap, _locations);
		//Add the top 5 candidate nouns to the lexicon
		learnedLocationLexicon.addAll(selectTopNCandidateWords(candidateWordToScoreMap, 5));
		System.out.println(learnedLocationLexicon);
	}

	private List<String> selectTopNCandidateWords(Map<String, Double> candidateWordToScoreMap, int n) {
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
		
		System.out.println(sortByValue);
		for(int i = 0; i < n; i++){
			result.add(sortByValue.get(i).getKey());
		}
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
		System.out.println(result);
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
