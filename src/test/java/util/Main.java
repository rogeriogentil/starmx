package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "import AAA    :	 a1 ; \n new line \n   import AAA:a2;\n end of string.";
		String regex = "import\\s+AAA\\s*:\\s*a2\\s*;";

		Pattern ptr = Pattern.compile(regex);
		Matcher m = ptr.matcher(s);
		
		String s2 = m.replaceFirst("#replaced str#");
		
		System.out.println(s);
		System.out.println();
		System.out.println(regex);
		System.out.println();
		System.out.println(s2);
	}

}
