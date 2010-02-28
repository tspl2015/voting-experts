package edu.arizona.algorithm.mbdp1;

/**
*
* @author  Daniel Hewlett
* Portions of this code adapted from Anand Venkataraman's C++ implementation.
*/
public abstract class Segmentation {
	PhonemeTable phonemes;
	WordTable words;
	double sumWordProb;
	int prevBound = 0;
	
	public Segmentation() {
		phonemes = new PhonemeTable();
		words = new WordTable();
	}
	
	public void setPhonemeTable(PhonemeTable pt) {
		phonemes = pt;
	}
	
	public void setWordTable(WordTable init) { 
		words = init; 
	}
	
	public void commit(Word useq) {
		int prevBound = 0;
		  
		for (int i = 0; i < useq.size(); i++) {
			if (useq.hasBoundary(i)) {
				Word subseq = useq.subseq(prevBound, i);
		      
				if (words.incFreq(subseq) == 1) { // meaning, if this the first time we've seen this word
					for (int j = 0; j < subseq.size(); j++) {
						phonemes.incFreq(subseq.get(j));
					}
					phonemes.incFreq(' ');
				}
		      	prevBound = i;
		    } 
		}
		
		// last boundary
		Word subseq = useq.subseq(prevBound, useq.size());  
		if (words.incFreq(subseq) == 1) { // meaning, if this the first time we've seen this word
			for (int j = 0; j < subseq.size(); j++) {
				phonemes.incFreq(subseq.get(j));
			}
			phonemes.incFreq(' ');
		}
		
		sumWordProb = findSumWordProb();
	}
	
	public double wordProb(Word useq) {
		double x = ((double) phonemes.freq(' ')) / phonemes.sumFreq();
		double prob = x / (1-x); // this factors in that boundary is a part of the word

		for (int i = 0; i < useq.size(); i++) {
			prob *= ((double) phonemes.freq(useq.get(i))) / phonemes.sumFreq();
		}
		
		return prob;
	}
	
	public double findSumWordProb() {
		double sum = 0;

		for (Word useq : words.getWords())
			sum += wordProb(useq);
		
		return sum;
	}
	
	public abstract double relativeProb(Word useq);
		
	public abstract void registerUtterance(Word utterance);
	
	// I have modified this to be as similar to p.99 of Brent 1999 as possible
	// DO NOT CHANGE - any small change can kill the algorithm completely
	public double dynSearch(Word utterance) {
		// implementation detail
		if (utterance.size() == 0)
		    return 0;
		
		registerUtterance(utterance);
		
		// This is the core algorithm, MDBP-1
		double[] bestProduct = new double[utterance.size()];
		int[] bestStart = new int[utterance.size()];

		for (int lastChar = 0; lastChar < utterance.size(); lastChar++) {
			bestProduct[lastChar] = relativeProb(utterance.subseq(0,lastChar+1));
		    bestStart[lastChar] = 0; 
		    for (int firstChar = 1; firstChar <= lastChar; firstChar++) {
		    	double wordScore = relativeProb(utterance.subseq(firstChar,lastChar+1)); 
		    	if (wordScore + bestProduct[firstChar-1] < bestProduct[lastChar]) {
		    		bestProduct[lastChar] = wordScore + bestProduct[firstChar-1];
		    		bestStart[lastChar] = firstChar;
		    	}
		    }
		}
	
//		System.out.println(Arrays.toString(bestProduct));
		
		int firstChar = bestStart[utterance.size()-1];
		while (firstChar > 0) {
		    utterance.insertBoundary(firstChar);
		    firstChar = bestStart[firstChar-1];
		} 
		
		return bestProduct[utterance.size()-1];
	}
}
