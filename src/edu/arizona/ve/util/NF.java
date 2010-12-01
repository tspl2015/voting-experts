package edu.arizona.ve.util;

import java.text.NumberFormat;
/**
*
* @author  Daniel Hewlett
*/
public class NF {
	private static NumberFormat nf;
	static {
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
	}
	
	public static String format(double number) {
		if (Double.isNaN(number))
			return "-----";
		return nf.format(number);
	}
}
