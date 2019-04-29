package it.unibo.disi.fox.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Classification {

	private String methodDescription;
	private String name;
	private Map<String, Klass> nameToClass = new HashMap<>();

	public Classification(String name, Set<Klass> classes, String methodDescription) {
		super();
		this.methodDescription = methodDescription;
		this.name = name;
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

}
