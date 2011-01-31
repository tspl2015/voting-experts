package edu.arizona.ve.util;

import java.util.Collection;
import java.util.Vector;

public class Stats {

	public static double mean(Collection<Double> numbers) {
		double mean = 0.0;
		for (Double number : numbers) {
			mean += number;
		}
		mean /= numbers.size();
		return mean;
	}
	
	public static double max(Collection<Double> numbers) {
		double max = Double.NEGATIVE_INFINITY;
		for (Double number : numbers) {
			if (number > max) 
				max = number;
		}
		return max;
	}
	
	public static double min(Collection<Double> numbers) {
		double min = Double.POSITIVE_INFINITY;
		for (Double number : numbers) {
			if (number < min) 
				min = number;
		}
		return min;
	}
	
	/**
	 * Sample standard deviation
	 * @param numbers
	 * @return
	 */
	public static double stDev(Collection<Double> numbers) {
		int n = numbers.size();
		double m = mean(numbers);
		
		double sum = 0.0;
		for (Double x : numbers) {
			sum += Math.pow(x - m, 2);
		}

		return Math.sqrt((1.0 / (n-1.0)) * sum);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector<Double> data = new Vector<Double>();
		data.add(1.0);
		data.add(2.0);
		data.add(3.0);
		data.add(4.0);
		
		System.out.println(mean(data));
		System.out.println(max(data));
		System.out.println(stDev(data));
	}

}
