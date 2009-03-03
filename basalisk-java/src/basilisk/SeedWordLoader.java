package basilisk;

import java.io.File;
import java.util.*;

public class SeedWordLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(loadFromFile("location-seeds"));
	}
	
	public static Set<String> loadFromFile(String fileName){
		File f = new File(fileName);
		
		if(!f.exists()){
			System.err.println("Seed file does not exist: " + f.getAbsolutePath());
		}
		
		System.out.println("Loading seeds from file: " + f.getAbsolutePath());
		return loadFromString(FileHelper.fileToString(f));
	}
	
	public static Set<String> loadFromString(String input){
		Set<String> result = new HashSet<String>();
		
		Scanner in = new Scanner(input);
		
		while(in.hasNextLine()){
			result.add(in.nextLine().trim().toLowerCase());
		}
		return result;
	}
}
