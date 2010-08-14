package edu.arizona.evaluation;

import edu.arizona.util.NF;

public class EvaluationResults {
	public int actualChunkCount;
	public int estimatedChunkCount;
	
	public double boundaryPrecision;
	public double boundaryRecall;
	public double chunkPrecision;
	public double chunkRecall;
	
	public double boundaryF1() {
		return F1(boundaryPrecision, boundaryRecall);
	}
	
	public double chunkF1() {
		return F1(chunkPrecision, chunkRecall);
	}
	
	public double F1(double precision, double recall) {
		return 2 * ((precision * recall) / (precision + recall));
	}
	
   public void printResults() {
//	   System.out.println("VE Number of Chunks:     " + estimatedChunkCount);
//	   System.out.println("Actual Number of Chunks: " + actualChunkCount);
	   System.out.println("BP\tBR\tBF\tWP\tWR\tWF");
	   System.out.print(NF.format(boundaryPrecision) + "\t" + NF.format(boundaryRecall) + "\t" + NF.format(boundaryF1()) + "\t");
	   System.out.print(NF.format(chunkPrecision) + "\t" + NF.format(chunkRecall) + "\t" + NF.format(chunkF1()));
	   System.out.println();
   }
}
