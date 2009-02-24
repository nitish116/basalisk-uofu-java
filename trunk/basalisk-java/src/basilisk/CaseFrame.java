package basilisk;

/**
 * Entity representing a CaseFrame.
 * 
 * @author john
 *
 */

public class CaseFrame {

	private String _caseFrame;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public CaseFrame(String s){
		_caseFrame = s;
	}

	public boolean equals(CaseFrame cf){
		if(_caseFrame.equalsIgnoreCase(cf.toString()))
			return true;
		return false;
	}
	
	public String toString(){
		return _caseFrame;
	}
}
