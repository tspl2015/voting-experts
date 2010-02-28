package edu.arizona.algorithm.mbdp1;

/**
*
* @author  Daniel Hewlett
* Portions of this code adapted from Anand Venkataraman's C++ implementation.
*/
public class BrentSegmentation extends Segmentation {
	
	// This is the log version of the Relative Probability function - I have left this intact from Anand
	// Because when I tried to transcribe the formulas from the paper it didn't work right
	@Override
	public double relativeProb(Word useq) {
		  double nk = words.size();
		  double k = words.sumFreq();
		  double fk = words.freq(useq);
		  double relProb;
		  double Z = Math.log(6.0) - 2 * Math.log(Math.PI); 

		  if (k == 0) {		// First word?
			  relProb = -(2*Z - (useq.size()+1) * Math.log(phonemes.size()+1));
		  } else if (fk > 0) {		// Familiar word?
			  relProb = -(2*Math.log(fk) - Math.log(k+1) - Math.log(fk+1));
		  } else {				// Novel word
			  relProb = -(Z - Math.log(k+1) + 2*Math.log(nk) - Math.log(nk+1) 
					  + Math.log(wordProb(useq)) - Math.log(1 - (nk/(nk+1) * sumWordProb)));
		  }
		  
		  return relProb;
	}

	@Override
	public void registerUtterance(Word utterance) {
		// Does nothing
		
	}
}
