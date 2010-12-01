package edu.arizona.ve.demo;

import edu.arizona.ve.api.Engine;
import edu.arizona.ve.api.Segmentation;
import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.Corpus.CorpusType;
import edu.arizona.ve.evaluation.EvaluationResults;
import edu.arizona.ve.evaluation.Evaluator;

public class ManualBVEDemo {

	/**
	 * A simple example of how to use BVE with manually-specified parameters.
	 * @param args
	 */
	public static void main(String[] args) {
		// Load the corpus 
		Corpus corpus = Corpus.autoLoad("bloom73", CorpusType.LETTER, true);
		// Create a segmentation engine, with a trie depth of 6
		Engine engine = new Engine(corpus, 6);
		// Segment using BVE with manually-specified parameters
		Segmentation segmentation = engine.voteBVE(5, 6, true, true);
		// Evaluate (compute precision, recall, F-measure, etc.) 
		EvaluationResults results = Evaluator.evaluate(segmentation, corpus);
		// Print the important evaluation metrics
		results.printResults();
	}

}
