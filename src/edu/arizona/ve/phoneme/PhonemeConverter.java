package edu.arizona.ve.phoneme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class PhonemeConverter {
	// Size of phoneme alphabet will be 62
	private static HashMap<String,String> phonemeMap = new HashMap<String,String>();
	private static HashSet<String> consonants = new HashSet<String>();
	private static HashSet<String> voiceless = new HashSet<String>();
	private static HashSet<String> sibilants = new HashSet<String>();
	private static HashSet<String> nuclei = new HashSet<String>();
	
	static {
		// Sibilants
		phonemeMap.put("S", "s");
		phonemeMap.put("Z", "z");
		phonemeMap.put("JH", "ʤ");
		phonemeMap.put("ZH", "ʒ");
		phonemeMap.put("SH", "ʃ");
		phonemeMap.put("CH", "ʧ");
				
		sibilants.addAll(phonemeMap.values());
		
		// Voiceless consonants
		phonemeMap.put("F", "f");
		phonemeMap.put("K", "k");
		phonemeMap.put("P", "p");
		phonemeMap.put("T", "t");
		phonemeMap.put("TH", "θ");
		phonemeMap.put("HH", "h"); // I guess?
		
		voiceless.addAll(phonemeMap.values());
		voiceless.removeAll(sibilants);
		
		phonemeMap.put("B", "b");
		phonemeMap.put("D", "d");
		phonemeMap.put("G", "g");
		phonemeMap.put("DH", "ð");
		phonemeMap.put("L", "l");
		phonemeMap.put("M", "m");
		phonemeMap.put("N", "n");
		phonemeMap.put("NG", "ŋ");
		phonemeMap.put("R", "r");
		phonemeMap.put("V", "v");
		phonemeMap.put("W", "w");
		phonemeMap.put("Y", "j");
	
		consonants.addAll(phonemeMap.values());
		
		// 24 consonant sounds
		// Now for the vowels, ugh.
		
		// but
		phonemeMap.put("AH0", "ə");
		phonemeMap.put("AH1", "ʌ");
		phonemeMap.put("AH2", "ӛ");
		
		// calm
		phonemeMap.put("AA0", "A");
		phonemeMap.put("AA1", "a");
		phonemeMap.put("AA2", "ȁ");
		
		// cat
		phonemeMap.put("AE0", "Æ");
		phonemeMap.put("AE1", "æ");
		phonemeMap.put("AE2", "ǽ");
		
		// paw
		phonemeMap.put("AO0", "Ɔ");
		phonemeMap.put("AO1", "ɔ");
		phonemeMap.put("AO2", "ĉ");
		
		// cow
		phonemeMap.put("AW0", "Ȣ");
		phonemeMap.put("AW1", "ȣ");
		phonemeMap.put("AW2", "ᴕ"); 
		
		// buy
		phonemeMap.put("AY0", "ᾈ");
		phonemeMap.put("AY1", "ᾼ"); 
		phonemeMap.put("AY2", "ᾌ"); 
		
		// bit
		phonemeMap.put("IH0", "ι");
		phonemeMap.put("IH1", "ɪ");
		phonemeMap.put("IH2", "ί");
		
		// red
		phonemeMap.put("EH0", "Ɛ");
		phonemeMap.put("EH1", "ɛ");
		phonemeMap.put("EH2", "έ");
		
		// came
		phonemeMap.put("EY0", "E");
		phonemeMap.put("EY1", "e");
		phonemeMap.put("EY2", "ȅ");
		
		// see
		phonemeMap.put("IY0", "İ");
		phonemeMap.put("IY1", "i");
		phonemeMap.put("IY2", "ï");
		
		// throw
		phonemeMap.put("OW0", "Ω"); 
		phonemeMap.put("OW1", "ω");
		phonemeMap.put("OW2", "ὤ"); 
		
		// boy
		phonemeMap.put("OY0", "Ƣ");
		phonemeMap.put("OY1", "ƣ");
		phonemeMap.put("OY2", "ѹ"); 
		
		// good
		phonemeMap.put("UH0", "Y");
		phonemeMap.put("UH1", "ʊ");
		phonemeMap.put("UH2", "ʋ");
		
		// blue
		phonemeMap.put("UW0", "U");
		phonemeMap.put("UW1", "u");
		phonemeMap.put("UW2", "ű");
		
		// Kerr -> syllabic r
		phonemeMap.put("ER0", "Ṛ");
		phonemeMap.put("ER1", "ṛ");
		phonemeMap.put("ER2", "ṝ");
		
		nuclei.addAll(phonemeMap.values());
		nuclei.removeAll(consonants);
		
		phonemeMap.put("*", "*");
	}
	
	// word input is phonemes!
	public static Vector<String> getSyllableNuclei(String word) {
		Vector<String> sylNuclei = new Vector<String>();
		for (Character c : word.toCharArray()) {
			if (nuclei.contains(c.toString())) {
				sylNuclei.add(c.toString());
			}
		}
		
		return sylNuclei;
	}
	
	// word input is phonemes!
	public static String syllabify(String word) {
		Vector<String> sylNuclei = getSyllableNuclei(word);
		Vector<String> syllables = new Vector<String>();
		
		if (sylNuclei.size() == 0 || sylNuclei.size() == 1) {
			return word;
		}
		
		for (int j = sylNuclei.size() - 1; j >= 0; j--) {
			String nucleus = sylNuclei.get(j);
			int index = word.lastIndexOf(nucleus);
			String syllable = word.substring(index);
			
			String lastOnset = new String();
			String onset = new String();
			for (int k = index - 1; k >= 0; k--) {
				onset = word.substring(k,index);
				if (!isValidOnset(onset)) {
					break;
				} else {
					lastOnset = onset;
				}
			}
			
			syllable = lastOnset + syllable;
			syllables.add(syllable);
			
			word = word.substring(0,word.length() - syllable.length());
//			System.out.println(j);
		}
		
		StringBuffer resultBuffer = new StringBuffer();
		
		Collections.reverse(syllables);
		
		for (String syl : syllables) {
			resultBuffer.append(syl + ".");
		}
		
		String result = resultBuffer.toString();
		result = result.substring(0, result.length()-1);
		
		return result;
	}
	
	public static boolean isValidOnset(String onset) {
		Vector<Character> invalids = new Vector<Character>(Arrays.asList(new Character[]{'ŋ'}));
		Vector<String> multiples = new Vector<String>(Arrays.asList(
				new String[] {	"pj", "pr", "pl", 
								"tw", "tr", 
								"kj", "kw", "kr", "kl",
								"bj", "br", "bl",
								"dw", "dr",
								"gj", "gw", "gr", "gl",
								"fj", "fr", "fl",
								"θw", "θr", 
								"ʃr", // "ʃl", // don't know about "Schlep"
								"sw", "sl", "sm", "sn", "sp", "st", "sk", 
								"str", "spr", "spl", "skr", "skw",
								"hj", "mj", "vj"}));
		
		for (String n : nuclei) {
			if (onset.contains(n)) {
				return false;
			}
		}
		
		// eliminate the eng - anything else
		for (Character c : onset.toCharArray()) {
			if (invalids.contains(c)) {	return false; }
		}
		
		// if multiple phonemes, use the rules above
		if (onset.length() > 1) {
			boolean found = false;
			if (multiples.contains(onset)) {
				found = true;
			}
			return found;
		}
		
		return true;
	}
	
	// Level 0 is no stress (all stressed)
	// Level 1 is stressed/unstressed (2nd stress becomes primary)
	// Anything else is standard 0,1,2 stress
	public static String convert(String phoneme, int stressLevel) {
		switch (stressLevel) {
		case 0: if (phoneme.length() == 3) {phoneme = phoneme.substring(0,2) + "1";} 
				break;
		case 1: if (phoneme.length() == 3 && phoneme.charAt(2) == '2') {phoneme = phoneme.substring(0,2) + "1";}
				break;
		default: break;
		}
		
		String result =  phonemeMap.get(phoneme);
		if (result == null) {
			System.out.println(phoneme + " KILLED ME!");
			System.exit(0);
		}
		return result;
	}
	
	@Deprecated
	public static String getPhonemeTable() {
		String result = new String();
		
		for (String phoneme : phonemeMap.values()) {
			result += phoneme + " ";
			if (consonants.contains(phoneme)) { 
				result += "C" + " ";
			} else {
				result += "V" + " ";
			}
		}
		
		return result.trim();	
	}
	
	public static String repair(String word, Dictionary dict) {
		if (word.contains("'") && word.length() > 1) {
			String mainWord = word.split("'")[0];
			String rest = new String();
			if (word.split("'").length > 1) {
				rest = word.split("'")[1];
			}
			
			String wordPhonemes = dict.getIPA(mainWord, 2);
			
			if (!wordPhonemes.isEmpty() && 
					(rest.equals("s") || rest.isEmpty())) {
//				System.out.println(wordPhonemes + " " + rest);
				String endPhoneme = Character.toString(wordPhonemes.charAt(wordPhonemes.length() - 1));
				
				if (sibilants.contains(endPhoneme)) {
					return wordPhonemes + "əz";
				} else if (voiceless.contains(endPhoneme)) {
					return wordPhonemes + "s";
				} else { 
					return wordPhonemes + "z";
				}
			}
		}
		
		return new String();
	}
	
	// Naive Syllabification
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("input/CHILDES/brown-brent.txt"));
		PrintStream out = new PrintStream("input/CHILDES/brown-naive-syllables.txt");
		
		StringBuffer oneBigWord = new StringBuffer();
		
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			oneBigWord.append(line.replaceAll("\\s", ""));
		}
		
		String oneBigSyllables = PhonemeConverter.syllabify(oneBigWord.toString());
		
		out.print(oneBigSyllables);
		out.close();
	}
	
	
	// Correct Syllabification
//	public static void main(String[] args) throws Exception {
//		BufferedReader br = new BufferedReader(new FileReader("input/CHILDES/brown-brent.txt"));
//		PrintStream out = new PrintStream("input/CHILDES/brown-syllables.txt");
//		
//		int[] counts = new int[6];
//		int total = 0;
//		for (String line = br.readLine(); line != null; line = br.readLine()) {
//			String syllabified = new String();
//		
//			for (String word : line.split(" ")) {
//				int numSyl = PhonemeConverter.getSyllableNuclei(word).size();
//				counts[numSyl]++;
//				total++;
//				
//				syllabified += PhonemeConverter.syllabify(word) + "*";
//			}
//			
//			if (!line.replaceAll("\\s", "").equals(syllabified.replaceAll("[*]|[.]", ""))) {
//				System.out.println(line);
//				System.out.println(syllabified);
//			}
//			
//			out.print(syllabified);
//		}
//		
//		for (int i = 0; i < counts.length; i++) {
//			System.out.println(i + "-SYLLABLE WORDS: " + counts[i]);
//		}
//		
//		System.out.println("TOTAL WORDS: " + total);
//	}
		
	// END SYLLABLE STUFF
	
//		System.out.println(PhonemeConverter.nuclei);
		
//		boolean stress = true;
//		
//		try {
//			BufferedReader br = new BufferedReader(new FileReader("input/CHILDES/Bloom73-phonemes.txt"));
//			PrintStream out = new PrintStream("input/CHILDES/Bloom73-phonemes2.txt");	
//			
//			for (String line = br.readLine(); line != null; line = br.readLine()) {
//				// Test string in CMU phonemes
//				//String[] input = "W IH1 L S EY1 V DH AH0 S W AH1 N F R ER0 L EY1 T ER0".split(" ");
//				
//				String[] input = line.split(" ");
//				
//				String ipa = new String();
//				for (String phoneme : input) {
//					if (!stress) {
//						phoneme = phoneme.replaceAll("[012]", "1");
//					}
//					String cool = phonemeMap.get(phoneme);
//					if (cool == null) {
//						System.out.println(phoneme + " KILLED ME!");
//						System.exit(0);
//					} else {
//						ipa = ipa.concat(cool);
//					}
//				}
//				
//				out.println(ipa);
//				System.out.println(ipa);
//			}
//		
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println("NUMBER OF DISTINCT PHONEMES: " + (phonemeMap.keySet().size() - 1));
//	}
}
