package it.unibo.disi.fox.model;

import java.util.Set;

public class BinaryFeature {

	private String featureName;
	private Set<String> entityWithFeature;

	public BinaryFeature(String featureName, Set<String> entityWithFeature) {
		super();
		this.featureName = featureName;
		this.entityWithFeature = entityWithFeature;
	}

	public boolean hasFeature(String entity) {
		return entityWithFeature.contains(entity);
	}

	public String getFeatureName() {
		return featureName;
	}

	public static String getFeatureNamePositive() {
		return "YES";
	}

	public static String getFeatureNameNegative() {
		return "NO";
	}

}
