package basilisk;

import java.util.*;

/**
 * Represents a head noun that has been extracted by a particular Pattern. Extends the Noun class. Identical to a Noun, except
 * an ExtractedNoun also contains information about the score it received from Basilisk. Additionally, the compareTo method was
 * overriden  so that ExtractedNoun's can be sorted by score (for example, by a TreeSet).<br/><br/>
 * 
 * VERY IMPORTANT NOTE: Because the compareTo method relies on the score of the ExtractedNoun, and not necessarily the noun itself,
 * checking to see if a treeSet contains an ExtractedNoun (treeSetOne.contains(en1)) produces unreliable results. For example, if
 * we want to see if our treeSet contains "monkey", our treeSet will return false unless it contains an instance of "monkey" with
 * the exact same score. For this reason, it is best to store ExtractedNoun's in HashSet, or any set that makes uses the "equals" method
 * for comparison purposes. Two ExtractedNoun's are considered equal so long as they contain the same word. 
 * @author John
 *
 */
public class ExtractedNoun extends Noun {


	private static void main(String[] args) {
//		// TODO Auto-generated method stub
//		Noun n1 = new Noun("bob");
//		ExtractedNoun n2 = new ExtractedNoun("country");
//		ExtractedNoun n3 = new ExtractedNoun("bob2");
//		
//		System.out.println(n1.compareTo(n2));
//		System.out.println(n1.equals(n2));
//		System.out.println(n2.equals(n3));
//		System.out.println(n2.compareTo(n3));
//		
//		TreeSet<ExtractedNoun> tree = new TreeSet<ExtractedNoun>();
//		tree.add(n2);
//		tree.add(n3);
//		
//		TreeMap<ExtractedNoun, Set<Pattern>> tree2 = new TreeMap<ExtractedNoun, Set<Pattern>>();
//		Pattern p1 = new Pattern("np");
//		Pattern p2 = new Pattern("vp");
//		Set<Pattern> patterns = new HashSet<Pattern>();
//		patterns.add(p1);
//		patterns.add(p2);
//		tree2.put(n2, patterns);
//		tree2.put(n3, patterns);
//		
//		System.out.println(tree2.get(n3));
//		System.out.println(tree2.containsKey(n3));
		
		Set<Noun> nouns = new HashSet<Noun>();
		nouns.add(new Noun("salvador"));
		
		
		System.out.println("Noun set countains 'savlador':" + nouns.contains(new ExtractedNoun("sAlvador")));
		
	}
	
	private double _score;
	
	/**
	 * Initializes an ExtractedNoun with the given input word. Initializes the default score to -2.0.
	 * @param s - Input word from which to initialize the ExtractedNoun. 
	 */
	public ExtractedNoun(String s){
		super(s);
		_score = -2.0; 	//default score
	}

	/**
	 * Resets the ExtractedNoun's score to -1.0.
	 */
	public void clearScore(){
		_score = -1.0;
	}
	
	/**
	 * Returns the score of the ExtractedNoun
	 * @return A double representing the score of the ExtractedNoun.
	 */
	public double getScore(){
		return _score;
	}
	/**
	 * Sets the score of the ExtractedNoun.
	 * 
	 * @param s - Double value to which the ExtractedNoun's score is to be set.
	 */
	public void setScore(double s){
		_score = s;
	}
	
	@Override
	public String toString(){
		return _noun;
	}
	
	/**
	 * Helper method that returns the String that represents the ExtractedNoun, along with its score. Looks like: <br/>
	 * noun, 10.0
	 * @return A string that contains the word and score represented by the ExtractedNoun.
	 */
	public String toStringWithScore(){
		return _noun  + ", " + _score;
	}
		
	@Override
	/**
	 * Compares one ExtractedNoun to another. Compares to ExtractedNoun's first by their score, and then by the word they represent.
	 * See the notes for the entire class for information about how this significantly affects using ExtractedNoun
	 */
	public int compareTo(Noun n2) {
		if(n2 instanceof ExtractedNoun){
			ExtractedNoun en2 = (ExtractedNoun) n2;
			if(_score == en2._score){
				return _noun.toLowerCase().compareTo(n2._noun.toLowerCase());
			}
			if(_score < en2._score)
				return -1;
			else if(_score > en2._score)
				return 1;
			else return 0;
		}
		else 
			return super.compareTo(n2);
	}

}
