package edu.arizona.demo;

import edu.arizona.api.AutoEngine;
import edu.arizona.api.Segmentation;
import edu.arizona.corpus.Corpus;
import edu.arizona.corpus.Corpus.CorpusType;
import edu.arizona.evaluation.EvaluationResults;
import edu.arizona.evaluation.Evaluator;

public class AutoBVEDemo {

	/** 
	 * A simple example of how to use the latest version of BVE with automatic parameter setting.
	 * Because it checks many possible parameter settings, execution may take some time.
	 * @param args
	 */
	public static void main(String[] args) {
		// Load the corpus 
		Corpus corpus = Corpus.autoLoad("bloom73", CorpusType.LETTER, true);
		// Segment using BVE with MDL (self-setting parameters)
		Segmentation segmentation = AutoEngine.autoBVE(corpus);
		// Evaluate (compute precision, recall, F-measure, etc.) 
		EvaluationResults results = Evaluator.evaluate(segmentation, corpus);
		// Print the important evaluation metrics
		results.printResults();
	}

}
