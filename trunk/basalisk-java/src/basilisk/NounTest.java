package basilisk;

public class NounTest implements Comparable<NounTest> {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		NounTest t1 = new NounTest("bob");
		ExtNounTest t2 = t1.new ExtNounTest("bob");
		ExtNounTest t3 = t1.new ExtNounTest("bob");
		t2.setScore(4);
		t3.setScore(5);
		
		System.out.println(t2.compareTo(t3));
	}
	

	protected final String _noun;
	
	public NounTest(String s){
		_noun = s;
	}
	
	@Override 
	public boolean equals(Object o){
		if((o instanceof NounTest) && ((NounTest) o)._noun.equalsIgnoreCase(_noun))
			return true;
		return false;
	}
	
	public int hashCode(){
		return _noun.hashCode();
	}
	
	@Override
	public int compareTo(NounTest n2) {
		return _noun.compareTo(n2._noun);
	}
	
	public class ExtNounTest extends NounTest{
		private int _score;
		
		public ExtNounTest(String s){
			super(s);
			_score = -1; 	//default score
		}
		
		public void setScore(int s){
			_score = s;
		}

		public void clearScore(){
			_score = -1;
		}
		

		public int compareTo(ExtNounTest n2) {
			if(_score == n2._score){
				return _noun.compareTo(n2._noun);
			}
			if(_score < n2._score)
				return -1;
			else if(_score > n2._score)
				return 1;
			else return 0;
		}
	}


}


