package edu.arizona.ve.algorithm.mbdp1;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;

/**
*
* @author  Daniel Hewlett
* Portions of this code adapted from Anand Venkataraman's C++ implementation.
*/
public class WordTable {
	private HashMap<Word,Integer> wordMap = new HashMap<Word,Integer>();
	private int totalFreq = 0;
	
	public WordTable() {}
	
	public WordTable(HashMap<String,Integer> seedLexicon) {
		for (String word : seedLexicon.keySet()) {
			int count = seedLexicon.get(word);
			Word w = new Word(word);
			wordMap.put(w, count);
			totalFreq += count;
		}
	}
	
	public static WordTable fromFile(String file) {
		HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				String[] split = line.split("\\s");
				wordCounts.put(split[0], 100 * Integer.parseInt(split[1]));
			}
		} catch (Exception e) {	e.printStackTrace(); }
		
		return new WordTable(wordCounts);
	}
	
	public int size() { return wordMap.size(); }

	public int sumFreq() { return totalFreq; }
	
	public int freq(Word useq) {
		if (wordMap.containsKey(useq)) { 
			return wordMap.get(useq);
		} else {
			return 0;
		}
	}

	public int incFreq(Word useq) { 
		++totalFreq; 
		if (wordMap.containsKey(useq)) {
			wordMap.put(useq, wordMap.get(useq) + 1);
		} else {
			wordMap.put(useq, 1);
		}
		
		return wordMap.get(useq); 
	}
	
	public void clear() { wordMap.clear(); totalFreq = 0; }
	
	public String toString() {
		  String result = "Contents of Word Table:\nFreq\tWord\n";
		  for (Word useq : wordMap.keySet()) {
		    result += wordMap.get(useq) + "\t\t" + "[" + useq + "]\n";
		  }
		  result += size() + " Words and " + totalFreq + " instances\n";
		  return result;
	}
	
	public void printSorted() {
//		Vector<StringInt> v = FrameClustering.sortWords(wordMap);
//		for (StringInt si : v) {
//			System.out.println(si);
//		}
//		System.out.println(size() + " Words and " + totalFreq + " instances\n");
	}
	
	public Set<Word> getWords() {
		return wordMap.keySet();
	}
}