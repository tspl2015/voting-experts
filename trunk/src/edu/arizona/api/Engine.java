package edu.arizona.api;

import java.util.List;
import java.util.Vector;

import edu.arizona.algorithm.VotingExperts;
import edu.arizona.api.Segmentation.Direction;
import edu.arizona.corpus.Corpus;
import edu.arizona.corpus.CorpusWriter;
import edu.arizona.evaluation.EvaluationResults;
import edu.arizona.evaluation.Evaluator;
import edu.arizona.trie.Trie;
import edu.arizona.util.Utils;

/**
* @author Daniel Hewlett
*/
public class Engine {

	public static boolean EVAL_FORWARD = false;
	public static boolean EVAL_BACKWARD = false;
	public static boolean EVALUATE = false;
	
	public static boolean DEBUG = false;
	
	int trieDepth;

	// The corpus 
	Corpus corpus;
	
	// The tries
	Trie forwardTrie;
	Trie backwardTrie;

	// The knowledge tries
	Trie forwardKnowledgeTrie;
	Trie backwardKnowledgeTrie;
	
	// Segmentations
	Segmentation forwardSegmentation = null;
	Segmentation backwardSegmentation = null;
	Segmentation partialSegmentation = null;
	Segmentation bidiSegmentation = null;
	
	// TODO: Implement a more elegant way to test the transfer condition
	VotingExperts pve;

	public Engine(Corpus corpus, int trieDepth) {
		this.trieDepth = trieDepth;
		this.corpus = corpus;
		initTries();
	}

	public void setCorpus(Corpus c) {
		corpus = c;
	}
	
	private void initTries() {
		forwardTrie = corpus.makeForwardTrie(trieDepth);
		backwardTrie = corpus.makeBackwardTrie(trieDepth);
	}
	
	public Trie getForwardKnowledgeTrie() {
		return forwardKnowledgeTrie;
	}
		
	public List<String> getForwardCorpus() {
		return corpus.getCleanChars();
	}
	
	public Corpus getCorpus() {
		return corpus;
	}
	
	public Trie getForwardTrie() {
		return forwardTrie;
	}

	public Trie getBackwardTrie() {
		return backwardTrie;
	}

	public Segmentation voteBVE(int window, int minThreshold, boolean useLocalMax, boolean bidiBVE) {
		// Try to GC the backward trie, now that we don't need it for voting
		backwardKnowledgeTrie = null;
		
		int startThreshold = window * (bidiBVE ? 3 : 2); // window + thresholdOffset 
		
		// Experimenting with dummy corpus 0 this just means the first time the knowledge expert won't do anything
		List<String> forwardKnowledgeCorpus = corpus.getCleanChars(); //new ArrayList<String>(bidiInput.getCleanChars());
		forwardKnowledgeTrie = new Trie();
		Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
		forwardKnowledgeTrie.generateStatistics();

		partialSegmentation = votePartial(window, startThreshold, useLocalMax, bidiBVE);
		CorpusWriter.writeCorpus(corpus.getName()  + "-partial-1.txt", corpus, partialSegmentation);
		
		if (DEBUG)
			evaluate();
		
		bidiSegmentation = null; // stop the printing of the bidi array, we're done with it
		
		int i = 1;
		for (int threshold = startThreshold - 1; threshold >= minThreshold; threshold-- ) {
			Corpus partialInputRest = new Corpus();
			partialInputRest.naiveLoad("output/" + corpus.getName()  + "-partial-" + i + ".txt", corpus.getType());
			
			forwardKnowledgeCorpus = partialInputRest.getCleanChars();
			forwardKnowledgeTrie = new Trie();
			Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
			forwardKnowledgeTrie.generateStatistics();
			
			partialSegmentation = votePartial(window, threshold, useLocalMax, bidiBVE);
			CorpusWriter.writeCorpus(corpus.getName()  + "-partial-" + (i+1) + ".txt", corpus, partialSegmentation);
			
			if (DEBUG)
				evaluate();
			
			i++;
		}
		
		//Trie.extractWords(forwardKnowledgeTrie);
		
		return partialSegmentation;
	}
	
	public Segmentation voteBVEMDL(int window, boolean useLocalMax, boolean bidiBVE) {
		// Try to GC the backward trie, now that we don't need it for voting
		backwardKnowledgeTrie = null;
		
		int startThreshold = window * (bidiBVE ? 3 : 2); // window + thresholdOffset 
		
		// Experimenting with dummy corpus 0 this just means the first time the knowledge expert won't do anything
		List<String> forwardKnowledgeCorpus = corpus.getCleanChars(); //new ArrayList<String>(bidiInput.getCleanChars());
		forwardKnowledgeTrie = new Trie();
		Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
		forwardKnowledgeTrie.generateStatistics();

		partialSegmentation = votePartial(window, startThreshold, useLocalMax, bidiBVE);
		CorpusWriter.writeCorpus(corpus.getName()  + "-partial-" + startThreshold + ".txt", corpus, partialSegmentation);
		
		if (DEBUG)
			evaluate();
		
		bidiSegmentation = null; // stop the printing of the bidi array, we're done with it
		
		// For now, let's segment with all thresholds and pick the best
		
		double minDL = Double.MAX_VALUE;
		Segmentation bestSegmentation = null;
		
		for (int threshold = startThreshold - 1; threshold >= 0; threshold-- ) {
			Corpus partialInputRest = new Corpus();
			partialInputRest.naiveLoad("output/" + corpus.getName()  + "-partial-" + (threshold+1) + ".txt", corpus.getType());
			
			forwardKnowledgeCorpus = partialInputRest.getCleanChars();
			forwardKnowledgeTrie = new Trie();
			Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
			forwardKnowledgeTrie.generateStatistics();
			
			partialSegmentation = votePartial(window, threshold, useLocalMax, bidiBVE);
			CorpusWriter.writeCorpus(corpus.getName()  + "-partial-" + threshold + ".txt", corpus, partialSegmentation);

//			System.out.println("THRESHOLD: " + threshold + "\tDL: " + partialSegmentation.descriptionLength);
			if (partialSegmentation.descriptionLength < minDL) {
				minDL = partialSegmentation.descriptionLength;
				bestSegmentation = partialSegmentation;
			}
			
			if (DEBUG)
				evaluate();
		}
		
		//Trie.extractWords(forwardKnowledgeTrie);
		
		partialSegmentation = bestSegmentation;
		
		return bestSegmentation;
	}
	
	public Segmentation voteTransfer(Corpus newCorpus, int window, int threshold, boolean useLocalMax) {
		setCorpus(newCorpus);
		return votePartial(window, threshold, useLocalMax, false);
	}
	
	public Segmentation voteForward(int windowSize, int threshold, boolean useLocalMax) {
		VotingExperts ve = VotingExperts.makeForwardVE(corpus, forwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);
	    
	    Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(ve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    forwardSegmentation = s;
	    
	    return s;
	}
	
	public Segmentation voteBackward(int windowSize, int threshold, boolean useLocalMax) {
	    VotingExperts ve = VotingExperts.makeBackwardVE(corpus, forwardTrie, backwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);
	    
	    Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(ve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    backwardSegmentation = s;
	    
	    System.out.println(ve.getVoteString(100));
	    System.out.println(ve.getSegmentedString(100, threshold));
	    
	    return s;
	}
	
	public List<Segmentation> bidiVoteAllThresholds(int windowSize, int minThreshold, int maxThreshold) {
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		VotingExperts ve = VotingExperts.makeBidiVE(corpus, forwardTrie, backwardTrie, windowSize, minThreshold);
		for (int t = minThreshold; t < maxThreshold; t++) {
			ve.setThreshold(t);
			
			ve.runAlgorithm(false);
			
			Segmentation maxOff = new Segmentation();
			maxOff.windowSize = windowSize;
			maxOff.threshold = t;
			maxOff.localMax = false;
			maxOff.cutPoints = Utils.makeArray(ve.getCutPoints());
			maxOff.descriptionLength = ve.computeDescriptionLength(forwardTrie);
			segmentations.add(maxOff);
			
			ve.makeCutPoints(ve.getCutPoints().size(), true);
			
			Segmentation maxOn = new Segmentation();
			maxOn.windowSize = windowSize;
			maxOn.threshold = t;
			maxOn.localMax = true;
			maxOn.cutPoints = Utils.makeArray(ve.getCutPoints());
			maxOn.descriptionLength = ve.computeDescriptionLength(forwardTrie);
			
			segmentations.add(maxOn);
		}
		return segmentations;
	}
	
	public List<Segmentation> voteAllThresholds(int windowSize, int minThreshold, int maxThreshold) {
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		VotingExperts ve = VotingExperts.makeForwardVE(corpus, forwardTrie, windowSize, minThreshold); 
		for (int t = minThreshold; t < maxThreshold; t++) {

			ve.setThreshold(t);
			
			// Local Max OFF
			ve.runAlgorithm(false);
			
			Segmentation maxOff = new Segmentation();
			maxOff.windowSize = windowSize;
			maxOff.threshold = t;
			maxOff.localMax = false;
			maxOff.cutPoints = Utils.makeArray(ve.getCutPoints());
			maxOff.descriptionLength = ve.computeDescriptionLength(forwardTrie);
			segmentations.add(maxOff);
			
			// Local Max ON
			ve.makeCutPoints(ve.getCutPoints().size(), true);
			
			Segmentation maxOn = new Segmentation();
			maxOn.windowSize = windowSize;
			maxOn.threshold = t;
			maxOn.localMax = true;
			maxOn.cutPoints = Utils.makeArray(ve.getCutPoints());
			maxOn.descriptionLength = ve.computeDescriptionLength(forwardTrie);
			
			segmentations.add(maxOn);
		}
		return segmentations;
	}
	
	public Segmentation votePartial(int windowSize, int threshold, boolean useLocalMax, boolean bidi) {
	    VotingExperts pve;
	    if (bidi) {
	    	pve = VotingExperts.makeBidiBVE(corpus, forwardTrie, backwardTrie, forwardKnowledgeTrie, windowSize, threshold);
	    } else {
	    	pve = VotingExperts.makeBVE(corpus, forwardTrie, forwardKnowledgeTrie, windowSize, threshold);
	    }
	    pve.runAlgorithm(useLocalMax);
	    
	    Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(pve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = pve.computeDescriptionLength(forwardTrie);
	    
	    partialSegmentation = s;
	    
	    if (DEBUG) {
		    System.out.println(pve.getVoteString(100));
		    System.out.println(pve.getSegmentedString(100, threshold));
	    }
	    
	    return s;
	}
	
	public Segmentation voteKnowledgeTransfer(int windowSize, int threshold, boolean useLocalMax) {
		pve.setCorpus(corpus.getCleanChars());
		pve.runAlgorithm(useLocalMax);
	    
		Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(pve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = pve.computeDescriptionLength(forwardTrie);
	    
	    return s;
	}	    
	
	public Segmentation voteBidi(int windowSize, int threshold, boolean useLocalMax) {
	    VotingExperts ve = VotingExperts.makeBidiVE(corpus, forwardTrie, backwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);

	    System.out.println(ve.getVoteString(100));
	    System.out.println(ve.getSegmentedString(100, threshold));
	    
	    bidiSegmentation = new Segmentation(windowSize, threshold);
	    bidiSegmentation.direction = Direction.BiDirectional;
	    bidiSegmentation.cutPoints = Utils.makeArray(ve.getCutPoints());
	    bidiSegmentation.localMax = useLocalMax;
	    bidiSegmentation.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    return bidiSegmentation;
	}
	
	public Segmentation voteMorpheme(int windowSize, int threshold, boolean useLocalMax) {
	    VotingExperts ve = VotingExperts.makeMorphemeVE(corpus, forwardTrie, backwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);

//	    System.out.println(ve.getVoteString(100));
	    
	    bidiSegmentation = new Segmentation(windowSize, threshold);
	    bidiSegmentation.direction = Direction.BiDirectional;
	    bidiSegmentation.cutPoints = Utils.makeArray(ve.getCutPoints());
	    bidiSegmentation.localMax = useLocalMax;
	    bidiSegmentation.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    return bidiSegmentation;
	}
	
	public void evaluate() {
		if (!EVALUATE) {
			return;
		}
		
		if (EVAL_FORWARD) {
		    System.out.println("FORWARD:");
		    EvaluationResults forwardResults = Evaluator.evaluate(forwardSegmentation.cutPoints, corpus.getCutPoints());
		    forwardResults.printResults();
//		    System.out.println("DL: " + forwardSegmentation.descriptionLength);
//		    System.out.println();
		}
	    
		if (EVAL_BACKWARD) {
		    System.out.println("BACKWARD:");
		    Evaluator.evaluate(backwardSegmentation.cutPoints, corpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + backwardSegmentation.descriptionLength);
//		    System.out.println();
		}
	    
	    if (bidiSegmentation != null) {
	    	System.out.println("BIDI:");
	    	Evaluator.evaluate(bidiSegmentation.cutPoints, corpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + bidiSegmentation.descriptionLength);
//		    System.out.println();
	    }
	    
	    if (partialSegmentation != null) {
	    	System.out.println("BOOTSTRAP (" + partialSegmentation.windowSize + "," + partialSegmentation.threshold + "):");
	    	Evaluator.evaluate(partialSegmentation.cutPoints, corpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + partialSegmentation.descriptionLength);
//		    System.out.println();
	    }
	}
	
	// Example of how to use the Engine class
	public static void main(String[] args) {	
		Engine.EVALUATE = true;
		
		Corpus corpus = Corpus.autoLoad("zarathustra", "downcase");
		Engine e = new Engine(corpus, 8);
		e.voteBackward(7, 3, false);
		e.evaluate();
		
//		Engine.bidiBootstrap(corpus, 7, 14, 3, false);
	}
}
