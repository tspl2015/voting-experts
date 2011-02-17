package edu.arizona.ve.corpus;

import edu.arizona.ve.corpus.Corpus.CorpusType;

public class HVECorpusMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Corpus c = Corpus.autoLoad("orwell-short", CorpusType.LETTER, false);
		
		CorpusWriter.writeForHVE("orwell-short-hve", c);
	}

}
