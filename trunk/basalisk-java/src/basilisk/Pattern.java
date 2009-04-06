package basilisk;

public class Pattern implements Comparable<Pattern> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Pattern p1 = new Pattern("InfVp_Prep_<NP>__ADVANCE_IN_26");
		Pattern p2 = new Pattern("Infvp_Prep_<NP>__ADVANCE_IN_26");
		System.out.println(p1.equals(p2));

	}
	
	
	public final String _caseFrame;
	private double _score;
	
	public Pattern(String s){
		_caseFrame = s;
		_score = -1.0; 	//default score
	}

	public boolean equals(Object o){
		if(this == o)
			return true;
		if((o instanceof Pattern) && ((Pattern) o)._caseFrame.equalsIgnoreCase(_caseFrame))
			return true;
		return false;
	}
	
	public void clearScore(){
		_score = -1;
	}
	
	public void setScore(double s){
		_score = s;
	}
	
	public int hashCode(){
		return _caseFrame.toLowerCase().hashCode();
	}
	
	public String toString(){
		return _caseFrame + ", Score: " + _score;
	}
	
	public String toStringNoScore(){
		return _caseFrame;
	}
	
	/**
	 * Highest scores come first
	 */
	@Override
	public int compareTo(Pattern p2) {
		if(_score == p2._score){
			return _caseFrame.toLowerCase().compareTo(p2._caseFrame.toLowerCase());
		}
		if(_score < p2._score)
			return -1;
		else if(_score > p2._score)
			return 1;
		else return 0;
	}
	
	public double getScore(){
		return _score;
	}


}
