package edu.arizona.ve.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.Corpus.CorpusType;
import edu.arizona.ve.experts.BackwardEntropyExpert;
import edu.arizona.ve.experts.ForwardEntropyExpert;
import edu.arizona.ve.experts.SurprisalExpert;
import edu.arizona.ve.trie.Trie;
import edu.arizona.ve.util.Utils;

public class ErrorAnalyzer {

	public static boolean scoreWindow(List<Boolean> actual, boolean[] ve) {
		for (int i = 0; i < actual.size(); i++) {
			if (actual.get(i) != ve[i])
				return false;
		}
		
		return true;
	}
	
	public static void analyzeErrors(Corpus corpus, int windowSize) {
		System.out.println("========================================================");
		System.out.println("Performing Error Analysis for Window Size " + windowSize);
		
		Trie trie = corpus.makeForwardTrie(windowSize + 1);
		ForwardEntropyExpert fe = new ForwardEntropyExpert(trie);
		BackwardEntropyExpert be = new BackwardEntropyExpert(corpus.makeBackwardTrie(windowSize + 1));
		SurprisalExpert se = new SurprisalExpert(trie);
		
		ArrayList<Boolean> cuts = new ArrayList<Boolean>(corpus.getCutPoints().length + 2);
		cuts.add(true);
		for (int i = 0; i < corpus.getCutPoints().length; i++) 
			cuts.add(corpus.getCutPoints()[i]);
		cuts.add(true);
		
		int totalWindows = 0;
		int errorWindows = 0;
		int zeroWindows = 0;
		int multWindows = 0;
		int veErrorWindows = 0, seErrorWindows = 0, feErrorWindows = 0, beErrorWindows = 0;
		int totalVoteChances = 0;
		int errors = 0;
		for (int i = 0; i <= corpus.getCleanChars().size() - windowSize; i++) {
			List<String> subSeq = Collections.unmodifiableList(corpus.getCleanChars().subList(i, i + windowSize));
			List<Boolean> actualCuts = Collections.unmodifiableList(cuts.subList(i, i + windowSize + 1));
			
			int numCutPoints = countCutPoints(actualCuts);
			errors += Math.abs(numCutPoints - 1);
			errorWindows += (numCutPoints == 1 ? 0 : 1);
			
			if (numCutPoints == 0) { zeroWindows++; }
			if (numCutPoints > 1) { multWindows++; }
			
			boolean[] feCuts = fe.segment(subSeq);
			if (!scoreWindow(actualCuts, feCuts))
				feErrorWindows++;
			
			boolean[] beCuts = be.segment(subSeq);
			if (!scoreWindow(actualCuts, beCuts))
				beErrorWindows++;
			
			boolean[] seCuts = se.segment(subSeq);
			if (!scoreWindow(actualCuts, seCuts))
				seErrorWindows++;
			
			boolean correct = scoreWindow(actualCuts, Utils.simpleUnion(Utils.simpleUnion(feCuts, seCuts), beCuts));
			if (!correct) { veErrorWindows++; };
			
			totalWindows++;
			totalVoteChances += actualCuts.size();
//			System.out.println(subSeq + " " + actualCuts);
		}
		
		System.out.println("Windows: " + totalWindows);
		// This one isn't exactly right
//		System.out.println("VE Error Windows: " + veErrorWindows + " (" + ((double) veErrorWindows / totalWindows) * 100 + "%)");
		System.out.println("Freq Error Windows: " + feErrorWindows + " (" + ((double) feErrorWindows / totalWindows) * 100 + "%)");
		System.out.println("Ent Error Windows: " + seErrorWindows + " (" + ((double) seErrorWindows / totalWindows) * 100 + "%)");
		System.out.println("Forced Error Windows: " + errorWindows + " (" + ((double) errorWindows / totalWindows) * 100 + "%)");
		System.out.println("  Zero Cuts: " + zeroWindows + " (" + ((double) zeroWindows / errorWindows) * 100 + "%)");
		System.out.println("  Multiple Cuts: " + multWindows + " (" + ((double) multWindows / errorWindows) * 100 + "%)");
//		System.out.println(zeroWindows + " " + multWindows);
//		System.out.println("Vote Chances: " + totalVoteChances);
//		System.out.println("Errors: " + errors + "(" + ((double) errors / totalVoteChances) * 100 + "%)");
	}
	
	public static int countCutPoints(List<Boolean> cuts) {
		int count = 0;
		for (boolean b : cuts) {
			if (b) { count++; }
		}
		return count;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Corpus corpus = Corpus.autoLoad("br87", CorpusType.LETTER, true);
		Corpus corpus = Corpus.autoLoad("caesar", CorpusType.LETTER, false);
		for (int windowSize = 2; windowSize <= 6; windowSize++) {
			analyzeErrors(corpus, windowSize);
		}
	}
}
