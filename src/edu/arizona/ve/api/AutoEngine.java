package edu.arizona.ve.api;

import java.util.Collections;
import java.util.Vector;

import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.evaluation.EvaluationResults;
import edu.arizona.ve.evaluation.Evaluator;
import edu.arizona.ve.util.NF;

/**
* @author  Daniel Hewlett
*/
public class AutoEngine {

	// For convenience, hard-coded maxWindow = 9 (larger windows can result in much longer running time)
	public static Segmentation autoVE(Corpus c) {
		return autoVE(c, 9);
	}
	
	public static Segmentation autoVE(Corpus c, int maxWindow) {
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		
		for (int window = 2; window <= maxWindow; window++) {
			Engine engine = new Engine(c, window+1);
			segmentations.addAll(engine.bidiVoteAllThresholds(window, 0, window));
		}
		
		Collections.sort(segmentations);
		
		Segmentation bestSegmentation = segmentations.get(0);
		return bestSegmentation;
	}
	
	// For convenience, hard-coded maxWindow = 9 (larger windows can result in much longer running time)
	public static Segmentation autoBVE(Corpus c) {
		int maxWindow = 9;
		return autoBVE(c, maxWindow);
	}
	
	public static Segmentation autoBVE(Corpus c, int maxWindow) {
		double minDL = Double.MAX_VALUE;
		Segmentation bestSegmentation = null;
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		Vector<Double> scores = new Vector<Double>();
		
		int minWindow = 2;
		
		Engine engine = new Engine(c, maxWindow + 1);
		
		// Local max on
		for (int window = minWindow; window <= maxWindow; window++) {
//			System.out.println("WINDOW " + window);
			Segmentation temp = engine.voteBVEMDL(window, true, false);
			segmentations.add(temp);
			if (temp.descriptionLength < minDL) {
				minDL = temp.descriptionLength;
				bestSegmentation = temp;
			}
		}
		
		// Local max off
		for (int window = minWindow; window <= maxWindow; window++) {
//			System.out.println("WINDOW " + window);
			Segmentation temp = engine.voteBVEMDL(window, false, false);
			segmentations.add(temp);
			if (temp.descriptionLength < minDL) {
				minDL = temp.descriptionLength;
				bestSegmentation = temp;
			}
		}
		
		for (Segmentation s : segmentations) {
			EvaluationResults results = Evaluator.evaluate(s, c);
			scores.add(results.boundaryF1());
		}
		
//		System.out.println(scores);
		
		double maxBF = Double.MIN_VALUE;
		double meanBF = 0.0;
		for (Double bf : scores) {
			if (bf > maxBF) { maxBF = bf; }
			meanBF += bf;
		}
		meanBF /= scores.size();
		
		EvaluationResults results = Evaluator.evaluate(bestSegmentation, c);
		double mdlBF = results.boundaryF1();
		double percentOfBest = mdlBF / maxBF;
		
//		System.out.println(bestSegmentation);
		
		System.out.println("MDL REPORT: ");
		System.out.println(c.getName() + "\t" + NF.format(meanBF) + "\t" +
			NF.format(mdlBF) + "\t" + NF.format(maxBF) + "\t" + NF.format(percentOfBest));
		
		return bestSegmentation;
	}
}