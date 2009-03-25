package basilisk;

public class Noun implements Comparable<Noun> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	protected final String _noun;
	
	public Noun(String s){
		_noun = s;
	}
	
	@Override 
	public boolean equals(Object o){
		if(this == o) return true;
		if((o instanceof Noun) && ((Noun) o)._noun.equalsIgnoreCase(_noun))
			return true;
		return false;
	}
	
	public int hashCode(){
		return _noun.toLowerCase().hashCode();
	}
	
	@Override
	public int compareTo(Noun n2) {
		return _noun.toLowerCase().compareTo(n2._noun.toLowerCase());
	}
	
	@Override
	public String toString(){
		return _noun;
	}

}
