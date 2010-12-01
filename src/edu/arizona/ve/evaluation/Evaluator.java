/*
 * Evaluator.java
 *
 * Created on November 14, 2005, 11:21 AM
 */
package edu.arizona.ve.evaluation;

import java.util.Arrays;

import edu.arizona.ve.api.Segmentation;
import edu.arizona.ve.corpus.Corpus;
/**
 *
 * @author  Wesley Kerr, Daniel Hewlett
 */
public class Evaluator {
	
	// Evaluate the "All Locations" baseline which segments at every possible location 
	public static EvaluationResults evaluateAllLocations(Corpus c) {
		return evaluateAllLocations(c.getCutPoints());
	}
	
	public static EvaluationResults evaluateAllLocations(boolean[] actual) {
		boolean[] allCutPoints = new boolean[actual.length];
		Arrays.fill(allCutPoints, true);
		return evaluate(allCutPoints, actual);
	}
	
	// Evaluate a proposed segmentation against the original corpus
	public static EvaluationResults evaluate(Segmentation s, Corpus c) {
		return evaluate(s.cutPoints, c.getCutPoints());
	}   
   
	public static EvaluationResults evaluate(boolean[] ve, boolean[] actual) { 
		int correctChunks = 0;
   
		int numExact;
		int numDangling;
		int numLost;
	   
		int numTruePositives = 0;
		int numFalsePositives = 0;
		int numTrueNegatives = 0;
		int numFalseNegatives= 0;
	
		int numNegatives = 0;

		if (ve.length != actual.length) {
			throw new RuntimeException("CUT POINT LENGTHS DO NOT MATCH: " + ve.length + " should equal " + actual.length);
		}
	   
//      System.out.println("evaluating...");
		int length = ve.length;
      
		int veBoundCount = 0;
		for (int i = 0; i < ve.length; ++i) {
			if (ve[i]) ++veBoundCount;
		}
      
		numExact = numLost = numDangling = 0;
      
		int l1, l2, r1, r2;
		l1 = l2 = 0; // no effect, why is this here?
      
		while (l1 < length) {
			r1 = l1+1;
         
			while (r1 < length && !actual[r1]) {
				++r1;
			}
         
			l2 = l1;
			r2 = r1;
         
			while (l2 >=0 && !ve[l2]) --l2;
			while (r2 < length && !ve[r2]) ++r2;
         
			if (r1 == r2 && l1 == l2) {
				++numExact;
            
            int divider = 0;
            for (int i = l2+1; i < r2; ++i) {
               if (ve[i]) ++divider;            	
            }
            if (divider == 0) ++correctChunks;
         } else {
            if (r1 == r2 || l1 == l2) {
               ++numDangling;
            } else {
               ++numLost;
            }
         }
         l1 = r1;
      }
      
      numTruePositives = 0;
      numFalsePositives = 0;
      numTrueNegatives = 0;
      numFalseNegatives= 0;
      for (int i = 0; i < actual.length; ++i) {
    	  if (ve[i]) {
    		  if (actual[i]) {
    			  numTruePositives++;
    		  } else {
    			  numFalsePositives++;
    			  numNegatives++;
    		  }
    	  } else {
    		  if (actual[i]) {
    			  numFalseNegatives++;
    		  } else {
    			  numTrueNegatives++;
    			  numNegatives++;
    		  }
    	  }
      }
      
      int total = numTruePositives + numFalsePositives + numTrueNegatives + numFalseNegatives;
      
      if (total != ve.length) {
    	  throw new RuntimeException("YO WE GOT PROBLEMS! " + total + " " + ve.length);
      }
      
//      precision = numTruePositives / ((double) numTruePositives + numFalsePositives);
//      recall    = numTruePositives / ((double) numTruePositives + numFalseNegatives);
//      fMeasure  = 2 * precision * recall / (precision + recall);
//      falsePositiveRate = ((double) numFalsePositives) / numNegatives;
      
      EvaluationResults results = new EvaluationResults();
      results.boundaryPrecision = numTruePositives / ((double) numTruePositives + numFalsePositives);
      results.boundaryRecall = numTruePositives / ((double) numTruePositives + numFalseNegatives);
      results.actualChunkCount = (numTruePositives + numFalseNegatives+1);
      results.estimatedChunkCount = (numTruePositives + numFalsePositives+1);
      results.chunkPrecision = correctChunks / ((double) results.estimatedChunkCount);
      results.chunkRecall = correctChunks / ((double) results.actualChunkCount);
      
      return results;
   }
}
