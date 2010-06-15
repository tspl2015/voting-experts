package edu.arizona.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import edu.arizona.algorithm.PhonemeToMorpheme;
import edu.arizona.algorithm.VotingExperts;
import edu.arizona.corpus.Corpus;
import edu.arizona.corpus.CorpusWriter;
import edu.arizona.evaluation.EvaluationResults;
import edu.arizona.evaluation.Evaluator;
import edu.arizona.trie.Trie;
import edu.arizona.util.NF;
import edu.arizona.util.Utils;

/**
*
* @author  Daniel Hewlett
*/
public class Engine {

	public enum Direction { Forward, Backward, BiDirectional };
	
	public class Segmentation implements Comparable<Segmentation> {
		public int windowSize;
		public int threshold;
		public boolean localMax = true;
		public boolean[] cutPoints;
		public double descriptionLength;
		public Direction direction = Direction.Forward;
		
		public Segmentation() {}
		
		public Segmentation(int window, int threshold) {
			this.windowSize = window;
			this.threshold = threshold;
		}
		
		public int compareTo(Segmentation other) {
			if (descriptionLength == other.descriptionLength) {
				return 0;
			} else if (descriptionLength < other.descriptionLength) {
				return -1;
			} else {
				return 1;
			}
		}
		
		public String makeString(Evaluator e) {
			 return (windowSize + "\t" + threshold + "\t" + (localMax ? 1 : 0) + "\t" + NF.format(descriptionLength) + "\t" + NF.format(e.precision) + "\t" + NF.format(e.recall) + "\t" + NF.format(e.fMeasure));
		}
	}
	
	public static boolean EVAL_FORWARD = false;
	public static boolean EVAL_BACKWARD = false;
	public static boolean EVALUATE = false;
	
	public static boolean DEBUG = false;
	
	int trieDepth;

	// The corpus and the reversed corpus
	Corpus _forwardCorpus;
	Corpus _backwardCorpus;
	
	// The tries
	Trie forwardTrie;
	Trie backwardTrie;

	// The knowledge tries
	Trie forwardKnowledgeTrie;
	Trie backwardKnowledgeTrie;

	// The knowledge corpora - why are these here again?
	List<String> forwardKnowledgeCorpus;
	List<String> backwardSentenceCorpus;
	
	// Segmentations
	Segmentation forwardSegmentation = null;
	Segmentation backwardSegmentation = null;
	Segmentation partialSegmentation = null;
	Segmentation bidiSegmentation = null;
	
	// TODO: Find a more elegant way to test the transfer condition
	VotingExperts pve;

//	String childName;
//	String file;
	
	public Engine(Corpus corpus, int trieDepth) {
		this.trieDepth = trieDepth;
		_forwardCorpus = corpus;
		_backwardCorpus = _forwardCorpus.getReverseCorpus();
		initTries();
	}

	public void setCorpus(Corpus c) {
		_forwardCorpus = c;
		_backwardCorpus = _forwardCorpus.getReverseCorpus();
	}
	
	public void initTries() {
		forwardTrie = new Trie();
		Trie.addAll(forwardTrie, _forwardCorpus.getCleanChars(), trieDepth);
		forwardTrie.generateStatistics();
		
		backwardTrie = new Trie();
		Trie.addAll(backwardTrie, _backwardCorpus.getCleanChars(), trieDepth);
		backwardTrie.generateStatistics();
	}
		
	public List<String> getForwardCorpus() {
		return _forwardCorpus.getCleanChars();
	}
	
	public boolean[] getForwardCutArray() {
		return forwardSegmentation.cutPoints;
	}
	
	public Corpus getCorpus() {
		return _forwardCorpus;
	}
	
	public Segmentation voteBVE(int window, int thresholdOffset, int minThreshold, boolean useLocalMax, boolean bidiBVE) {
		// Try to GC the backward trie, now that we don't need it for voting
		backwardKnowledgeTrie = null;
		
		int startThreshold = window * (bidiBVE ? 3 : 2); // window + thresholdOffset 
		
		// Experimenting with dummy corpus 0 this just means the first time the knowledge expert won't do anything
		forwardKnowledgeCorpus = _forwardCorpus.getCleanChars(); //new ArrayList<String>(bidiInput.getCleanChars());
		forwardKnowledgeTrie = new Trie();
		Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
		forwardKnowledgeTrie.generateStatistics();

		partialSegmentation = votePartial(window, startThreshold, useLocalMax, bidiBVE);
		CorpusWriter.writeCorpus(_forwardCorpus.getName()  + "-partial-1.txt", _forwardCorpus, partialSegmentation);
		
		if (DEBUG)
			evaluate();
		
		bidiSegmentation = null; // stop the printing of the bidi array, we're done with it
		
		int i = 1;
		for (int threshold = startThreshold - 1; threshold >= minThreshold; threshold-- ) {
			Corpus partialInputRest = new Corpus();
			partialInputRest.naiveLoad("output/" + _forwardCorpus.getName()  + "-partial-" + i + ".txt", _forwardCorpus.getType());
			
			forwardKnowledgeCorpus = partialInputRest.getCleanChars();
			forwardKnowledgeTrie = new Trie();
			Trie.addAll(forwardKnowledgeTrie, forwardKnowledgeCorpus, trieDepth+1);
			forwardKnowledgeTrie.generateStatistics();
			
			partialSegmentation = votePartial(window, threshold, useLocalMax, bidiBVE);
			CorpusWriter.writeCorpus(_forwardCorpus.getName()  + "-partial-" + (i+1) + ".txt", _forwardCorpus, partialSegmentation);
			
			if (DEBUG)
				evaluate();
			
			i++;
		}
		
		//Trie.extractWords(forwardKnowledgeTrie);
		
		return partialSegmentation;
	}
	
	public Segmentation voteTransfer(Corpus newCorpus, int window, int threshold, boolean useLocalMax) {
		setCorpus(newCorpus);
		return votePartial(window, threshold, useLocalMax, false);
	}
	
	public Segmentation voteForward(int windowSize, int threshold, boolean useLocalMax) {
		VotingExperts ve = VotingExperts.makeForwardVE(_forwardCorpus, forwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);
	    
	    Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(ve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    forwardSegmentation = s;
	    
	    return s;
	}
	
	public Segmentation voteBackward(int windowSize, int threshold, boolean useLocalMax) {
	    VotingExperts ve = VotingExperts.makeBackwardVE(_forwardCorpus, forwardTrie, backwardTrie, windowSize, threshold);
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
		VotingExperts ve = VotingExperts.makeBidiVE(_forwardCorpus, forwardTrie, backwardTrie, windowSize, minThreshold);
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
		VotingExperts ve = VotingExperts.makeForwardVE(_forwardCorpus, forwardTrie, windowSize, minThreshold); 
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
	    	pve = VotingExperts.makeBidiBVE(_forwardCorpus, forwardTrie, backwardTrie, forwardKnowledgeTrie, windowSize, threshold);
	    } else {
	    	pve = VotingExperts.makeBVE(_forwardCorpus, forwardTrie, forwardKnowledgeTrie, windowSize, threshold);
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
		pve.setCorpus(_forwardCorpus.getCleanChars());
		pve.runAlgorithm(useLocalMax);
	    
		Segmentation s = new Segmentation(windowSize, threshold);
	    s.cutPoints = Utils.makeArray(pve.getCutPoints());
	    s.localMax = useLocalMax;
	    s.descriptionLength = pve.computeDescriptionLength(forwardTrie);
	    
	    return s;
	}	    
	
	public Segmentation voteBidi(int windowSize, int threshold, boolean useLocalMax) {
	    VotingExperts ve = VotingExperts.makeBidiVE(_forwardCorpus, forwardTrie, backwardTrie, windowSize, threshold);
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
	    VotingExperts ve = VotingExperts.makeMorphemeVE(_forwardCorpus, forwardTrie, backwardTrie, windowSize, threshold);
	    ve.runAlgorithm(useLocalMax);

	    System.out.println(ve.getVoteString(100));
	    
	    bidiSegmentation = new Segmentation(windowSize, threshold);
	    bidiSegmentation.direction = Direction.BiDirectional;
	    bidiSegmentation.cutPoints = Utils.makeArray(ve.getCutPoints());
	    bidiSegmentation.localMax = useLocalMax;
	    bidiSegmentation.descriptionLength = ve.computeDescriptionLength(forwardTrie);

	    return bidiSegmentation;
	}
	
	public boolean[] combine(boolean[] forward, boolean[] backward) {
		if (forward.length != backward.length)
			System.out.println("ERROR in COMBINE!");
		
	    boolean[] reversed = new boolean[backward.length];
	    for (int i = 0; i < backward.length; i++) {
	    	reversed[backward.length - i - 1] = backward[i];
	    }	
	    
	    boolean[] bidiCutArray = new boolean[forward.length];
	    for (int i = 0; i < forward.length; i++) 
	    	bidiCutArray[i] = forward[i] && reversed[i];
	    
	    return bidiCutArray;
	} 
	
	public void evaluateAllLocations() {
		Evaluator e = new Evaluator("*");
	      
	    boolean[] all = new boolean[_forwardCorpus.getCutPoints().length];
	    Arrays.fill(all, true);
	    
	    System.out.println("ALL-LOCATIONS:");
	    EvaluationResults results = e.evaluate(all, _forwardCorpus.getCutPoints());
	    results.printResults();
	    System.out.println();
	}
	
	public void evaluate() {
		if (!EVALUATE) {
			return;
		}
		
		if (EVAL_FORWARD) {
			Evaluator e = new Evaluator("*");
			
		    System.out.println("FORWARD:");
		    EvaluationResults forwardResults = e.evaluate(forwardSegmentation.cutPoints, _forwardCorpus.getCutPoints());
		    forwardResults.printResults();
//		    System.out.println("DL: " + forwardSegmentation.descriptionLength);
//		    System.out.println();
		}
	    
		if (EVAL_BACKWARD) {
		    Evaluator backwardEval = new Evaluator("*");
		    
		    System.out.println("BACKWARD:");
		    backwardEval.evaluate(backwardSegmentation.cutPoints, _forwardCorpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + backwardSegmentation.descriptionLength);
//		    System.out.println();
		}
	    
	    if (bidiSegmentation != null) {
	    	System.out.println("BIDI:");
		    Evaluator eb = new Evaluator("*");
		    
		    eb.evaluate(bidiSegmentation.cutPoints, _forwardCorpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + bidiSegmentation.descriptionLength);
//		    System.out.println();
	    }
	    
	    if (partialSegmentation != null) {
	    	System.out.println("BOOTSTRAP (" + partialSegmentation.windowSize + "," + partialSegmentation.threshold + "):");
		    Evaluator p = new Evaluator("*");
		    p.evaluate(partialSegmentation.cutPoints, _forwardCorpus.getCutPoints()).printResults();
//		    System.out.println("DL: " + partialSegmentation.descriptionLength);
//		    System.out.println();
	    }
	}

	public static Segmentation bootstrap(Corpus c, int windowSize, int maxThreshold, int minThreshold, boolean useLocalMax) {
		Engine bidi = new Engine(c, windowSize+1);
		return bidi.voteBVE(windowSize, maxThreshold - windowSize, minThreshold, useLocalMax, false);   // 5,8,0 = 5,3,0 
	}

	public static Segmentation bidiBootstrap(Corpus c, int windowSize, int maxThreshold, int minThreshold, boolean useLocalMax) {
		Engine bidi = new Engine(c, windowSize+1);
		return bidi.voteBVE(windowSize, maxThreshold - windowSize, minThreshold, useLocalMax, true);   // 5,8,0 = 5,3,0 
	}
	
	// Now less wasteful!
	public static void mdl(Corpus corpus) {
		System.out.println("Window Size\tThreshold\tLocal Max?\tDescription Length\tPrecision\tRecall\tF-Measure");
		
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		for (int window = 3; window < 9; window++) {
			Engine bidi = new Engine(corpus, window+1);
			segmentations.addAll(bidi.voteAllThresholds(window, 0, window));
		}	
		
		Collections.sort(segmentations);
		
		for (Segmentation s : segmentations) {
			evalMDL(s, corpus.getCutPoints());
			CorpusWriter.writeCorpus(corpus, s);
		}
	}
	
	// Now less wasteful!
	public static void bidiMDL(Corpus corpus) {
		Vector<Segmentation> segmentations = new Vector<Segmentation>();
		for (int window = 4; window < 8; window++) {
			Engine engine = new Engine(corpus, window+1);
			segmentations.addAll(engine.bidiVoteAllThresholds(window, 0, window));
			System.out.println("Window Size " + window + " Complete.");
		}	

		System.out.println("Window Size\tThreshold\tLocal Max?\tDescription Length\tPrecision\tRecall\tF-Measure");
		
		Collections.sort(segmentations);
		
		for (Segmentation s : segmentations) {
			evalMDL(s, corpus.getCutPoints());
			CorpusWriter.writeCorpus(corpus, s);
			System.exit(0);
		}
	}
	
	public static boolean evalMDL(Segmentation s, boolean[] actual) {
		Evaluator e = new Evaluator("*");
		
	    e.evaluate(s.cutPoints, actual);
	    
	    // Don't bother printing if F-measure is NaN (means threshold was too high for any segmentation
	    if (!Double.isNaN(e.fMeasure)) {
	    	System.out.println(s.makeString(e));
	    	return true;
	    } else {
	    	System.out.println("WTF?");
	    	return false;
	    }
	}
	
	public static void gssMain() {
		Engine.EVALUATE = true;
		
//		Corpus morphs = Corpus.autoLoad("latin", "word");
		
		// Start HERE
		Corpus letters = Corpus.autoLoad("latin-morph", "preserve_case");

//		Corpus caesar = Corpus.autoLoad("caesar", "nocase");
//		int window = 8; 
//		Engine e = new Engine(caesar, window + 1);
//		Segmentation s = e.voteForward(window, 4, false); Engine.EVAL_FORWARD = true;
//		Segmentation s = e.voteBidi(window, 6, false); // 4 is the answer
//		Segmentation s = e.voteMorpheme(window, 7, false); // 4 is the answer
//		e.evaluate();
//		e.evaluateAllLocations();
		
		
		int window = 6;
		Engine e = new Engine(letters, window + 1);
//		Segmentation s = e.voteForward(window, 2, false); Engine.EVAL_FORWARD = true;
//		Segmentation s = e.voteBidi(window, 3, false); // 4 is the answer
		Segmentation s = e.voteMorpheme(window, 4, false); // 4 is the answer
//		PhonemeToMorpheme ptm = new PhonemeToMorpheme(e.forwardTrie, e.backwardTrie, letters, window);
//		ptm.runAlgorithm(0.8);
		
//		System.out.println("THRESHOLD: " + NF.format(threshold));
//		Evaluator eptm = new Evaluator();
//		eptm.evaluate(ptm.getCutPoints(), letters.getCutPoints());
//		eptm.printResults();
		e.evaluate();
//		
		CorpusWriter.writeCorpus("word/latin-morph-ve-output.txt", letters, s.cutPoints, true);
//		CorpusWriter.writeCorpus("word/latin-morph-ve-output.txt", letters, ptm.getCutPoints(), true);

//		// Load the VE-generated morphs and segment into words
		Corpus veMorphs = Corpus.autoLoad("latin-morph-ve-output", "word");
//		Corpus trueMorphs = Corpus.autoLoad("latin", "word");
//		
		int window2 = 6;
	
		Engine e2 = new Engine(veMorphs, window2 + 1);
		PhonemeToMorpheme ptm2 = new PhonemeToMorpheme(e2.forwardTrie, e2.backwardTrie, veMorphs, window);
		ptm2.runAlgorithm(0.0);
		
//		Segmentation s2 = e2.voteForward(window2, 1, false); // 4 is the answer
//		Segmentation s2 = e2.voteBidi(window2, 2, false); // 4 is the answer
//		Segmentation s2 = e2.voteMorpheme(window2, 2, false); // 4 is the answer
//
		CorpusWriter.writeCorpus("case/latin-morph-ve-final.txt", veMorphs, ptm2.getCutPoints(), false);
//		
//		// Load the VE-generated words and test against the actual (character) gold standard
		Corpus finalCorpus = Corpus.autoLoad("latin-morph-ve-final", "case");
		Corpus gold = Corpus.autoLoad("latin-test-good", "case");
		Evaluator fe = new Evaluator();
		
		System.out.println("FINAL: ");
		EvaluationResults results = fe.evaluate(finalCorpus.getCutPoints(), gold.getCutPoints());
		results.printResults();
		
			
//		int window2 = 4;
//		Corpus corpus = Corpus.autoLoad("latin-cluster", "case");
//		Corpus corpus = Corpus.autoLoad("latin-ve-cluster", "case");
//		Engine e2 = new Engine(corpus, window2 + 1);
//		e2.voteForward(window2, 1, false);
////		e.evaluate();
//		Segmentation segmentation = e2.forwardSegmentation;
//		CorpusWriter.writeCorpus("case/latin-test.txt", veMorphs, segmentation.cutPoints, false);
//		CorpusWriter.writeCorpus("case/latin-test.txt", trueMorphs, segmentation.cutPoints, false);
		
		
		// GSS EVALUATION
		
//		Corpus gold = Corpus.autoLoad("latin-test-good", "case");
//		Corpus ve = Corpus.autoLoad("latin-test", "case");
//		
//		Evaluator ev = new Evaluator();
//		ev.evaluate(ve.getCutPoints(), gold.getCutPoints());
//		ev.printResults();
		
		// Sanity check
	}
	
	public static void bveTests() {
//		Corpus corpus = Corpus.autoLoad("br87-cmu", "case");
//		Engine e = new Engine(corpus, 6);
//		e.voteBVE(5, 3, 0, true, true);
		
		Corpus corpus = Corpus.autoLoad("orwell-short", "nocase");
		Engine e = new Engine(corpus, 8);
		e.voteBVE(7, 3, 0, true, false);
		
//		Corpus corpus = Corpus.autoLoad("chinese-gw", "case");
//		Engine e = new Engine(corpus, 3);
//		e.voteBVE(2, 3, 0, false, false);
		
//		Corpus corpus = Corpus.autoLoad("latin-morph", "case");
//		Engine e = new Engine(corpus, 8);
//		e.voteBVE(7, 4, 0, true, true);
	}
	
	public static void main(String[] args) {	
		Engine.EVALUATE = true;
		
		Corpus corpus = Corpus.autoLoad("zarathustra", "downcase");
		Engine e = new Engine(corpus, 8);
		e.voteBackward(7, 3, false);
		e.evaluate();
		
//		Engine.bidiBootstrap(corpus, 7, 14, 3, false);
	}
}
