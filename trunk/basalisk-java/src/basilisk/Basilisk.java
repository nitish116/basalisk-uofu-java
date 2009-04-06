package basilisk;

import java.io.*;
import java.util.*;

public class Basilisk {

	private static String _stopWordsFile = "stopwords.dat";
	
	private int _iterations;
	private boolean _initializedProperly;
	private String _outputPrefix;
	private HashMap<Pattern, Set<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, Set<Pattern>> _extractedNounsToPatternsMap;
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
		

		List<ExtractedNoun> learnedLexicon = new ArrayList<ExtractedNoun>();
		
		
		//Run the bootstrapping 5 times
		for(int i = 0; i < 1; i++){
			System.out.format("Iteration %d\n", i + 1);
			//Iteration trace header
			traceIterationHeader(trace, i+1);
			
			//Score the patterns
			TreeSet<Pattern> scoredPatterns = scorePatterns(_patternsToExtractedNounMap, _seeds);
			
			//Select the top patterns
			TreeSet<Pattern> patternPool = selectTopNPatterns(scoredPatterns, 20 + i);
			
			//Trace the top patterns
			tracePatternPool(trace, patternPool);

			//Gather the nouns that were extracted by the patterns in the pattern pool
			Set<ExtractedNoun> candidateNounPool = selectNounsFromPatterns(patternPool, _patternsToExtractedNounMap);

			//Score the candidate nouns
			TreeSet<ExtractedNoun> scoredNouns = scoreCandidateNouns(candidateNounPool, _extractedNounsToPatternsMap, _patternsToExtractedNounMap, _seeds);
			
			//Select the top extracted nouns
			TreeSet<ExtractedNoun> topNewWords = selectTopNNewCandidateWords(scoredNouns, 5);
			System.out.format("%d new words were added to the lexicon on iteration #%d of bootstrapping.\n", topNewWords.size(), i);
			
			_seeds.addAll(topNewWords);
			learnedLexicon.addAll(topNewWords);
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
		
		for(ExtractedNoun learnedWord: learnedLexicon){
			out.print(learnedWord + "\n");
		}
		out.close();
		trace.close();
	}

	private void traceIterationHeader(PrintStream trace, int iteration) {
		trace.append("************************************************************\n");
		trace.format("                    Iteration %3d\n", iteration);
		trace.append("************************************************************\n\n");
		
	}


	/**
	 * Appends information about patterns in the pattern pool to the output trace.
	 * Looks like: 
	 *   1 PATTERN
     *		Extracted nouns: a, b, c
     *		Num Extracted Nouns: x
     *		Score: x.x
	 * @param trace - PrintStream where the trace file is being written
	 * @param patternPool - a Treeset of patterns (patterns are naturally sorted by score
	 */
	private void tracePatternPool(PrintStream trace, TreeSet<Pattern> patternPool) {
		trace.format("Pattern pool (%d patterns)\n", patternPool.size());
		
		String infoPadding = "     "; //Padding to indent the information below a pattern
		
		//Loop through each pattern in the pattern pool
		int i = 1;
		for(Pattern p: patternPool.descendingSet()){
			//Print: X PATTERNX
			trace.format("%3d %s \n", i, p.toString());
			
			//Print: Extracted nouns:
			trace.format(infoPadding + "Extracted nouns: ");
		
			//Loop through each extracted noun
			int numNouns = _patternsToExtractedNounMap.get(p).size();
			Iterator<ExtractedNoun> nounsIt = _patternsToExtractedNounMap.get(p).iterator();
			for(int j = 0; nounsIt.hasNext(); j++){
				//Print: extractedNounX
				trace.append(nounsIt.next().toStringNoScore());
				
				//Append a comma after every noun except the last
				if(j < numNouns - 1)
					trace.append(", ");
			}
			trace.append("\n");		//New line after printing out nouns
			i++;
			
			//Print: Num Extracted Nouns: x
			trace.format(infoPadding + "Num Extracted Nouns: %d\n", numNouns);
			
			//Print: Score: x.x
			trace.format(infoPadding + "Score: %f\n", p.getScore());
			
			//One last new line between patterns
			trace.append("\n");
		}
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

	private TreeSet<ExtractedNoun> selectTopNNewCandidateWords(TreeSet<ExtractedNoun> scoredNouns, int n) {
		TreeSet<ExtractedNoun> result = new TreeSet<ExtractedNoun>();
		
		//Cycle through the scoredNouns, from high to low, adding to the result as we go
		Iterator<ExtractedNoun> descNounIt = scoredNouns.descendingIterator();
		while(descNounIt.hasNext()){
			result.add(descNounIt.next());
		}
		
		return result;
	}

	private TreeSet<ExtractedNoun> scoreCandidateNouns(Set<ExtractedNoun> candidateNounPool, 
													Map<ExtractedNoun, Set<Pattern>> extractedNouns,
													Map<Pattern, Set<ExtractedNoun>> patterns, 
													Set<Noun> knownCategoryWords) {
		TreeSet<ExtractedNoun> result = new TreeSet<ExtractedNoun>();
		
		//Score each noun in our candidate pool
		for(ExtractedNoun candidateNoun: candidateNounPool){
			double sumLog2 = 0.0; 	//Sumation of (log2(F+1))
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
			double avgScore = (sumLog2/(double) numPatterns);
			ExtractedNoun scored = new ExtractedNoun(candidateNoun._noun);
			scored.setScore(avgScore);
			result.add(scored);
		}
		
		return result;
	}

	private Set<ExtractedNoun> selectNounsFromPatterns(Set<Pattern> patternPool, Map<Pattern, Set<ExtractedNoun>> patterns) {
		Set<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		for(Pattern p: patternPool){
			result.addAll(patterns.get(p));
		}
		
		return result;
	}

	private TreeSet<Pattern> selectTopNPatterns(TreeSet<Pattern> patterns, int n) {
		TreeSet<Pattern> result = new TreeSet<Pattern>();
		
		Iterator<Pattern> it = patterns.descendingIterator();
		for(int i = 0; i < n; i++){
			result.add(it.next());
		}
		return result;
	}

	private TreeSet<Pattern> scorePatterns(Map<Pattern, Set<ExtractedNoun>> patterns, Set<Noun> knownCategoryMembers) {
		
		TreeSet<Pattern> result = new TreeSet<Pattern>();
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
			
			//Store the pattern (with it's new score) into the result
			Pattern scored = new Pattern(p._caseFrame);
			scored.setScore(rLogF);
			result.add(scored);
		}		
		return result;
	}
	
	public boolean generateMaps(String allCasesFile){
		//Initialize both maps. 
		_patternsToExtractedNounMap = new HashMap<Pattern, Set<ExtractedNoun>>();
		_extractedNounsToPatternsMap = new HashMap<ExtractedNoun, Set<Pattern>>();
		
		
		//Read in the cases file
		try{
			BufferedReader br = new BufferedReader(new FileReader(allCasesFile));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				line = line.trim();
				
				//An asteric separates the extracted noun from the caseframe that extracted it
				String noun = line.split("\\*")[0].trim();
				String cf = line.split("\\*")[1].trim();
				
				//Process the caseframe
				Pattern p = new Pattern(cf);								
				
				//Process the noun
				noun = noun.replaceAll(" [oO][fF] .+", ""); //Elimante any attached "of" phrases
				String[] words = noun.split(" +"); 			//Grab the right most word (head noun)
				noun = words[words.length - 1].trim();
				ExtractedNoun n = new ExtractedNoun(noun);
				
				//Add it if it isn't a stopword
				if(!_stopWords.contains(n)){
					//Map CFs to Cases
					Set<ExtractedNoun> nouns = _patternsToExtractedNounMap.get(p);
					if(nouns == null){
						nouns = new HashSet<ExtractedNoun>();
					}
					nouns.add(n);
					_patternsToExtractedNounMap.put(p, nouns);
				
					
					//Map Cases to CFs
					Set<Pattern> patterns = _extractedNounsToPatternsMap.get(n);
					if(patterns == null)
						patterns = new HashSet<Pattern>();
					patterns.add(p);
					_extractedNounsToPatternsMap.put(n, patterns);
					if(n.equals(new ExtractedNoun("country")) && _extractedNounsToPatternsMap.get(new ExtractedNoun("country")) != null){
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
		if(_extractedNounsToPatternsMap.get(new ExtractedNoun("country")) != null)
				System.out.println("country detected in noun map");
		return true;
	}

}
