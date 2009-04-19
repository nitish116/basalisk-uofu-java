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

	
	private HashMap<Pattern, Set<ExtractedNoun>> _patternsToExtractedNounMap;
	private HashMap<ExtractedNoun, Set<Pattern>> _extractedNounsToPatternsMap;
	private Set<Noun> _seeds;
	private ArrayList<HashSet<Noun>> _listsOfKnownCategoryWords;
	private ArrayList<String> _outputPrefixList;

	
	private Set<Noun> _stopWords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		//Create our "seeds"
		Noun building = new Noun("human");	
		Noun event = new Noun("embassy");
		Noun human = new Noun("people"); 
		Noun location = new Noun("country");
		Noun time = new Noun("november");		
		Noun vehicle = new Noun("plane");	
		Noun weapon = new Noun("BOMB");	
		
		//Create lists to hold seeds
		ArrayList
		
		Noun n5 = new 
		
		ExtractedNoun en1 = 

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
