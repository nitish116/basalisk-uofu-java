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
	private ArrayList<Set<Noun>> _listsOfKnownCategoryWords;
	private ArrayList<String> _outputPrefixList;
	private Set<Noun> _stopWords;

	
	/**
	 * Initialize basilisk.
	 * 
	 * Input arg ordering:
	 *    <category_seeds_slist> <all_cases_file> 
	 *    			-n <num_iterations>
	 * @param args
	 */
	public static void main(String[] args) {
		//Check input arguments
		if(args.length < 2){
			System.err.println("Missing input arguments: <category_seeds_slist> <all_cases_file>");
			return;
		}
		String categorySeedsSlistFile = args[0];
		String allCasesFile = args[1];
		
		System.out.println("Starting basilisk.\n");
		
		//Check for optional input arguments
		int argsSeen = 2;
		
		//Set default values
		int iterations = 5;
		
		while ((argsSeen + 2) <= args.length){
			if(args[argsSeen].equals("-n"))
				iterations = Integer.parseInt(args[argsSeen+1]);
			else{
				System.err.println("Unknown input options: " + args[argsSeen] + args[argsSeen+1]);
			}
			argsSeen += 2;
		}


		//Initialize the data
		Basilisk b1 = new Basilisk(categorySeedsSlistFile, allCasesFile, iterations);
		if(!b1._initializedProperly)
			return;
		//Start the bootstrapping
		b1.bootstrap();

		
	}
	
	
	public Basilisk(String categorySeedsSlistFile, String allCasesFile, int iterations){
		_iterations = iterations;
		System.out.println("Bootstrapping iterations: " + _iterations);
		
		//Initialize the output prefix list
		_outputPrefixList = new ArrayList<String>();
		
		
		//Generate the seed lists and the stopword list
		System.out.println("Loading data.\n");
		_listsOfKnownCategoryWords = loadCategoriesFromSList(categorySeedsSlistFile);
		_stopWords = loadSet(_stopWordsFile);
		
		//Map caseframes to cases and vice versa
		System.out.println("Generating dictionary files");
		if(!generateMaps(allCasesFile))
				_initializedProperly = false;
		_initializedProperly = true;
	}
	
	private ArrayList<Set<Noun>> loadCategoriesFromSList(String categorySeedsSlistFile) {
		ArrayList<Set<Noun>> result = new ArrayList<Set<Noun>>();
		
		File f = new File(categorySeedsSlistFile);
		
		if(!f.exists()){
			System.err.println("Error proccessing list of seed files. File could not be found: " + f.getAbsolutePath());
			return null;
		}
		
		Scanner in = null;
		
		try{
			in = new Scanner(f);
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		//The first line is the directory information
		String dir = in.nextLine().trim();
		
		while(in.hasNextLine()){
			//Grab the file name from the slist
			String fileName = in.nextLine().trim();
			
			//Record the output prefix
			String outputPrefix = fileName.replaceAll(".*/", ""); 	//remove subdirectories if specified
			outputPrefix =  outputPrefix.replaceAll("\\..+", ""); //remove extensions
			
			//Record the output in the output list
			_outputPrefixList.add(outputPrefix);
			
			//Load the set from the file
			result.add(loadSet(dir + fileName));
		}
		
		return result;
	}


	/**
	 * After a new instance of Basilisk has been created, this method is used to initialize the bootstrapping process.
	 */
	public void bootstrap(){
		System.out.println("Starting bootstrapping.\n");
		
		//Initialize a list of traces - one for each lexicon (which can be gathered from the output prefix list
		ArrayList<PrintStream> tracesList = new ArrayList<PrintStream>();
		for(String outputPrefix: _outputPrefixList){
			PrintStream newTrace = null;
			try{
				newTrace = new PrintStream(outputPrefix + ".trace");
				tracesList.add(newTrace);
			}
			catch (Exception e){
				System.err.println(e.getMessage());
			}
		}

		//Initialize a list to keep track of all the new words for each category
		ArrayList<List<ExtractedNoun>> learnedLexicons = new ArrayList<List<ExtractedNoun>>();
		for(int j = 0; j < _listsOfKnownCategoryWords.size(); j++){
			learnedLexicons.add(new ArrayList<ExtractedNoun>());
		}
		
		
		
		//Run the bootstrapping 5 times
		for(int i = 0; i < _iterations; i++){
			System.out.format("Iteration %d\n", i + 1);
			
			//Iteration trace header
			for(PrintStream trace: tracesList){
				traceIterationHeader(trace, i+1);
			}			
			
			//Score the patterns for each category
			ArrayList<TreeSet<Pattern>> listsOfScoredPatterns = new ArrayList<TreeSet<Pattern>>();
			for(Set<Noun> knownWords: _listsOfKnownCategoryWords){
				listsOfScoredPatterns.add(scorePatterns(_patternsToExtractedNounMap, knownWords));
			}
			
			//Select the top patterns for each category
			ArrayList<TreeSet<Pattern>> listsOfPatternPools = new ArrayList<TreeSet<Pattern>>();
			for(int j = 0; j < listsOfScoredPatterns.size(); j++){
				TreeSet<Pattern> scoredPatterns = listsOfScoredPatterns.get(j);
				TreeSet<Pattern> patternPool = selectTopNPatterns(scoredPatterns, 20 + i);
				listsOfPatternPools.add(patternPool);
				
				//Trace the top patterns
				tracePatternPool(tracesList.get(j), patternPool);
			}

			//Gather the nouns that were extracted by the patterns in each pattern pool
			ArrayList<Set<ExtractedNoun>> listsOfCandidateNounPools = new ArrayList<Set<ExtractedNoun>>();
			for(TreeSet<Pattern> patternPool: listsOfPatternPools){
				listsOfCandidateNounPools.add(selectNounsFromPatterns(patternPool, _patternsToExtractedNounMap));
			}

			//Score the candidate nouns inside of each candidate noun pool
			ArrayList<HashSet<ExtractedNoun>> listsOfScoredNouns = new ArrayList<HashSet<ExtractedNoun>>();
			for(int j = 0; j <  listsOfCandidateNounPools.size(); j++){
				Set<ExtractedNoun> candidateNounPool = listsOfCandidateNounPools.get(j);
				listsOfScoredNouns.add(scoreCandidateNouns(candidateNounPool, _extractedNounsToPatternsMap, _patternsToExtractedNounMap, _listsOfKnownCategoryWords.get(j)));
			}

			//Select the top extracted nouns from each list - one category per sense
			//Once we're done, add the new words to the parallel list of known words
			ArrayList<TreeSet<ExtractedNoun>> listsOfTopNewWords = new ArrayList<TreeSet<ExtractedNoun>>();
			for(int j = 0; j < listsOfScoredNouns.size(); j++){
				HashSet<ExtractedNoun> scoredNouns = listsOfScoredNouns.get(j);
				
				//Select the top nouns from the current scored list of nouns
				TreeSet<ExtractedNoun> topNewWords = avgDiffSelectTopNNewCandidateWords(scoredNouns, j, listsOfScoredNouns, 5);
				listsOfTopNewWords.add(topNewWords);
				
				//Trace the top nouns
				traceNewNouns(tracesList.get(j), topNewWords,_listsOfKnownCategoryWords.get(j));

				System.out.format("Adding %d new words to the %s lexicon.\n", topNewWords.size(), _outputPrefixList.get(j));
				
				//Print out new words to the console
				for(ExtractedNoun en: topNewWords.descendingSet()){
					System.out.println("\t" + en.toString());
				}
				System.out.println("");
				
				//Add the new words to the known category member list
				_listsOfKnownCategoryWords.get(j).addAll(topNewWords);
				

				//Add the new words to the list containing all new words
				learnedLexicons.get(j).addAll(topNewWords);
			}	
		}

		//Print out the list of learned words to their own files
		for(int j = 0; j < _listsOfKnownCategoryWords.size(); j++){
			PrintStream out = null; 
			try {
				out = new PrintStream(_outputPrefixList.get(j) + ".lexicon");
			}
			catch (Exception e){
				System.err.println(e.getMessage());
			}
			
			for(ExtractedNoun learnedWord: learnedLexicons.get(j)){
				out.print(learnedWord + "\n");
			}
		}
		
		//Close the traces
		for(PrintStream trace: tracesList){
			trace.close();
		}
	}

	/**
	 * Uses the avgDiff selection criteria to try and guide the selection process for words that are strongly favored by one
	 * category. 
	 *  Each word wi in the candidate word pool receives a score for category ca based on the following formula: 
	 *  	diff(wi,ca) = AvgLog(wi,ca) - max (AvgLog(wi,cb)), where a != b
	 *  
	 *  Essentially takes a given word, recomputes it's score if it's been found and scored by other categories, and only 
	 *  returns the word if it's strongly associated with it's given category.
	 *  
	 * @param scoredNouns - List of scored nouns for which we want to recompute the diff score value
	 * @param catNumber - Category number associated with this list of scored nouns. Since category lists are generated in order
	 * 						category list at index 0, should always be associated with the same category
	 * @param listsOfScoredNouns - List containing all of the scored candidate nouns, used to calculate the different score
	 * @param n - number of nouns to return in this list
	 * @return - List of the top scored nouns in this list.
	 */
	private TreeSet<ExtractedNoun> avgDiffSelectTopNNewCandidateWords( 	HashSet<ExtractedNoun> scoredNouns, int catNumber, 
																		ArrayList<HashSet<ExtractedNoun>> listsOfScoredNouns, 
																		int n) {
//		Look at a word
//		Cycle through all the other sets, calculating maximum score
//		Set new score in result, as currScore-maxScore
//		Take the top words, as long as the score > 0
		
		TreeSet<ExtractedNoun> diffScoredNouns = new TreeSet<ExtractedNoun>();
		
		//Examine each word in the current set to rescore it
		for(ExtractedNoun en: scoredNouns){
			//Cycle every OTHER set (i.e., sets that don't have the same category number) to compute max score in other cats
			double maxScore = 0.0;
			for(int i = 0; i < listsOfScoredNouns.size(); i++){
				if(i == catNumber)
					continue;
				
				//Check to see if the other sets even contain the given word
				if(listsOfScoredNouns.get(i).contains(en)){
					//If they do, find out it's score, and update maxscore
					for(ExtractedNoun otherNoun: listsOfScoredNouns.get(i)){
						if(otherNoun.equals(en)){
							maxScore = Math.max(maxScore, otherNoun.getScore());
						}
					}
				}
			}
			
			//Finally, add the oldScore-newScore to the diffScore list
			ExtractedNoun diffScored = new ExtractedNoun(en._noun);
			diffScored.setScore(en.getScore() - maxScore);
			diffScoredNouns.add(diffScored);
		}
		
		
		//Finally, choose the top N words from the diff scored list, so long as they are greater than 0 and not already known
		TreeSet<ExtractedNoun> result = new TreeSet<ExtractedNoun>();
		Iterator<ExtractedNoun> diffScoreIt = diffScoredNouns.descendingIterator();
		for(int wordsAdded = 0; wordsAdded < n && diffScoreIt.hasNext();){
			ExtractedNoun diffScored = diffScoreIt.next();
			
			//Check to see if it's in the list of known words
			boolean alreadyKnown = false;
			for(Set<Noun> knownWords: _listsOfKnownCategoryWords){
				//If it is, continue to the next word
				if(knownWords.contains(diffScored)){
					alreadyKnown = true;
					break;
				}	
			}
			//If so, continue to the next word
			if(alreadyKnown)
				continue;
			
			//If it's not, and it's score >= 0, add it to the result
			if(diffScored.getScore() >= 0){
				result.add(diffScored);
				wordsAdded++;
			}
		}
		return result;
	}


	/**
	 *  Take the list of scored words. Sort it. Start from the top. Take the highest scored word, and compare 
	 *	it against all the known category words. If it's already known, continue to the next word. If not, check the word against 
	 *	all the other scored words in other lists. Make sure that score is greater than equal to the score in all other 
	 *	scored words lists. If not, continue to the next word. If it is, add that word to the list. Repeat until we have i words.
	 * 
	 * @param scoredNouns - List of scored words to choose the best i words from
	 * @param listsOfScoredNouns - Lists of all the other scored nouns, to make sure we have the highest scored word
	 * @param i - number of best words to return
	 * @return List of the top i scored new words, with no duplicates and no words that have already been learned before.
	 */
	private TreeSet<ExtractedNoun> selectTopNNewCandidateWords( HashSet<ExtractedNoun> scoredNouns,
																ArrayList<HashSet<ExtractedNoun>> listsOfScoredNouns, 
																int i) {

		TreeSet<ExtractedNoun> result = new TreeSet<ExtractedNoun>();
		
		//Sort the list of extracted nouns that we are currently examining. 
		TreeSet<ExtractedNoun> sortedScoredNouns = new TreeSet<ExtractedNoun>();
		for(ExtractedNoun en: scoredNouns){
			sortedScoredNouns.add(en);
		}
		
		//Iterate through the list of sorted extracted nouns, starting with the one with the highest score
		for(ExtractedNoun en: sortedScoredNouns.descendingSet()){
			boolean alreadyKnown = false;
			
			//Check to see if it's in the list of known words
			for(Set<Noun> knownWords: _listsOfKnownCategoryWords){
				//If it is, continue to the next word
				if(knownWords.contains(en)){
					alreadyKnown = true;
					break;
				}	
			}
			
			//If the word is already known, continue to the next word
			if(alreadyKnown) continue;
			
			//Otherwise, check to make sure the word has the highest (or tied for highest) score compared to all other known scored words 
			//for this iteration
			boolean hasHighestScore = true;
			for(HashSet<ExtractedNoun> otherScoredNouns: listsOfScoredNouns){
				//Iterate through the list until we find the match (damn java - no "get" method for hashsets)
				for(ExtractedNoun otherNoun: otherScoredNouns){
					if(otherNoun.equals(en)){
						if(en.getScore() < otherNoun.getScore()){
							hasHighestScore = false;
							break;
						}
					}
				}
				if(!hasHighestScore) break;
			}
			
			//If it doesn't have the highest score, continue to the next word
			if(!hasHighestScore) continue;
			
			//Otherwise, we're looking at a new, highest scoring word !!Add it to the set already
			result.add(en);
			
			//If we've filled up our list, break out of the loop
			if(result.size() == i )
				break;
		}
		return result;
	}


	private void traceNewNouns(PrintStream trace, TreeSet<ExtractedNoun> topNewWords, Set<Noun> knownCategoryMembers) {
		//Append header
		trace.format("Top %d Candidate Nouns\n\n", topNewWords.size());
		
		//Loop through each new extracted noun in the list
		Iterator<ExtractedNoun> nnIt = topNewWords.descendingIterator();
		while(nnIt.hasNext()){
			ExtractedNoun newNoun = nnIt.next();
			trace.format("\tNoun: %s\n", newNoun.toString());
			trace.format("\tPattern: num_known_words_extracted: log2(1+num_known_words_extracted)\n");
			
			//Loop through each pattern that extracted the new noun
			for(Pattern newNounExtractor: _extractedNounsToPatternsMap.get(newNoun)){
				//Count how many known category members the pattern extracted
				int knownExtractions = 0;
				for(ExtractedNoun possibleKnownMember: _patternsToExtractedNounMap.get(newNounExtractor)){
					if(knownCategoryMembers.contains(possibleKnownMember))
						knownExtractions++;
				}
				
				//Print out each pattern, and the number of known category members it extracted
				trace.format("\t\t%40s :%3d : %f\n",  newNounExtractor.toString(), knownExtractions, Math.log(1+knownExtractions)/Math.log(2));
			}
			
			//Print out the number of patterns that extracted this new noun
			trace.format("\tNum of patterns: %d\n", _extractedNounsToPatternsMap.get(newNoun).size());
			
			//Print out the score of this new noun
			trace.format("\tScore: %f\n\n", newNoun.getScore());
		}
		
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
				trace.append(nounsIt.next().toString());
				
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
		for(int i = 0; descNounIt.hasNext() && i < n;){
			ExtractedNoun en = descNounIt.next();
			if(!_seeds.contains(en)){
				result.add(en);
				i++;
			}
		}
		return result;
	}

	private HashSet<ExtractedNoun> scoreCandidateNouns(Set<ExtractedNoun> candidateNounPool, 
													Map<ExtractedNoun, Set<Pattern>> extractedNouns,
													Map<Pattern, Set<ExtractedNoun>> patterns, 
													Set<Noun> knownCategoryWords) {
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		
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
				
				//An asteric separates the extracted noun from the pattern that extracted it
				String noun = line.split("\\*")[0].trim();
				String pattern = line.split("\\*")[1].trim();
				
				//Process the pattern
				Pattern p = new Pattern(pattern);								
				
				//Process the noun
				noun = noun.replaceAll(" [oO][fF] .+", ""); //Elimante any attached "of" phrases
				String[] words = noun.split(" +"); 			//Grab the right most word (head noun)
				noun = words[words.length - 1].trim();
				
				//Check to see if the extracted noun was a number (encoded by && preceding the digits)..if so, continue to next word
				if(noun.contains("&&")){
					continue;
				}
				ExtractedNoun n = new ExtractedNoun(noun);
				
				//Add it if it isn't a stopword
				if(!_stopWords.contains(n)){
					//Map patterns to extracted nouns
					Set<ExtractedNoun> nouns = _patternsToExtractedNounMap.get(p);
					if(nouns == null){
						nouns = new HashSet<ExtractedNoun>();
					}
					nouns.add(n);
					_patternsToExtractedNounMap.put(p, nouns);
				
					
					//Map extracted nouns to patterns
					Set<Pattern> patterns = _extractedNounsToPatternsMap.get(n);
					if(patterns == null)
						patterns = new HashSet<Pattern>();
					patterns.add(p);
					_extractedNounsToPatternsMap.put(n, patterns);
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
