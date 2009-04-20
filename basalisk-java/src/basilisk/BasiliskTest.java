package basilisk;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasiliskTest {

	
	private HashMap<Pattern, HashSet<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, HashSet<Pattern>> _extractedNounsToPatternsMap;
	private HashMap<String, HashSet<Noun>> _listsOfKnownCategoryWords;
	private Basilisk _bas;

	
	private HashSet<Noun> _stopWords;
	
	@Before
	public void setUp() throws Exception {
		_bas = new Basilisk();
		
		//Create the stopwords list
		_stopWords = new HashSet<Noun>();
		_stopWords.add(new Noun("it"));
		
		//Create our "seeds"
		Noun building1 = new Noun("human");	
		Noun event1 = new Noun("embassy");
		Noun human1 = new Noun("people"); 
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
		
		//Create extraction patterns
		Pattern vp1 = new Pattern("VP1");
		Pattern vp2 = new Pattern("VP2");
		
		//Create a list of extracted nouns
		ExtractedNoun enKnown1 = new ExtractedNoun("human");		//part of seed list
		ExtractedNoun enKnown2 = new ExtractedNoun("embasy"); 		//part of seed list
		ExtractedNoun enUnKnown1 = new ExtractedNoun("bill");		//unknown
		ExtractedNoun enUnKnown2 = new ExtractedNoun("super man");	//unknown
		ExtractedNoun enUnKnown3 = new ExtractedNoun("rabbit"); 	//unknown
		
		//Create extracted noun sets
		HashSet<ExtractedNoun> vp1Extractions = new HashSet<ExtractedNoun>();
		vp1Extractions.add(new ExtractedNoun(enKnown1._noun));
		vp1Extractions.add(new ExtractedNoun(enUnKnown1._noun));
		HashSet<ExtractedNoun> vp2Extractions = new HashSet<ExtractedNoun>();
		vp2Extractions.add(new ExtractedNoun(enKnown2._noun));
		vp2Extractions.add(new ExtractedNoun(enUnKnown2._noun));
		vp2Extractions.add(new ExtractedNoun(enUnKnown3._noun));
		
		//Store the sets in the p-to-n map
		_patternsToExtractedNounMap = new HashMap<Pattern, HashSet<ExtractedNoun>>();
		_patternsToExtractedNounMap.put(vp1, vp1Extractions);
		_patternsToExtractedNounMap.put(vp2, vp2Extractions);
		
		
		
		//Create lists of extractors
		HashSet<Pattern> en1Extractor = new HashSet<Pattern>();
		en1Extractor.add(new Pattern("VP1"));
		HashSet<Pattern> en2Extractor = new HashSet<Pattern>();
		en2Extractor.add(new Pattern("VP1"));
		HashSet<Pattern> en3Extractor = new HashSet<Pattern>();
		en3Extractor.add(new Pattern("VP2"));
		HashSet<Pattern> en4Extractor = new HashSet<Pattern>();
		en4Extractor.add(new Pattern("VP2"));
		HashSet<Pattern> en5Extractor = new HashSet<Pattern>();
		en5Extractor.add(new Pattern("VP2"));
		
		//Store the lists in the n-to-p map
		_extractedNounsToPatternsMap = new HashMap<ExtractedNoun, HashSet<Pattern>>();
		_extractedNounsToPatternsMap.put(enKnown1, en1Extractor);
		_extractedNounsToPatternsMap.put(enKnown2, en2Extractor);
		_extractedNounsToPatternsMap.put(enUnKnown1, en3Extractor);
		_extractedNounsToPatternsMap.put(enUnKnown2, en4Extractor);
		_extractedNounsToPatternsMap.put(enUnKnown3, en5Extractor);
		
		

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
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveAlreadyKnownWords() {
		fail("Not yet implemented");
	}

	@Test
	public void testResolveSimpleConflicts() {
		fail("Not yet implemented");
	}

	@Test
	public void testScoreAllNouns() {
		fail("Not yet implemented");
	}

	@Test
	public void testScoreCandidateNoun() {
		fail("Not yet implemented");
	}

	@Test
	public void testScorePatterns() {
		fail("Not yet implemented");
	}

	@Test
	public void testSelectTopNNewCandidateNouns() {
		fail("Not yet implemented");
	}

	@Test
	public void testSelectTopNPatterns() {
		fail("Not yet implemented");
	}

}
