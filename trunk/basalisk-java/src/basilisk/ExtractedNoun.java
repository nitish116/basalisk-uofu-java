package basilisk;

import java.util.*;

public class ExtractedNoun extends Noun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
	
	public ExtractedNoun(String s){
		super(s);
		_score = -1; 	//default score
	}

	public void clearScore(){
		_score = -1.0;
	}
	
	public void setScore(double s){
		_score = s;
	}
	
//	@Override
//	public boolean equals(Object o){
//		if(this == o)
//			return true;
//		if(o instanceof ExtractedNoun){
//			ExtractedNoun en = (ExtractedNoun) o;
//			if(_score == en._score && _noun.equalsIgnoreCase(en._noun))
//				return true;
//			else 
//				return false;
//		}
//		return false;
//	}
	
	@Override
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
