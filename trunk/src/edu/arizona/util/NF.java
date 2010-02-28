package edu.arizona.util;

import java.text.NumberFormat;
/**
*
* @author  Daniel Hewlett
*/
public class NF {
	private static NumberFormat nf;
	static {
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
	}
	
	public static String format(double number) {
		return nf.format(number);
	}
}
