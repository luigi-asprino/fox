package it.unibo.disi.fox.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import it.unibo.disi.fox.utils.Utils;

public final class Tipalo implements AlignmentBasedMethod{
	
	private Set<String> classes = new HashSet<>();
	private Set<String> physical_object = new HashSet<>();
	private static Tipalo instance = null;

	private Tipalo(String listClassesFile, String listPhysicalObjectFile) throws IOException {
		classes.addAll(Utils.readFileToListString(listClassesFile));
		physical_object.addAll(Utils.readFileToListString(listPhysicalObjectFile));
	}

	public static Tipalo getInstance(String listClassesFile, String listPhysicalObjectFile) throws IOException {
		if (instance == null) {
			instance = new Tipalo(listClassesFile, listPhysicalObjectFile);
		}
		return instance;

	}

	@Override
	public Set<String> getClassEntities() {
		return classes;
	}

	@Override
	public Set<String> getPhysicalObjectEntities() {
		return physical_object;
	}

	

}
