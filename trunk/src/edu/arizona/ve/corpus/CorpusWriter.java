package edu.arizona.ve.corpus;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import edu.arizona.ve.api.Segmentation;


/**
*
* @author  Daniel Hewlett
*/
public class CorpusWriter {
	
	public static void writeCorpus(Corpus corpus, Segmentation s) {
		writeCorpus(corpus.getName() + ".txt", corpus, s);
	}
	
	public static void writeCorpus(String fileName, Corpus corpus, Segmentation s) {
		writeCorpus(fileName, corpus, s.cutPoints);
	}
	
	// THIS VERSION IS MODIFIED FOR LATIN TESTING DO NOT USE!
	public static void writeCorpus(String fileName, Corpus corpus, boolean[] cuts, boolean spaces) {
		if (cuts.length != corpus.getCleanChars().size() - 1) {
			System.out.println("ERROR: Cut point length mismatch in writer");
		}
		
		try {
			PrintStream out = new PrintStream("input/" + fileName);
			
			int numLetters = corpus.getCleanChars().size();
			int numCutPoints = corpus.getCutPoints().length;
			
			for (int i = 0; i < numLetters; i++) {
				out.print(corpus.getCleanChars().get(i));

				if (i < numCutPoints) {
					if (cuts[i]) {
						if (spaces) {
							out.print(" * ");
						} else {
							out.print("*");
						}
					} 
				}
			
//				if (spaces) out.print(" ");
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeCorpus(String fileName, Corpus corpus, boolean[] cuts) {
		if (cuts.length != corpus.getCleanChars().size() - 1) {
			System.out.println("ERROR: Cut point length mismatch in writer");
		}
		
		boolean spaces = (corpus.type.equals(Corpus.CorpusType.WORD));
		
		try {
			PrintStream out = new PrintStream("output/" + fileName);
			
			int numLetters = corpus.getCleanChars().size();
			int numCutPoints = corpus.getCutPoints().length;
			
			for (int i = 0; i < numLetters; i++) {
				out.print(corpus.getCleanChars().get(i));

				if (i < numCutPoints) {
					if (cuts[i]) {
						if (spaces) out.print(" ");
						out.print("*");
					} 
				}
			
				if (spaces) out.print(" ");
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeCorpus(Corpus corpus, boolean[] cuts) {
		writeCorpus(corpus.getName() + ".txt", corpus, cuts);
	}
	
	public static String corpusToString(List<String> corpus, boolean[] cutPoints) {
		StringBuffer out = new StringBuffer();
		out.append(corpus.get(0));
		for (int i = 0; i < corpus.size()-1; i++) {
			String s = corpus.get(i+1);
			boolean cut = cutPoints[i];
			if (cut) out.append("*");
			out.append(s);
		}
			
		return out.toString();
	}
	
	public static void main(String[] args) {
		Corpus c = new Corpus();
		c.loadArray(new String[] {"t", "h", "e", "c", "a", "t"});
		boolean[] cuts = new boolean[] {true, false, false, false, true};
		System.out.println(CorpusWriter.corpusToString(c.getCleanChars(), cuts));
	}
}
