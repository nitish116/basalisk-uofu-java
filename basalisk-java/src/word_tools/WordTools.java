package word_tools;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class WordTools {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}
	
	public static Hashtable<String, Integer> countWords(String fileName){
		Hashtable<String, Integer> wordCount = new Hashtable<String, Integer>();
		
		File inFile = new File(fileName);
		
		if(!inFile.exists()){
			System.out.println("Error counting words in document: File \"" + inFile.getAbsolutePath() + "\" does not exist.");
			return null;
		}
		
		System.out.println("Counting the words in file \"" + inFile.getAbsolutePath() + "\".");
		
		Scanner in = null;
		
		try{
			in = new Scanner(inFile);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

		
		while(in.hasNext()){
			String word = in.next().trim();
			word = stripPunctuation(word);
			if(wordCount.get(word) != null){
				int oldCount = wordCount.get(word);
				wordCount.put(word.toLowerCase(), oldCount + 1);
			}
			else{
				wordCount.put(word.toLowerCase(), 1);
			}
		}
		
		in.close();
		
		return wordCount;
	}
	
	public static String stripPunctuation(String word){
		//Basic punctuation
		word = word.replaceAll("[\\[\\]\\(\\)\".,;/!~?]+", "");
		//'s removal
		word = word.replaceAll("('s)|('S)", "");
		//' at the end removal
		word = word.replaceAll("'$", "");
		return word;
	}
	
	public static Hashtable<String, Integer> countWordsInDocs(String listFile){
		Hashtable<String, Integer> wordCount = new Hashtable<String, Integer>();
		
		File inFile = new File(listFile);
		
		if(!inFile.exists()){
			System.out.println("Error reading listfile to count words: File \"" + inFile.getAbsolutePath() + "\" does not exist.");
			return null;
		}
		
		Scanner in = null;
		
		try{
			in = new Scanner(inFile);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		while(in.hasNext()){
			String nextFile = in.nextLine().trim();
			Hashtable<String, Integer> temp = countWords(nextFile);
			Enumeration<String> e = temp.keys();
			
			while(e.hasMoreElements()){
				String word = e.nextElement();
				int count = temp.get(word);
				
				if(wordCount.get(word) == null){
					wordCount.put(word, 1);
				}
				else{
					int oldCount = wordCount.get(word);
					wordCount.put(word, count + oldCount);
				}
			}
		}
		
		in.close();
		return wordCount;
	}

}
