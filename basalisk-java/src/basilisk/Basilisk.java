package basilisk;

import java.io.*;
import java.util.*;

public class Basilisk {

	private Set<String> _caseFrameList;
	private Map<String, Set<String>> _caseFrameToCaseMap;
	private Map<String, Set<String>> _caseToCaseFrameMap;
	private Set<String> _locations;
	private Set<String> _stopWords;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String locationSeedFile = "texts/terrorism/seed-lists/locations.seed";
		String allCasesFile = "texts/terrorism/cases/all.cases";
		String stopWordFile = "stopwords.dat";
		Basilisk b = new Basilisk(allCasesFile, stopWordFile, locationSeedFile);
		
	}
	
	public Basilisk(String allCasesFile, String stopWordFile, String locationSeedFile){
		//_caseFrameList = CaseFrameLoader.loadFromFile(fileWithOnlyCaseFrames);
		_stopWords = loadSet(stopWordFile);
		_locations = loadSet(locationSeedFile);
		_caseFrameToCaseMap = new HashMap<String, Set<String>>();
		_caseToCaseFrameMap = new HashMap<String, Set<String>>();
		
		if(_stopWords == null)
			return;
	
		if(!generateMaps(allCasesFile))
			return;
		System.out.println("Finished reading input cases.");
		
		List<String> learnedLocationLexicon = new ArrayList<String>();
		
		
		//Run the bootstrapping 5 times
		for(int i = 0; i < 5; i++){
			//Score the caseFrameList
			Map<String, Double> locationCFToRlogFMap = scoreCaseFrames(_caseFrameToCaseMap, _locations);
			//Select the top rated caseframes
			Set<String> patternPool = selectTopNCaseFrames(locationCFToRlogFMap, 20 + i);
			System.out.println("Pattern pool size: " + patternPool.size());
			//Form the candidate pool from the pattern pool
			Set<String> candidateNounPool = selectNounsFromCaseFrames(patternPool, _caseFrameToCaseMap);
			System.out.println(candidateNounPool);
			System.out.println("Candidate noun pool size: " + candidateNounPool.size());
			//Score the candidate nouns
			Map<String, Double> candidateWordToScoreMap = scoreCandidateNouns(candidateNounPool, _caseToCaseFrameMap, _caseFrameToCaseMap, _locations);
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

	private Set<String> loadSet(String listOfWords) {
		File f = new File(listOfWords);
		
		if(!f.exists()){
			System.err.println("Stop words file could not be found: " + f.getAbsolutePath());
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		
		Scanner in = null;
		
		try{
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
			return null;
		}
		
		while(in.hasNextLine()){
			result.add(in.nextLine().toLowerCase().trim());
		}
		
		return result;
		
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
													Map<String, Set<String>> caseToCaseFrameMap,
													Map<String, Set<String>> caseFrameToCaseMap, 
													Set<String> knownCategoryWords) {
		Map<String, Double> result = new HashMap<String, Double>();
		
		//Score each noun in our candidate pool
		for(String noun: candidateNounPool){
			double sumLog2 = 0.0; //Sumation of (log2(F+1))
			int numPatterns = 0; 	//P number of patterns that extracted candidate noun
			
			//Loop through each caseframe that extracted the given noun
			//Incrementing the score by log2(F+1), where F is the number of known
			//category words that caseframe extracted
			for(String cf : caseToCaseFrameMap.get(noun)){
				int FPlusOne = 1;
				//Loop through each noun extracted by the cf, checking to see if it's a known word
				for(String extractedNoun: caseFrameToCaseMap.get(cf)){
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

	private Set<String> selectNounsFromCaseFrames(Set<String> patternPool, Map<String, Set<String>> caseFrameToCaseMap) {
		Set<String> result = new HashSet<String>();
		for(String cf: patternPool){
			result.addAll(caseFrameToCaseMap.get(cf));
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

	private Map<String, Double> scoreCaseFrames(Map<String, Set<String>> caseFrameToCaseMap, Set<String> knownCategoryMembers) {
		Map<String, Double> result = new HashMap<String, Double>();
		
		//Score each case frame
		for(String cf: caseFrameToCaseMap.keySet()){
			
			int f = 0;	//total number of known category members extracted
			int n = 0; 	//total words extracted by pattern
			
			//Loop through each noun extracted by the caseframe
			for(String noun: caseFrameToCaseMap.get(cf)){
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
	
	public boolean generateMaps(String allCasesFile){
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(allCasesFile));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				line = line.toLowerCase().trim();
				
				String noun = line.split("\\*")[0];
				String cf = line.split("\\*")[1];
				
				//Elimante any attached "of" phrases
				noun = noun.replaceAll(" of .+", "");
				
				//Grab the right most word
				String[] words = noun.split(" +");
				noun = words[words.length - 1];
				
				//Add it if it isn't a stopword
				if(!_stopWords.contains(noun)){
					//Map CFs to Cases
					Set<String> rel = _caseFrameToCaseMap.get(cf);
					
					if(rel == null){
						rel = new HashSet<String>();
					}
					rel.add(noun);
					_caseFrameToCaseMap.put(cf, rel);
				
					
					//Map Cases to CFs
					rel = _caseToCaseFrameMap.get(noun);
					if(rel == null)
						rel = new HashSet<String>();
					rel.add(cf);
					_caseToCaseFrameMap.put(noun, rel);

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
