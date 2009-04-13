package outdated;

/**
 * Entity representing a CaseFrame.
 * 
 * @author john
 *
 */

public class CaseFrame{

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

	public boolean equals(Object o){
		if((o instanceof CaseFrame) && ((CaseFrame) o)._caseFrame.equalsIgnoreCase(_caseFrame))
			return true;
		return false;
	}
	
	public int hashCode(){
		return _caseFrame.hashCode();
	}
	
	public String toString(){
		return _caseFrame;
	}

}
