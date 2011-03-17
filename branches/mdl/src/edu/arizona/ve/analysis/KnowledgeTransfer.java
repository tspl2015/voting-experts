package edu.arizona.ve.analysis;

import edu.arizona.ve.api.AutoEngine;
import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.Corpus.CorpusType;
import edu.arizona.ve.util.NF;

public class KnowledgeTransfer {
	public static void testTransfer(Corpus fullTrain, Corpus test, double percentTraining) {
		System.out.println("Performing transfer test with " + NF.format(percentTraining) + " percent of training corpus...");
		
		Corpus train = fullTrain.getSubCorpus((int) Math.floor(fullTrain.getSegmentedChars().size() * percentTraining));
		
		AutoEngine.autoTransfer(train, test);
		
		System.out.println();
	}
	
	/**
	 * Illustrates BVE building knowledge trie and setting parameters by segmenting (a certain percentage of)
	 * a training corpus, and then testing performance on another corpus with the same knowledge trie and parameters.
	 * @param args
	 */
	public static void main(String[] args) {
		Corpus train = Corpus.autoLoad("uav-train", CorpusType.WORD, true);
		Corpus test = Corpus.autoLoad("uav-test", CorpusType.WORD, true);
			
		for (double percentTraining = 0.05; percentTraining <= 1.01; percentTraining += 0.05) {
			testTransfer(train, test, percentTraining);
		}
	}
}
