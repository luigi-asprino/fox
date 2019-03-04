package it.unibo.disi.utils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * @author Luigi Asprino
 *
 */
public class Utils {

	/**
	 * 
	 * A very simple tokenizer based on PTBTokenizer of Stanford's CoreNLP library.
	 * 
	 * @param text The input text to tokenize. 
	 * @return A list of tokens contained in the input text.
	 */
	public static List<String> tokenize(String text) {
		List<String> result = new ArrayList<>();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(text), new CoreLabelTokenFactory(), "");
		while (ptbt.hasNext()) {
			CoreLabel label = ptbt.next();
			result.add(label.originalText());
		}
		return result;
	}

}
