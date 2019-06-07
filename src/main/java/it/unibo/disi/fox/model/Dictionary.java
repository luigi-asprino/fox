package it.unibo.disi.fox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;

public class Dictionary {

	private Map<String, Integer> tokens = new HashMap<String, Integer>();

	public void addDictionary(Dictionary d) {
		for (Entry<String, Integer> e : d.tokens.entrySet()) {
			if (tokens.containsKey(e.getKey())) {
				tokens.put(e.getKey(), tokens.get(e.getKey()) + e.getValue());
			} else {
				tokens.put(e.getKey(), e.getValue());
			}
		}
	}

	public void addToken(String s) {
		Integer k = tokens.get(s);
		if (k == null) {
			k = 0;
		}
		k++;
		tokens.put(s, k);
	}

	public Set<String> getTokens() {
		return tokens.keySet();
	}

	public List<String> getTokens(int mostFrequent) {
		List<Entry<String, Integer>> m = sortByValue(tokens);
		List<String> result = new ArrayList<>();
		for (int i = 0; i < mostFrequent; i++) {
			result.add(m.get(i).getKey());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {

		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());

		list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

		list = Lists.reverse(list);

		return list;
	}

	public static void main(String[] args) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("a", 100);
		map.put("b", 1);
		map.put("c", 1100);

	}

}
