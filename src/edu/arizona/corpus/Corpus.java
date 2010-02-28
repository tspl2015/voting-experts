package edu.arizona.corpus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.arizona.util.Utils;

/**
*
* @author  Daniel Hewlett
*/
public class Corpus {

	public enum CorpusType { Letter, Word, Syllable };
	
	private List<String> cleanChars = new ArrayList<String>();
	private List<String> segChars = new ArrayList<String>();
	
	private List<List<String>> segments; // TODO: this should probably go away
	
	private boolean[] cutPoints;
	private String name = "CORPUS";
	
	CorpusType type = CorpusType.Letter;
	boolean casePreserved = false; // only meaningful for letter type

	public static final String BOUNDARY = "*";
	
	public static boolean isBoundary(String s) {
		return s.startsWith(BOUNDARY);
	}
	
	public static boolean isBoundary(Character c) {
		return c.equals(BOUNDARY.charAt(0));
	}
	
	// input is of the form "br87", "case"
	public static Corpus autoLoad(String file, String type) {
		Corpus cl = new Corpus();

        String wd = System.getProperty("user.dir");
        if (wd.equals("/")) {
            wd = System.getProperty("user.home") + "/Projects/clojure-segmentation";
        }

		String path = wd + "/" + "input/" + type + "/" + file + ".txt";

		cl.name = file;
		
		if (type.equals("case")) {
			cl.load(path, false);
			cl.type = CorpusType.Letter;
			cl.casePreserved = true;
		} else if (type.equals("nocase")) {
			cl.load(path, true);
			cl.type = CorpusType.Letter;
			cl.casePreserved = false;
		} else if (type.equals("syllable")) {
			cl.loadSyllables(path);
			cl.type = CorpusType.Syllable;
		} else if (type.equals("word")) {
			cl.loadWords(path);
			cl.type = CorpusType.Word;
		} else if (type.equals("sent")) {
			System.out.println("AUTOLOAD ERROR: sent TYPE NOT YET SUPPORTED");
		} else {
			System.out.println("AUTOLOAD ERROR: INVALID FILE");
			return null;
		}

		return cl;
	}

	public static Corpus fromList(List<String> list) {
		Corpus c = new Corpus();
		c.loadList(list);
		return c;
	}
	
	// Only removes whitespace and *
	protected String wsClean(String file) {
		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while (in.ready()) { 
				char c = (char) in.read();
				if (!Character.isWhitespace(c) && c != '*') {
					buf.append(c);
				} else {
					buf.append(' ');
				}
			}
			in.close();
		} catch (Exception e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}

		return buf.toString().trim();
	}

	private String clean(String file) {
		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while (in.ready()) { 
				char c = (char) in.read();
				if (Character.isLetter(c)) {
					buf.append(c);
				} else {
					buf.append(' ');
				}
			}
			in.close();
		} catch (Exception e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}

		String result = buf.toString().replaceAll("\\s+", " ");
		result = result.trim();
		return result;
	}

	// the*cat*was*in.dig.nant -> [the, *, cat, *, was, *, in, dig, nant]
	// TODO: Probably should remove this as this is basically the same as the word case.
	public void loadSyllables(String file) {
		cleanChars = new ArrayList<String>();
		segChars = new ArrayList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String corpus = in.readLine();

			for (String word : corpus.split("[*]")) {

				if (word.contains(".")) {
					for (String syllable : word.split("[.]")) {
						cleanChars.add(syllable);
						segChars.add(syllable);
					}
					segChars.add(BOUNDARY);
				} else {
					cleanChars.add(word);
					segChars.add(word);
					segChars.add(BOUNDARY);
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void loadVerbatim(String file) {
		cleanChars = new ArrayList<String>();
		segChars = new ArrayList<String>();
		ArrayList<Boolean> tempCutPoints = new ArrayList<Boolean>();

		String cleaned = wsClean(file);
		// For checking
		//	   System.out.println(cleaned.substring(0, 100));

		for (int i = 0; i < cleaned.length(); ++i) {
			Character c = cleaned.charAt(i);

			if (!Character.isWhitespace(c)) {
				cleanChars.add(c.toString());
				segChars.add(c.toString());
				tempCutPoints.add(false);
			} else if (!segChars.isEmpty()) { 
				if (!isBoundary(segChars.get(segChars.size()-1))) {
					segChars.add("*");
					tempCutPoints.set(tempCutPoints.size()-1,true);
				}
			}	
		}  

		tempCutPoints.remove(tempCutPoints.size() - 1);
		
		cutPoints = Utils.makeArray(tempCutPoints);
	}

	// NB: this will strip off all numbers as well
	public void load(String file, boolean lowerCase) {
		cleanChars = new ArrayList<String>();
		segChars = new ArrayList<String>();
		ArrayList<Boolean> tempCutPoints = new ArrayList<Boolean>();

		String cleaned;
		if (lowerCase)
			cleaned = clean(file);
		else 
			cleaned = wsClean(file);
		// For checking
//		System.out.println(cleaned.substring(0, 100));

		for (int i = 0; i < cleaned.length(); ++i) {
			Character c = cleaned.charAt(i);

			if (Character.isLetter(c) || (!lowerCase && !(Character.isWhitespace(c) || isBoundary(c)))) {
				if (lowerCase) {
					cleanChars.add(Character.toLowerCase(c) + "");
					segChars.add(Character.toLowerCase(c) + "");
				} else {
					cleanChars.add(c.toString());
					segChars.add(c.toString());
				}

				tempCutPoints.add(false);
			} else if (c.equals(' ') && !segChars.isEmpty()) { 
				if (!isBoundary(segChars.get(segChars.size()-1))) {
					segChars.add(BOUNDARY);
					tempCutPoints.set(tempCutPoints.size()-1,true);
				}
			}	
		}        

		tempCutPoints.remove(tempCutPoints.size() - 1);

		if (tempCutPoints.size() != (cleanChars.size() - 1)) {
			System.out.println("ERROR: Got " + tempCutPoints.size() + " cut points for " + cleanChars.size() + " letters!");
		}
		
		cutPoints = Utils.makeArray(tempCutPoints);
	}

	// Preserves boundaries as tokens, you can use this for the sentence boundary information
	// cleanChars = segChars = whatever characters were in the corpus file
	public void naiveLoad(String file, CorpusType ctype) {
		
			try {	
				BufferedReader in = new BufferedReader(new FileReader(file));
				String corpus = in.readLine().trim();
				in.close();
	
				if (ctype.equals(CorpusType.Letter)) {
					cleanChars = new ArrayList<String>();
					segChars = new ArrayList<String>();
		
					for (int i = 0; i < corpus.length(); ++i) {
						Character c = corpus.charAt(i);
						cleanChars.add(Character.toString(c));
						segChars.add(Character.toString(c));
					}    
					
				} else if (ctype.equals(CorpusType.Word)) {
					cleanChars = Arrays.asList(corpus.trim().split("\\s+"));
					segChars = cleanChars; // is this OK?
				}
				
				cutPoints = new boolean[cleanChars.size()-1];
				
//				System.out.println(cleanChars.subList(0, 100));
			} catch (Exception e) {
				System.out.println("ERROR - " + e.getMessage());
				e.printStackTrace();
			}
		
	}

	public void loadWords(String file) {
		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while (in.ready()) { 
				char c = (char) in.read();
				buf.append(c);
			}
			in.close();
		} catch (Exception e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}

		String tempString = buf.toString();
		String[] words = tempString.split("\\s+");

		loadArray(words);
	}

	public void loadArray(String[] input) {
		ArrayList<Boolean> tempCutPoints = new ArrayList<Boolean>();
		
		for (String s : input) {
			if (isBoundary(s)) {
				if (!segChars.get(segChars.size()-1).equals(BOUNDARY)) {
					segChars.add(BOUNDARY);
					tempCutPoints.set(tempCutPoints.size()-1,true);	
				}
			} else {
				cleanChars.add(s);
				segChars.add(s);
				tempCutPoints.add(false);
			}
		}
		
		tempCutPoints.remove(tempCutPoints.size() - 1);
		
		cutPoints = Utils.makeArray(tempCutPoints);
	}

	public void loadList(List<String> input) {
		ArrayList<Boolean> tempCutPoints = new ArrayList<Boolean>();
		
		for (String s : input) {
			if (isBoundary(s)) {
				if (!segChars.get(segChars.size()-1).equals(BOUNDARY)) {
					segChars.add(BOUNDARY);
					tempCutPoints.set(tempCutPoints.size()-1,true);	
				}
			} else {
				cleanChars.add(s);
				segChars.add(s);
				tempCutPoints.add(false);
			}
		}
		
		tempCutPoints.remove(tempCutPoints.size() - 1);
		
		cutPoints = Utils.makeArray(tempCutPoints);
	}
	
	// for testing convenience
	public void loadString(String corpus) {
		cleanChars = new ArrayList<String>();
		segChars = new ArrayList<String>();
		ArrayList<Boolean> tempCutPoints = new ArrayList<Boolean>();

		for (int i = 0; i < corpus.length(); ++i) {
			Character c = corpus.charAt(i);

			if ((isBoundary(c) || c.equals(' ')) && !segChars.isEmpty()) { 
				if (!segChars.get(segChars.size()-1).equals(BOUNDARY)) {
					segChars.add(BOUNDARY);
					tempCutPoints.set(tempCutPoints.size()-1,true);	
				}
			} else {
				cleanChars.add(c + "");
				segChars.add(c + "");
				tempCutPoints.add(false);
			}
		}

		tempCutPoints.remove(tempCutPoints.size() - 1);
		
		cutPoints = Utils.makeArray(tempCutPoints);
	}

	public boolean[] getCutPoints() {
		return cutPoints;
	}
	
	public List<String> getCleanChars() {
		return cleanChars;
	}

	public String getName() {
		return name;
	}
	
	public CorpusType getType() {
		return type;
	}
	
	public void setType(CorpusType t) {
		type = t;
	}
	
	public List<String> getReversed() {
		ArrayList<String> backwardCorpus = new ArrayList<String>(cleanChars);
		Collections.reverse(backwardCorpus);
		return backwardCorpus;
	}

	public List<String> getSegmentedChars() {
		return segChars;
	}

	public List<String> getSegments() {
		ArrayList<String> segs = new ArrayList<String>();
		String segment = new String();
		for (int i = 0; i < cutPoints.length; i++) {
			boolean cut = cutPoints[i];
			if (cut) {
				if (i != 0) { 
					segs.add(segment); 
				}
				segment = new String(cleanChars.get(i+1));
			} else {
				segment = segment.concat(cleanChars.get(i+1));
			}
		}

		segs.add(segment);

		return segs;
	}

	public Corpus getReverseCorpus() {
		Corpus rev = new Corpus();
		
		rev.type = type; 
		
		ArrayList<String> backwardCorpus = new ArrayList<String>(getCleanChars());
		Collections.reverse(backwardCorpus);
		rev.cleanChars = backwardCorpus;
		
		ArrayList<String> backwardSegCorpus = new ArrayList<String>(getSegmentedChars());
		Collections.reverse(backwardSegCorpus);
		rev.segChars = backwardSegCorpus;
		
		rev.cutPoints = new boolean[cutPoints.length];
		for (int i = 0; i < cutPoints.length; i++) {
			rev.cutPoints[i] = cutPoints[cutPoints.length-i-1];
		}
		
		return rev;
	}
	
	public Corpus getSubCorpus(int size) {
		List<String> subList = new ArrayList<String>(getSegmentedChars().subList(0, size));
		Corpus subCorpus = Corpus.fromList(subList);
		subCorpus.setType(this.getType());
		return subCorpus;
	}
	
	// The behavior of this function is odd and wrong
	public void loadSegments(String file) {
		try {	
			BufferedReader in = new BufferedReader(new FileReader(file));
			String corpus = in.readLine().trim();
			in.close();

			String[] segStrings = corpus.split("[*]");

			segments = new ArrayList<List<String>>();
			for (String s : segStrings) {
				cleanChars.add(s);
				segChars.add(s);
				ArrayList<String> segment = new ArrayList<String>();
				for (Character c : s.toCharArray()) { segment.add(c.toString()); }
				//			   segment.add("*"); // every segment ends with *
				segments.add(segment);
			}        
			
			cutPoints = new boolean[cleanChars.size()-1];

		} catch (Exception e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}
	}
}
