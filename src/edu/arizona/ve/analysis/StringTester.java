package edu.arizona.ve.analysis;

import java.util.List;

import edu.arizona.ve.api.AutoEngine;
import edu.arizona.ve.api.Segmentation;
import edu.arizona.ve.corpus.Corpus;
import edu.arizona.ve.corpus.CorpusWriter;
import edu.arizona.ve.util.Utils;

public class StringTester {

	public static void printVE(String input) {
		List<String> characters = Utils.toCharList(input);
		Corpus corpus = Corpus.fromList(characters);
		Segmentation segmentation = AutoEngine.autoVE(corpus);
		System.out.println(CorpusWriter.corpusToString(corpus.getCleanChars(), segmentation.cutPoints));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		printVE("aaabababbbabcccbbbbccccababababab");
		printVE("ababccccabcabababab");
		printVE("bcddddbddddbddddaabababddddcdddd");
	}

}
