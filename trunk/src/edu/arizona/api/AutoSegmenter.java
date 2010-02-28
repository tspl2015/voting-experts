package edu.arizona.api;

import java.util.Collections;
import java.util.Vector;


import edu.arizona.api.Engine.Segmentation;
import edu.arizona.corpus.Corpus;
import edu.arizona.corpus.Corpus.CorpusType;

/**
*
* @author  Daniel Hewlett
*/
public class AutoSegmenter {

	Corpus corpus = new Corpus();

	// NB: String "*" will designate a boundary, but the input does not need to 
	// contain any boundaries as for now we are using the fully unsupervised version
	public AutoSegmenter(String[] input) {
		corpus.loadArray(input);
		corpus.setType(CorpusType.Word);
	}
	
	public boolean[] autoSegment() {
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		
		for (int window = 2; window < 10; window++) {
			Engine bidi = new Engine(corpus, window+1);
			segmentations.addAll(bidi.voteAllThresholds(window, 0, window));
		}
		
		Collections.sort(segmentations);
		
		Segmentation bestSegmentation = segmentations.get(0);
		
		return bestSegmentation.cutPoints;
	}
	
	public boolean[] segment(int windowSize, int threshold) {
		Engine exp = new Engine(corpus, windowSize + 1);
		Segmentation segmentation = exp.voteForward(windowSize, threshold, true);
		
		return segmentation.cutPoints;
	}
}