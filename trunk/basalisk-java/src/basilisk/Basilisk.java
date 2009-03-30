package basilisk;

import java.io.*;
import java.util.*;

public class Basilisk {

	private static String _stopWordsFile = "stopwords.dat";
	
	private int _iterations;
	private boolean _initializedProperly;
	private String _outputPrefix;
	private TreeMap<Pattern, Set<ExtractedNoun>> _patterns;
	private TreeMap<ExtractedNoun, Set<Pattern>> _extractedNouns;
	private Set<Noun> _seeds;
	private Set<Noun> _stopWords;

	
	/**
	 * Initialize basilisk.
	 * 
	 * Input arg ordering:
	 *    <seed_file> <all_cases_file>
	 *    			-n <num_iterations>
	 * @param args
	 */
	public static void main(String[] args) {
		//Check input arguments
		if(args.length < 2){
			System.err.println("Missing input arguments: <seed_file> <all_cases_file>");
			return;
		}
		String seedFile = args[0];
		String allCasesFile = args[1];
		
		System.out.println("Starting basilisk.\n");
		
		//Check for optional input arguments
		int argsSeen = 2;
		
		//Set default values
		int iterations = 5;
		
		while ((argsSeen + 2) < args.length){
			if(args[argsSeen].equals("-n"))
				iterations = Integer.parseInt(args[argsSeen+1]);
			else{
				System.err.println("Unknown input options: " + args[argsSeen] + args[argsSeen+1]);
			}
			argsSeen += 2;
		}


		//Initialize the data
		Basilisk b1 = new Basilisk(seedFile, allCasesFile, iterations);
		if(!b1._initializedProperly)
			return;
		//Start the bootstrapping
		b1.bootstrap();

		
	}
	
	
	public Basilisk(String seedFile, String allCasesFile, int iterations){
		_iterations = iterations;
		System.out.println("Bootstrapping iterations: " + _iterations);
		
		//Calculate output prefix from root of seed file name
		_outputPrefix = seedFile.replaceAll(".*/", ""); 	//remove subdirectories if specified
		_outputPrefix = _outputPrefix.replaceAll("\\..+", ""); //remove extensions
		System.out.println("Root name of input file: " + _outputPrefix);
		
		//Generate the seed list and the stopword list
		System.out.println("Loading data.\n");
		_seeds = loadSet(seedFile);
		_stopWords = loadSet(_stopWordsFile);
		
		//Map caseframes to cases and vice versa
		System.out.println("Generating dictionary files");
		if(!generateMaps(allCasesFile))
				_initializedProperly = false;	
		_initializedProperly = true;
	}
	
	
	public void bootstrap(){
		System.out.println("Starting bootstrapping.\n");
		//Initialize the trace printstream writer
		PrintStream trace = null;
		try {
			trace = new PrintStream(_outputPrefix + ".trace");
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		

		List<String> learnedLexicon = new ArrayList<String>();
		
		
		//Run the bootstrapping 5 times
		for(int i = 0; i < 1; i++){
			//Score the caseFrameList
			Pattern topP = new Pattern("InfVp_Prep_<NP>__ADVANCE_IN_26");
			topP.setScore(1.666d);
			Set<ExtractedNoun> topNouns = _patterns.get(new Pattern("InfVp_Prep_<NP>__ADVANCE_IN_26"));
			
			_patterns = scorePatterns(_patterns, _seeds);
			NavigableMap<Pattern, Set<ExtractedNoun>> rm = _patterns.descendingMap();
			topNouns = _patterns.get(new Pattern("InfVp_Prep_<NP>__ADVANCE_IN_26"));
			//Select the top rated caseframes
			TreeSet<Pattern> patternPool = selectTopNPatterns(_patterns, 20 + i);
			System.out.println(patternPool.descendingSet());
			tracePatternPool(patternPool, trace);
//			System.out.println("Pattern pool size: " + patternPool.size());
//			//Form the candidate pool from the pattern pool
//			Set<String> candidateNounPool = selectNounsFromCaseFrames(patternPool, _patterns);
//			System.out.println(candidateNounPool);
//			System.out.println("Candidate noun pool size: " + candidateNounPool.size());
//			//Score the candidate nouns
//			Map<String, Double> candidateWordToScoreMap = scoreCandidateNouns(candidateNounPool, _nouns, _patterns, _seeds);
//			//Add the top 5 candidate nouns to the lexicon
//			List<String> topNewWords = selectTopNNewCandidateWords(candidateWordToScoreMap, 5);
//			
//			System.out.printf("%d new words were added to the lexicon on iteration #%d of bootstrapping.\n", topNewWords.size(), i);
//			
//			_seeds.addAll(topNewWords);
//			learnedLexicon.addAll(topNewWords);
		}
		System.out.println(learnedLexicon);
		System.out.println("Learned lexicon size:" + learnedLexicon.size());

		//Print out the list of learned words to a file
		PrintStream out = null;
		try {
			out = new PrintStream("learned-locations.txt");
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		for(String learnedWord: learnedLexicon){
			out.print(learnedWord + "\n");
		}
		out.close();
		trace.close();
	}

	private void tracePatternPool(TreeSet<Pattern> patternPool, PrintStream trace) {
		trace.format("Pattern pool (%d patterns)\n", patternPool.size());
		
		NavigableMap<Pattern, Set<ExtractedNoun>> reverseMap = _patterns.descendingMap();
		
		int i = 1;
		for(Pattern p: patternPool.descendingSet()){
			trace.format("%3d %s", i, p.toString());
			trace.append("Extracted nouns: ");
			Set<ExtractedNoun> en = _patterns.get(p);
			for(ExtractedNoun extractedNoun: _patterns.get(p)){
				trace.append(extractedNoun.toString());
			}
			
			i++;
		}
//		//Sort the pattern pool by descending rlogf value
//		List<Map.Entry<String, Double>> sortByValue = new ArrayList<Map.Entry<String, Double>>(patternPool.entrySet());
//		Collections.sort(sortByValue, new Comparator<Map.Entry>(){
//			public int compare(Map.Entry e1, Map.Entry e2){
//				Double d1 = (Double) e1.getValue();
//				Double d2 = (Double) e2.getValue();
//				
//				return d2.compareTo(d1);
//			}
//		});
//		
//		System.out.println(sortByValue);
//		for(int i = 0; i < n; i++){
//			result.add(sortByValue.get(i).getKey());
//		}
		
	}


	private Set<Noun> loadSet(String listOfWords) {
		File f = new File(listOfWords);
		
		if(!f.exists()){
			System.err.println("Input file could not be found: " + f.getAbsolutePath());
			return null;
		}
		
		Set<Noun> result = new HashSet<Noun>();
		
		Scanner in = null;
		
		try{
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
			return null;
		}
		
		while(in.hasNextLine()){
			result.add(new Noun(in.nextLine().toLowerCase().trim()));
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
			if(!_seeds.contains(word) && !result.contains(word)){
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

	private void scoreCandidateNouns(Set<ExtractedNoun> candidateNounPool, 
													Map<ExtractedNoun, Set<Pattern>> extractedNouns,
													Map<Pattern, Set<ExtractedNoun>> patterns, 
													Set<Noun> knownCategoryWords) {
		
		//Score each noun in our candidate pool
		for(ExtractedNoun candidateNoun: candidateNounPool){
			double sumLog2 = 0.0; //Sumation of (log2(F+1))
			int numPatterns = 0; 	//P number of patterns that extracted candidate noun
			
			//Loop through each pattern that extracted the given noun
			//Incrementing the score by log2(F+1), where F is the number of known
			//category words that pattern extracted
			for(Pattern candidatePattern: extractedNouns.get(candidateNoun)){
				int FPlusOne = 1;
				//Loop through each noun extracted by the pattern, checking to see if it's a known word
				for(ExtractedNoun extractedNoun: patterns.get(candidatePattern)){
					if(knownCategoryWords.contains(extractedNoun))
						FPlusOne++;
				}
				sumLog2 += Math.log(FPlusOne)/Math.log(2);
				numPatterns++;
			}
			//Average the nounScore over the number of patterns that contributed to it
			candidateNoun.setScore((sumLog2/(double) numPatterns));
		}
	}

	private Set<String> selectNounsFromCaseFrames(Set<String> patternPool, Map<String, Set<String>> caseFrameToCaseMap) {
		Set<String> result = new HashSet<String>();
		for(String cf: patternPool){
			result.addAll(caseFrameToCaseMap.get(cf));
		}
		
		return result;
	}

	private TreeSet<Pattern> selectTopNPatterns(TreeMap<Pattern, Set<ExtractedNoun>> patterns, int n) {
		TreeSet<Pattern> result = new TreeSet<Pattern>();
		
		Iterator<Pattern> it = patterns.descendingMap().keySet().iterator();
		for(int i = 0; i < n; i++){
			result.add(it.next());
		}
		return result;
	}

	private TreeMap<Pattern, Set<ExtractedNoun>> scorePatterns(Map<Pattern, Set<ExtractedNoun>> patterns, Set<Noun> knownCategoryMembers) {
		
		TreeMap<Pattern, Set<ExtractedNoun>> result = new TreeMap<Pattern, Set<ExtractedNoun>>();
		//Score each case frame
		for(Pattern p: patterns.keySet()){
			if(p.equals(new Pattern("Np_Prep_<NP>__JOURNALISTS_IN_1039"))){
				System.out.println("2nd to top pattern in iteration one reached: Np_Prep_<NP>__JOURNALISTS_IN_1039");
				System.out.println(patterns.get(p));
			}
			if(p.equals(new Pattern("InfVp_Prep_<NP>__ADVANCE_IN_26"))){
				System.out.println("1st to top pattern in iteration one reached: InfVp_Prep_<NP>__ADVANCE_IN_26" );
				System.out.println(patterns.get(p));
			}
			if(p.equals(new Pattern("ActVp_Prep_<NP>__LAUNCHED_THROUGHOUT_2342"))){
				System.out.println("2nd pattern for me in first iterations: ActVp_Prep_<NP>__LAUNCHED_THROUGHOUT_2342" );
				System.out.println(patterns.get(p));
			}
			int f = 0;	//total number of known category members extracted
			int n = 0; 	//total words extracted by pattern
			
			//Loop through each noun extracted by the caseframe
			for(ExtractedNoun noun: patterns.get(p)){
				if(knownCategoryMembers.contains(noun)){
					f++;
				}
				n++; 
			}
			
			double rLogF = (f/(double) n)*(Math.log(f)/Math.log(2));
			
			//If we get -infinity as a result, change it to -1
			if(Double.compare(rLogF, Double.NaN) == 0)
				rLogF = -1.0;
			
			//Set the patterns score
			Pattern scored = new Pattern(p._caseFrame);
			scored.setScore(rLogF);
			result.put(scored, patterns.get(scored));
		}		
		return result;
	}
	
	public boolean generateMaps(String allCasesFile){
		//Initialize both maps. Note: default odering is ascending (lower scores first).
		_patterns = new TreeMap<Pattern, Set<ExtractedNoun>>();
		_extractedNouns = new TreeMap<ExtractedNoun, Set<Pattern>>();
		
		
		//Read in the cases file
		try{
			BufferedReader br = new BufferedReader(new FileReader(allCasesFile));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				line = line.trim();
				
				//An asteric separates the extracted noun from the caseframe that extracted it
				String noun = line.split("\\*")[0].trim();
				String cf = line.split("\\*")[1].trim();
				
//				if(cf.equalsIgnoreCase("Np_Prep_<NP>__SECTOR_OF_434"))
//					System.out.println("parsing known country extractor");
				
				//Process the caseframe
				Pattern p = new Pattern(cf);
				
				
				
				//Process the noun
				noun = noun.replaceAll(" [oO][fF] .+", ""); //Elimante any attached "of" phrases
				String[] words = noun.split(" +"); //Grab the right most word (head noun)
				noun = words[words.length - 1].trim();
				ExtractedNoun n = new ExtractedNoun(noun);
//				if(n.equals(new ExtractedNoun("country")))
//					System.out.println("Country extracted as noun");
				
				//Add it if it isn't a stopword
				if(!_stopWords.contains(n)){
					//Map CFs to Cases
					Set<ExtractedNoun> nouns = _patterns.get(p);
					if(nouns == null){
						nouns = new HashSet<ExtractedNoun>();
					}
					nouns.add(n);
					_patterns.put(p, nouns);
				
					
					//Map Cases to CFs
					Set<Pattern> patterns = _extractedNouns.get(n);
					if(patterns == null)
						patterns = new HashSet<Pattern>();
					patterns.add(p);
					_extractedNouns.put(n, patterns);
					if(n.equals(new ExtractedNoun("country")) && _extractedNouns.get(new ExtractedNoun("country")) != null){
						//System.out.println(_extractedNouns.get(new ExtractedNoun("country")));
					}
				}
			}
			br.close();
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			return false;
		}
		if(_extractedNouns.get(new ExtractedNoun("country")) != null)
				System.out.println("country detected in noun map");
		return true;
	}

}
