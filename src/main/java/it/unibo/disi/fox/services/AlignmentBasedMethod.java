package it.unibo.disi.fox.services;

import java.util.Set;

public interface AlignmentBasedMethod {

	public Set<String> getClassEntities();

	public Set<String> getPhysicalObjectEntities();

	public default boolean isClass(String uri) {
		return getClassEntities().contains(uri);
	}

	public default boolean isInstance(String uri) {
		return !getClassEntities().contains(uri);
	}

	public default boolean isPhysicalObject(String uri) {
		return getPhysicalObjectEntities().contains(uri);
	}

	public default boolean isNotAPhysicalObject(String uri) {
		return !getPhysicalObjectEntities().contains(uri);
	}

}
