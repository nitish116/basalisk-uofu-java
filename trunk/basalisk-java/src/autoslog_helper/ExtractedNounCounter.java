package autoslog_helper;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Vector;

import word_tools.WordTools;

public class ExtractedNounCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Hashtable<String, Integer> mucCount = WordTools.countWordsInDocs("muc3-listfile");

		List<Map.Entry<String, Integer>> sortByValue = new ArrayList<Map.Entry<String, Integer>>(mucCount.entrySet());
		Collections.sort(sortByValue, new Comparator<Map.Entry>(){
			public int compare(Map.Entry e1, Map.Entry e2){
				Integer i1 = (Integer) e1.getValue();
				Integer i2 = (Integer) e2.getValue();
				return i2.compareTo(i1);
			}
		});
		
	
//        System.out.println("letter - freq");
//        System.out.println("-------------");
//        for(Map.Entry e : sortByValue) {
//            System.out.printf("%-15s%d%n", e.getKey(), e.getValue());
//        }
		
		//Create a file writer to print word count
		FileWriter out = null;
		try{
			out = new FileWriter("mucFrequentWords.txt");
		
	        for(Map.Entry e : sortByValue) {
	          	out.write(e.getKey().toString()  + "\n");
	        }
			out.close();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

}
