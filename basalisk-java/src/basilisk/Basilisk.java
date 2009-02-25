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
	private HashSet<String> _locations;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File caseFrameFile = new File("sample-texts/muc-out/muc-1thru20-cfs.txt");
		if(!caseFrameFile.exists()){
			System.err.println("Case frame file not found: " + caseFrameFile.getAbsolutePath());
			return;
		}
		
		File listFileExtNouns = new File("sample-texts/muc-out/muc3-listfile.cases");
		if(!listFileExtNouns.exists()){
			System.err.println("Listfile for Extracted nouns not found: " + listFileExtNouns.getAbsolutePath());
			return;
		}

		
		Basilisk b = new Basilisk(caseFrameFile);
		b.loadCaseFrames(caseFrameFile);
		b.loadExtractedNouns(listFileExtNouns);
		
	}
	
	public Basilisk(File caseFrameFile){
		
		System.out.println(_caseFrameList);
		System.out.println(_caseFrameList.size());
		
		Pattern p = Pattern.compile("\"[^\"\r\n]*\"");
		
		String s = "Bob ran up to me and said \"I really like you\".";
		Matcher m = p.matcher(s);
		
		System.out.println(s);
		
		while(m.find()){
			String substr = s.substring(m.start()+1, m.end()-1);
			System.out.println(substr);
		}
	}
	
	private void loadCaseFrames(File caseFrameFile) {
		_caseFrameList = CaseFrameLoader.loadFromFile(caseFrameFile);
		
	}
	
	private void loadExtractedNouns(File listFileExtNouns) {
		
		
	}



	
	

}
