package edu.arizona.algorithm.mbdp1;
import java.util.HashMap;

/**
*
* @author  Daniel Hewlett
*/
public class PhonemeTable {	
	private HashMap<Character,Integer> phonemeMap = new HashMap<Character,Integer>();
	private int totalFreq = 0;
	
	public int size() { return phonemeMap.size(); }

	public int sumFreq() {
		return totalFreq;
	}
	
	public int freq(Character phoneme) {
		Integer f = phonemeMap.get(phoneme);
		return (f == null ? 0 : f);
	}
	
	public int incFreq(Character phoneme) {
		++totalFreq;
		
		Integer f = phonemeMap.get(phoneme);
		
		if (f == null) {
			phonemeMap.put(phoneme, 1);
		} else {
			phonemeMap.put(phoneme, f + 1);
		}
		
		return phonemeMap.get(phoneme);
	}
	
	void readPhonemes(String dict) {
		String[] split = dict.split(" ");
		for (int i = 0; i < split.length; i += 2) {
			char ph = split[i].charAt(0);
//			char cat = split[i+1].charAt(0); // Ignored
		    if (phonemeMap.containsKey(ph)) {
		      System.err.println("Something is wrong.  There was a duplicate phoneme [" + ph + "] in the\n"
			   + "phoneme dictionary.  Perhaps a missing field threw me off track?");
		      System.exit(1);
		    }
		    
		    totalFreq++;
		    phonemeMap.put(ph, 1);
		  }
		
		  totalFreq++;
		  phonemeMap.put(' ', 1);
	}
	
	public String toString() {
		String result = "Contents of Phoneme Table:\nPh\tFreq\tCat\n";
		for (Character phoneme : phonemeMap.keySet()) {
		    result = result + '[' + phoneme + ']' + "\t" + phonemeMap.get(phoneme) + "\n";
		}
		result = result + phonemeMap.size() + " Phonemes (including ' ') and " + totalFreq + " instances\n";
		return result;
	}
}
