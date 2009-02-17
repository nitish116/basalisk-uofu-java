package word_tools;

import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class WordCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String exc = "I like apples";
		System.out.println("" + exc.indexOf("!"));
	}
	
	public static Hashtable<String, Integer> countWords(String fileName){
		Hashtable<String, Integer> wordCount = new Hashtable<String, Integer>();
		
		//Try to read the file
		try{
			BufferedReader in = new BufferedReader(new FileReader(fileName));
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		//Please work
		return wordCount;
	}

}
