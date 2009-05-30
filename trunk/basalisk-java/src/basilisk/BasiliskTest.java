package basilisk;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test frame work for Basilisk class. Could be significantly more robust.
 * 
 * @author John Tabet
 */
public class BasiliskTest {

	
	private HashMap<Pattern, HashSet<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, HashSet<Pattern>> _extractedNounsToPatternsMap;
	private HashMap<String, HashSet<Noun>> _listsOfKnownCategoryWords;
	private Basilisk _bas;
	private ExtractedNoun _enKnownHuman1, _enKnownHuman2,  _enKnownEvent1, _enUnknown1, _enUnknown2, _enUnknown3;

	
	private HashSet<Noun> _stopWords;
	
	@Before
	public void setUp() throws Exception {
		_bas = new Basilisk();
		
		//Create the stopwords list
		_stopWords = new HashSet<Noun>();
		_stopWords.add(new Noun("it"));
		
		//Create our "seeds"
		Noun building1 = new Noun("embassy");	
		Noun event1 = new Noun("attack");
		Noun human1 = new Noun("people"); 
		Noun human2 = new Noun("guerrillas"); 
		Noun location1 = new Noun("country");
		Noun time1 = new Noun("november");		
		Noun vehicle1 = new Noun("plane");	
		Noun weapon1 = new Noun("BOMB");	
		
		//Create lists to hold seeds
		HashSet<Noun> buildingList = new HashSet<Noun>();
		buildingList.add(building1);
		HashSet<Noun> eventList = new HashSet<Noun>();
		eventList.add(event1);
		HashSet<Noun> humanList = new HashSet<Noun>();
		humanList.add(human1);
		humanList.add(human2);
		HashSet<Noun> locationList = new HashSet<Noun>();
		locationList.add(location1);
		HashSet<Noun> timeList = new HashSet<Noun>();
		timeList.add(time1);
		HashSet<Noun> vehicleList = new HashSet<Noun>();
		vehicleList.add(vehicle1);
		HashSet<Noun> weaponList = new HashSet<Noun>();
		weaponList.add(weapon1);
		
		//Create the lists of known category words
		_listsOfKnownCategoryWords = new HashMap<String, HashSet<Noun>>();
		_listsOfKnownCategoryWords.put("building", buildingList);
		_listsOfKnownCategoryWords.put("event", eventList);
		_listsOfKnownCategoryWords.put("human", humanList);
		_listsOfKnownCategoryWords.put("location", locationList);
		_listsOfKnownCategoryWords.put("time", timeList);
		_listsOfKnownCategoryWords.put("vehicle", vehicleList);
		_listsOfKnownCategoryWords.put("weapon", weaponList);		
		
		//Create a list of known/unknown extracted nouns
		_enKnownHuman1 = new ExtractedNoun("people");		//part of seed list
		_enKnownHuman2 = new ExtractedNoun("guerrillas");		//part of seed list
		_enKnownEvent1 = new ExtractedNoun("attack"); 		//part of seed list
		_enUnknown1 = new ExtractedNoun("bill");		//unknown
		_enUnknown2 = new ExtractedNoun("super man");	//unknown
		_enUnknown3 = new ExtractedNoun("rabbit"); 	//unknown

		//Initialize maps
		_patternsToExtractedNounMap = new HashMap<Pattern, HashSet<ExtractedNoun>>();
		_extractedNounsToPatternsMap = new HashMap<ExtractedNoun, HashSet<Pattern>>();
	}

	@Test
	public void testIdentifyHeadNouns() {
		ArrayList<ExtractedNoun> headNouns = _bas.identifyHeadNouns("The boy and girl of great respect", _stopWords);
		
		//Check to make sure that only two head nouns were extracted
		assertEquals("Failed to extract 2 head nouns", 2, headNouns.size());
		
		//Check the content of each headnoun
		for(ExtractedNoun en: headNouns){
			//Make sure each headnoun is either "boy" or "girl"
			assertEquals("Failed to identify boy or girl as head noun",
						 true, 
						 en.equals(new ExtractedNoun("boy")) || en.equals(new ExtractedNoun("girl")));
		}
	}

	@Test
	public void testIsNumber() {
		ExtractedNoun number1 = new ExtractedNoun("7");
		 assertEquals("Noun '7' should test positive as a number", true, _bas.isNumber(number1));
	}

	@Test
	public void testIsPatternDepletedAllExtractedAlreadyKnown() {
		//Create a pattern that contains all the known words from a particular list of words
		boolean noKnownWords = true;
		HashSet<ExtractedNoun> extractedNouns = new HashSet<ExtractedNoun>();
		for(Set<Noun> knownWords: this._listsOfKnownCategoryWords.values()){
			if(knownWords.size() != 0){
				noKnownWords = false;
				for(Noun known: knownWords){
					extractedNouns.add(new ExtractedNoun(known._noun));
				}
				break;
			}
		}
		if(noKnownWords)
			fail("Failed to add any known words to the list of known words list");
		Pattern depletedPattern = new Pattern("Depleted");
		_patternsToExtractedNounMap.put(depletedPattern, extractedNouns);
		Boolean isDepleted = _bas.isPatternDepleted(depletedPattern, _patternsToExtractedNounMap, _listsOfKnownCategoryWords);
		assertEquals("Pattern with all known words fails to check as depleted", true, isDepleted);
	}
	
	@Test
	public void testIsPatternDepletedAllExtractedAlreadyKnownAndNumbers() {
		//Create a pattern that contains all the known words from a particular list of words
		boolean noKnownWords = true;
		HashSet<ExtractedNoun> extractedNouns = new HashSet<ExtractedNoun>();
		for(Set<Noun> knownWords: this._listsOfKnownCategoryWords.values()){
			if(knownWords.size() != 0){
				noKnownWords = false;
				for(Noun known: knownWords){
					extractedNouns.add(new ExtractedNoun(known._noun));
				}
				break;
			}
		}
		if(noKnownWords)
			fail("Failed to add any known words to the list of known words list");
		
		//Add a number to the list
		extractedNouns.add(new ExtractedNoun("567"));
		
		//Create the pattern, and add it to the map
		Pattern depletedPattern = new Pattern("Depleted");
		_patternsToExtractedNounMap.put(depletedPattern, extractedNouns);
		
		//It should be depleted, problem if not
		Boolean isDepleted = _bas.isPatternDepleted(depletedPattern, _patternsToExtractedNounMap, _listsOfKnownCategoryWords);
		assertEquals("Pattern with all known words and numbers fails to check as depleted", true, isDepleted);
	}

	@Test
	public void testIsPossessive() {
		assertEquals(true, _bas.isPossessive(new ExtractedNoun("@Bob@s")));
	}

	@Test
	public void testRemoveAlreadyKnownWords() {
		HashSet<ExtractedNoun> oneUnknownWords = new HashSet<ExtractedNoun>();
		oneUnknownWords.add(new ExtractedNoun(_enKnownHuman1._noun));
		oneUnknownWords.add(new ExtractedNoun(_enKnownEvent1._noun));
		oneUnknownWords.add(new ExtractedNoun(_enUnknown1._noun));
		assertEquals(1, _bas.removeAlreadyKnownWords(oneUnknownWords, _listsOfKnownCategoryWords).size());
	}

	@Test
	public void testResolveSimpleConflicts() {
		//If two lists contain the same words, then only the list with the highest scored result should keep it's score
		ExtractedNoun lowerScore = new ExtractedNoun("bill");
		lowerScore.setScore(1.0);
		HashSet<ExtractedNoun> lowScoreList = new HashSet<ExtractedNoun>();
		lowScoreList.add(lowerScore);
		
		ExtractedNoun higherScore = new ExtractedNoun("bill");
		higherScore.setScore(2.0);
		HashSet<ExtractedNoun> highScoreList = new HashSet<ExtractedNoun>();
		highScoreList.add(higherScore);
		
		HashMap<String, HashSet<ExtractedNoun>> listsOfScoredNouns = new HashMap<String, HashSet<ExtractedNoun>>();
		listsOfScoredNouns.put("lower score list", lowScoreList);
		listsOfScoredNouns.put("high score list", highScoreList);
		
		//assertEquals(1, _bas.resolveSimpleConflicts(highScoreList, "high score list", listsOfScoredNouns).size());
		//assertEquals(0, _bas.resolveSimpleConflicts(lowScoreList, "lower score list", listsOfScoredNouns).size());
	}

	/**
	 * One noun will be extracted by a pattern with 1 known and 1 unknown (log(1+1)/1)
	 * Another noun will be extracted by 2 patterns, one with a known word, the other with unknown words(log(1+1)+ log(0+1)/2)
	 */
	@Test
	public void testScoreAllNouns() {
		
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp1Extractions = new HashSet<ExtractedNoun>();
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman1._noun));
		vp1Extractions.add(new ExtractedNoun(_enUnknown1._noun));
		HashSet<ExtractedNoun> vp2Extractions = new HashSet<ExtractedNoun>();
		vp2Extractions.add(new ExtractedNoun(_enKnownEvent1._noun));
		vp2Extractions.add(new ExtractedNoun(_enUnknown1._noun));
		vp2Extractions.add(new ExtractedNoun(_enUnknown2._noun));
		vp2Extractions.add(new ExtractedNoun(_enUnknown3._noun));
		
		//Store the sets in the p-to-n map
		_patternsToExtractedNounMap.put(new Pattern("VP1"), vp1Extractions);
		_patternsToExtractedNounMap.put(new Pattern("VP2"), vp2Extractions);
		
		
		
		//Create lists of extractors
		HashSet<Pattern> vp1Extractor1 = new HashSet<Pattern>();
		vp1Extractor1.add(new Pattern("VP1"));
		HashSet<Pattern> vp1Extractor2 = new HashSet<Pattern>();
		vp1Extractor2.add(new Pattern("VP1"));
		vp1Extractor2.add(new Pattern("VP2"));
		HashSet<Pattern> vp2Extractor1 = new HashSet<Pattern>();
		vp2Extractor1.add(new Pattern("VP2"));
		HashSet<Pattern> vp2Extractor2 = new HashSet<Pattern>();
		vp2Extractor2.add(new Pattern("VP2"));
		HashSet<Pattern> vp2Extractor3 = new HashSet<Pattern>();
		vp2Extractor3.add(new Pattern("VP2"));
		HashSet<Pattern> vp2Extractor4 = new HashSet<Pattern>();
		vp2Extractor4.add(new Pattern("VP2"));
		
		//Store the lists in the n-to-p map
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enKnownHuman1._noun), vp1Extractor1);
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enUnknown1._noun), vp1Extractor2);
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enKnownEvent1._noun), vp2Extractor1);
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enUnknown2._noun), vp2Extractor2);
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enUnknown3._noun), vp2Extractor3);
		
		//List the nouns to score
		HashSet<ExtractedNoun> scoredNouns = new HashSet<ExtractedNoun>();
		scoredNouns.add(new ExtractedNoun(_enKnownHuman1._noun)); 	//Should receive score of log(1+1)/1
		scoredNouns.add(new ExtractedNoun(_enUnknown1._noun)); //Should receive score of log(1+1)+log(0+1)/2
		
		HashSet<Noun> humanList = _listsOfKnownCategoryWords.get("human");
		
		scoredNouns = _bas.scoreAllNouns(scoredNouns, _extractedNounsToPatternsMap, _patternsToExtractedNounMap, humanList);
		
		for(ExtractedNoun en: scoredNouns){
			if(en.equals(new ExtractedNoun(_enKnownHuman1._noun)))
				assertEquals(1.0d, en.getScore());
			if(en.equals(new ExtractedNoun(_enUnknown1._noun)))
				assertEquals(0.5d, en.getScore(), .0001d);
		}
	}

	/**
	 * Create a pattern that extracts one known noun and one unknown noun.
	 * Score should be log(1+1)/1 = 1.000
	 */
	@Test
	public void testScoreCandidateNoun() {
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp1Extractions = new HashSet<ExtractedNoun>();
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman1._noun));
		vp1Extractions.add(new ExtractedNoun(_enUnknown1._noun));
		
		//Store the sets in the p-to-n map
		_patternsToExtractedNounMap.put(new Pattern("VP1"), vp1Extractions);
		
		//Create lists of extractors
		HashSet<Pattern> en1Extractor = new HashSet<Pattern>();
		en1Extractor.add(new Pattern("VP1"));
		HashSet<Pattern> en2Extractor = new HashSet<Pattern>();
		en2Extractor.add(new Pattern("VP1"));
		
		//Store the lists in the n-to-p map
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enKnownHuman1._noun), en1Extractor);
		_extractedNounsToPatternsMap.put(new ExtractedNoun(_enUnknown1._noun), en2Extractor);
		HashSet<Noun> humanList = _listsOfKnownCategoryWords.get("human");
		assertEquals((Math.log(2.0)/Math.log(2.0))/1.0d, 
					  _bas.scoreCandidateNoun(new ExtractedNoun(_enUnknown1._noun), 
															   _extractedNounsToPatternsMap, 
															   _patternsToExtractedNounMap, 
															   humanList),
					  .00001d);
	}

	/**
	 * Create a pattern with 2 known words, and 1 unknown. It should receive a score of 2/3*log(2)
	 */
	@Test
	public void testScorePatterns() {
		//Create the pattern
		Pattern vp1 = new Pattern("VP1");
		
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp1Extractions = new HashSet<ExtractedNoun>();
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman1._noun));
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman2._noun));
		vp1Extractions.add(new ExtractedNoun(_enUnknown2._noun));
		
		HashSet<Noun> humanList = _listsOfKnownCategoryWords.get("human");
		
		//Store the sets in the p-to-n map
		_patternsToExtractedNounMap.put(vp1, vp1Extractions);
		
		//Score the patterns
		HashSet<Pattern> scoredPatterns = _bas.scorePatterns(_patternsToExtractedNounMap, humanList);
		
		for(Pattern p: scoredPatterns){
			double thirds = 2.0d/3.0d;
			assertEquals(2.0d/3.0d, p.getScore(), .00001d);
		}
	}

	/**
	 * Create a list with two words. One word has the highest score, and is known. The other word has a lower score but is unlearned.
	 * The unlearned word should be the only word selected.
	 */
	@Test
	public void testSelectTopNNewCandidateNouns() {
		ExtractedNoun highButKnown = new ExtractedNoun(_enKnownHuman1._noun);
		highButKnown.setScore(4.0d);
		ExtractedNoun lowButUnknown = new ExtractedNoun("billybob");
		lowButUnknown.setScore(2.0d);
		
		HashSet<ExtractedNoun> candidateNouns = new HashSet<ExtractedNoun>();
		candidateNouns.add(lowButUnknown);
		candidateNouns.add(highButKnown);
		
		TreeSet<ExtractedNoun> topNoun = _bas.selectTopNCandidateNouns(candidateNouns, 1, _listsOfKnownCategoryWords);
		for(ExtractedNoun en: topNoun){
			assertEquals(new ExtractedNoun("billybob"), en);
		}
	}

	/**
	 * Create two patterns.
	 * 	One pattern: 2 known words, and 1 unknown. It should receive a score of 2/3*log(2)
	 * 	2nd pattern: 1 known word, and one uknown. It shoudl receive a score of 1/2*log(1);
	 */
	@Test
	public void testSelectTopNPatterns() {
		//Create the first pattern
		Pattern vp1 = new Pattern("VP1");
		
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp1Extractions = new HashSet<ExtractedNoun>();
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman1._noun));
		vp1Extractions.add(new ExtractedNoun(_enKnownHuman2._noun));
		vp1Extractions.add(new ExtractedNoun(_enUnknown1._noun));
		
		//Create the second pattern
		Pattern vp2 = new Pattern("VP2");
		
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp2Extractions = new HashSet<ExtractedNoun>();
		vp2Extractions.add(new ExtractedNoun(_enKnownHuman1._noun));
		vp2Extractions.add(new ExtractedNoun(_enUnknown1._noun));
		
		//List of known human words
		HashSet<Noun> humanList = _listsOfKnownCategoryWords.get("human");
		
		//Store the sets in the p-to-n map
		_patternsToExtractedNounMap.put(vp1, vp1Extractions);
		_patternsToExtractedNounMap.put(vp2, vp2Extractions);
		
		//Score the patterns
		HashSet<Pattern> scoredPatterns = _bas.scorePatterns(_patternsToExtractedNounMap, humanList);
		
		//Select the top pattern
		scoredPatterns = _bas.selectTopNPatterns(scoredPatterns, 1, _patternsToExtractedNounMap, _listsOfKnownCategoryWords);
		for(Pattern p: scoredPatterns){
			assertEquals(new Pattern("VP1"), p);
		}
	}

}
