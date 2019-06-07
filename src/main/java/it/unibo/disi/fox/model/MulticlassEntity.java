package it.unibo.disi.fox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibo.disi.fox.utils.Utils;

public class MulticlassEntity {

	private List<ClassBelonging> classBelonging = new ArrayList<>();
	private String _uri;
	private String _abstract;
	private Map<String, Set<Object>> features = new HashMap<>();

	public MulticlassEntity(String _uri, String _abstract) {
		super();
		this._uri = _uri;
		this._abstract = _abstract;
	}

	public void addKlassBelonging(Klass klass, double confidence, Classification classification) {
		classBelonging.add(new ClassBelonging(klass, confidence, classification));
	}

	public void addFeatureValue(String featureName, Object value) {
		Set<Object> values = features.get(featureName);
		if (values == null) {
			values = new HashSet<>();
		}
		values.add(value);
		features.put(featureName, values);
	}

	public Set<Object> getFeatureValue(String featureName) {
		return features.get(featureName);
	}

	public Klass getDominantClass() {
		ClassBelonging cb = classBelonging.get(0);
		for (ClassBelonging c : classBelonging) {
			if (c.getConfidence() > cb.getConfidence()) {
				cb = c;
			}
		}
		return cb.getKlass();
	}

	public boolean hasPositiveConfidenceOn(Klass klass) {
		for (ClassBelonging c : classBelonging) {
			if (c.getKlass().equals(klass) && c.getConfidence() > 0) {
				return true;
			}
		}
		return false;
	}

	public boolean hasNoConfidenceOn(Klass klass) {
		for (ClassBelonging c : classBelonging) {
			if (c.getKlass().equals(klass) && c.getConfidence() > 0) {
				return false;
			}
		}
		return true;
	}

	public double getConfidenceOnClass(Klass klass) {
		for (ClassBelonging c : classBelonging) {
			if (c.getKlass().equals(klass)) {
				return c.getConfidence();
			}
		}
		return 0;
	}

	public Set<String> getFeatures() {
		return features.keySet();
	}

	public Map<String, Set<Object>> features() {
		return features;
	}

	public List<ClassBelonging> getClassBelonging() {
		return classBelonging;
	}

	public String get_uri() {
		return _uri;
	}

	public String get_abstract() {
		return _abstract;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_uri == null) ? 0 : _uri.hashCode());
		return result;
	}

	public double getSumOfConfidence() {
		double r = 0.0;
		for (ClassBelonging c : classBelonging) {
			r += c.getConfidence();
		}
		return r;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MulticlassEntity other = (MulticlassEntity) obj;
		if (_uri == null) {
			if (other._uri != null)
				return false;
		} else if (!_uri.equals(other._uri))
			return false;
		return true;
	}

	public static Set<String> getFeatures(Set<MulticlassEntity> entities) {
		Set<String> features = new HashSet<>();
		for (MulticlassEntity e : entities) {
			features.addAll(e.getFeatures());
		}
		return features;
	}

	public static double getAverageAgreementOnClass(Set<MulticlassEntity> entities, Klass c) {
		double sum = 0;
		for (MulticlassEntity e : entities) {
			sum += e.getConfidenceOnClass(c);
		}

		return sum / entities.size();
	}

	public static double getAverageAgreementOnClass(Set<MulticlassEntity> entities1, Set<MulticlassEntity> entities2,
			Klass c1, Klass c2) {
		double sum = 0;
		for (MulticlassEntity e : entities1) {
			sum += e.getConfidenceOnClass(c1);
		}

		for (MulticlassEntity e : entities2) {
			sum += e.getConfidenceOnClass(c2);
		}

		return sum / (entities1.size() + entities2.size());
	}

}
