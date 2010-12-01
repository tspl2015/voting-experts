package edu.arizona.ve.experts;

import java.util.List;

import edu.arizona.ve.trie.Trie;


/**
*
* @author  Daniel Hewlett
*/
public abstract class Expert {
	
	protected Trie _trie;
	
	public Expert(Trie trie) {
		_trie = trie;
	}
	
	public abstract boolean[] segment(List<String> subSequence);
}
