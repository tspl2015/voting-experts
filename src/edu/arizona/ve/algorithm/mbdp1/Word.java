package edu.arizona.ve.algorithm.mbdp1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
*
* @author  Daniel Hewlett
* Portions of this code adapted from Anand Venkataraman's C++ implementation.
*/
public class Word {
	private String sequence = new String(); 
	private Boolean[] boundaries;

	public Word() {}
	
	public Word(String word) {
		sequence = word;
		boundaries = new Boolean[word.length() + 1];
		Arrays.fill(boundaries, false);
	}

	public static Word fromList(List<String> list) {
		Word w = new Word();
		for (String s : list) {
			w.sequence += s;
		}
		
		w.boundaries = new Boolean[w.size() + 1];
		Arrays.fill(w.boundaries, false);
		
		return w; 
	}
	
	public void processLine(String line) {
		ArrayList<Boolean> boundaryList = new ArrayList<Boolean>(); 
		
		boolean bound = false;
		for (char c : line.toCharArray()) {
			if (c == ' ') {
				bound = true;
		    } else { 
		    	boundaryList.add(bound);
		    	sequence += c;
		    	bound = false;
		    }
		}
		
		boundaryList.add(true); // this means every sequence ends in a boundary - do we want this?
		
		boundaries = boundaryList.toArray(new Boolean[0]);
	}

	public void delBoundaries() {
		for (int i = 0; i < boundaries.length; i++) {
			boundaries[i] = false;
		}
	}
	
	public void insertBoundary(int index) {
		boundaries[index] = true;
	}
	
	public boolean hasBoundary(int index) {
		return boundaries[index];
	}
	
	public Word subseq(int from, int to) {
		Word newSeq = new Word();
		
		if (from > to || from > size()) { return newSeq; } 
		
		newSeq.sequence = sequence.substring(from, to);
		newSeq.boundaries = new Boolean[newSeq.sequence.length()+1];
		for (int i = 0; i < newSeq.boundaries.length; i++) {
			newSeq.boundaries[i] = false; //boundaries[i+from];
		}
		
		return newSeq;
	}

	public int size() {
		return sequence.length();
	}
	
	public Character get(int i) {
		return sequence.charAt(i);
	}
	
	@Override
	public int hashCode() {
		return sequence.hashCode();
	}
	
	public boolean equals(Object o) {
		return sequence.equals(((Word) o).sequence);
	}
	
	public String toString() {
//		System.out.println(boundaries.length + " " + sequence.length());
		String result = new String();
		for (int i = 0; i < sequence.length(); i++) {
			if (boundaries[i]) { result += ' '; }
			result += sequence.charAt(i);
		}
		if (boundaries[sequence.length()]) { result += " "; }
		
		return result;
	}
	
	public List<String> toList() {
		Vector<String> result = new Vector<String>();
		
		for (Character c : sequence.toCharArray()) {
			result.add(c.toString());
		}
		
		return result;
	}
}