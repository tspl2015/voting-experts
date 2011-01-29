package edu.arizona.ve.analysis;

import java.util.Arrays;

import edu.arizona.ve.api.Segmentation;
import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.Corpus.CorpusType;
import edu.arizona.ve.evaluation.EvaluationResults;
import edu.arizona.ve.evaluation.Evaluator;

public class AllPoints {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Corpus corpus = Corpus.autoLoad("orwell-short", CorpusType.LETTER, false);
		// Segment using BVE with MDL (self-setting parameters)
		Segmentation segmentation = new Segmentation();
		segmentation.cutPoints = new boolean[corpus.getCutPoints().length];
		Arrays.fill(segmentation.cutPoints, true);
		// Evaluate (compute precision, recall, F-measure, etc.) 
		EvaluationResults results = Evaluator.evaluate(segmentation, corpus);
		// Print the important evaluation metrics
		results.printResults();
	}

}
