package edu.arizona.ve.algorithm.mbdp1;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
*
* @author  Daniel Hewlett
* Portions of this code adapted from Anand Venkataraman's C++ implementation.
*/
public class Score {
	int n;
	float nPredicts, nHits, nTrue;
	float nLexRight, nLexWrong;
	WordTable wt1 = new WordTable(), wt2 = new WordTable();
	
	public void update(Word word1, Word word2) {
		++n;
		
		for (int i = 1; i < word1.size() && i < word2.size(); i++) {
			boolean b1 = word1.hasBoundary(i);
			boolean b2 = word2.hasBoundary(i);
			
			if (b1 && b2) { // recall and precision
				++nHits;
			} 
			
			if (b1) { // missed precision
				++nPredicts;
			} 
			
			if (b2) { // missed recall
				++nTrue;
			}
		}
	}
	
	public String toString() {
		String result = new String();
		NumberFormat d = DecimalFormat.getPercentInstance();
		d.setMinimumFractionDigits(1);
		
		nLexWrong = nLexRight = 0;
		for (Word s : wt1.getWords()) {
			if (!wt2.getWords().contains(s)) {
				++nLexWrong;
			} else {
				++nLexRight;
			}
		}

		float lexTotal = nLexRight + nLexWrong;
		float precision = nHits / (nPredicts > 0 ? nPredicts : 1);
		float recall = nHits / (nTrue > 0 ? nTrue : 1);
		float f = (2 * precision * recall) / (precision + recall);
		result += 
			d.format(precision) + "\t"
			+ d.format(recall) + "\t"
			+ d.format(nLexRight/(lexTotal > 0 ? lexTotal : 1))   + "\t"
			+ (int) nLexRight + "\t" + (int) nLexWrong   + "\t"
			+ d.format(f); 

		nPredicts = nHits = nTrue = n = 0;
		
		return result;
	}

}
