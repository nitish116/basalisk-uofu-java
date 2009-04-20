package basilisk;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasiliskTest {

	
	private HashMap<Pattern, HashSet<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, HashSet<Pattern>> _extractedNounsToPatternsMap;
	private HashMap<String, HashSet<Noun>> _listsOfKnownCategoryWords;

	
	private Set<Noun> _stopWords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
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

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testIdentifyHeadNouns() {
		fail("Not yet implemented");
		
	}

	@Test
	public void testIsNumber() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPatternDepleted() {
		fail("Not yet implemented");
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
