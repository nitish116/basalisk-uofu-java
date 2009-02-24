package basilisk;

import java.io.File;
import java.util.ArrayList;

public class Basilisk {

	private ArrayList<CaseFrame> _caseFrameList;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File caseFrameFile = new File("sample-texts/muc-out/muc-1thru20-cfs");
		
		if(!caseFrameFile.exists()){
			System.err.println("Case frame file not found: " + caseFrameFile.getAbsolutePath());
			return;
		}
		
		Basilisk b = new Basilisk(caseFrameFile);
		
	}
	
	public Basilisk(File caseFrameFile){
		_caseFrameList = CaseFrameLoader.loadFromFile(caseFrameFile);
		//System.out.println(_caseFrameList);
		//System.out.println(_caseFrameList.size());
	}
	
	

}
