package edu.arizona.algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;


import edu.arizona.corpus.Corpus;
import edu.arizona.trie.Trie;

/**
*
* @author  Daniel Hewlett
* An incremental (sentence-at-a-time) version of VE. Used for comparison with 
* MBDP-1 algorithm in Hewlett and Cohen (2009).
*/
public class IncrementalVE {
	VotingExperts ve;
	Trie trie;
	int trieDepth;
	int windowSize = 7;
	int threshold = 3;
	int n = 0;
	int wait = 40;
	
	public IncrementalVE() {
		trie = new Trie();
		trieDepth = windowSize + 1;
	}
	
	public String segment(String utterance) {
		if (n < wait) { n++; return utterance; } // can't segment the first utterance 
		
		Corpus cl = new Corpus();
		cl.loadString(utterance);
		
		ve = VotingExperts.makeForwardVE(cl, trie, windowSize, threshold);
		ve.runAlgorithm(true);
		
		String segmented = new String();
		for (List<String> seg : ve.getSegments()) {
			for (String c : seg) { segmented += c; }
			segmented += " ";
		}
		segmented = segmented.trim();
//		System.out.println(segmented);
//		System.out.println(ve.getSegments());
		
		n++;
		
		return segmented;
	}
	
	public void commit(String segmented) {	
//		String[] segments = segmented.split(" ");
		
		Trie.addAll(trie, split(segmented.replaceAll(" ", "")), trieDepth);
	
		Trie.generateStatistics(trie);
	}
	
	public List<String> split(String s) {
		List<String> result = new Vector<String>();
		for (Character c : s.toCharArray()) {
			result.add(c.toString());
		}
		return result;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		String corpus = "input/brent-corpus.txt";
//		String corpus = "input/CHILDES/brent-data-brent.txt";
		String corpus = "input/sent/br87.txt";
		
		IncrementalVE exp = new IncrementalVE();
		
		int sentNum = 0;
		
		BufferedReader in = new BufferedReader(new FileReader(corpus));
		PrintStream out = new PrintStream("output/incremental-output.txt");
		for (String line = in.readLine(); line != null; line = in.readLine()) {
//			System.out.println(line);
			String stripped = line.replaceAll(" ", "");
			String segmented = exp.segment(stripped); // this does the actual segmentation
			
			System.out.println(sentNum + ": " + segmented);
			out.println(segmented);
			
			if (!segmented.replaceAll(" ", "").equals(stripped))
				System.exit(1);
			
			exp.commit(segmented);
			
			sentNum++;
		}
		
		out.close();
		
		MBDP1.score("output/incremental-output.txt", corpus,500);
	}
}
