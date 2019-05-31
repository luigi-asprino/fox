package it.unibo.disi.fox.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Classification {

	private String methodDescription;
	private String name, id, classification_service_url;
	private Set<Feature> features = new HashSet<>();
	private Map<String, Klass> nameToClass = new HashMap<>();

	public Classification(String id, String name, String classification_service_url, Set<Klass> classes, String methodDescription) {
		super();
		this.methodDescription = methodDescription;
		this.id = id;
		this.name = name;
		this.classification_service_url = classification_service_url;
		for (Klass k : classes) {
			nameToClass.put(k.getClassName(), k);
		}
	}
	
	

	public Collection<Klass> getClasses() {
		return nameToClass.values();
	}

	public Klass getClass(String className) {
		return nameToClass.get(className);
	}

	public String getMethodDescription() {
		return methodDescription;
	}

	public String getName() {
		return name;
	}

	public void addFeature(Feature f) {
		features.add(f);
	}

	public Set<Feature> getFeatures() {
		return features;
	}

	public String getId() {
		return id;
	}



	public String getClassificationServiceUrl() {
		return classification_service_url;
	}

}
