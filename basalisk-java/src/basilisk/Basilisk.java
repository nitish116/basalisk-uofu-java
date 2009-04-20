package basilisk;

import java.io.*;
import java.util.*;

public class Basilisk {

	private static String _stopWordsFile = "stopwords.dat";
	
	/**
	 * Initialize basilisk.
	 * 
	 * Input arg ordering:
	 *    <category_seeds_slist> <all_cases_file> 
	 *    			-n <num_iterations>
	 *    			-c 0/1 (0: simple conflict resolution; 1: improved conflict resolution
	 *    			-o output-dir/
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
		String outputDir = "";
		boolean useImprovedConflictResolution = false;
		
		while ((argsSeen + 2) <= args.length){
			if(args[argsSeen].equalsIgnoreCase("-n"))
				iterations = Integer.parseInt(args[argsSeen+1]);
			else if(args[argsSeen].equalsIgnoreCase("-o"))
				outputDir = args[argsSeen+1];
			else if(args[argsSeen].equalsIgnoreCase("-c"))
				if(Integer.parseInt(args[argsSeen+1]) == 1)
					useImprovedConflictResolution = true;
				else useImprovedConflictResolution = false;
			else{
				System.err.println("Unknown input options: " + args[argsSeen] + " " + args[argsSeen+1]);
			}
			argsSeen += 2;
		}


		//Initialize basilisk
		Basilisk b1 = new Basilisk(categorySeedsSlistFile, allCasesFile, iterations, outputDir, useImprovedConflictResolution);
	}
	
	private int _iterations;
	private boolean _useImprovedConflictResolution;
	private HashMap<Pattern, HashSet<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, HashSet<Pattern>> _extractedNounsToPatternsMap;
	private HashMap<String, HashSet<Noun>> _listsOfKnownCategoryWords;
	private ArrayList<String> _outputPrefixList;

	
	private HashSet<Noun> _stopWords;
	
	//Used for debugging
	protected Basilisk(){
	
	}
	
	public Basilisk(String categorySeedsSlistFile, 
					String allCasesFile, 
					int iterations, 
					String outputDir, 
					boolean useImprovedConflictResolution){
		_iterations = iterations;
		System.out.println("Bootstrapping iterations: " + _iterations);
		
		_useImprovedConflictResolution = useImprovedConflictResolution;
		if(_useImprovedConflictResolution)
			System.out.println("Using improved multiple category conflict resolution");
		else 
			System.out.println("Using simple multiple category conflict resolution");
		//Initialize the output prefix list
		_outputPrefixList = new ArrayList<String>();
		
		
		//Generate the seed lists and the stopword list
		System.out.println("Loading data.\n");
		_listsOfKnownCategoryWords = loadCategoriesFromSList(categorySeedsSlistFile);
		_stopWords = loadSet(_stopWordsFile);
		
		//Map nouns to patterns and vice versa
		System.out.println("Attempting to read input files.");
		if(!generateMaps(allCasesFile)){
			System.out.println("Unable to initialize basilisk data properly. Check the formatting of input files");
			return;
		}
		else{
			System.out.println("Finished reading input files.");
			System.out.format("Discovered %d head nouns and %d patterns\n", _extractedNounsToPatternsMap.size(), _patternsToExtractedNounMap.size());
		}
		
		bootstrap(outputDir);
	}
	
	/**
	 * After a new instance of Basilisk has been created, this method is used to initialize the bootstrapping process.
	 */
	public void bootstrap(String outputDir){
		System.out.println("Starting bootstrapping.\n");
		
		//Initialize a list of traces - one for each lexicon (which can be gathered from the output prefix list
		HashMap<String, PrintStream> tracesList = new HashMap<String, PrintStream>();
		for(String outputPrefix: _outputPrefixList){
			PrintStream newTrace = null;
			try{
				newTrace = new PrintStream(outputDir + outputPrefix + ".trace");
				tracesList.put(outputPrefix, newTrace);
			}
			catch (Exception e){
				System.err.println(e.getMessage());
			}
		}

		//Initialize a list to keep track of all the new words for each category
		HashMap<String, List<ExtractedNoun>> learnedLexicons = new HashMap<String, List<ExtractedNoun>>();	
		for(String category: _listsOfKnownCategoryWords.keySet()){
			learnedLexicons.put(category, new ArrayList<ExtractedNoun>());
		}
		
		
		//Run the bootstrapping _iteration times
		for(int i = 0; i < _iterations; i++){
			System.out.format("Iteration %d\n", i + 1);
			if(i == 66){
				System.out.println("Why aren't you adding any buildings?");
			}
			
			//Iteration trace header
			for(PrintStream trace: tracesList.values()){
				traceIterationHeader(trace, i+1);
			}			
			
			//Score the patterns for each category
			HashMap<String, HashSet<Pattern>> listsOfScoredPatterns = new HashMap<String, HashSet<Pattern>>();
			for(String category: _listsOfKnownCategoryWords.keySet()){
				HashSet<Noun> knownWords = _listsOfKnownCategoryWords.get(category);
				listsOfScoredPatterns.put(category, scorePatterns(_patternsToExtractedNounMap, knownWords));
			}
			
			//Select the top patterns for each category
			HashMap<String, HashSet<Pattern>> listsOfPatternPools = new HashMap<String, HashSet<Pattern>>();
			for(String category: listsOfScoredPatterns.keySet()){
				HashSet<Pattern> scoredPatterns = listsOfScoredPatterns.get(category);
				HashSet<Pattern> patternPool = selectTopNPatterns(scoredPatterns, 20 + i, _patternsToExtractedNounMap, _listsOfKnownCategoryWords);
				listsOfPatternPools.put(category, patternPool);
				
				//Trace the top patterns
				tracePatternPool(tracesList.get(category), patternPool);
			}

			//Gather the nouns that were extracted by the patterns in each pattern pool
			HashMap<String, HashSet<ExtractedNoun>> listsOfCandidateNounPools = new HashMap<String, HashSet<ExtractedNoun>>();
			for(String category: listsOfPatternPools.keySet()){
				HashSet<Pattern> patternPool = listsOfPatternPools.get(category);
				listsOfCandidateNounPools.put(category, selectNounsFromPatterns(patternPool, _patternsToExtractedNounMap));
			}

			//Score the candidate nouns inside of each candidate noun pool
			HashMap<String, HashSet<ExtractedNoun>> listsOfScoredNouns = new HashMap<String, HashSet<ExtractedNoun>>();
			for(String category: listsOfCandidateNounPools.keySet()){
				HashSet<ExtractedNoun> candidateNounPool = listsOfCandidateNounPools.get(category);
				listsOfScoredNouns.put(category, scoreAllNouns(candidateNounPool, _extractedNounsToPatternsMap,_patternsToExtractedNounMap,  _listsOfKnownCategoryWords.get(category)));
			}
			
			//Resolve conflicts
			HashMap<String, HashSet<ExtractedNoun>> listsOfConflictResolvedNouns = new HashMap<String, HashSet<ExtractedNoun>>();
			for(String category: listsOfScoredNouns.keySet()){
				HashSet<ExtractedNoun> scoredNouns = listsOfScoredNouns.get(category);
				
				//Create a list to store the results of removing conflicts
				HashSet<ExtractedNoun> noConflictNouns = new HashSet<ExtractedNoun>();

				//Remove nouns that have already been added to the lexicon in previous iterations
				noConflictNouns = removeAlreadyKnownWords(scoredNouns, _listsOfKnownCategoryWords);
				
				//Resolve any remaining conflicts with either simple conflict resolution or improved conflict resolution
				if(_useImprovedConflictResolution){
					noConflictNouns = diffScoreAllNouns(noConflictNouns, category, _extractedNounsToPatternsMap, _patternsToExtractedNounMap, _listsOfKnownCategoryWords);
				}
				else{
					noConflictNouns = resolveSimpleConflicts(noConflictNouns, listsOfScoredNouns);
				}
				
				listsOfConflictResolvedNouns.put(category, noConflictNouns);
			}

			//Select the top extracted nouns from each list of conflict resolved nouns
			//Once we're done, add the new words to the parallel list of known words
			for(String category: listsOfConflictResolvedNouns.keySet()){
				HashSet<ExtractedNoun> resolvedNouns = listsOfConflictResolvedNouns.get(category);
				
				//Select the top nouns from the current scored list of nouns
				TreeSet<ExtractedNoun> topNewWords = selectTopNNewCandidateNouns(resolvedNouns, 5, _listsOfKnownCategoryWords);
				
				//Trace the top nouns
				traceNewNouns(tracesList.get(category), topNewWords,_listsOfKnownCategoryWords.get(category));

				System.out.format("Adding %d new words to the %s lexicon.\n", topNewWords.size(), category);
				
				//Print out new words to the console
				for(ExtractedNoun en: topNewWords.descendingSet()){
					System.out.println("\t" + en.toString());
				}
				System.out.println("");
				
				//Add the new words to the known category member list
				_listsOfKnownCategoryWords.get(category).addAll(topNewWords);
				

				//Add the new words to the list containing all new words
				learnedLexicons.get(category).addAll(topNewWords);
			}	
		}

		//Print out the list of learned words to their own files
		for(String category: _listsOfKnownCategoryWords.keySet()){
			PrintStream out = null; 
			try {
				out = new PrintStream(outputDir + category + ".lexicon");
			}
			catch (Exception e){
				System.err.println(e.getMessage());
			}
			
			for(ExtractedNoun learnedWord: learnedLexicons.get(category)){
				out.print(learnedWord + "\n");
			}
		}
		
		//Close the traces
		for(PrintStream trace: tracesList.values()){
			trace.close();
		}
	}
	
	public HashSet<ExtractedNoun> diffScoreAllNouns(HashSet<ExtractedNoun> candidateNounPool, 
													String category,
													HashMap<ExtractedNoun, HashSet<Pattern>> nounToPatternMap,
												    HashMap<Pattern, HashSet<ExtractedNoun>> patternToNounMap,
													HashMap<String, HashSet<Noun>> listsOfKnownCategoryWords){
		
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		
		//Examine each word in the current set to score it
		for(ExtractedNoun candidateNoun: candidateNounPool){
			double score = 0.0;
			double maxOtherScore = 0.0;
			
			for(String knownCategory: listsOfKnownCategoryWords.keySet()){
				HashSet<Noun> knownCategoryWords = listsOfKnownCategoryWords.get(knownCategory);
				//Check to see if we're scoring against the current category
				if(category.equalsIgnoreCase(knownCategory))
					score = scoreCandidateNoun(candidateNoun, nounToPatternMap, patternToNounMap, knownCategoryWords);
				else
					maxOtherScore = Math.max(maxOtherScore, scoreCandidateNoun(candidateNoun, nounToPatternMap, patternToNounMap, knownCategoryWords));
			}
			
			score = score - maxOtherScore;
			ExtractedNoun diffScored = new ExtractedNoun(candidateNoun._noun);
			diffScored.setScore(score);
			result.add(diffScored);
		}
		
		return result;
		
	}


	public boolean generateMaps(String allCasesFile){
		//Initialize both maps. 
		_patternsToExtractedNounMap = new HashMap<Pattern, HashSet<ExtractedNoun>>();
		_extractedNounsToPatternsMap = new HashMap<ExtractedNoun, HashSet<Pattern>>();
		
		
		//Read in the cases file
		try{
			BufferedReader br = new BufferedReader(new FileReader(allCasesFile));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				line = line.trim();
				
				//An asteric separates the extracted noun from the pattern that extracted it
				String nounPhrase = line.split("\\*")[0].trim();
				String pattern = line.split("\\*")[1].trim();
				
				//Process the pattern
				Pattern p = new Pattern(pattern);		
				
				//Identify the head nouns in the noun phrase
				ArrayList<ExtractedNoun> headNouns = identifyHeadNouns(nounPhrase, _stopWords);
				
				//Create a new word for each headnoun of the and phrase
				for(ExtractedNoun noun: headNouns){
					//Map patterns to extracted nouns
					HashSet<ExtractedNoun> nouns = _patternsToExtractedNounMap.get(p);
					if(nouns == null){
						nouns = new HashSet<ExtractedNoun>();
					}
					nouns.add(noun);
					_patternsToExtractedNounMap.put(p, nouns);
				
					
					//Map extracted nouns to patterns
					HashSet<Pattern> patterns = _extractedNounsToPatternsMap.get(noun);
					if(patterns == null)
						patterns = new HashSet<Pattern>();
					patterns.add(p);
					_extractedNounsToPatternsMap.put(noun, patterns);
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

	public ArrayList<ExtractedNoun> identifyHeadNouns(String nounPhrase, HashSet<Noun> stopWords){
		ArrayList<ExtractedNoun> result = new ArrayList<ExtractedNoun>();
		
		//Eliminate any attached "of" phrases
		nounPhrase = nounPhrase.replaceAll(" [oO][fF] .+", ""); 
		
		//Split the remaining phrase around known conjunctions
		String[] conjunctionSplit = nounPhrase.split("(?i)(( and )|(>COMMA))");
		
		//Cycle through each subConjuction
		for(int i = 0; i < conjunctionSplit.length; i++){
			if(conjunctionSplit[i].trim().equals(""))
				continue;
			
			//Grab the right most word (head noun)
			String[] words = conjunctionSplit[i].split(" +"); 			
			String noun = words[words.length - 1].trim();
			
			//Sanity check
			if(!noun.equals("")){
				ExtractedNoun n = new ExtractedNoun(noun);
				
				//As long as it isn't a stop word, add it to the result
				if(!stopWords.contains(n))
					result.add(n);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if any given extracted noun consists of only a sequence of ampersands, digits, periods, dashes, commas or colons.
	 * For example, matches:
	 * 		&&63, 14, 1999-2008
	 * @param en - ExtractedNoun to be checked for being a number
	 * @return true iff en is a number
	 */
	public boolean isNumber(ExtractedNoun en) {
		java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("[&\\d\\.\\-,:]+");
		java.util.regex.Matcher m = numPattern.matcher(en._noun);
		
		return m.matches();
	}


	/**
	 *  Checks a pattern to see if all of it's nouns have already been added to lexicons and/or if all of it's nouns are on the
	 * non-candidate nouns (numbers and posssessive nouns)
	 * 
	 * @param p - Pattern to check and see if it's depleted  
	 * @return true iff the given pattern is depleted
	 */
	public boolean isPatternDepleted(Pattern p, 
									 HashMap<Pattern, HashSet<ExtractedNoun>> patternToNounMap, 
									 HashMap<String, HashSet<Noun>> listsOfKnownCategoryWords) {
		boolean isDepleted = true;	//Assume the pattern is depelted, check to prove otherwise
		for(ExtractedNoun en: patternToNounMap.get(p)){
			//Assume the word is unknown. Check all lists of known words to prove this assumption wrong
			boolean isKnown = false;
			for(HashSet<Noun> knownCategoryWords: listsOfKnownCategoryWords.values()){
				if(knownCategoryWords.contains(en)){
					//If even a single list of known words contains this word, then it's known
					isKnown = true;
					break;
				}
			}
			//If the word isn't known, isn't a number, and isn't possessive, then the pattern still has some life left in it
			if (!isKnown && !isNumber(en) && !isPossessive(en)){
				isDepleted = false;
				break;
			}
		}
		return isDepleted;
	}

	/**
	 * Returns true if any given extracted noun starts with the @ symbol.
	 * 
	 * @param en - ExtractedNoun to be checked for being possessive
	 * @return true if en is possessive
	 */
	public boolean isPossessive(ExtractedNoun en) {
		java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("^@.*");
		java.util.regex.Matcher m = numPattern.matcher(en._noun);
		
		return m.matches();
	}


	public HashMap<String, HashSet<Noun>> loadCategoriesFromSList(String categorySeedsSlistFile) {
		
		HashMap<String, HashSet<Noun>> result = new HashMap<String, HashSet<Noun>>();
		
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
			//Store along with the category it is associated with
			result.put(outputPrefix, loadSet(dir + fileName));
		}
		
		return result;
	}




/*	private TreeSet<ExtractedNoun> selectTopNNewCandidateWords(TreeSet<ExtractedNoun> scoredNouns, int n) {
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
	}*/
	
	public HashSet<Noun> loadSet(String listOfWords) {
		File f = new File(listOfWords);
		
		if(!f.exists()){
			System.err.println("Input file could not be found: " + f.getAbsolutePath());
			return null;
		}
		
		HashSet<Noun> result = new HashSet<Noun>();
		
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
	
	/**
	 * Returns a new set of scored nouns that will have removed all of the words that are in the learned lexicons.
	 * 
	 * @param scoredNouns - List of scored nouns from which to remove already known words
	 * @return Set of scored nouns that is guaranteed to contain no members from the set of already known words
	 */
	public HashSet<ExtractedNoun> removeAlreadyKnownWords(HashSet<ExtractedNoun> scoredNouns, 
														  HashMap<String, HashSet<Noun>> listsOfKnownCategoryWords){
		
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		result.addAll(scoredNouns);
		
		for(HashSet<Noun> knownCategoryWords: listsOfKnownCategoryWords.values()){
			result.removeAll(knownCategoryWords);
		}
		
		return result;
	}
	
	/**
	 * Checks the current list of scored nouns against other lists of scored nouns to see if their are conflicting words, i.e. two 
	 * categories trying to add the same word. If such a conflict exists, retain the current noun only if it has the highest
	 * score compared to any other category that is also trying to add the given word.
	 * 
	 * @param scoredNouns - set of scored nouns to be checked for conflicts with other categories
	 * @param listsOfScoredNouns
	 * @return
	 */
	public HashSet<ExtractedNoun> resolveSimpleConflicts(HashSet<ExtractedNoun> scoredNouns, 
														 HashMap<String, HashSet<ExtractedNoun>> listsOfScoredNouns){
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		
		//Check to make sure each word has the highest (or tied for highest) score compared to all other known scored words 
		for(ExtractedNoun en: scoredNouns){
			boolean hasHighestScore = true;
			for(HashSet<ExtractedNoun> otherScoredNouns: listsOfScoredNouns.values()){
				//Check to see if there is even a conflict
				if(otherScoredNouns.contains(en)){
					//Iterate through the list until we find the match (damn java - no "get" method for hashsets)
					//Once we find the match, compare scores and set the flag appropriately 
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
			}
			
			//If it doesn't have the highest score, continue to the next word
			if(!hasHighestScore) continue;
			
			//Otherwise, we're looking at a new, highest scoring word !!Add it to the result already
			result.add(en);
		}

		
		return result;
	}

	/**
	 * Scores all of the candidate nouns in a given candidate noun pool using the AvgLog scoring function. 
	 * 
	 * @see scoreCandidateNoun for details on the AvgLog scoring function
	 * @param candidateNounPool - Set of candidate nouns to be scored by the AvgLog Function
	 * @param knownCategoryWords - Set of known category words for a particular category
	 * @return
	 */
	public HashSet<ExtractedNoun> scoreAllNouns(HashSet<ExtractedNoun> candidateNounPool, 
											    HashMap<ExtractedNoun, HashSet<Pattern>> nounToPatternMap,
											    HashMap<Pattern, HashSet<ExtractedNoun>> patternToNounMap,
											 	HashSet<Noun> knownCategoryWords) {
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		
		//Score each noun in our candidate pool
		for(ExtractedNoun candidateNoun: candidateNounPool){
			double avgScore = scoreCandidateNoun(candidateNoun, nounToPatternMap, patternToNounMap, knownCategoryWords);
			ExtractedNoun scored = new ExtractedNoun(candidateNoun._noun);
			scored.setScore(avgScore);
			result.add(scored);
		}
		
		return result;
	}
	
	/**
	 * Scores a particular word for a given category. The score for a noun is defined by the forumula
	 * 			AvgLog(word) = SUM[log2(F+1)]/N, where F is the number of known words also extracted by each of the 
	 * 											 patterns that extracted this given noun, and N is the number of patterns
	 * 											that extracted the given noun.
	 * @param noun - Extracted noun to be scored
	 * @param knownCategoryMembers - List of already known category words for the given category
	 * @return - score for the extracted noun
	 */
	public double scoreCandidateNoun(ExtractedNoun candidateNoun, 
									 HashMap<ExtractedNoun, HashSet<Pattern>> nounToPatternMap,
									 HashMap<Pattern, HashSet<ExtractedNoun>> patternToNounMap,
									 HashSet<Noun> knownCategoryMembers){
		double sumLog2 = 0.0; 	//Sumation of (log2(F+1))
		int numPatterns = 0; 	//P number of patterns that extracted candidate noun
		
		//Loop through each pattern that extracted the given noun
		//Incrementing the score by log2(F+1), where F is the number of known
		//category words that pattern extracted
		for(Pattern candidatePattern: nounToPatternMap.get(candidateNoun)){
			int FPlusOne = 1;
			//Loop through each noun extracted by the pattern, checking to see if it's a known word
			for(ExtractedNoun extractedNoun: patternToNounMap.get(candidatePattern)){
				if(knownCategoryMembers.contains(extractedNoun))
					FPlusOne++;
			}
			sumLog2 += Math.log(FPlusOne)/Math.log(2);
			numPatterns++;
		}
		//Average the nounScore over the number of patterns that contributed to it
		double avgScore = (sumLog2/(double) numPatterns);
		return avgScore;
	}
	
	public HashSet<Pattern> scorePatterns(HashMap<Pattern, HashSet<ExtractedNoun>> patterns, HashSet<Noun> knownCategoryMembers) {
		
		HashSet<Pattern> result = new HashSet<Pattern>();
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

	public HashSet<ExtractedNoun> selectNounsFromPatterns(HashSet<Pattern> patternPool, HashMap<Pattern, HashSet<ExtractedNoun>> patterns) {
		HashSet<ExtractedNoun> result = new HashSet<ExtractedNoun>();
		for(Pattern p: patternPool){
			for(ExtractedNoun en: patterns.get(p)){
				if(!isNumber(en) && !isPossessive(en))
					result.add(en);
			}
		}
		
		return result;
	}

	/**
	 *  Simply takes a hashset of scored nouns, sorts them, and returns N nouns with with the highest score.
	 *  NOTE: Use some sort of conflict resolution before selecting the top nouns. Otherwise this will blindly chose the top scored
	 *  nouns, some of which might have been learned already or some of which might be claimed by another category with a high score
	 * 
	 * @param scoredNouns - List of scored words to choose the best n words from
	 * @param n - number of best words to return
	 * @return List of the top n scored new words
	 */
	public TreeSet<ExtractedNoun> selectTopNNewCandidateNouns(HashSet<ExtractedNoun> scoredNouns,
															  int n,
														 	   HashMap<String, HashSet<Noun>> listsOfKnownCategoryWords) {

		TreeSet<ExtractedNoun> result = new TreeSet<ExtractedNoun>();
		
		//Sort the list of extracted nouns that we are currently examining. 
		TreeSet<ExtractedNoun> sortedScoredNouns = new TreeSet<ExtractedNoun>();
		sortedScoredNouns.addAll(scoredNouns);
		
		//Iterate through the list of sorted extracted nouns, starting with the one with the highest score
		for(ExtractedNoun en: sortedScoredNouns.descendingSet()){
			//Check to make sure one of the other categories hasn't it learned it either
			boolean alreadyLearned = false;
			for(HashSet<Noun> knownWords: listsOfKnownCategoryWords.values()){
				if(knownWords.contains(en)){
					alreadyLearned = true;
					break;
				}
			}
			
			if(alreadyLearned)
				continue;
			
			result.add(en);
			
			//If we've filled up our list, break out of the loop
			if(result.size() == n )
				break;
		}
		return result;
	}

	public HashSet<Pattern> selectTopNPatterns(HashSet<Pattern> patterns, 
											   int n, 
											   HashMap<Pattern, HashSet<ExtractedNoun>> patternsToNounsMap,
											   HashMap<String, HashSet<Noun>> listsOfKnownCategoryWords) {
		HashSet<Pattern> result = new HashSet<Pattern>();
		
		TreeSet<Pattern> sortedPatterns = new TreeSet<Pattern>();
		sortedPatterns.addAll(patterns);
		
		Iterator<Pattern> it = sortedPatterns.descendingIterator();
		for(int i = 0; i < n && it.hasNext();){
			Pattern nextPattern = it.next();
			//Check to make sure the pattern isn't depleted
			if(!isPatternDepleted(nextPattern, patternsToNounsMap, listsOfKnownCategoryWords)){
				result.add(nextPattern);
				i++;
			}
		}
		return result;
	}


	private void traceIterationHeader(PrintStream trace, int iteration) {
		trace.append("************************************************************\n");
		trace.format("                    Iteration %3d\n", iteration);
		trace.append("************************************************************\n\n");
		
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
/*	private TreeSet<ExtractedNoun> avgDiffSelectTopNNewCandidateWords( 	HashSet<ExtractedNoun> scoredNouns, int catNumber, 
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
	}*/
	




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
	private void tracePatternPool(PrintStream trace, HashSet<Pattern> patternPool) {
		trace.format("Pattern pool (%d patterns)\n", patternPool.size());
		
		String infoPadding = "     "; //Padding to indent the information below a pattern
		
		//Sort the patterns
		TreeSet<Pattern> sortedPatterns = new TreeSet<Pattern>();
		sortedPatterns.addAll(patternPool);
		
		//Loop through each pattern in the pattern pool
		int i = 1;
		for(Pattern p: sortedPatterns.descendingSet()){
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

}
