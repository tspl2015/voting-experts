/*
 * Evaluator.java
 *
 * Created on November 14, 2005, 11:21 AM
 */
package edu.arizona.evaluation;

import edu.arizona.api.Engine.Segmentation;
import edu.arizona.corpus.Corpus;
/**
 *
 * @author  Wesley Kerr, Daniel Hewlett
 */
public class Evaluator {
   
	public String delimiter;
		
	public int correctChunks;
   
    public int numExact;
    public int numDangling;
    public int numLost;
    public int n;
    public int numUnigrams; // meaning a chunk that has length 1
       
    public int numTruePositives = 0;
    public int numFalsePositives = 0;
    public int numTrueNegatives = 0;
    public int numFalseNegatives= 0;
    
//    int numPositives = 0;
    public int numNegatives = 0;
    
    public double precision;
    public double recall;
    public double fMeasure;
    
//    public double truePositiveRate; // not needed, is just recall
    public double falsePositiveRate;
   
   /** Creates a new instance of Evaluator */
   public Evaluator(String delim) {
	   delimiter = delim;
   }
   
   public Evaluator() {
	   delimiter = Corpus.BOUNDARY;
   }
   
   // Convenience function
   public EvaluationResults evaluate(Segmentation s, Corpus c) {
	   return evaluate(s.cutPoints, c.getCutPoints());
   }
   
   public EvaluationResults evaluate(boolean[] ve, boolean[] actual) { 
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
