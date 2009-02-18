package autoslog_helper;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import word_tools.WordTools;

public class NounListGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Hashtable<String, Integer> mucCount = WordTools.countWordsInDocs("muc3-listfile");
		
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for(Integer i: mucCount.values()){
			counts.add(i);
		}
		Collections.sort(counts);
		
		ArrayList<String> words = new ArrayList<String>();
		for(String s: mucCount.keySet()){
			words.add(s);
		}
		Collections.sort(words);
		
		//Create a file writer to print word count
		FileWriter out = null;
		try{
			out = new FileWriter("mucFrequentWords.txt");
		
			//Print the word count to the file
			for(int i = words.size() - 1; i >= 0; i--){
				out.write(words.get(i) + "\n");
			}
			out.close();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

}
