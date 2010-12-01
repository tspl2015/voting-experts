package edu.arizona.ve.experts;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.ve.trie.Trie;


/**
*
* @author  Daniel Hewlett
*/
public class KnowledgeExpert extends Expert {

	/**
	 * Please make sure to provide a forward knowledge trie!
	 * @param trie
	 */
	public KnowledgeExpert(Trie trie) {
		super(trie);
	}

	@Override
	public boolean[] segment(List<String> segment) {
		int cutSize = segment.size() + 1;
		
		boolean[] votes = new boolean[cutSize];
		double[] scorefKnowledge = new double[segment.size()+1];
		for (int i = 0; i < scorefKnowledge.length; i++) {
			scorefKnowledge[i] = Double.POSITIVE_INFINITY;
		}
		
		// Special case for beginning 
//		ArrayList<String> segK0 = new ArrayList<String>(segment); 
//		segK0.add(0, "*");
//		scorefKnowledge[0] = _trie.getStdIntEntropy(segK0); 
//		System.out.println(scorefKnowledge[0] + " " + segK0);
		
		List<String> seg1, seg2;
		for (int i = 1; i < segment.size(); ++i) {
			seg1 = segment.subList(0,i);
			seg2 = segment.subList(i, segment.size());
			
			// Prior Knowledge 
			ArrayList<String> segK1 = new ArrayList<String>(seg1); 
			segK1.add("*");
			scorefKnowledge[i] = _trie.getStdIntEntropy(segK1);
			
			ArrayList<String> segK2 = new ArrayList<String>(seg2);
			segK2.add(0, "*");
			scorefKnowledge[i] += _trie.getStdIntEntropy(segK2);
			
//			System.out.println(scorefKnowledge[i] + " " + segK1 + " || " + segK2);
		}

		// special case for end
//		ArrayList<String> segK = new ArrayList<String>(segment); 
//		segK.add("*");
//		scorefKnowledge[segment.size()] = _trie.getStdIntEntropy(segK); 
//		System.out.println(scorefKnowledge[segment.size()] + " " + segK);
		
		double minfKnowledgeEnt = Double.POSITIVE_INFINITY;

		int cutfKnowledge = -1;

		// <= ? that should favor pushing the tie to the next item - do we want that?
		for (int i = 0; i < scorefKnowledge.length; ++i) {
			if (scorefKnowledge[i] < minfKnowledgeEnt) {
				minfKnowledgeEnt = scorefKnowledge[i];
				cutfKnowledge = i;
			}
		}

		if (cutfKnowledge != -1) {
			votes[cutfKnowledge] = true;
		} else {
//			System.out.println("Don't know anything about " + segment);
		}
		
		return votes;
	}

}
