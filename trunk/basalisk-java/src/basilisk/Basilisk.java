package basilisk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Basilisk {

	private HashSet<CaseFrame> _caseFrameList;
	private HashMap<CaseFrame, HashMap<String, Integer>> _caseFrameToNounMap;
	private HashMap<String, HashSet<CaseFrame>> _nounToCaseFrameMap;
	private HashSet<String> _locations;
	
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
		_caseFrameList = CaseFrameLoader.loadFromFile(fileWithOnlyCaseFrames);
		_caseFrameToNounMap = CaseFrameToExtractedNounMap.loadFromMultipleFiles(listOfFilesWithExtractedNouns);
		_nounToCaseFrameMap = ExtractedNounToCaseFrameMap.loadFromMultipleFiles(listOfFilesWithExtractedNouns);
		_locations = SeedWordLoader.loadFromFile(locationSeedFile);
		
	}

}
