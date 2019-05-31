package it.unibo.disi.fox;

import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.fox.experiments.fdistinctions.WekaUtils;
import it.unibo.disi.fox.services.SENECA;
import it.unibo.disi.fox.services.Tipalo;
import it.unibo.disi.fox.utils.Dataset;

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

			createDataset();

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createDataset() {
		logger.info("Create dataset!");

		Dataset d = new Dataset();
		try {
			d.loadInstances(WekaUtils.loadARFFInstances("iris.arff"), "class", "http://localhost:8080/iris/classify");
			d.exportToJSON("crowdfeed/inputdata_examples/iris.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
