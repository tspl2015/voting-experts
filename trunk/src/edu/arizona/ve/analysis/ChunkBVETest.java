package edu.arizona.ve.analysis;

import edu.arizona.ve.api.Engine;
import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.Corpus.CorpusType;

public class ChunkBVETest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Corpus corpus = Corpus.autoLoad("orwell-short", CorpusType.LETTER, false);
		Corpus corpus = Corpus.autoLoad("br87", CorpusType.LETTER, true);
		
		int window = 5;
		
		Engine.EVALUATE = true;
		Engine.DEBUG = true;
		Engine e = new Engine(corpus, window);
		e.voteBVE(window, 0, false, true);
		e.evaluate();
	}

}
