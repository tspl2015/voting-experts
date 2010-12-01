package edu.arizona.phoneme;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * This class is specifically built to parse the CMU Phoneme Dictionary and
 * then allow lookup from a word to its most common phonemic pronunciation 
 * and vice versa
 * @author mamille - This is lifted from Alex's code and modified, 
 * it's easier to use this for now than to have to switch into lisp.
 * 
 */
public class Dictionary{
	private HashMap<String, String> dict;
	private HashMap<String, String> back;
	
	public Dictionary() {
		dict = new HashMap<String, String>();
		back = new HashMap<String, String>();
		
		try {
			System.out.println("Loading Dictionary File...");
			
			File inputFile = new File("phoneme/cmudict.txt");
			Scanner fileIn = new Scanner(inputFile);
			
			while (fileIn.hasNextLine()) {
				String line = fileIn.nextLine();
				Scanner lineReader = new Scanner(line);
				String word = lineReader.next();
				int numSpellings = lineReader.nextInt();
				String phonemes = lineReader.next();
				
				while (lineReader.hasNext()) { phonemes += " " + lineReader.next(); }
		
				// Only stores the most common phoneme representation
				if (numSpellings == 1) {
					dict.put(word.toLowerCase(), phonemes.toUpperCase());
					back.put(phonemes.toUpperCase(), word.toLowerCase());
				}
			}
			
			System.out.println("Dictionary Loaded.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// uses CMU format ("AH", L", etc.)
	public String getPhonemes(String word){
		if (dict.containsKey(word.toLowerCase())) 
			return dict.get(word.toLowerCase());
		
		System.out.println("Word not found: " + word);
		return "";
	}

	// uses CMU format ("AA", "AH", etc.)
	public List<String> getPhonemeList(String word) {
		if (dict.containsKey(word.toLowerCase())) 
			return Arrays.asList(dict.get(word.toLowerCase()).split(" "));
		
		return new ArrayList<String>();
	}
	
	// uses my format ("a", ə", etc.)
	public String getIPA(String word, int stressLevel) {
		StringBuffer s = new StringBuffer();
		
		if (!word.isEmpty()) {
			for (String phoneme : getPhonemeList(word)) {
				s.append(PhonemeConverter.convert(phoneme, stressLevel));
			}
			
			if (s.toString().isEmpty()) {
				String repaired = PhonemeConverter.repair(word,this);
				if (repaired.isEmpty()) { 
					System.out.println("Word not found or repaired: " + word); 
				} else {
//					System.out.println("REPAIRED: " + word);
				}
				return repaired;
			} else {
				return s.toString();
			}
		}
		
		return "";
	}
	
	public String getWord(String phonemes){
		if (back.containsKey(phonemes.toLowerCase()))
			return back.get(phonemes.toLowerCase());
		
		return "";
	}
	
	public static void main(String[] args) {
		Dictionary d = new Dictionary();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			for (String line = in.readLine().trim(); line != null; line = in.readLine().trim()) {
				String phonemes = d.getIPA(line, 2);
				
				if (phonemes.isEmpty()) {
					System.out.println();
				} else {
					System.out.println(phonemes);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
