package edu.arizona.ve.experts;

import java.util.List;

import edu.arizona.ve.trie.Trie;


/**
*
* @author  Daniel Hewlett
*/
public class SurprisalExpert extends Expert {

	// What I've been calling "Internal Entropy" is more properly called "Surprisal"
	public SurprisalExpert(Trie trie) {
		super(trie);
	}

	public boolean[] segment(List<String> segment) {
		int cutSize = segment.size() + 1;
		
		double[] scoreIEnt = new double[cutSize];
		boolean[] votes = new boolean[cutSize];
		
		List<String> seg1, seg2;
		for (int i = 1; i < segment.size(); ++i) {
			seg1 = segment.subList(0,i);
			seg2 = segment.subList(i, segment.size());
			
			scoreIEnt[i] = _trie.getStdIntEntropy(seg1) + _trie.getStdIntEntropy(seg2);
		}

		scoreIEnt[segment.size()] = _trie.getStdIntEntropy(segment);

		double minIEnt = scoreIEnt[1];

		int cutFreq = 1;

		for (int i = 1; i <= segment.size(); ++i) {
			if (scoreIEnt[i] <= minIEnt) {
				minIEnt = scoreIEnt[i];
				cutFreq = i;
			}
		}

		votes[cutFreq] = true;
		
		return votes;
	}

}
