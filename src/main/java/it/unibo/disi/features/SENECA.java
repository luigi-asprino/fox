package it.unibo.disi.features;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import it.unibo.disi.utils.Utils;

public final class SENECA implements AlignmentBasedMethod {

	private Set<String> classes = new HashSet<>();
	private Set<String> physical_object = new HashSet<>();
	private static SENECA instance = null;

	private SENECA(String listClassesFile, String listPhysicalObjectFile) throws IOException {
		classes.addAll(Utils.readFileToListString(listClassesFile));
		physical_object.addAll(Utils.readFileToListString(listPhysicalObjectFile));
	}

	public static SENECA getInstance(String listClassesFile, String listPhysicalObjectFile) throws IOException {
		if (instance == null) {
			instance = new SENECA(listClassesFile, listPhysicalObjectFile);
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
