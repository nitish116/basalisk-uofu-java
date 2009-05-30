package basilisk;

/**
 * Used to represent a head noun. Overrides the compare method, so that two nouns are considered equal so long as they contain the 
 * same sequence of letters, case insensitive. 
 * 
 * @author John Tabet
 *
 */
public class Noun implements Comparable<Noun> {

	private static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	protected final String _noun;
	
	/**
	 * Initializes a new Noun with the given input String.
	 * 
	 * @param s - Input string to init
	 */
	public Noun(String s){
		_noun = s;
	}
	
	@Override 
	/**
	 * Two nouns are considered equal so long as they contain the same sequence of characters, case insensitve.
	 */
	public boolean equals(Object o){
		if(this == o) return true;
		if((o instanceof Noun) && ((Noun) o)._noun.equalsIgnoreCase(_noun))
			return true;
		return false;
	}
	
	/**
	 * Calls the hashCode method on the String contained by this Noun, case insensitive.
	 */
	public int hashCode(){
		return _noun.toLowerCase().hashCode();
	}
	
	@Override
	/**
	 * Calls the compareTo method on the String that this Noun represents, case insensitive. Nouns will therefore be sorted
	 * alphabetically. 
	 */
	public int compareTo(Noun n2) {
		return _noun.toLowerCase().compareTo(n2._noun.toLowerCase());
	}
	
	@Override
	/**
	 * Returns the string represented by the Noun.
	 */
	public String toString(){
		return _noun;
	}

}
