package basilisk;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;

public class SeedWordLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(loadFromFile("location-seeds"));
	}
	
	public static HashSet<String> loadFromFile(String fileName){
		HashSet<String> result = new HashSet<String>();
		
		File f = new File(fileName);
		
		if(!f.exists()){
			System.err.println("Seed file does not exist: " + f.getAbsolutePath());
		}
		
		System.out.println("Loading seeds from file: " + f.getAbsolutePath());
		return loadFromString(FileHelper.fileToString(f));
	}
	
	public static HashSet<String> loadFromString(String input){
		HashSet<String> result = new HashSet<String>();
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			result.add(in.nextLine().trim());
		}
		return result;
	}
}
