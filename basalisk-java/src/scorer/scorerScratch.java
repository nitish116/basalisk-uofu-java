package scorer;

import java.util.ArrayList;
import java.util.Collections;

public class scorerScratch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String categoryAndSuffix = "pizza-mcat-plus.lexicon";
		String category = categoryAndSuffix.replaceAll("-.*", "");
		String suffix = categoryAndSuffix.substring(categoryAndSuffix.indexOf("-"), categoryAndSuffix.indexOf("."));
		System.out.println("Category: " + category + " suffix: " + suffix);
		
		if(2.0 < 2.4)
			System.out.println("double comparison success");
		
	}

}
