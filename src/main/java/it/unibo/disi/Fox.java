
package it.unibo.disi;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.features.SENECA;
import it.unibo.disi.features.Tipalo;
import it.unibo.disi.fox.model.Classification;
import it.unibo.disi.fox.model.Klass;
import it.unibo.disi.utils.fox.Dataset;

public class Fox {

	private static Logger logger = LoggerFactory.getLogger(Fox.class);

	public static void main(String[] args) {
		try {
			Configurations configs = new Configurations();
			Configuration config;

			if (args.length > 1) {
				config = configs.properties(args[0]);
			} else {
				config = configs.properties("config.properties");
			}

			SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
			Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

			logger.info("Is dbr:Xanthophyll a Class according to SENECA method? {}", seneca.isClass("http://dbpedia.org/resource/Xanthophyll"));
			logger.info("Is dbr:Xanthophyll a Class according to Tipalo method?  {}", tipalo.isClass("http://dbpedia.org/resource/Xanthophyll"));

			logger.info("Is dbr:Xanthophyll a Physical Object according to SENECA method? {}", seneca.isPhysicalObject("http://dbpedia.org/resource/Xanthophyll"));
			logger.info("Is dbr:Xanthophyll a Physical Object according to Tipalo method?  {}", tipalo.isPhysicalObject("http://dbpedia.org/resource/Xanthophyll"));

			exportDatasets();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void exportDatasets() throws Exception {
		{
			// Export crowd annotated dataset
			Dataset d = new Dataset();
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/crowd/lev0/class_with_edges.xlsx", new Klass("class"), new Classification("Class-Instance", Sets.newHashSet(new Klass("class"), new Klass("instance")), "Class-Instance annotated by Crowd"));
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/crowd/lev0/instance_with_edges.xlsx", new Klass("instance"), new Classification("Class-Instance", Sets.newHashSet(new Klass("instance"), new Klass("instance")), "Class-Instance annotated by Crowd"));
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/crowd/lev1/physical_object_with_edges.xlsx", new Klass("physical_object"), new Classification("PhysicalObject", Sets.newHashSet(new Klass("physical_object"), new Klass("non_physical_object")), "Physical Object annotated byCrowd"));
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/crowd/lev1/non_physical_object_with_edges.xlsx", new Klass("non_physical_object"), new Classification("PhysicalObject", Sets.newHashSet(new Klass("physical_object"), new Klass("non_physical_object")), "Physical Objectannotated by Crowd"));
			d.exportToJSON("/Users/lgu/Desktop/export_crowd.json");
		}

		{
			// Export experts annotated dataset
			Dataset d = new Dataset();
			Map<String, String> mapClassName = new HashMap<>();
			mapClassName.put("generic", "class");
			mapClassName.put("nongeneric", "instance");
			d.setMapClassName(mapClassName);

			d.loadEdgeFileWithClasses("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/experts/lev0/dbpedia_with_edges.xlsx", 1, 0, new Classification("Class-Instance", Sets.newHashSet(new Klass("class"), new Klass("instance")), "Class-Instance annotated by Experts"));
			d.loadEdgeFileWithClasses("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/experts/lev0/sun_with_edges.xlsx", 1, 0, new Classification("Class-Instance", Sets.newHashSet(new Klass("class"), new Klass("instance")), "Class-Instance annotated by Experts"));
			d.loadEdgeFileWithClasses("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/experts/lev0/nasari_with_edges.xlsx", 1, 0, new Classification("Class-Instance", Sets.newHashSet(new Klass("class"), new Klass("instance")), "Class-Instance annotated by Experts"));
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/experts/lev1/physical_object.xlsx", new Klass("physical_object"), new Classification("PhysicalObject", Sets.newHashSet(new Klass("physical_object"), new Klass("non_physical_object")), "Physical Object annotated by Experts"));
			d.loadEdgeFile("/Users/lgu/Dropbox/repository/workspace_oxy/gecko/data/experts/lev1/non_physical_object.xlsx", new Klass("non_physical_object"), new Classification("PhysicalObject", Sets.newHashSet(new Klass("physical_object"), new Klass("non_physical_object")), "Physical Object annotated by Experts"));

			d.exportToJSON("/Users/lgu/Desktop/export_experts.json");
		}
	}
}
