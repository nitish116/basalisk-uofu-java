package scorer;

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

	}

}
